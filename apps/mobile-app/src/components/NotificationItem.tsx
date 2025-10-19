import React from 'react';
import { View, Text, Image, TouchableOpacity } from 'react-native';
import { icons } from '../constants';
import { useTheme } from '../../app/_layout';

interface NotificationItemProps {
  type: string;
  title: string;
  message: string;
  timeAgo: string;
  readStatus: boolean;
  onPress?: () => void;
}

const NotificationItem: React.FC<NotificationItemProps> = ({
  type,
  title,
  message,
  timeAgo,
  readStatus,
  onPress,
}) => {
  const { isDarkMode, colors } = useTheme();

  const getIconForType = () => {
    switch(type.toLowerCase()) {
      case 'payment':
        return icons.debitcard;
      case 'reminder':
        return icons.remainder;
      case 'alert':
        return icons.alert;
      default:
        return icons.bell;
    }
  };

  const unreadStyle = isDarkMode
    ? { backgroundColor: 'rgba(29,161,250,0.10)' } // subtle primary tint in dark mode
    : { backgroundColor: '#EFF6FF' }; // Tailwind blue-50

  return (
    <TouchableOpacity 
      onPress={onPress}
      className="p-4 flex-row items-center justify-around"
      style={!readStatus ? unreadStyle : undefined}
    >
      <View className="mr-3">
        <Image
          source={getIconForType()}
          className="w-9 h-9"
          resizeMode="contain"
        />
      </View>
      
      <View className="flex-1">
        {/* Optional title could go here if used */}
        <Text className="mt-1 mx-1 text-sm" style={{ color: colors.text, opacity: 0.8 }}>
          {message}
        </Text>
      </View>
      
      <Text className="text-xs" style={{ color: colors.text, opacity: 0.6 }}>
        {timeAgo}
      </Text>
    </TouchableOpacity>
  );
};

export default NotificationItem;