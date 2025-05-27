import React from 'react';
import { View, TextInput, Image, StyleSheet } from 'react-native';
import { icons } from '../constants';

interface SearchBarProps {
  value: string;
  onChangeText: (text: string) => void;
  placeholder?: string;
}

const SearchBar: React.FC<SearchBarProps> = ({ value, onChangeText, placeholder }) => {
  return (
    <View className="flex-row items-center bg-white border border-gray-300 rounded-full px-4 py-2 shadow-sm">
      <TextInput
        value={value}
        onChangeText={onChangeText}
        placeholder={placeholder || "Search for a Stokvel"}
        className="flex-1 text-base text-gray-800 pr-3"
        placeholderTextColor="#6F6F6F"
      />
      <View style={styles.iconWrapper}>
        <Image 
          source={icons.search}
          style={styles.icon}
          resizeMode="contain"
        />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  iconWrapper: {
    backgroundColor: '#1DA1FA',
    borderRadius: 999,
    padding: 10,
    justifyContent: 'center',
    alignItems: 'center',
  },
  icon: {
    width: 18,
    height: 18,
    tintColor: '#FFFFFF',
  },
});

export default SearchBar;
