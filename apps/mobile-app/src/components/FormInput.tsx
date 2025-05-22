import React, { useState } from "react";
import { View, TouchableOpacity, Image, Text } from "react-native";
import { TextInput } from "react-native-paper";
import { icons } from "../constants";

type FormFieldProps = {
  title: string;
  value: string;
  placeholder: string;
  handleChangeText: (text: string) => void;
  otherStyles?: string;
  error?: string;
  secureTextEntry?: boolean;
  [key: string]: any;
};

const FormField = ({
  title,
  value,
  placeholder,
  handleChangeText,
  otherStyles = "",
  error = "",
  ...props
}: FormFieldProps) => {
  const [showPassword, setShowPassword] = useState(false);

  const toggleVisibility = () => {
    setShowPassword(!showPassword);
  };

  const isPasswordField = title.includes("Password");
  const shouldHideText = isPasswordField && !showPassword;

  return (
    <View className={otherStyles}>
      <View className="w-full">
        <TextInput
          mode="outlined"
          label={title}
          value={value}
          placeholder={placeholder}
          placeholderTextColor="#8F9098"
          onChangeText={handleChangeText}
          secureTextEntry={shouldHideText}
          className="flex-1 bg-transparent"
          outlineStyle={{ borderRadius: 12 }}
          outlineColor={error ? "#FF3B30" : "#C5C6CC"}
          activeOutlineColor={error ? "#FF3B30" : "#1DA1FA"}
          contextMenuHidden={true}
          right={
            isPasswordField && (
              <TextInput.Icon
                icon={() => (
                  <TouchableOpacity onPress={toggleVisibility}>
                    <Image
                      source={shouldHideText ? icons.eye : icons.eyehide}
                      className="w-6 h-6"
                      resizeMode="contain"
                    />
                  </TouchableOpacity>
                )}
              />
            )
          }
          {...props}
        />
        {error && (
          <Text className="text-red-500 text-xs mt-1 ml-3">{error}</Text>
        )}
      </View>
    </View>
  );
};

export default FormField;