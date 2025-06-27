import React from 'react';
import { View, Text, Image, TouchableOpacity } from 'react-native';
import { icons } from '../constants';
import { useTheme } from '../../app/_layout';

interface StokvelCardProps {
  name: string;
  memberCount: number;
  balance: string | number;
  profileImage?: string | null; // Add this prop
  onPress: () => void;
}

const StokvelCard: React.FC<StokvelCardProps> = ({
  name,
  memberCount,
  balance,
  profileImage,
  onPress
}) => {
  const { isDarkMode, colors } = useTheme();

  return (
    <TouchableOpacity
      onPress={onPress}
      
      className="w-full flex-row items-center mb-1 px-2 "
    >
      {/* Left section: Icon and info */}
      <View className="flex-row items-center flex-1">
        {/* Profile Image with fallback */}

        <Image
          source={profileImage ? { uri: profileImage } : icons.group}
          className="w-20 h-20  shadow-xs shadow-[#1DA1FA]/90 self-center"
          resizeMode={"contain"}
        />


        <View className='flex-1 justify-center'>
          <Text
            style={{ color: isDarkMode ? '#CCCCCC' : '#1A1A1A' }}
            className="text-base font-['PlusJakartaSans-SemiBold']"
            numberOfLines={1}
          >
            {name}
          </Text>
          <Text
            style={{ color: isDarkMode ? '#AAAAAA' : '#6F6F6F' }}
            className="text-sm font-['PlusJakartaSans-Regular']"
          >
            {memberCount} {memberCount === 1 ? 'Member' : 'Members'}
          </Text>
        </View>
      </View>

      {/* Right section: Balance */}
      <Text className="text-base font-['PlusJakartaSans-SemiBold'] text-[#5BDA8C]">
        {balance}
      </Text>
    </TouchableOpacity>
  );
};

export default StokvelCard;