import React from 'react';
import { View, Text, TouchableOpacity, Image } from 'react-native';
import { useRouter } from 'expo-router';
import { icons } from '../constants';
import { useTheme } from '../../app/_layout';
import HelpButton from './help/HelpButton';

interface TopBarProps {
  title?: string;
  showBackButton?: boolean;
  rightComponent?: React.ReactNode;
  onBackPress?: () => void;
}

const TopBar: React.FC<TopBarProps> = ({
  title,
  showBackButton = false,
  rightComponent,
  onBackPress,
}) => {
  const router = useRouter();
  const { isDarkMode, toggleTheme, colors } = useTheme();

  const handleBackPress = () => {
    if (onBackPress) {
      onBackPress();
    } else {
      router.back();
    }
  };

  return (
    <View 
      style={{ backgroundColor: colors.background }} 
      className="w-full px-6 flex-row justify-between items-center"
    >
      <View className="flex-row items-center">
        {showBackButton && (
          <TouchableOpacity onPress={handleBackPress} className="mr-4">
            <Image
              source={icons.back}
              style={{ tintColor: colors.text }}
              className="w-6 h-6"
            />
          </TouchableOpacity>
        )}
        {title && (
          <Text style={{ color: colors.text }} className="text-lg font-['PlusJakartaSans-SemiBold']">
            {title}
          </Text>
        )}
      </View>

      <View className="flex-row items-center">
        

        {rightComponent}

        {/* Help Button */}
        <HelpButton />
        
        {/* Theme Toggle Button */}
        <TouchableOpacity
          onPress={toggleTheme}
          className="p-2"
        >
          <Image 
            source={icons.light}
            style={{ 
              width: 28,
              height: 28,
              tintColor: isDarkMode ? '#FFFFFF' : '#000000'
            }}
            resizeMode="contain"
          />
        </TouchableOpacity>

        {/* Notifications Button */}
        <TouchableOpacity 
          onPress={() => router.push('/notifications')}
          className="p-2 mr-2"
        >
          <Image 
            source={icons.bell_filled}
            style={{ 
              width: 24,
              height: 24,
              // tintColor: colors.text
            }}
            resizeMode="contain"
          />
        </TouchableOpacity>
      </View>
    </View>
  );
};

export default TopBar;