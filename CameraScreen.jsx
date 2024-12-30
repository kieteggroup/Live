import React, {useEffect, useState} from 'react';
import {StyleSheet, View, NativeModules, Button} from 'react-native';

const {PixelFree} = NativeModules;

const CameraView = () => {
  const [isCameraStarted, setIsCameraStarted] = useState(false);
  useEffect(() => {
    const setupCamera = async () => {
      try {
        // await PixelFree.initializeCameraTrack();
        // await PixelFree.startCamera();
        setIsCameraStarted(true);
      } catch (error) {
        console.error('Camera setup error:', error);
      }
    };

    setTimeout(() => {
      setupCamera();
    }, 1000);

    // Cleanup when component unmounts
    return () => {
      PixelFree.stopCamera();
    };
  }, []);


  const showBeautyControls = async () => {
    try {
      // Lấy các giá trị beauty hiện tại (nếu có)
      // const currentBeautyParams = await PixelFree.getBeautyParams();
      await PixelFree.showBeautyDialog();
    } catch (error) {
      console.error('Show beauty dialog error:', error);
    }
  };

  return (
    <View style={styles.container}>
      {/* Camera preview sẽ hiển thị ở đây */}
      <View style={styles.controls}>
        <Button title="Beauty Controls" onPress={showBeautyControls} />
        <Button title="sTop" onPress={() => {
          PixelFree.stopCamera()
        }} />
        <Button title="sStart"  onPress={async () => {
          await PixelFree.startCamera();
        }} />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  controls: {
    position: 'absolute',
    bottom: 20,
    width: '100%',
    alignItems: 'center',
  },
});

export default CameraView;
