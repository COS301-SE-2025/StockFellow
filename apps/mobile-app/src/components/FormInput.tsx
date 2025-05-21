import React, { useState } from "react";
import { View, TouchableOpacity, Image } from "react-native";
import { TextInput } from "react-native-paper";
import { icons } from "../constants";

type FormFieldProps = {
  title: string;
  value: string;
  placeholder: string;
  handleChangeText: (text: string) => void;
  otherStyles?: string;
  [key: string]: any;
};

const FormField = ({
  title,
  value,
  placeholder,
  handleChangeText,
  otherStyles = "",
  ...props
}: FormFieldProps) => {
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const toggleVisibility = () => {
    if (title === "Password") {
      setShowPassword(!showPassword);
    } else if (title === "Confirm Password") {
      setShowConfirmPassword(!showConfirmPassword);
    }
  };

  const isPasswordField = title === "Password" || title === "Confirm Password";
  const shouldHideText = 
    (title === "Password" && !showPassword) || 
    (title === "Confirm Password" && !showConfirmPassword);

  return (
    <View className={otherStyles}>
      <View className="w-full h-16 flex-row items-center">
        <TextInput
          mode="outlined"
          label={title}
          value={value}
          placeholder={placeholder}
          placeholderTextColor="#7B7B8B"
          onChangeText={handleChangeText}
          secureTextEntry={shouldHideText}
          className="flex-1 bg-transparent"
          outlineStyle={{ borderRadius: 12 }}
          outlineColor="#8E8E8E"
          activeOutlineColor="#007D05"
          right={
            isPasswordField && (
              <TextInput.Icon
                icon={() => (
                  <TouchableOpacity onPress={toggleVisibility}>
                    <Image
                      source={shouldHideText ? icons.eye : icons.eyehide}
                      className="w-6 h-6 mt-3"
                      resizeMode="contain"
                    />
                  </TouchableOpacity>
                )}
              />
            )
          }
          {...props}
        />
      </View>
    </View>
  );
};

export default FormField;