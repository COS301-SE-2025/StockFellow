import React from 'react';
import { View, Text } from 'react-native';

interface HeaderProps {
  title: string;
}

const Header: React.FC<HeaderProps> = ({ title }) => {
  return (
    <View className="p-4 bg-primary">
      <Text className="text-xl font-bold text-center">
        {title}
      </Text>
    </View>
  );
};

export default Header;