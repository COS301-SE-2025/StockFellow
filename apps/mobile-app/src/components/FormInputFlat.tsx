import React from "react";
import { View, TextInput, TouchableOpacity, Image, Text } from "react-native";
import { useTheme } from "../../app/_layout";

interface FormInputFlatProps {
  title?: string;
  value: string;
  placeholder?: string;
  handleChangeText: (text: string) => void;
  icon?: any; // Consider using ImageSourcePropType for more specific typing
  onIconPress?: () => void;
  helperText?: string;
  helperTextColor?: string; // Optional color override
  [key: string]: any; // For additional props
}

const FormInputFlat: React.FC<FormInputFlatProps> = ({
  title,
  value,
  placeholder,
  handleChangeText,
  icon,
  onIconPress,
  helperText,
  helperTextColor = "#1DA1FA", // Default gray color
  ...props
}) => {
  const { isDarkMode } = useTheme();
  return (
    <View className="mb-5">
      {/* Label */}
      {title && <Text className="text-lg font-light mb-1">{title}</Text>}

      {/* Input with Icon */}
      <View className="flex-row items-center border rounded-xl border-[#C5C6CC] pb-1">
        <TextInput
          value={value}
          cursorColor="#009963"
          placeholder={placeholder}
          placeholderTextColor="#A0A0A0"
          onChangeText={handleChangeText}
          className="flex-1 text-base text-gray-800 px-5 py-3 font-light" // Added py-3 for better vertical padding
          style={isDarkMode ? { color: '#FFFFFF' } : undefined}
          {...props}
        />
        {icon && (
          <TouchableOpacity onPress={onIconPress} className="pr-4">
            <Image 
              source={icon} 
              className="w-6 h-6" 
              style={{ tintColor: "#009963" }} 
            />
          </TouchableOpacity>
        )}
      </View>

      {/* Helper Text */}
      {helperText && (
        <Text 
          className="text-xs mt-2"
          style={{ color: helperTextColor }}
        >
          {helperText}
        </Text>
      )}
    </View>
  );
};

export default FormInputFlat;