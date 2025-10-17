import React from 'react';
import { View, Text, TouchableOpacity, Modal, Image } from 'react-native';
import { icons } from '../constants';
import { useTheme } from '../../app/_layout';

interface PayoutCountdownModalProps {
  isVisible: boolean;
  onClose: () => void;
  daysUntilPayout: number;
  nextPayoutDate: string;
}

const PayoutCountdownModal: React.FC<PayoutCountdownModalProps> = ({
  isVisible,
  onClose,
  daysUntilPayout,
  nextPayoutDate
}) => {
  const { colors } = useTheme();

  const getCountdownColor = () => {
    if (daysUntilPayout <= 7) return '#F59E0B'; // Amber
    if (daysUntilPayout <= 30) return '#1DA1FA'; // Blue
    return '#10B981'; // Green
  };

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
              Payout Countdown
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

          {/* Countdown Display */}
          <View className="items-center mb-6">
            <View 
              className="w-32 h-32 rounded-full items-center justify-center mb-4"
              style={{ backgroundColor: getCountdownColor() + '20' }}
            >
              <Text 
                className="text-4xl font-['PlusJakartaSans-Bold']"
                style={{ color: getCountdownColor() }}
              >
                {daysUntilPayout}
              </Text>
              <Text 
                className="text-sm font-['PlusJakartaSans-Medium']"
                style={{ color: getCountdownColor() }}
              >
                days left
              </Text>
            </View>

            <Text 
              style={{ color: colors.text }}
              className="text-lg font-['PlusJakartaSans-SemiBold'] mb-2"
            >
              Next Payout Date
            </Text>
            <Text 
              style={{ color: colors.text }}
              className="text-base font-['PlusJakartaSans-Medium'] opacity-70"
            >
              {nextPayoutDate}
            </Text>
          </View>

          {/* Status Message */}
          <View className="bg-blue-50 rounded-xl p-4 mb-6">
            <Text 
              className="text-center text-sm font-['PlusJakartaSans-Medium']"
              style={{ color: '#1DA1FA' }}
            >
              {daysUntilPayout <= 7 
                ? "Your payout is coming up soon! Make sure all contributions are up to date."
                : "Keep making your contributions on time to ensure your payout."
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

export default PayoutCountdownModal;