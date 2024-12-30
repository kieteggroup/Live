import React from 'react';
import { View } from 'react-native';
import { NodePlayer } from 'react-native-nodemediaclient';

function PlayScreen({ route, navigation }) {
    const { url } = route.params;
    console.log(url);
    return (
        <NodePlayer
            style={{ flex: 1, height: 200 , width: '100%'}}
            url={url}
            autoplay={true}
            scaleMode={1}
            bufferTime={500}
        ></NodePlayer>
    )
}

export default PlayScreen;