import React, { useState, useEffect } from 'react';
import { View, Text, ScrollView, ActivityIndicator } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import DebitCard from '../../src/components/DebitCard';
import TopBar from '../../src/components/TopBar';
import TransactionLog, { Transaction } from '../../src/components/TransactionLog'; // Import Transaction type
import { icons } from '../../src/constants';

const Transactions = () => {
  // Sample transaction data with proper typing
  const [transactions, setTransactions] = useState<Transaction[]>([
    {
      id: '1',
      type: 'contribution',
      amount: 500,
      groupName: 'Family Stokvel',
      date: '2025-06-15',
      profileImage: null,
    },
    {
      id: '2',
      type: 'payout',
      amount: 1200,
      groupName: 'Work Stokvel',
      date: '2025-06-10',
      profileImage: null,
    },
    {
      id: '3',
      type: 'contribution',
      amount: 300,
      groupName: 'Neighborhood Group',
      date: '2025-05-28',
      profileImage: null,
    },
    {
      id: '4',
      type: 'payout',
      amount: 800,
      groupName: 'Church Savings',
      date: '2025-05-15',
      profileImage: null,
    },
    {
      id: '5',
      type: 'contribution',
      amount: 450,
      groupName: 'Friends Circle',
      date: '2025-04-30',
      profileImage: null,
    },
    {
      id: '6',
      type: 'payout',
      amount: 450,
      groupName: 'Family Stokvel',
      date: '2025-04-30',
      profileImage: null,
    },
    {
      id: '7',
      type: 'contribution',
      amount: 450,
      groupName: 'Friends Circle',
      date: '2025-04-30',
      profileImage: null,
    },
  ]);

  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const timer = setTimeout(() => {
      setLoading(false);
    }, 1000);
    return () => clearTimeout(timer);
  }, []);

  return (
    <GestureHandlerRootView className="flex-1">
      <SafeAreaView className="flex-1 bg-white">
        <TopBar title="Transactions" />

        <ScrollView
          contentContainerStyle={{ flexGrow: 1, paddingTop: 20 }}
          nestedScrollEnabled={true}
          keyboardShouldPersistTaps="handled"
        >
          <View className="w-full flex-1 justify-start items-center h-full px-6">
            <Text className="text-base font-['PlusJakartaSans-SemiBold'] mb-4 mt-2 self-start">
              My Debit Card
            </Text>

            <View className="w-full px-1">
              <DebitCard
                bankName="My Bank"
                cardNumber="•••• •••• •••• 1234"
                cardHolderName="L SMITH"
                expiryDate="10/26"
                cardType="mastercard"
              />
            </View>

            <Text className="text-base font-['PlusJakartaSans-SemiBold'] mb-4 mt-2 self-start">
              Transaction History
            </Text>

            {loading ? (
              <ActivityIndicator size="large" color="#0000ff" className="mt-10" />
            ) : (
              <View className="w-full">
                <TransactionLog transactions={transactions} />
              </View>
            )}

            {!loading && transactions.length === 0 && (
              <View className="w-full items-center justify-center py-10">
                <Text className="text-gray-500 text-center">
                  No transactions found. Your transaction history will appear here.
                </Text>
              </View>
            )}
          </View>
        </ScrollView>
      </SafeAreaView>
    </GestureHandlerRootView>
  );
};

export default Transactions;