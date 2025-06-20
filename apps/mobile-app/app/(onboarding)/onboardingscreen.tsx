import React, { useState, useRef } from 'react';
import { View, Text, Image, Dimensions, FlatList, TouchableOpacity, StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import Animated, { useSharedValue, useAnimatedStyle, withTiming } from 'react-native-reanimated';
import { icons } from '../../src/constants';

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
    title: 'Welcome to StockFellow',
    description: 'Your digital stokvel companion. Join thriving savings groups, automate your contributions, and grow together.',
    image: icons.savings,
  },
  {
    id: '2',
    title: 'Save Together, Stress-Free',
    description: 'Automate contributions and payouts with secure debit orders. Our intelligent fraud detection and verification tools keep your money safe.',
    image: icons.contribute,
  },
  {
    id: '3',
    title: 'Find the Right Group, Unlock New Tiers',
    description: 'Get matched with groups that fit your financial profile. Contribute consistently and get promoted to higher saving tiers with added benefits.',
    image: icons.growth,
  },
  {
    id: '4',
    title: 'Stay Updated, Stay Engaged',
    description: 'Receive real-time notifications for every transaction, tier change, or alert. Your savings history and group activity are always at your fingertips.',
    image: icons.notifications,
  },
];

const OnboardingScreen = () => {
  const [currentIndex, setCurrentIndex] = useState(0);
  const flatListRef = useRef<FlatList>(null);
  const router = useRouter();
  
  const progressValue = useSharedValue(0);
  
  const progressStyle = useAnimatedStyle(() => {
    return {
      width: `${(progressValue.value / onboardingData.length) * 100}%`,
    };
  });

  // calculations to update progress
  React.useEffect(() => {
    progressValue.value = withTiming(currentIndex + 1);
  }, [currentIndex]);

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
  
  const handleSkip = () => {
    router.push('/(auth)/signup');
  };

  const renderSlide = ({ item }: { item: OnboardingSlide }) => {
    return (
      <View style={{ width }} className="flex-1 justify-center items-center px-6">
        <Image 
          source={item.image} 
          style={{ width: 400, height: 400 }}
          resizeMode="contain"
        />
        <Text className="text-[#1DA1FA] text-2xl font-['PlusJakartaSans-Bold'] mt-4 mb-4 text-center">
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
      
      {/* Skip button */}
      <View className="absolute top-12 right-10 z-10">
        <TouchableOpacity onPress={handleSkip}>
            <Text className="text-gray-400 text-xl font-['PlusJakartaSans-Medium']">
            Skip
            </Text>
        </TouchableOpacity>
      </View>
      
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
            const newIndex = Math.ceil(contentOffset / width);
            if (newIndex !== currentIndex) {
              setCurrentIndex(newIndex);
            }
          }}
        />
      </View>
      
      {/* Progress and Next Button */}
      <View className="px-8 pb-10">
        {/* Progress bar */}
        <View className="h-1 bg-gray-200 rounded-full w-full mb-8">
          <Animated.View 
            className="h-1 bg-[#1DA1FA] rounded-full" 
            style={progressStyle} 
          />
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