import { View, Text } from 'react-native';
import React from 'react';
import { SafeAreaView } from 'react-native-safe-area-context';

const Home = () => {
  return (
    <SafeAreaView className="flex-1 bg-white">
      <View className="flex-1 p-6">
        <Text className="text-2xl font-['PlusJakartaSans-SemiBold']">
          Home
        </Text>
      </View>
    </SafeAreaView>
  );
};

export default Home;