import React, { useState } from "react";
import { View, TouchableOpacity, Image } from "react-native";
import { TextInput } from "react-native-paper";
import { icons } from "../constants";
import tw from "twrnc";

const FormField = ({
  title,
  value,
  placeholder,
  handleChangeText,
  otherStyles,
  ...props
}) => {
  // Separate states for Password and Confirm Password visibility
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const toggleVisibility = () => {
    if (title === "Password") {
      setShowPassword(!showPassword);
    } else if (title === "Confirm Password") {
      setShowConfirmPassword(!showConfirmPassword);
    }
  };

  return (
    <View style={[otherStyles]}>
      <View style={tw`w-full h-16 flex-row items-center`}>
        <TextInput
          mode="outlined"
          label={title}
          value={value}
          placeholder={placeholder}
          placeholderTextColor="#7B7B8B"
          onChangeText={handleChangeText}
          secureTextEntry={
            (title === "Password" && !showPassword) ||
            (title === "Confirm Password" && !showConfirmPassword)
          }
          style={tw`flex-1 bg-transparent`}
          outlineStyle={tw`rounded-xl`}
          outlineColor="#8E8E8E"
          activeOutlineColor="#007D05"
          right={
            (title === "Password" || title === "Confirm Password") && (
              <TextInput.Icon
                icon={() => (
                  <TouchableOpacity onPress={toggleVisibility}>
                    <Image
                      source={
                        (title === "Password" && !showPassword) ||
                        (title === "Confirm Password" && !showConfirmPassword)
                          ? icons.eye
                          : icons.eyehide
                      }
                      style={tw`w-6 h-6 mt-3`}
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
