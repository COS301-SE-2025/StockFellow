// apps/mobile-app/app/(tabs)/transactions.tsx
import React, { useState, useEffect } from 'react';
import { View, Text, ScrollView, ActivityIndicator, TouchableOpacity, Image } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import DebitCard from '../../src/components/DebitCard';
import TopBar from '../../src/components/TopBar';
import TransactionLog, { Transaction } from '../../src/components/TransactionLog';
import { icons } from '../../src/constants';
import { useRouter } from 'expo-router';


const mockCards = require('../../src/services/mockData.json') as Card[];

interface Card {
  id: string;
  bank: string;
  cardNumber: string;
  cardHolderName: string;
  expiryMonth: string;
  expiryYear: string;
  cardType: 'mastercard' | 'visa';
  isActive?: boolean;
}

const Transactions = () => {
  const router = useRouter();
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
  const [cards, setCards] = useState<Card[]>(mockCards);
  const activeCard = cards.find(card => card.isActive) || null;


  useEffect(() => {
    const timer = setTimeout(() => {
      setLoading(false);
    }, 1000);
    return () => clearTimeout(timer);
  }, []);

  if (loading) {
    return (
      <GestureHandlerRootView className="flex-1">
        <SafeAreaView className="flex-1 bg-white">
          <TopBar title="Transactions" />
          <View className="flex-1 justify-center items-center">
            <ActivityIndicator size="large" color="#0000ff" />
          </View>
        </SafeAreaView>
      </GestureHandlerRootView>
    );
  }

  return (
    <GestureHandlerRootView className="flex-1">
      <SafeAreaView className="flex-1 bg-white">
        <TopBar title="Transactions" />

        <ScrollView
          contentContainerStyle={{ flexGrow: 1, paddingTop: 20, paddingBottom: 80 }}
          nestedScrollEnabled={true}
          keyboardShouldPersistTaps="handled"
        >
          <View className="w-full flex-1 justify-start items-center h-full px-6">
            <Text className="text-base font-['PlusJakartaSans-SemiBold'] mb-4 mt-2 self-start">
              My Debit Card
            </Text>

            <View className="w-full px-1">
              {activeCard ? (
                <TouchableOpacity onPress={() => router.push('/transactions/cards')}>
                  <DebitCard
                    bankName={activeCard.bank}
                    cardNumber={`•••• •••• •••• ${activeCard.cardNumber.slice(-4)}`}
                    cardHolderName={activeCard.cardHolderName}
                    expiryDate={`${activeCard.expiryMonth}/${activeCard.expiryYear}`}
                    cardType={activeCard.cardType}
                  />
                </TouchableOpacity>
              ) : (
                // Update the navigation to only pass serializable data
                <TouchableOpacity
                  className="w-full h-[200px] border-2 border-dashed border-[#1DA1FA] rounded-2xl flex items-center justify-center"
                  onPress={() => router.push({
                    pathname: '/transactions/cardform',
                    params: {
                      cards: JSON.stringify(cards)
                    }
                  })}
                >
                  <View className="items-center">
                    <Image source={icons.plus} className="w-12 h-12 mb-2" tintColor={"#1DA1FA"} />
                    <Text className="text-[#1DA1FA]">Add a debit card</Text>
                  </View>
                </TouchableOpacity>
              )}
            </View>

            <Text className="text-base font-['PlusJakartaSans-SemiBold'] mb-4 mt-2 self-start">
              Transaction History
            </Text>

            <View className="w-full">
              <TransactionLog transactions={transactions} />
            </View>

            {transactions.length === 0 && (
              <View className="w-full items-center justify-center py-10">
                <Text className="text-gray-500 text-center">
                  No transactions found. Your transaction history will appear here.
                </Text>
              </View>
            )}
          </View>
        </ScrollView>

        {/* Fixed Add Card Button (shown only when no cards exist) */}
        {!activeCard && (
          <View className="absolute bottom-5 left-0 right-0 px-6">
            <TouchableOpacity
              className="bg-blue-500 p-4 rounded-lg items-center"
              onPress={() => router.push('/transactions/cardform')}
            >
              <Text className="text-white font-bold">Add Card</Text>
            </TouchableOpacity>
          </View>
        )}
      </SafeAreaView>
    </GestureHandlerRootView>
  );
};

export default Transactions;