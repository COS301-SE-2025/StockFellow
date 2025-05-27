import { View, Text } from 'react-native';
import React from 'react';
import { SafeAreaView } from 'react-native-safe-area-context';
import { StatusBar } from 'expo-status-bar';
const Home = () => {
  return (
    <SafeAreaView className="flex-1 bg-white">
      <StatusBar style="dark" />
      <View className="flex-1 p-6">
        <Text className="text-2xl font-['PlusJakartaSans-SemiBold']">
          Home
        </Text>
      </View>
    </SafeAreaView>
  );
};

export default Home;