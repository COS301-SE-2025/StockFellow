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
  const { colors } = useTheme();

  return (
    <View className="bg-blue-300 rounded-2xl p-6 mb-6 shadow-sm">
      {/* Header with Tier Badge and Money Icon */}
      <View className="flex-row justify-between items-start mb-4">
        {/* User Tier Badge */}
        {/* <View className="bg-white/50 px-4 py-2 rounded-2xl"> */}
          <Text className="text-black text-2xl font-['PlusJakartaSans-Bold']">
            {userTier} Member
          </Text>
        {/* </View> */}

        {/* Money Icon */}
        <Image 
          source={icons.money}
          className="w-8 h-8"
          resizeMode="contain"
        />
      </View>

      {/* Main Content */}
      <View className="space-y-4">
        {/* Next Contribution */}
        <View>
          <Text className="text-black/80 text-sm font-['PlusJakartaSans-Medium'] mb-1">
            Next Contribution
          </Text>
          <View className="flex-row justify-between items-center">
            <Text className="text-white text-lg font-['PlusJakartaSans-SemiBold']">
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
          <Text className="text-black/80 text-sm font-['PlusJakartaSans-Medium'] mb-1">
            Next Payout
          </Text>
          <Text className="text-white text-lg font-['PlusJakartaSans-SemiBold']">
            {nextPayoutDate}
          </Text>
        </View>
      </View>
    </View>
  );
};

export default SavingsCard;