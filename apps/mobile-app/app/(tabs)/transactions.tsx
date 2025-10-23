// apps/mobile-app/app/(tabs)/transactions.tsx
import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, ScrollView, ActivityIndicator, TouchableOpacity, Image, RefreshControl } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import DebitCard from '../../src/components/DebitCard';
import TopBar from '../../src/components/TopBar';
import TransactionLog, { Transaction } from '../../src/components/TransactionLog';
import { icons } from '../../src/constants';
import { useRouter } from 'expo-router';
import cardService from '../../src/services/cardService';
import { StatusBar } from 'expo-status-bar';
import { useTheme } from '../_layout';

const Transactions = () => {
  const router = useRouter();
  const { colors, isDarkMode } = useTheme();
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [cards, setCards] = useState<any[]>([]);
  const [cardsLoading, setCardsLoading] = useState(true);
  const [transactionsLoading, setTransactionsLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [hasMoreTransactions, setHasMoreTransactions] = useState(true);

  // Fetch transactions function
  const fetchTransactions = async (page: number = 0, append: boolean = false) => {
    try {
      if (!append) setTransactionsLoading(true);
      
      const response = await cardService.fetchTransactions(page, 20);
      
      // Transform backend data to your Transaction interface
      const formattedTransactions = response.content.map(tx => ({
        id: tx.id,
        type: (tx.type?.toLowerCase() || 'contribution') as 'contribution' | 'payout',
        amount: tx.amount,
        groupName: tx.groupName || 'Unknown Group',
        date: tx.createdAt || tx.date,
        profileImage: tx.profileImage || null,
      }));

      if (append) {
        setTransactions(prev => [...prev, ...formattedTransactions]);
      } else {
        setTransactions(formattedTransactions);
      }
      
      setHasMoreTransactions(!response.last);
      setCurrentPage(page);
      
    } catch (error) {
      console.error('Error fetching transactions:', error);
      // Optionally show error toast/alert
    } finally {
      setTransactionsLoading(false);
      setRefreshing(false);
    }
  };

  // Fetch cards function (your existing logic)
  const fetchCards = async () => {
    try {
      setCardsLoading(true);
      const userCards = await cardService.getUserBankDetails();
      const formattedCards = userCards.map((card) => {
        const expMonth = Number(card.expiryMonth);
        const expYearNum = Number(card.expiryYear);
        const twoDigitYear = Number.isFinite(expYearNum) ? expYearNum % 100 : 0;

        return {
          id: card.id,
          bank: card.bank,
          last4Digits: card.last4Digits,
          cardHolder: card.cardHolder,
          expiryMonth: String(Number.isFinite(expMonth) ? expMonth : 0).padStart(2, '0'),
          expiryYear: String(twoDigitYear).padStart(2, '0'),
          cardType: String(card.cardType || '').toLowerCase() as 'mastercard' | 'visa',
          isActive: !!card.isActive,
        };
      });
      setCards(formattedCards);
    } catch (error) {
      console.error('Error fetching cards:', error);
    } finally {
      setCardsLoading(false);
    }
  };
  
  // Load more transactions (for pagination)
  const loadMoreTransactions = () => {
    if (!transactionsLoading && hasMoreTransactions) {
      fetchTransactions(currentPage + 1, true);
    }
  };

  // Pull to refresh
  const onRefresh = useCallback(() => {
    setRefreshing(true);
    Promise.all([
      fetchTransactions(0, false),
      fetchCards()
    ]).finally(() => setRefreshing(false));
  }, []);

  // Initial load
  useEffect(() => {
    const loadInitialData = async () => {
      setLoading(true);
      try {
        await Promise.all([
          fetchTransactions(0, false),
          fetchCards()
        ]);
      } finally {
        setLoading(false);
      }
    };

    loadInitialData();
  }, []);

  const activeCard = cards.find(card => card.isActive) || null;

  if (loading) {
    return (
      <GestureHandlerRootView className="flex-1">
        <SafeAreaView className="flex-1 bg-white" style={{ backgroundColor: colors.background }}>
          <StatusBar style={isDarkMode ? 'light' : 'dark'} />
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
      <SafeAreaView className="flex-1 bg-white" style={{ backgroundColor: colors.background }}>
        <StatusBar style={isDarkMode ? 'light' : 'dark'} />
        <TopBar title="Transactions" />

        <ScrollView
          contentContainerStyle={{ flexGrow: 1, paddingTop: 20, paddingBottom: 80 }}
          style={{ backgroundColor: colors.background }}
          nestedScrollEnabled={true}
          keyboardShouldPersistTaps="handled"
          refreshControl={
            <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
          }
          onScroll={({ nativeEvent }) => {
            const { layoutMeasurement, contentOffset, contentSize } = nativeEvent;
            const paddingToBottom = 20;
            if (layoutMeasurement.height + contentOffset.y >= 
                contentSize.height - paddingToBottom) {
              loadMoreTransactions();
            }
          }}
          scrollEventThrottle={400}
        >
          <View className="w-full flex-1 justify-start items-center h-full px-6">
            {/* Your existing card UI */}
            <Text
              className="text-base font-['PlusJakartaSans-SemiBold'] mb-4 mt-2 self-start"
              style={{ color: colors.text }}
            >
              My Debit Card
            </Text>

            <View className="w-full px-1">
              {activeCard ? (
                <TouchableOpacity onPress={() => router.push('/transactions/cards')}>
                  <DebitCard
                    bankName={activeCard.bank}
                    cardNumber={`•••• •••• •••• ${activeCard.last4Digits}`}
                    cardHolder={activeCard.cardHolder}
                    expiryDate={`${activeCard.expiryMonth}/${activeCard.expiryYear}`}
                    cardType={activeCard.cardType}
                  />
                </TouchableOpacity>
              ) : (
                <TouchableOpacity
                  className="w-full h-[200px] border-2 border-dashed border-[#1DA1FA] rounded-2xl flex items-center justify-center"
                  onPress={() => router.push('/transactions/cards')}
                >
                  <View className="items-center">
                    <Image source={icons.plus} className="w-12 h-12 mb-2" tintColor={"#1DA1FA"} />
                    <Text className="text-[#1DA1FA]">Add a debit card</Text>
                  </View>
                </TouchableOpacity>
              )}
            </View>

            <Text
              className="text-base font-['PlusJakartaSans-SemiBold'] mb-4 mt-2 self-start"
              style={{ color: colors.text }}
            >
              Transaction History
            </Text>

            <View className="w-full">
              <TransactionLog transactions={transactions} />
            </View>

            {/* Loading indicator for pagination */}
            {transactionsLoading && transactions.length > 0 && (
              <View className="w-full items-center py-4">
                <ActivityIndicator size="small" color="#0000ff" />
              </View>
            )}

            {transactions.length === 0 && !transactionsLoading && (
              <View className="w-full items-center justify-center py-10">
                <Text
                  className="text-gray-500 text-center"
                  style={{ color: colors.text, opacity: 0.7 }}
                >
                  No transactions found. Your transaction history will appear here.
                </Text>
              </View>
            )}
          </View>
        </ScrollView>

        {/* Your existing fixed add card button */}
        {!activeCard && (
          <View className="absolute bottom-5 left-0 right-0 px-6">
            <TouchableOpacity
              className="bg-blue-500 p-4 rounded-lg items-center"
              onPress={() => router.push('/transactions/cards')}
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