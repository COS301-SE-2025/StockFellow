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
      style={{ 
        height: 85, 
      }}
      className="flex-row items-center mb-4 p-2 rounded-lg"
    >
      {/* Left section: Icon and info */}
      <View className="flex-row items-center flex-1">
        {/* Profile Image with fallback */}
        <View className="w-16 h-16 rounded-full bg-gray-200 items-center justify-center mr-3">
          <Image 
            source={profileImage ? { uri: profileImage } : icons.group}
            className="w-14 h-14 rounded-full"
            resizeMode={profileImage ? "cover" : "contain"}
          />
        </View>

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
        R{balance}
      </Text>
    </TouchableOpacity>
  );
};

export default StokvelCard;