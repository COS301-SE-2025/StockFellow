import { TouchableOpacity, Text, ActivityIndicator, View } from 'react-native';
import React from 'react';

interface CustomButtonProps {
  title: string;
  containerStyles?: string;
  textStyles?: string;
  handlePress?: () => void;
  icon?: React.ReactNode;
  isLoading?: boolean;
  disabled?: boolean;
}

const CustomButton = ({
  title, 
  handlePress, 
  containerStyles = '', 
  textStyles = '', 
  isLoading = false,
  disabled = false
}: CustomButtonProps) => {
  return (
    <TouchableOpacity
      onPress={handlePress}
      activeOpacity={0.7}
      className={`justify-center items-center ${containerStyles} ${
        (isLoading || disabled) ? 'opacity-70' : ''
      }`}
      disabled={isLoading || disabled}
    >
      {isLoading ? (
        <View className="flex-row items-center justify-center">
          <ActivityIndicator 
            size="small" 
            color={textStyles.includes('text-white') ? 'white' : '#1DA1FA'} 
            className="mr-2"
          />
          <Text className={`${textStyles}`}>
            Processing...
          </Text>
        </View>
      ) : (
        <Text className={`${textStyles}`}>
          {title}
        </Text>
      )}
    </TouchableOpacity>
  );
};

export default CustomButton;