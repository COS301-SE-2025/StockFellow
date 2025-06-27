import { TouchableOpacity, Text } from 'react-native'
import React from 'react'

interface CustomButtonProps {
  title: string;
  containerStyles?: string;
  textStyles?: string;
  handlePress?: () => void;
  icon?: React.ReactNode;
  isLoading?: boolean;
}

const CustomButton = ({
  title, 
  handlePress, 
  containerStyles = '', 
  textStyles = '', 
  isLoading = false
}: CustomButtonProps) => {
  return (
    <TouchableOpacity
      onPress={handlePress}
      activeOpacity={0.7}
      className={`justify-center items-center ${containerStyles}`}
      disabled={isLoading}
    >
      <Text className={`${textStyles}`}>
        {title}
      </Text>
    </TouchableOpacity>
  )
}

export default CustomButton