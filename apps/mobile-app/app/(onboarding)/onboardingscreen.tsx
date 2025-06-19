import React, { useState, useRef } from 'react';
import { View, Text, Image, Dimensions, FlatList, TouchableOpacity } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import { StatusBar } from 'expo-status-bar';

const { width } = Dimensions.get('window');

interface OnboardingSlide {
  id: string;
  title: string;
  description: string;
  image: any;
}

const onboardingData: OnboardingSlide[] = [
  {
    id: '1',
    title: 'Onboarding 1',
    description: 'Description',
    image: require('../../assets/adaptive-icon.png'), // Replace with actual image
  },
  {
    id: '2',
    title: 'Onboarding 2',
    description: 'Description',
    image: require('../../assets/adaptive-icon.png'), // Replace with actual image
  },
  {
    id: '3',
    title: 'Onboarding 3',
    description: 'Description',
    image: require('../../assets/adaptive-icon.png'), // Replace with actual image
  },
  {
    id: '4',
    title: 'Onboarding 4',
    description: 'Description',
    image: require('../../assets/adaptive-icon.png'), // Replace with actual image
  },
];

const OnboardingScreen = () => {
  const [currentIndex, setCurrentIndex] = useState(0);
  const flatListRef = useRef<FlatList>(null);
  const router = useRouter();
  
  const handleNext = () => {
    if (currentIndex < onboardingData.length - 1) {
      flatListRef.current?.scrollToIndex({
        index: currentIndex + 1,
        animated: true,
      });
      setCurrentIndex(currentIndex + 1);
    } else {
      router.push('/(auth)/signup');
    }
  };

  const renderSlide = ({ item }: { item: OnboardingSlide }) => {
    return (
      <View style={{ width }} className="flex-1 justify-center items-center px-8">
        <Image 
          source={item.image} 
          style={{ width: width * 0.6, height: width * 0.6, tintColor: '#1DA1FA' }}
          resizeMode="contain"
        />
        <Text className="text-[#1DA1FA] text-2xl font-['PlusJakartaSans-Bold'] mt-8 mb-4 text-center">
          {item.title}
        </Text>
        <Text className="text-gray-600 text-base font-['PlusJakartaSans-Regular'] text-center px-4">
          {item.description}
        </Text>
      </View>
    );
  };

  return (
    <SafeAreaView className="flex-1 bg-white">
      <StatusBar style="dark" />
      
      <View className="flex-1">
        <FlatList
          ref={flatListRef}
          data={onboardingData}
          renderItem={renderSlide}
          horizontal
          pagingEnabled
          showsHorizontalScrollIndicator={false}
          keyExtractor={(item) => item.id}
          onMomentumScrollEnd={(e) => {
            const contentOffset = e.nativeEvent.contentOffset.x;
            const newIndex = Math.floor(contentOffset / width);
            if (newIndex !== currentIndex) {
              setCurrentIndex(newIndex);
            }
          }}
        />
      </View>
      
      {/* Progress and Next Button */}
      <View className="px-8 pb-10">
        {/* Progress indicator dots */}
        <View className="flex-row justify-center mb-8">
          {onboardingData.map((_, index) => (
            <View 
              key={index}
              className={`h-2 rounded-full mx-1 ${
                index === currentIndex ? 'bg-[#1DA1FA] w-6' : 'bg-gray-200 w-2'
              }`}
            />
          ))}
        </View>
        
        <TouchableOpacity
          onPress={handleNext}
          className="bg-[#1DA1FA] py-4 px-8 rounded-full"
        >
          <Text className="text-white text-center text-lg font-['PlusJakartaSans-SemiBold']">
            {currentIndex === onboardingData.length - 1 ? 'Get Started' : 'Next'}
          </Text>
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
};

export default OnboardingScreen;