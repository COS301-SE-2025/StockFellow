import React from 'react';
import { View, Text } from 'react-native';
import { useTheme } from '../../app/_layout';

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
  const { colors } = useTheme();

  return (
    <View className="bg-blue-400 rounded-2xl p-6 mb-6 shadow-sm">
      {/* User Tier Badge */}
      <View className="bg-white/30 self-start px-3 py-1 rounded-full mb-4">
        <Text className="text-white text-sm font-['PlusJakartaSans-SemiBold']">
          {userTier} Member
        </Text>
      </View>

      {/* Main Content */}
      <View className="space-y-4">
        {/* Next Contribution */}
        <View>
          <Text className="text-white/90 text-sm font-['PlusJakartaSans-Medium'] mb-1">
            Next Contribution
          </Text>
          <View className="flex-row justify-between items-center">
            <Text className="text-white text-base font-['PlusJakartaSans-SemiBold']">
              {nextContributionDate}
            </Text>
            <Text className="text-white text-xl font-['PlusJakartaSans-Bold']">
              R{contributionAmount}
            </Text>
          </View>
        </View>

        {/* Divider */}
        <View className="h-px bg-white/30" />

        {/* Next Payout */}
        <View>
          <Text className="text-white/90 text-sm font-['PlusJakartaSans-Medium'] mb-1">
            Next Payout
          </Text>
          <Text className="text-white text-base font-['PlusJakartaSans-SemiBold']">
            {nextPayoutDate}
          </Text>
        </View>
      </View>
    </View>
  );
};

export default SavingsCard;