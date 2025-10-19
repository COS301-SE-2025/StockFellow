import React from 'react';
import { TouchableOpacity, Text, View } from 'react-native';
import { useTheme } from '../../app/_layout';

interface RadioBoxProps {
  options: string[];
  selectedOption: string;
  onSelect: (option: string) => void;
}

const RadioBox: React.FC<RadioBoxProps> = ({ options, selectedOption, onSelect }) => {
  const { isDarkMode, colors } = useTheme();

  return (
    <View className="flex-row  mb-6">
      {options.map((option) => {
        const isSelected = selectedOption === option;
        const textColor = isSelected
          ? colors.primary // keep selected blue
          : (isDarkMode ? '#FFFFFF' : '#111827'); // white on dark, gray-900 on light
        const fontWeightClass = isSelected ? 'font-semibold' : 'font-light';

        return (
          <TouchableOpacity
            key={option}
            onPress={() => onSelect(option)}
            className="pb-2 px-4 mt-3 border-b-4"
            style={{ borderBottomColor: isSelected ? colors.primary : 'transparent' }}
          >
            <Text
              className={`text-lg ${fontWeightClass}`}
              style={{ color: textColor }}
            >
              {option}
            </Text>
          </TouchableOpacity>
        );
      })}
    </View>
  );
};

export default RadioBox;