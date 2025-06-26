import React from 'react';
import { View, Text, ScrollView } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { StatusBar } from 'expo-status-bar';
import { useTheme } from '../_layout';
import SavingsCard from '../../src/components/SavingsCard';
import QuickActions from '../../src/components/QuickActions';
import ActivityItem from '../../src/components/ActivityItem';
import TopBar from '../../src/components/TopBar';

const Home = () => {
  const { colors } = useTheme();

  const recentActivity = [
    { title: 'Stokvel Group 1', amount: '5000.00', type: 'debit' as const },
    { title: 'Stokvel Group 2', amount: '570.00', type: 'credit' as const },
  ];

  return (
    <SafeAreaView style={{ backgroundColor: colors.background }} className="flex-1">
      <StatusBar style="dark" />
      
      <TopBar title="Home" />

      <ScrollView className="flex-1 px-6">
        {/* Savings Card */}
        <View className="mt-6">
          <SavingsCard balance="7,785.00" />
        </View>

        {/* Quick Actions */}
        <QuickActions />

        {/* Recent Activity */}
        <View>
          <View className="flex-row justify-between items-center mb-4">
            <Text className="text-lg font-['PlusJakartaSans-SemiBold']" style={{ color: colors.text }}>
              Recent Activity
            </Text>
            <Text className="text-m font-['PlusJakartaSans-Medium'] text-[#1DA1FA]">
              View More...
            </Text>
          </View>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

export default Home;