import { TouchableOpacity, Text} from 'react-native'
import React from 'react'
import tw from 'twrnc';

const CustomButton = ({title, handlePress, containerStyles, textStyles, isLoading}) => {
  return (
    <TouchableOpacity
      onPress={handlePress}
      activeOpacity={0.7}
      style={tw `justify-center items-center
        ${containerStyles}`}
        disabled={isLoading}
      >
      <Text style={tw `${textStyles}`}>
        {title}
      </Text>
    </TouchableOpacity>
  )
}

export default CustomButton