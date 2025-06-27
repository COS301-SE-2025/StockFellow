import React from 'react';
import { View, Text, Image } from 'react-native';
import { useTheme } from '../../app/_layout';
import { icons } from '../constants';

interface SavingsCardProps {
  balance: string;
}

const SavingsCard: React.FC<SavingsCardProps> = ({ balance }) => {
  const { colors } = useTheme();

  return (
    <View className="bg-[#1da2fa96] rounded-2xl p-6 w-full mt-4 mb-4 h-48">
      <View className="flex-row items-center justify-between">
        <Text className="text-black text-2xl font-['PlusJakartaSans-SemiBold']">
          Stockfellow Savings
        </Text>
        <Image 
          source={icons.money}
          className="w-10 h-10 mr-2"
          style={{ tintColor: '#000000' }}
          resizeMode="contain"
        />
      </View>
      <Text className="text-white text-3xl font-extrabold mt-4">
        R {balance}
      </Text>
      <Text className="text-black text-sm font-['PlusJakartaSans-Regular'] mt-1 mb-2">
        Total Balance
      </Text>
    </View>
  );
};

export default SavingsCard;