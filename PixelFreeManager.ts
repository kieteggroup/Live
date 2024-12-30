import { NativeModules } from 'react-native';

const { PixelFree } = NativeModules;

export const PixelFreeManager = {
  initializeCamera: (): Promise<boolean> => {
    return PixelFree.initializeCamera();
  },

  startCamera: (): Promise<boolean> => {
    return PixelFree.startCamera();
  },

  stopCamera: (): Promise<boolean> => {
    return PixelFree.stopCamera();
  },

  showBeautyDialog: (): Promise<boolean> => {
    return PixelFree.showBeautyDialog();
  },

  hideBeautyDialog: (): Promise<boolean> => {
    return PixelFree.hideBeautyDialog();
  }
};