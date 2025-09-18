import React from 'react';
import { View, Text, Modal, TouchableOpacity, Image } from 'react-native';
import { useTheme } from '../../app/_layout';
import { icons } from '../constants';
interface AutoJoinPromptProps {
  visible: boolean;
  onAccept: () => void;
  onDecline: () => void;
  onClose: () => void;
  tierName: string;
}

const AutoJoinPrompt: React.FC<AutoJoinPromptProps> = ({
  visible,
  onAccept,
  onDecline,
  onClose,
  tierName
}) => {
  const { colors } = useTheme();

  return (
    <Modal
      visible={visible}
      transparent={true}
      animationType="fade"
      onRequestClose={onClose}
    >
      <View className="flex-1 justify-center items-center bg-black/50 px-6">
        <View 
          style={{ backgroundColor: colors.card }}
          className="rounded-2xl p-6 w-full max-w-md"
        >
          {/* Header */}
          <View className="items-center mb-4">
            <Image
              source={icons.stokvel} // Replace with your icon
              style={{ width: 60, height: 60, tintColor: colors.primary }}
              resizeMode="contain"
            />
            <Text 
              style={{ color: colors.text }}
              className="text-xl font-['PlusJakartaSans-Bold'] mt-3 text-center"
            >
              Join a Stokvel?
            </Text>
          </View>

          {/* Message */}
          <Text 
            style={{ color: colors.primary }}
            className="text-base font-['PlusJakartaSans-Regular'] text-center mb-6"
          >
            We noticed you're not part of any stokvels yet. Would you like to join a{' '}
            <Text style={{ color: colors.primary }} className="font-['PlusJakartaSans-SemiBold']">
              {tierName}
            </Text>{' '}
            stokvel automatically created for your spending tier?
          </Text>

          {/* Benefits */}
          <View className="bg-primary/10 rounded-xl p-4 mb-6">
            <Text 
              style={{ color: colors.primary }}
              className="text-sm font-['PlusJakartaSans-SemiBold'] mb-2"
            >
              Benefits:
            </Text>
            <View className="space-y-1">
              <Text style={{ color: colors.primary }} className="text-sm">
                • Automatic monthly contributions
              </Text>
              <Text style={{ color: colors.primary }} className="text-sm">
                • Regular payouts based on your tier
              </Text>
              <Text style={{ color: colors.primary }} className="text-sm">
                • Join members with similar spending habits
              </Text>
            </View>
          </View>

          {/* Buttons */}
          <View className="flex-row justify-between space-x-4">
            <TouchableOpacity
              onPress={onDecline}
              style={{ borderColor: colors.primary, borderWidth: 1 }}
              className="flex-1 py-3 rounded-xl items-center"
            >
              <Text style={{ color: colors.primary }} className="font-['PlusJakartaSans-SemiBold']">
                No Thanks
              </Text>
            </TouchableOpacity>
            
            <TouchableOpacity
              onPress={onAccept}
              style={{ backgroundColor: colors.primary }}
              className="flex-1 py-3 rounded-xl items-center"
            >
              <Text className="text-white font-['PlusJakartaSans-SemiBold']">
                Join Now
              </Text>
            </TouchableOpacity>
          </View>

          {/* Footnote */}
          <Text 
            style={{ color: colors.primary }}
            className="text-xs text-center mt-4"
          >
            You can always create or join other stokvels later
          </Text>
        </View>
      </View>
    </Modal>
  );
};

export default AutoJoinPrompt;