// src/components/SimpleRadioBox.tsx
import React from 'react';
import { TouchableOpacity, View } from 'react-native';

interface SimpleRadioBoxProps {
  isSelected: boolean;
  onPress: () => void;
  size?: number;
  selectedColor?: string;
  unselectedColor?: string;
}

const SimpleRadioBox: React.FC<SimpleRadioBoxProps> = ({
  isSelected,
  onPress,
  size = 24,
  selectedColor = '#006FFD',
  unselectedColor = '#D1D5DB',
}) => {
  return (
    <TouchableOpacity onPress={onPress}>
      <View
        style={{
          width: size,
          height: size,
          borderRadius: size / 2,
          borderWidth: 2,
          borderColor: isSelected ? selectedColor : unselectedColor,
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        {isSelected && (
          <View
            style={{
              width: size * 0.6,
              height: size * 0.6,
              borderRadius: (size * 0.6) / 2,
              backgroundColor: selectedColor,
            }}
          />
        )}
      </View>
    </TouchableOpacity>
  );
};

export default SimpleRadioBox;