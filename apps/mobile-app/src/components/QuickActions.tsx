import React, { useState } from 'react';
import { View, TouchableOpacity, Image, Text } from 'react-native';
import { icons } from '../constants';
import HelpMenu from './help/HelpMenu';
import PayoutCountdownModal from './PayoutCountdownModal';
import ProgressTrackerModal from './ProgressTrackerModal';

interface QuickActionsProps {
  contributionsLeft?: number;
  totalContributions?: number;
  daysUntilPayout?: number;
}

const QuickActions: React.FC<QuickActionsProps> = ({
  contributionsLeft = 3,
  totalContributions = 12,
  daysUntilPayout = 45
}) => {
  const [showHelp, setShowHelp] = useState(false);
  const [showPayoutCountdown, setShowPayoutCountdown] = useState(false);
  const [showProgressTracker, setShowProgressTracker] = useState(false);

  const actions = [
    { 
      icon: icons.help, 
      label: 'Help',
      onPress: () => setShowHelp(true)
    },
    { 
      icon: icons.transactions, 
      label: 'Payout',
      onPress: () => setShowPayoutCountdown(true)
    },
    { 
      icon: icons.transactions, 
      label: 'Progress',
      onPress: () => setShowProgressTracker(true)
    },
  ];

  return (
    <>
      <View className="flex-row justify-around mt-6 mb-8">
        {actions.map((action, index) => (
          <TouchableOpacity 
            key={index}
            className="items-center"
            onPress={action.onPress}
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

      {/* Modals */}
      <HelpMenu 
        isVisible={showHelp}
        onClose={() => setShowHelp(false)}
      />
      
      <PayoutCountdownModal
        isVisible={showPayoutCountdown}
        onClose={() => setShowPayoutCountdown(false)}
        daysUntilPayout={daysUntilPayout}
        nextPayoutDate="30 Dec 2025"
      />
      
      <ProgressTrackerModal
        isVisible={showProgressTracker}
        onClose={() => setShowProgressTracker(false)}
        contributionsLeft={contributionsLeft}
        totalContributions={totalContributions}
      />
    </>
  );
};

export default QuickActions;