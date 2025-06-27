import React from 'react';
import { View, Text, TouchableOpacity, Image } from 'react-native';
import { icons } from '../constants';

interface ActivityItemProps {
  title: string;
  subtitle: string;
  amount: string;
  onPress?: () => void;
}

const ActivityItem: React.FC<ActivityItemProps> = ({ 
  title, 
  subtitle, 
  amount, 
  onPress 
}) => {
  return (
    <TouchableOpacity 
      onPress={onPress}
      className="flex-row items-center bg-blue-50 rounded-2xl p-4 mb-3"
    >
      {/* Profile Icon */}
      <View className="mr-4">
        <Image 
          source={icons.up}
          className="w-4 h-4"
          resizeMode="contain"
        />
      </View>

      {/* Content */}
      <View className="flex-1 flex-row items-center justify-between">
        {/* Text Section */}
        <View>
          <Text className="text-gray-900 text-base font-['PlusJakartaSans-SemiBold']">
            {title}
          </Text>
          <Text className="text-gray-500 text-sm font-['PlusJakartaSans-Regular']">
            {subtitle}
          </Text>
        </View>

        {/* Amount Section with Blue Line */}
        <View className="flex-row items-center">
          {/* Blue Vertical Line */}
          <View className="w-[2px] h-12 bg-[#1DA1FA] mr-3" />
          
          {/* Amount and Arrow */}
          <View className="flex-row items-center">
            <Text className="text-gray-900 text-base font-['PlusJakartaSans-SemiBold'] mr-3">
              R{amount}
            </Text>
            <Image 
              source={icons.right}
              className="w-4 h-4"
              style={{ tintColor: '#000' }}
              resizeMode="contain"
            />
          </View>
        </View>
      </View>
    </TouchableOpacity>
  );
};

export default ActivityItem;
