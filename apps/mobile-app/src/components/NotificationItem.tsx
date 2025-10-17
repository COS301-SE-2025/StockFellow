import React from 'react';
import { View, Text, Image, TouchableOpacity } from 'react-native';
import { icons } from '../constants';

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
  const getIconForType = () => {
    switch(type.toLowerCase()) {
      case 'payment':
        return icons.debitcard;
    //   case 'invite':
    //     return icons.userPlus;
      case 'reminder':
        return icons.remainder;
      case 'alert':
        return icons.alert;
      default:
        return icons.bell;
    }
  };

  return (
    <TouchableOpacity 
      onPress={onPress}
      className={`p-4 flex-row items-center justify-around ${!readStatus ? 'bg-blue-50' : ''}`}
    >
      <View className="mr-3">
        <Image
          source={getIconForType()}
          className="w-9 h-9"
          resizeMode="contain"
        />
      </View>
      
      <View className="flex-1">
        {/* <Text className="font-['PlusJakartaSans-SemiBold'] text-base">{title}</Text> */}
        <Text className="text-gray-600 mt-1 mx-1 text-sm">{message}</Text>
      </View>
      
      <Text className="text-gray-400 text-xs">{timeAgo}</Text>
    </TouchableOpacity>
  );
};

export default NotificationItem;