import React from 'react'; 
import { View, Text, Image, TouchableOpacity } from 'react-native';
import { icons } from '../constants';
import { useTheme } from '../../app/_layout';

interface StokvelCardProps {
  name: string;
  memberCount: number;
  balance: string | number;
  onPress: () => void;
}

const StokvelCard: React.FC<StokvelCardProps> = ({ name, memberCount, balance, onPress }) => {
  const { isDarkMode, colors } = useTheme();
  
  return (
    <TouchableOpacity 
      onPress={onPress}
      style={{ 
        height: 85, 
      }}
      className="flex-row items-center mb-1 p-2 rounded-lg"
    >
      {/* Left section: Icon and info */}
      <View className="flex-row items-center flex-1 ml-[-10px]">
        <Image 
          source={icons.group}
          className="w-13 h-13"
          style={{ 
            opacity: 0.9
          }}
        />

        <View>
          <Text 
            style={{ color: isDarkMode ? '#CCCCCC' : '#1A1A1A' }}
            className="text-base font-['PlusJakartaSans-SemiBold']"
          >
            {name}
          </Text>
          <Text 
            style={{ color: isDarkMode ? '#AAAAAA' : '#6F6F6F' }}
            className="text-sm font-['PlusJakartaSans-Regular']"
          >
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
