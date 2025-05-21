import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import tw from 'twrnc'; // If you're using tailwind with React Native

const Header = ({ title }) => {
  return (
    <View style={tw`p-4`}>
      <Text style={tw`text-white text-xl font-bold text-center`}>
        {title}
      </Text>
    </View>
  );
};

export default Header;
