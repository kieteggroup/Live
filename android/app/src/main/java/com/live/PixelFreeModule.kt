package com.live

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.UiThreadUtil

import com.hapi.pixelfree.*
import com.hapi.avcapture.CameraXTrack
import com.hapi.avcapture.FrameCall
import com.hapi.avcapture.HapiTrackFactory
import com.hapi.avparam.VideoFrame
import com.hapi.avrender.HapiCapturePreView
import com.hapi.pixelfreeuikit.IndicatorSeekBar
import com.hapi.pixelfreeuikit.PixeBeautyDialog

class PixelFreeModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {
    private val mPixelFree by lazy { PixelFree() }
    val hapiCapturePreView by lazy {
        val activity =
            reactContext.currentActivity as? AppCompatActivity
                ?: throw IllegalStateException("Activity is null")
        activity.findViewById<HapiCapturePreView>(R.id.preview)?: throw IllegalStateException("HapiCapturePreView is not found in layout")
    }

    private val mPixeBeautyDialog by lazy { PixeBeautyDialog(mPixelFree) }

    override fun getName(): String {
        return "PixelFree"
    }

    // 摄像头轨道
    private var cameraTrack: CameraXTrack? = null

    private fun initializeCameraTrack(): CameraXTrack {
        val activity =
            reactContext.currentActivity as? AppCompatActivity
                ?: throw IllegalStateException("Activity is null")

        return HapiTrackFactory.createCameraXTrack(activity, activity, 720, 1280).apply {
            frameCall =
                object : FrameCall<VideoFrame> {
                    override fun onFrame(frame: VideoFrame) {}

                    override fun onProcessFrame(frame: VideoFrame): VideoFrame {
                        if (mPixelFree.isCreate()) {
                            val pxInput =
                                PFIamgeInput().apply {
                                    wigth = frame.width
                                    height = frame.height
                                    p_data0 = frame.data
                                    p_data1 = frame.data
                                    p_data2 = frame.data
                                    stride_0 = frame.rowStride
                                    stride_1 = frame.rowStride
                                    stride_2 = frame.rowStride
                                    format = PFDetectFormat.PFFORMAT_IMAGE_RGBA
                                    rotationMode = PFRotationMode.PFRotationMode90
                                }

                              val startTime = System.currentTimeMillis()
                        mPixelFree.processWithBuffer(pxInput)
                        val endTime = System.currentTimeMillis()
                        val timeCost = endTime - startTime
                        println("sunmu----processWithBuffer 耗时：$timeCost 毫秒")

                        frame.textureID = pxInput.textureID
                        }
                        return super.onProcessFrame(frame)
                    }
                }
        }
    }


 @ReactMethod
fun startCamera(promise: Promise) {
    UiThreadUtil.runOnUiThread {
        try {
            val activity = currentActivity as? AppCompatActivity
                ?: throw IllegalStateException("Activity is null")

            Log.d("PixelFreeModule", "Starting camera initialization")

            if (cameraTrack == null) {
                Log.d("PixelFreeModule", "Creating new camera track")
                cameraTrack = initializeCameraTrack()
            }

            activity.setContentView(R.layout.activity_main)
            Log.d("PixelFreeModule", "Set content view")
            
            cameraTrack?.playerView = hapiCapturePreView
            cameraTrack?.start()
            Log.d("PixelFreeModule", "Camera track started")
            println("Face filter size: Camera track started")

            hapiCapturePreView.mHapiGLSurfacePreview.mOpenGLRender.glCreateCall = {
                try {
                    Log.d("PixelFreeModule", "Initializing PixelFree")
                    val createResult = mPixelFree.create()
                    Log.d("PixelFreeModule", "PixelFree create result: $createResult")

                    val authData = mPixelFree.readBundleFile(activity, "pixelfreeAuth.lic")
                    Log.d("PixelFreeModule", "Auth data size: ${authData.size}")
                    mPixelFree.auth(activity.applicationContext, authData, authData.size)
                    
                    val faceFilter = mPixelFree.readBundleFile(activity, "filter_model.bundle")
                    Log.d("PixelFreeModule", "Face filter size: ${faceFilter.size}")
                    mPixelFree.createBeautyItemFormBundle(
                        faceFilter,
                        faceFilter.size,
                        PFSrcType.PFSrcTypeFilter
                    )

                    mPixeBeautyDialog.show(activity.supportFragmentManager, "")
                    Log.d("PixelFreeModule", "Beauty dialog shown")
                } catch (e: Exception) {
                    Log.e("PixelFreeModule", "Error in glCreateCall: ${e.message}", e)
                    throw e
                }
            }

            promise.resolve(true)
        } catch (e: Exception) {
            Log.e("PixelFreeModule", "Failed to start camera: ${e.message}", e)
            promise.reject("START_CAMERA_ERROR", "Failed to start camera: ${e.message}", e)
        }
    }
}


    @ReactMethod
    fun stopCamera(promise: Promise) {
        UiThreadUtil.runOnUiThread {
            try {
                cameraTrack?.stop()
                cameraTrack = null
                promise.resolve(true)
            } catch (e: Exception) {
                promise.reject("STOP_CAMERA_ERROR", e.message, e)
            }
        }
    }

    @ReactMethod
    fun showBeautyDialog(promise: Promise) {
        UiThreadUtil.runOnUiThread {
            try {
                val activity =
                    currentActivity as? AppCompatActivity
                        ?: throw IllegalStateException("Activity is null")

                mPixeBeautyDialog.show(activity.supportFragmentManager, "beauty_dialog")
                promise.resolve(true)
            } catch (e: Exception) {
                promise.reject("SHOW_BEAUTY_ERROR", e.message, e)
            }
        }
    }

    @ReactMethod
    fun hideBeautyDialog(promise: Promise) {
        UiThreadUtil.runOnUiThread {
            try {
                mPixeBeautyDialog.dismiss()
                promise.resolve(true)
            } catch (e: Exception) {
                promise.reject("HIDE_BEAUTY_ERROR", e.message, e)
            }
        }
    }


    override fun onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy()
        // Giải phóng tài nguyên của PixelFree khi activity bị hủy
        mPixelFree.release()
    }

}
