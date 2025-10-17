import React from 'react';
import { View, Text, Image, TouchableOpacity, ScrollView } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Link, useRouter } from "expo-router";
import { StatusBar } from 'expo-status-bar';
import { icons } from '../../src/constants';

interface FeatureProps {
  icon: any;
  title: string;
  description: string;
}

const Feature: React.FC<FeatureProps> = ({ icon, title, description }) => (
  <View className="flex-row items-start mb-2">
    <View className="rounded-full">
      <Image
        source={icon}
        style={{ width: 80, height: 80 }}
      // resizeMode="contain"
      />
    </View>
    <View className="flex-1 ml-2">
      <Text className="text-gray-800 text-lg font-['PlusJakartaSans-SemiBold'] mb-1">
        {title}
      </Text>
      <Text className="text-gray-600 font-['PlusJakartaSans-Regular']">
        {description}
      </Text>
    </View>
  </View>
);

const OnboardingScreen = () => {
  const router = useRouter();

  return (
    <SafeAreaView className="flex-1 bg-white">
      <StatusBar style="dark" />

      <ScrollView className="flex-1">
        {/* Header Section */}
        <View className="items-center pt-4 pb-10">
          <Image
            source={icons.savings}
            style={{ width: 180, height: 180 }}
            resizeMode="contain"
          />
          <Text className="text-[#1DA1FA] text-3xl font-['PlusJakartaSans-Bold'] mt-2">
            Welcome to StockFellow
          </Text>
          <Text className="text-gray-600 text-center text-lg font-['PlusJakartaSans-Regular'] mt-2 px-8">
            Your digital stokvel companion for seamless group savings
          </Text>
        </View>

        {/* Features Section */}
        <View className="px-6">
          <Feature
            icon={icons.contribute}
            title="Save Together"
            description="Join trusted stokvel groups or create your own to build wealth together"
          />

          <Feature
            icon={icons.notifications}
            title="Smart & Secure"
            description="Automated contributions with secure debit orders and fraud protection"
          />

          <Feature
            icon={icons.growth}
            title="Grow & Earn"
            description="Unlock higher tiers and better benefits as your savings grow"
          />
        </View>
      </ScrollView>

      {/* Action Buttons */}
      <View className="px-6 pb-8">
        <TouchableOpacity
          onPress={() => router.push('/(auth)/signup')}
          className="bg-[#1DA1FA] py-4 rounded-full mb-4"
        >
          <Text className="text-white text-center text-lg font-['PlusJakartaSans-SemiBold']">
            Get Started
          </Text>
        </TouchableOpacity>

        <View className="flex-row justify-center gap-2 mt-1">
          <Text className="text-base text-[#71727A]">Already have an account?</Text>
          <Link href="/login" className="text-[#1DA1FA] font-semibold text-base">
            Login
          </Link>
        </View>
      </View>
    </SafeAreaView>
  );
};

export default OnboardingScreen;