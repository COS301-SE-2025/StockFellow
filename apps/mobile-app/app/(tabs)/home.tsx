import React from 'react';
import { View, Text, ScrollView } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { StatusBar } from 'expo-status-bar';
import { useTheme } from '../_layout';
import SavingsCard from '../../src/components/SavingsCard';
import QuickActions from '../../src/components/QuickActions';
import ActivityItem from '../../src/components/ActivityItem';
import TopBar from '../../src/components/TopBar';
import GreetingCard from '../../src/components/GreetingCard';

const Home = () => {
  const { colors } = useTheme();

  const recentActivity = [
    { 
      title: 'Breaking Bank',
      subtitle: 'Walter White',
      amount: '2000,00',
      type: 'debit' as const 
    },
    { 
      title: 'Game of Loans',
      subtitle: 'Jon Snow',
      amount: '5570,00',
      type: 'credit' as const 
    },
    { 
      title: 'Rick & Mortgage',
      subtitle: 'Morty Smith',
      amount: '1500,00',
      type: 'debit' as const 
    },
  ];

  return (
    <SafeAreaView style={{ backgroundColor: colors.background }} className="flex-1">
      <StatusBar style="dark" />
      
      <TopBar title="Home" />

      <ScrollView className="flex-1 px-6">
        {/* Greeting */}
        <View className="mt-6">
          <GreetingCard userName="Son Goku" groupCount={3} />
        </View>

        {/* Savings Card */}
        <View>
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

          {recentActivity.map((activity, index) => (
            <ActivityItem
              key={index}
              title={activity.title}
              subtitle={activity.subtitle}
              amount={activity.amount}
              onPress={() => {}}
            />
          ))}
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

export default Home;