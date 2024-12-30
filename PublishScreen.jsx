/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React, {useEffect, useRef, useState} from 'react';
import type {PropsWithChildren} from 'react';
import {
  Button,
  Dimensions,
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  useColorScheme,
  View,
} from 'react-native';
import RTMPPublisher, {
  AudioInputType,
  RTMPPublisherRefProps,
} from 'react-native-rtmp-publisher';
import Video from 'react-native-video';
import { VLCPlayer, VlCPlayerView } from 'react-native-vlc-media-player';

import {
  Colors,
  DebugInstructions,
  Header,
  LearnMoreLinks,
  ReloadInstructions,
} from 'react-native/Libraries/NewAppScreen';

type SectionProps = PropsWithChildren<{
  title: string;
}>;



function PublishScreen(): React.JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';
  const publisherRef = useRef<RTMPPublisherRefProps>(null);
  const playerRef = useRef(null);
  const [streamUrl, setStreamUrl] = useState('');
  const [isStreaming, setIsStreaming] = useState<boolean>(false);
  const [publishUrl, setPublishUrl] = useState('');
  const [playbackUrl, setPlaybackUrl] = useState('');t

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  useEffect(() => {
    async function publisherActions() {
      // await publisherRef.current?.startStream();

      await publisherRef.current?.unmute();
      await publisherRef.current?.switchCamera();
      await publisherRef.current?.getPublishURL();
      await publisherRef.current?.isMuted();
      await publisherRef.current?.isStreaming();
      await publisherRef.current?.toggleFlash();
      await publisherRef.current?.hasCongestion();
      await publisherRef.current?.isAudioPrepared();
      await publisherRef.current?.isVideoPrepared();
      await publisherRef.current?.isCameraOnPreview();
      await publisherRef.current?.setAudioInput(AudioInputType.SPEAKER);
    }
    publisherActions();
  }, []);

  const height = Dimensions.get('window').height;
  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar
        barStyle={isDarkMode ? 'light-content' : 'dark-content'}
        backgroundColor={backgroundStyle.backgroundColor}
      />

        {/* Stream Preview */}
          <RTMPPublisher
            ref={publisherRef}
            streamURL="rtmp://a.rtmp.youtube.com/live2"
            streamName="v6pa-c2wg-xybw-8jw1-4k06"
            onConnectionFailed={error => {
              console.error('Connection failed:', error);
            }}
            onConnectionStarted={() => {
              console.log('Connection started');
            }}
            onConnectionSuccess={() => {
              console.log('Connection success');
            }}
            onDisconnect={error => {
              console.error('Disconnect:', error);
            }}
            onNewBitrateReceived={bitrate => {
              console.log('New bitrate:', bitrate);
            }}
            onStreamStateChanged={(status: streamState) => {
              console.log('Stream state changed:', status);
            }}
            style={{
              width: '100%',
              height: height/2,
            }}
          />

        {/* Stream Player */}
        {playbackUrl && (
           <NodePlayer
           style={{ flex: 1 }}
           url={url}
           autoplay={true}
           scaleMode={1}
           bufferTime={500}
       ></NodePlayer>
        )}

        {/* Controls */}
        <View
          style={{
            position: 'absolute',
            bottom: 20,
            left: 0,
            right: 0,
            flexDirection: 'row',
            justifyContent: 'center',
            gap: 10,
            padding: 10,
          }}>
          <Button
            title={isStreaming ? 'Dừng Stream' : 'Bắt đầu Stream'}
            onPress={async () => {
              if (isStreaming) {
                await publisherRef.current?.stopStream();
              } else {
                await publisherRef.current?.startStream();
              }
              setIsStreaming(!isStreaming);
            }}
          />

          <Button
            title="Đổi Camera"
            onPress={async () => {
              await publisherRef.current?.switchCamera();
            }}
          />

          <Button
            title="Lấy URL Stream"
            onPress={async () => {
              const url = await publisherRef.current?.getPublishURL();
              if (url) {
                setPublishUrl(url);
                const fbPlaybackUrl = 'https://www.facebook.com/1295380934824002/videos/live';
                setPlaybackUrl(fbPlaybackUrl);
                console.log('Publish URL:', url);
                console.log('Playback URL:', fbPlaybackUrl);
              }
            }}
          />
        </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
  },
  highlight: {
    fontWeight: '700',
  },
});

export default PublishScreen;
