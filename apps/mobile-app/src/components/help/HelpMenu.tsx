import React, { useState } from "react";
import { View, Text, TouchableOpacity, Image, Modal, ScrollView } from 'react-native';
import { icons } from '../../constants';
import { useTheme } from '../../../app/_layout';
import { useRouter } from 'expo-router';
import { Linking } from 'react-native';
import { useTutorial } from '../help/TutorialContext';

interface HelpMenuProps {
  isVisible: boolean;
  onClose: () => void;
}

const HelpMenu: React.FC<HelpMenuProps> = ({ isVisible, onClose }) => {
  const { isDarkMode, colors } = useTheme();
  const router = useRouter();
  const { startTutorial } = useTutorial();

  const [showFAQ, setShowFAQ] = useState(false);
  const [openFAQIndex, setOpenFAQIndex] = useState<number | null>(null);

  const toggleFAQ = (index: number) => {
    setOpenFAQIndex(openFAQIndex === index ? null : index);
  };

  const handleStartTutorial = () => {
    onClose();
    startTutorial();
  };

  const faqItems = [
    {
      question: 'Can I join more than one stokvel?',
      answer: 'Yes! You can join or create multiple stokvel groups, as long as the total monthly contribution across all your groups stays within your affordability limit'
    },
    {
      question: 'Can I leave a stokvel after joining?',
      answer: 'Yes, go to the group settings and choose "Leave Group". Note: you may only be allowed to leave after the current pay cycle ends.'
    },
    {
      question: 'How do I advance to a higher tier?',
      answer: 'By contributing on time, maintaining a positive record, and staying a reliable member, StockFellow will automatically update your tier.'
    },
    {
      question: 'Is my information secure?',
      answer: 'Absolutely. We use encrypted storage, multi-factor authentication and real-time fraud detection to protect your data.'
    },
    {
      question: 'Does StockFellow check my credit score?',
      answer: 'Yes, but we also allow alternatives like behavioral scoring for those without formal credit history.'
    }
  ];

  const helpItems = [
    {
      title: 'App Tutorial',
      description: 'Take a guided tour of StockFellow',
      icon: icons.help,
      onPress: handleStartTutorial
    },
    {
      title: 'Contact Support',
      description: 'Get help from our team',
      icon: icons.help,
      onPress: () => {
        Linking.openURL('mailto:devopps.capstone@gmail.com?subject=Support Request')
      }
    },
    {
      title: 'FAQ',
      description: 'Learn about stokvel groups',
      icon: icons.help,
      onPress: () => {
        setShowFAQ(!showFAQ);
      }
    }
  ];

  return (
    <Modal
      visible={isVisible}
      transparent
      animationType="fade"
      onRequestClose={onClose}
    >
      <View className="flex-1 justify-center items-center bg-black/30">
        <View 
          style={{ backgroundColor: colors.card }}
          className={`w-[85%] rounded-2xl overflow-hidden ${
            showFAQ ? 'h-[500px]' : 'h-[320px]'
          }`}
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

          {/* Scrollable Content */}
          <ScrollView className="px-0 pt-1 pb-6">
            {helpItems.map((item, index) => (
              <TouchableOpacity 
                key={index}
                className="flex-row items-center p-4 border border-gray-200"
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

            {showFAQ && (
              <View className="px-4 pb-4">
                <Text 
                  style={{ color: colors.text }}
                  className="text-lg font-['PlusJakartaSans-Bold'] my-4"
                >
                  Frequently Asked Questions
                </Text>
                {faqItems.map((faq, index) => (
                  <View key={index} className="mb-2 border border-gray-200 rounded-xl">
                    <TouchableOpacity 
                      onPress={() => toggleFAQ(index)} 
                      className="p-3 bg-gray-100 dark:bg-gray-800 rounded-t-xl"
                    >
                      <Text 
                        className="font-['PlusJakartaSans-SemiBold'] text-base"
                        style={{ color: colors.text }}
                      >
                        {faq.question}
                      </Text>
                    </TouchableOpacity>
                    {openFAQIndex === index && (
                      <View className="p-3 bg-white dark:bg-gray-900 rounded-b-xl">
                        <Text 
                          className="text-sm font-['PlusJakartaSans-Regular']"
                          style={{ color: isDarkMode ? '#D1D5DB' : '#374151' }}
                        >
                          {faq.answer}
                        </Text>
                      </View>
                    )}
                  </View>
                ))}
              </View>
            )}
          </ScrollView>
        </View>
      </View>
    </Modal>
  );
};

export default HelpMenu;
