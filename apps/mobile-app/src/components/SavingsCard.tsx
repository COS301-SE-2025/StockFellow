import React from 'react';
import { View, Text, Image } from 'react-native';
import { useTheme } from '../../app/_layout';
import { icons } from '../constants';

interface SavingsCardProps {
  userTier: string;
  nextContributionDate: string;
  contributionAmount: string;
  nextPayoutDate: string;
}

const SavingsCard: React.FC<SavingsCardProps> = ({
  userTier,
  nextContributionDate,
  contributionAmount,
  nextPayoutDate
}) => {
  const { colors, isDarkMode } = useTheme();

  return (
    <View
      className="bg-blue-300 rounded-2xl p-6 mb-6 shadow-sm"
      style={isDarkMode ? { backgroundColor: colors.card } : undefined}
    >
      {/* Header with Tier Badge and Money Icon */}
      <View className="flex-row justify-between items-start mb-4">
        {/* User Tier Badge */}
        <Text
          className="text-black text-2xl font-['PlusJakartaSans-Bold']"
          style={isDarkMode ? { color: colors.text } : undefined}
        >
          {userTier} Member
        </Text>

        {/* Money Icon */}
        <Image 
          source={icons.money}
          className="w-8 h-8"
          resizeMode="contain"
          style={isDarkMode ? { tintColor: colors.text } : undefined}
        />
      </View>

      {/* Main Content */}
      <View className="space-y-4">
        {/* Next Contribution */}
        <View>
          <Text
            className="text-black/80 text-sm font-['PlusJakartaSans-Medium'] mb-1"
            style={isDarkMode ? { color: colors.text, opacity: 0.7 } : undefined}
          >
            Next Contribution
          </Text>
          <View className="flex-row justify-between items-center">
            <Text
              className="text-white text-lg font-['PlusJakartaSans-SemiBold']"
              style={isDarkMode ? { color: colors.text } : undefined}
            >
              {nextContributionDate}
            </Text>
            <Text
              className="text-white text-xl font-['PlusJakartaSans-Bold']"
              style={isDarkMode ? { color: colors.text } : undefined}
            >
              R{contributionAmount}
            </Text>
          </View>
        </View>

        {/* Divider */}
        <View
          className="h-px bg-white/30"
          style={isDarkMode ? { backgroundColor: colors.text, opacity: 0.2 } : undefined}
        />

        {/* Next Payout */}
        <View>
          <Text
            className="text-black/80 text-sm font-['PlusJakartaSans-Medium'] mb-1"
            style={isDarkMode ? { color: colors.text, opacity: 0.7 } : undefined}
          >
            Next Payout
          </Text>
          <Text
            className="text-white text-lg font-['PlusJakartaSans-SemiBold']"
            style={isDarkMode ? { color: colors.text } : undefined}
          >
            {nextPayoutDate}
          </Text>
        </View>
      </View>
    </View>
  );
};

export default SavingsCard;