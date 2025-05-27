import React from 'react';
import { TouchableOpacity, Text, View } from 'react-native';

interface RadioBoxProps {
  options: string[];
  selectedOption: string;
  onSelect: (option: string) => void;
}

const RadioBox: React.FC<RadioBoxProps> = ({ options, selectedOption, onSelect }) => {
  return (
    <View className="flex-row  mb-6">
      {options.map((option) => (
        <TouchableOpacity
          key={option}
          onPress={() => onSelect(option)}
          className={`pb-2 px-4 mt-3 ${selectedOption === option ? 'border-b-4 border-[#006FFD]' : 'border-b-4 border-transparent'}`}
        >
          <Text 
            className={`text-lg ${selectedOption === option ? 'text-[#006FFD] font-semibold' : 'text-gray-900 font-light'}`}
          >
            {option}
          </Text>
        </TouchableOpacity>
      ))}
    </View>
  );
};

export default RadioBox;