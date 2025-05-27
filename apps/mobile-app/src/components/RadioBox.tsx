import React from 'react';
import { TouchableOpacity, Text, View } from 'react-native';

interface RadioBoxProps {
  options: string[];
  selectedOption: string;
  onSelect: (option: string) => void;
}

const RadioBox: React.FC<RadioBoxProps> = ({ options, selectedOption, onSelect }) => {
  return (
    <View className="flex-row justify-between mb-6">
      {options.map((option) => (
        <TouchableOpacity
          key={option}
          onPress={() => onSelect(option)}
          className={`pb-2 ${selectedOption === option ? 'border-b-2 border-[#006FFD]' : 'border-b-2 border-transparent'}`}
        >
          <Text 
            className={`text-base ${selectedOption === option ? 'text-[#006FFD] font-bold' : 'text-gray-600'}`}
          >
            {option}
          </Text>
        </TouchableOpacity>
      ))}
    </View>
  );
};

export default RadioBox;