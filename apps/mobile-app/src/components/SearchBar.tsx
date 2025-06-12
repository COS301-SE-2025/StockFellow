import React from 'react';
import { View, TextInput, Image, StyleSheet } from 'react-native';
import { icons } from '../constants';
import { useTheme } from '../../app/_layout';

interface SearchBarProps {
  value: string;
  onChangeText: (text: string) => void;
  placeholder?: string;
}

const SearchBar: React.FC<SearchBarProps> = ({ value, onChangeText, placeholder }) => {
  const { isDarkMode, colors } = useTheme();
  
  return (
    <View 
      style={{ 
        backgroundColor: isDarkMode ? '#2C2C2C' : 'white',
        borderColor: isDarkMode ? '#444444' : '#F0F0F0'
      }}
      className="flex-row items-center border rounded-full px-4 py-2 shadow-sm"
    >
      <TextInput
        value={value}
        onChangeText={onChangeText}
        placeholder={placeholder || "Search for a Stokvel"}
        placeholderTextColor={isDarkMode ? '#A0A0A0' : '#6F6F6F'}
        style={{ color: isDarkMode ? '#FFFFFF' : '#000000' }}
        className="flex-1 text-base pr-3"
      />
      <View style={[
        styles.iconWrapper,
        { backgroundColor: '#1DA1FA' } // Darker blue in dark mode
      ]}>
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
    padding: 10,
    justifyContent: 'center',
    alignItems: 'center',
    borderRadius: 999,
  },
  icon: {
    width: 18,
    height: 18,
    tintColor: '#FFFFFF',
  },
});

export default SearchBar;
