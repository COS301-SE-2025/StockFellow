import React from 'react'; 
import { View, Text, Image, TouchableOpacity } from 'react-native';
import { icons } from '../constants';

interface StokvelCardProps {
  name: string;
  memberCount: number;
  balance: string | number;
  onPress: () => void;
}

const StokvelCard: React.FC<StokvelCardProps> = ({ name, memberCount, balance, onPress }) => {
  return (
    <TouchableOpacity 
      onPress={onPress}
      style={{ height: 85 }}
      className="flex-row items-center bg-white mb-1 p-2"
    >
      {/* Left section: Icon and info */}
      <View className="flex-row items-center flex-1 ml-[-10px]">
        <Image 
          source={icons.group}
          className="w-13 h-13 tint-[#1DA1FA]"
        />

        <View>
          <Text className="text-base font-['PlusJakartaSans-SemiBold'] text-gray-800">
            {name}
          </Text>
          <Text className="text-sm font-['PlusJakartaSans-Regular'] text-gray-500">
            {memberCount} Members
          </Text>
        </View>
      </View>

      {/* Right section: Balance */}
      <Text className="text-base font-['PlusJakartaSans-SemiBold'] text-[#5BDA8C]">
        R{balance}
      </Text>
    </TouchableOpacity>
  );
};

export default StokvelCard;
