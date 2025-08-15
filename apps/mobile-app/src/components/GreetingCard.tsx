import React from 'react';
import { View, Text, Image } from 'react-native';
import { useTheme } from '../../app/_layout';

interface GreetingCardProps {
  userName: string;
  groupCount: number;
  profileImage?: string;
}

const GreetingCard: React.FC<GreetingCardProps> = ({ userName, groupCount, profileImage }) => {
  const { colors } = useTheme();

  return (
    <View className="bg-white rounded-2xl p-4 mb-4 shadow-sm flex-row items-center" style={{ backgroundColor: colors.card }}>
      {/* Profile Picture */}
      <View className="mr-3">
        {profileImage ? (
          <Image 
            source={{ uri: profileImage }}
            className="w-12 h-12 rounded-full"
          />
        ) : (
          <View 
            className="w-12 h-12 rounded-full items-center justify-center"
            style={{ backgroundColor: colors.primary }}
          >
            <Text className="text-white text-lg font-['PlusJakartaSans-Bold']">
              {userName.charAt(0).toUpperCase()}
            </Text>
          </View>
        )}
      </View>

      {/* Greeting Text */}
      <View className="flex-1">
        <Text 
          className="text-xl font-['PlusJakartaSans-Bold'] mb-1" 
          style={{ color: colors.text }}
        >
          Welcome back, {userName}
        </Text>
        <Text 
          className="text-sm font-['PlusJakartaSans-Medium'] opacity-70" 
          style={{ color: colors.text }}
        >
          You're helping {groupCount} groups stay on track this month!
        </Text>
      </View>
    </View>
  );
};

export default GreetingCard;