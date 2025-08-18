import React from 'react';
import { View, TouchableOpacity, Image, Text } from 'react-native';
import { icons } from '../constants';

const QuickActions = () => {

  const actions = [
    { icon: icons.transactions, label: 'Pay' },
    { icon: icons.transactions, label: 'Transfer' },
    { icon: icons.transactions, label: 'History' },
  ];

  return (
    <View className="flex-row justify-around mt-6 mb-8">
      {actions.map((action, index) => (
        <TouchableOpacity 
          key={index}
          className="items-center"
        >
          <View className="bg-[#1DA1FA] p-4 rounded-xl mb-2">
            <Image 
              source={action.icon}
              className="w-10 h-10"
              style={{ tintColor: '#FFFFFF' }}
            />
          </View>
          <Text className="text-sm font-['PlusJakartaSans-Medium'] text-gray-700">
            {action.label}
          </Text>
        </TouchableOpacity>
      ))}
    </View>
  );
};

export default QuickActions;