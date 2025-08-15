import React from 'react';
import { View, Text, Image } from 'react-native';

interface GreetingCardProps {
  userName: string;
  profileImage?: string;
}

const GreetingCard: React.FC<GreetingCardProps> = ({ userName, profileImage }) => {
  return (
    <View className="rounded-2xl p-4 mb-4 flex-row items-center bg-blue-50 shadow-sm">
      {/* Profile Picture */}
      <View className="mr-3">
        {profileImage ? (
          <Image 
            source={{ uri: profileImage }}
            className="w-12 h-12 rounded-full"
          />
        ) : (
          <View className="w-12 h-12 rounded-full items-center justify-center bg-blue-500">
            <Text className="text-white text-lg font-['PlusJakartaSans-Bold']">
              {userName.charAt(0).toUpperCase()}
            </Text>
          </View>
        )}
      </View>

      {/* Greeting Text */}
      <View className="flex-1">
        <Text className="text-xl font-['PlusJakartaSans-Bold'] mb-1 text-gray-900">
          Welcome back, {userName}!
        </Text>
        <Text className="text-sm font-['PlusJakartaSans-Medium'] opacity-70 text-gray-700">
          Let’s check your stokvel progress.
        </Text>
      </View>
    </View>
  );
};

export default GreetingCard;