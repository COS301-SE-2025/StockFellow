import React from 'react';
import { View, Text, Image } from 'react-native';
import { icons } from '../constants';
import { useTheme } from '../../app/_layout';

interface GreetingCardProps {
  userName: string;
  profileImage?: string;
}

const GreetingCard: React.FC<GreetingCardProps> = ({ userName, profileImage }) => {
  const { isDarkMode, colors } = useTheme();

  return (
    <View
      className="rounded-2xl p-4 mb-4 flex-row items-center bg-blue-50 shadow-sm"
      style={isDarkMode ? { backgroundColor: colors.card } : undefined}
    >
      {/* Profile Picture */}
      <View className="mr-3">
        <Image 
          source={icons.user}
          className="w-12 h-12 rounded-full"
          resizeMode="cover"
        />
      </View>

      {/* Greeting Text */}
      <View className="flex-1">
        <Text
          className="text-xl font-['PlusJakartaSans-Bold'] mb-1 text-gray-900"
          style={isDarkMode ? { color: colors.text } : undefined}
        >
          Welcome back, {userName}!
        </Text>
        <Text
          className="text-sm font-['PlusJakartaSans-Medium'] opacity-70 text-gray-700"
          style={isDarkMode ? { color: colors.text, opacity: 0.7 } : undefined}
        >
          Let's check your stokvel progress.
        </Text>
      </View>
    </View>
  );
};

export default GreetingCard;