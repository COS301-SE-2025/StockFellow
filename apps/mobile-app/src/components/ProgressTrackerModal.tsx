import React from 'react';
import { View, Text, TouchableOpacity, Modal, Image } from 'react-native';
import { icons } from '../constants';
import { useTheme } from '../../app/_layout';

interface ProgressTrackerModalProps {
  isVisible: boolean;
  onClose: () => void;
  contributionsLeft: number;
  totalContributions: number;
}

const ProgressTrackerModal: React.FC<ProgressTrackerModalProps> = ({
  isVisible,
  onClose,
  contributionsLeft,
  totalContributions
}) => {
  const { colors } = useTheme();
  
  const completedContributions = totalContributions - contributionsLeft;
  const progressPercentage = (completedContributions / totalContributions) * 100;

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
          className="w-[85%] rounded-2xl p-6"
        >
          {/* Header */}
          <View className="flex-row justify-between items-center mb-6">
            <Text 
              style={{ color: colors.text }}
              className="text-xl font-['PlusJakartaSans-Bold']"
            >
              Progress Tracker
            </Text>
            <TouchableOpacity onPress={onClose} className="p-2">
              <Image 
                source={icons.close}
                className="w-4 h-4"
                style={{ tintColor: colors.text }}
                resizeMode="contain"
              />
            </TouchableOpacity>
          </View>

          {/* Progress Circle */}
          <View className="items-center mb-6">
            <View className="relative w-40 h-40 items-center justify-center mb-4">
              {/* Background Circle */}
              <View className="absolute w-40 h-40 rounded-full border-8 border-gray-200" />
              
              {/* Progress Circle */}
              <View 
                className="absolute w-40 h-40 rounded-full border-8 border-[#1DA1FA]"
                style={{
                  transform: [{ rotate: '-90deg' }],
                  borderTopColor: progressPercentage >= 25 ? '#1DA1FA' : 'transparent',
                  borderRightColor: progressPercentage >= 50 ? '#1DA1FA' : 'transparent',
                  borderBottomColor: progressPercentage >= 75 ? '#1DA1FA' : 'transparent',
                  borderLeftColor: progressPercentage >= 100 ? '#1DA1FA' : 'transparent',
                }}
              />
              
              {/* Center Content */}
              <View className="items-center">
                <Text 
                  className="text-3xl font-['PlusJakartaSans-Bold']"
                  style={{ color: colors.text }}
                >
                  {completedContributions}/{totalContributions}
                </Text>
                <Text 
                  className="text-sm font-['PlusJakartaSans-Medium'] opacity-70"
                  style={{ color: colors.text }}
                >
                  Contributions
                </Text>
              </View>
            </View>

            <Text 
              style={{ color: colors.text }}
              className="text-lg font-['PlusJakartaSans-SemiBold'] mb-2"
            >
              {Math.round(progressPercentage)}% Complete
            </Text>
            <Text 
              style={{ color: colors.text }}
              className="text-base font-['PlusJakartaSans-Medium'] opacity-70 text-center"
            >
              {contributionsLeft} contributions left until your payout turn
            </Text>
          </View>

          {/* Progress Steps */}
          <View className="mb-6">
            <Text 
              style={{ color: colors.text }}
              className="text-base font-['PlusJakartaSans-SemiBold'] mb-3"
            >
              Recent Contributions
            </Text>
            <View className="flex-row justify-between">
              {Array.from({ length: Math.min(5, totalContributions) }).map((_, index) => (
                <View 
                  key={index}
                  className={`w-12 h-12 rounded-full items-center justify-center ${
                    index < completedContributions ? 'bg-[#1DA1FA]' : 'bg-gray-200'
                  }`}
                >
                  <Text 
                    className={`text-sm font-['PlusJakartaSans-Bold'] ${
                      index < completedContributions ? 'text-white' : 'text-gray-500'
                    }`}
                  >
                    {index + 1}
                  </Text>
                </View>
              ))}
            </View>
          </View>

          {/* Status Message */}
          <View className="bg-green-50 rounded-xl p-4 mb-6">
            <Text 
              className="text-center text-sm font-['PlusJakartaSans-Medium']"
              style={{ color: '#10B981' }}
            >
              {contributionsLeft === 0 
                ? "Congratulations! You're ready for your payout!"
                : `Keep it up! ${contributionsLeft} more contributions to go.`
              }
            </Text>
          </View>

          {/* Close Button */}
          <TouchableOpacity
            onPress={onClose}
            className="bg-[#1DA1FA] rounded-xl py-3"
          >
            <Text className="text-center text-white font-['PlusJakartaSans-SemiBold']">
              Got it
            </Text>
          </TouchableOpacity>
        </View>
      </View>
    </Modal>
  );
};

export default ProgressTrackerModal;