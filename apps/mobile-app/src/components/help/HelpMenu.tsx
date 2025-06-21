import React from "react";
import { View, Text, TouchableOpacity, Image, Modal } from 'react-native';
import { icons } from '../../constants';
import { useTheme } from '../../../app/_layout';
import { useRouter } from 'expo-router';

interface HelpMenuProps {
  isVisible: boolean;
  onClose: () => void;
}

const HelpMenu: React.FC<HelpMenuProps> = ({ isVisible, onClose }) => {
  const { isDarkMode, colors } = useTheme();
  const router = useRouter();

  const helpItems = [
    {
      title: 'App Tutorial',
      description: 'Take a guided tour of StockFellow',
      icon: icons.help,
      onPress: () => {}
    },
    {
      title: 'FAQ',
      description: 'Learn about stokvel groups',
      icon: icons.help,
      onPress: () => {}
    },
    {
      title: 'Contact Support',
      description: 'Get help with any issues',
      icon: icons.help || icons.profile,
      onPress: () => {}
    }
  ];

  return (
    <Modal
      visible={isVisible}
      transparent
      animationType="fade"
      onRequestClose={onClose}
    >
      <View className="flex-1 bg-black/50 justify-center items-center">
        <View 
          style={{ backgroundColor: colors.card }}
          className="w-[85%] rounded-2xl overflow-hidden"
        >
          {/* Header */}
          <View className="flex-row justify-between items-center p-4 border-b border-gray-200">
            <Text 
              style={{ color: colors.text }}
              className="text-xl font-['PlusJakartaSans-Bold']"
            >
              Help & Support
            </Text>
            <TouchableOpacity onPress={onClose} className="p-2">
              <Image 
                source={icons.help || icons.back}
                className="w-6 h-6"
                style={{ tintColor: colors.text }}
                resizeMode="contain"
              />
            </TouchableOpacity>
          </View>

          {/* Help Items */}
          <View>
            {helpItems.map((item, index) => (
              <TouchableOpacity 
                key={index}
                className="flex-row items-center p-4 border border-gray-200 "
                onPress={item.onPress}
              >
                <View className="items-center justify-center mr-6 ml-2">
                  <Image 
                    source={item.icon}
                    className="w-6 h-6"
                    style={{ tintColor: '#1DA1FA' }}
                    resizeMode="contain"
                  />
                </View>
                <View className="flex-1">
                  <Text 
                    style={{ color: colors.text }}
                    className="text-base font-['PlusJakartaSans-SemiBold'] mb-1"
                  >
                    {item.title}
                  </Text>
                  <Text 
                    style={{ color: isDarkMode ? '#9CA3AF' : '#6B7280' }}
                    className="text-sm font-['PlusJakartaSans-Regular']"
                  >
                    {item.description}
                  </Text>
                </View>
              </TouchableOpacity>
            ))}
          </View>
        </View>
      </View>
    </Modal>
  );
};

export default HelpMenu;