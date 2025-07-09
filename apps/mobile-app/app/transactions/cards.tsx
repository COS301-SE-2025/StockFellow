// apps/mobile-app/app/transactions/cards.tsx
import React, { useState, useEffect } from 'react';
import { View, Text, TouchableOpacity, Image, Alert, ActivityIndicator } from 'react-native';
import { SafeAreaView } from "react-native-safe-area-context";
import { ScrollView, GestureHandlerRootView } from "react-native-gesture-handler";
import { useRouter } from 'expo-router';
import DebitCard from '../../src/components/DebitCard';
import TopBar from '../../src/components/TopBar';
import { icons } from '../../src/constants';
import cardService from '../../src/services/cardService';

const Cards = () => {
  const router = useRouter();
  const [cards, setCards] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [activatingCardId, setActivatingCardId] = useState<string | null>(null);
  const [deletingCardId, setDeletingCardId] = useState<string | null>(null);

  useEffect(() => {
    const fetchCards = async () => {
      try {
        const userCards = await cardService.getUserBankDetails();
        // Convert backend data to frontend format
        const formattedCards = userCards.map(card => ({
          id: card.id,
          bank: card.bank,
          last4Digits: card.last4Digits ||  '',
          cardHolder: card.cardHolder ||  '',
          expiryMonth: card.expiryMonth?.toString().padStart(2, '0') || '',
          expiryYear: (card.expiryYear % 100)?.toString().padStart(2, '0') || '',
          cardType: (card.cardType?.toLowerCase() as 'mastercard' | 'visa') || 'mastercard',
          isActive: card.isActive
        }));
        setCards(formattedCards);
      } catch (error) {
        console.error('Error fetching cards:', error);
        Alert.alert('Error', 'Failed to load cards');
      } finally {
        setLoading(false);
      }
    };

    fetchCards();
  }, []);

  const setActiveCard = async (cardId: string) => {
    try {
      setActivatingCardId(cardId);
      await cardService.activateBankDetails(cardId);
      // Update local state to reflect the change
      setCards(cards.map(card => ({
        ...card,
        isActive: card.id === cardId
      })));
    } catch (error) {
      console.error('Error activating card:', error);
      Alert.alert('Error', 'Failed to set card as active');
    } finally {
      setActivatingCardId(null);
    }
  };

  const deleteCard = async (cardId: string) => {
    Alert.alert(
      'Delete Card',
      'Are you sure you want to delete this card?',
      [
        {
          text: 'Cancel',
          style: 'cancel',
          onPress: () => setDeletingCardId(null)
        },
        {
          text: 'Delete',
          style: 'destructive',
          onPress: async () => {
            try {
              setDeletingCardId(cardId);
              await cardService.deleteBankDetails(cardId);
              // Update local state
              const updatedCards = cards.filter(card => card.id !== cardId);
              setCards(updatedCards);
            } catch (error) {
              console.error('Error deleting card:', error);
              Alert.alert('Error', 'Failed to delete card');
            } finally {
              setDeletingCardId(null);
            }
          }
        }
      ]
    );
  };

  if (loading) {
    return (
      <GestureHandlerRootView className="flex-1">
        <SafeAreaView className="flex-1 bg-white">
          <TopBar title="My Cards" />
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
        <TopBar title="My Cards" />
        <ScrollView className="flex-1 p-6" contentContainerStyle={{ paddingBottom: 80 }}>
          {/* Header with "My Cards" and Add Button */}
          <View className="flex-row justify-between items-center mb-6">
            <Text className="text-xl font-['PlusJakartaSans-Bold']">My Cards</Text>
            <TouchableOpacity
              onPress={() => router.push('/transactions/cardform')}
              className="p-2"
            >
              <Image 
                source={icons.plus}
                className="w-8 h-8"
                resizeMode="contain"
              />
            </TouchableOpacity>
          </View>

          {/* Current Active Card */}
          <Text className="text-base font-['PlusJakartaSans-SemiBold'] mb-4">Current Active Card</Text>
          {cards.filter(card => card.isActive).map(card => (
            <View key={card.id}>
              <DebitCard
                bankName={card.bank}
                cardNumber={`•••• •••• •••• ${card.last4Digits}`}
                cardHolder={card.cardHolder}
                expiryDate={`${card.expiryMonth}/${card.expiryYear}`}
                cardType={card.cardType}
              />
            </View>
          ))}

          {/* Other Cards */}
          {cards.filter(card => !card.isActive).length > 0 && (
            <>
              <Text className="text-base font-['PlusJakartaSans-SemiBold'] mb-4">Other Cards</Text>
              {cards.filter(card => !card.isActive).map(card => (
                <View key={card.id} className="mb-6">
                  <DebitCard
                    bankName={card.bank}
                    cardNumber={`•••• •••• •••• ${card.last4Digits}`}
                    cardHolder={card.cardHolder}
                    expiryDate={`${card.expiryMonth}/${card.expiryYear}`}
                    cardType={card.cardType}
                  />
                  <View className="flex-row justify-center mt-1">
                    <TouchableOpacity
                      className="bg-[#0C0C0F] px-4 py-3 mx-2 rounded-3xl items-center justify-center min-w-[120px]"
                      onPress={() => setActiveCard(card.id)}
                      disabled={!!activatingCardId}
                    >
                      {activatingCardId === card.id ? (
                        <ActivityIndicator color="white" />
                      ) : (
                        <Text className="text-white">Set as Active</Text>
                      )}
                    </TouchableOpacity>
                    <TouchableOpacity
                      className="bg-[#D10000] px-4 py-3 mx-2 rounded-3xl items-center justify-center min-w-[120px]"
                      onPress={() => deleteCard(card.id)}
                      disabled={!!deletingCardId}
                    >
                      {deletingCardId === card.id ? (
                        <ActivityIndicator color="white" />
                      ) : (
                        <Text className="text-white">Delete Card</Text>
                      )}
                    </TouchableOpacity>
                  </View>
                </View>
              ))}
            </>
          )}

          {cards.length === 0 && (
            <View className="items-center justify-center py-10">
              <Text className="text-gray-500">No cards added yet</Text>
            </View>
          )}
        </ScrollView>
      </SafeAreaView>
    </GestureHandlerRootView>
  );
};

export default Cards;