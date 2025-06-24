import React from 'react';
import { View, Text, Image, useWindowDimensions, StyleSheet } from 'react-native';
import { OnboardingSlide as SlideType } from './onboarding';

interface OnboardingSlideProps {
  item: SlideType;
}

const OnboardingSlide: React.FC<OnboardingSlideProps> = ({ item }) => {
  const { width } = useWindowDimensions();

  return (
    <View className="flex-1 justify-center items-center px-8" style={{ width }}>
      <Image 
        source={item.image} 
        className="h-64 w-64 mb-8"
        resizeMode="contain"
      />
      <Text className="text-[#1DA1FA] text-3xl font-bold mb-3 text-center">
        {item.title}
      </Text>
      <Text className="text-gray-600 text-center text-lg mb-8 leading-relaxed">
        {item.description}
      </Text>
    </View>
  );
};

export default OnboardingSlide;