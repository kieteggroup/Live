/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React from 'react';
import { SafeAreaView } from 'react-native';
import CameraView from './CameraScreen';

const App = () => {
  return (
    <SafeAreaView style={{ flex: 1 }}>
      <CameraView />
    </SafeAreaView>
  );
};

export default App;