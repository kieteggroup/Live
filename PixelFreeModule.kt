package com.live

import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.hapi.avcapture.CameraXTrack
import com.hapi.avcapture.FrameCall
import com.hapi.avcapture.HapiTrackFactory
import com.hapi.avparam.VideoFrame
import com.hapi.avrender.HapiCapturePreView
import com.hapi.pixelfree.PixelFree
import com.hapi.pixelfree.PFDetectFormat
import com.hapi.pixelfree.PFIamgeInput
import com.hapi.pixelfree.PFRotationMode
import com.hapi.pixelfree.PFSrcType
import com.facebook.react.bridge.UiThreadUtil
import com.hapi.pixelfreeuikit.PixeBeautyDialog

class PixelFreeModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private val pixelFree = PixelFree()
    private var cameraTrack: CameraXTrack? = null
    private var hapiCapturePreView: HapiCapturePreView? = null
    private val pixeBeautyDialog by lazy { 
        PixeBeautyDialog(pixelFree)
    }

    override fun getName(): String {
        return "PixelFree"
    }

    @ReactMethod
    fun initializeCamera(promise: Promise) {
        UiThreadUtil.runOnUiThread {
            try {
                val activity = currentActivity as? AppCompatActivity 
                    ?: throw IllegalStateException("Activity is null or not AppCompatActivity")
                
                // Khởi tạo preview view nếu chưa có
                if (hapiCapturePreView == null) {
                    hapiCapturePreView = HapiCapturePreView(activity)
                }

                // Thêm view vào layout với index 0 để đảm bảo nó nằm dưới cùng
                val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
                rootView.addView(hapiCapturePreView, 
                    0,  // Thêm vào index 0
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )

                cameraTrack = HapiTrackFactory.createCameraXTrack(
                    activity, 
                    activity as LifecycleOwner,
                    720, 1280
                ).apply {
                    frameCall = object : FrameCall<VideoFrame> {
                        override fun onFrame(frame: VideoFrame) {
                            // Không cần xử lý
                        }

                        override fun onProcessFrame(frame: VideoFrame): VideoFrame {
                            if (pixelFree.isCreate()) {
                                val pxInput = PFIamgeInput().apply {
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
                                pixelFree.processWithBuffer(pxInput)
                                frame.textureID = pxInput.textureID
                            }
                            return super.onProcessFrame(frame)
                        }
                    }
                }
                promise.resolve(true)
            } catch (e: Exception) {
                promise.reject("INITIALIZE_ERROR", e.message, e)
            }
        }
    }

    @ReactMethod
    fun startCamera(promise: Promise) {
        UiThreadUtil.runOnUiThread {
            try {
                // Kiểm tra activity và preview view
                val activity = currentActivity as? AppCompatActivity
                    ?: throw IllegalStateException("Activity is null")

                if (hapiCapturePreView == null) {
                    throw IllegalStateException("Camera preview not initialized. Call initializeCamera first")
                }

                // Kiểm tra và khởi tạo GL Surface
                hapiCapturePreView?.mHapiGLSurfacePreview?.let { glSurface ->
                    glSurface.mOpenGLRender?.glCreateCall = {
                        try {
                            // Khởi tạo PixelFree nếu chưa được tạo
                            if (!pixelFree.isCreate()) {
                                pixelFree.create()
                            }
                            
                            // Đọc và xác thực license
                            val authData = pixelFree.readBundleFile(activity, "pixelfreeAuth.lic")
                                ?: throw IllegalStateException("Could not read auth file")
                            pixelFree.auth(activity.applicationContext, authData, authData.size)
                            
                            // Đọc và tạo beauty filter
                            val faceFilter = pixelFree.readBundleFile(activity, "filter_model.bundle")
                                ?: throw IllegalStateException("Could not read filter bundle")
                            pixelFree.createBeautyItemFormBundle(
                                faceFilter,
                                faceFilter.size,
                                PFSrcType.PFSrcTypeFilter
                            )
                        } catch (e: Exception) {
                            promise.reject("PIXELFREE_INIT_ERROR", "Failed to initialize PixelFree: ${e.message}")
                        }
                    }
                }

                // Khởi động camera track
                cameraTrack?.apply {
                    playerView = hapiCapturePreView
                    start()
                } ?: throw IllegalStateException("Camera track not initialized")

                promise.resolve(true)
            } catch (e: Exception) {
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
                val activity = currentActivity as? AppCompatActivity
                    ?: throw IllegalStateException("Activity is null")
                
                pixeBeautyDialog.show(activity.supportFragmentManager, "beauty_dialog")
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
                pixeBeautyDialog.dismiss()
                promise.resolve(true)
            } catch (e: Exception) {
                promise.reject("HIDE_BEAUTY_ERROR", e.message, e)
            }
        }
    }

    override fun onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy()
        try {
            cameraTrack?.stop()
            cameraTrack = null
            pixelFree.release()
        } catch (e: Exception) {
            // Handle any errors during cleanup
        }
    }
}