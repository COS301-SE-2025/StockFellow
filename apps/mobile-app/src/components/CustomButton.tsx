import { TouchableOpacity, Text } from 'react-native'
import React from 'react'

type CustomButtonProps = {
  title: string;
  handlePress: () => void;
  containerStyles?: string;
  textStyles?: string;
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