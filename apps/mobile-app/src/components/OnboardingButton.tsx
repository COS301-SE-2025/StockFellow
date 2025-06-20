import React from 'react';
import { TouchableOpacity, Text, StyleSheet } from 'react-native';

interface OnboardingButtonProps {
  title: string;
  onPress: () => void;
  primary?: boolean;
}

const OnboardingButton: React.FC<OnboardingButtonProps> = ({ 
  title, 
  onPress, 
  primary = true 
}) => {
  return (
    <TouchableOpacity
      onPress={onPress}
      className={`py-4 px-8 rounded-full ${primary ? 'bg-[#1DA1FA]' : 'bg-white border border-[#1DA1FA]'}`}
      style={styles.buttonShadow}
    >
      <Text 
        className={`text-center font-bold text-lg ${primary ? 'text-white' : 'text-[#1DA1FA]'}`}
      >
        {title}
      </Text>
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  buttonShadow: {
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  }
});

export default OnboardingButton;