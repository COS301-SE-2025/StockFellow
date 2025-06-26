// apps/mobile-app/app/transactions/cards.tsx
import React, { useState, useEffect } from 'react';
import { View, Text, TouchableOpacity, Image, Alert } from 'react-native';
import { SafeAreaView } from "react-native-safe-area-context";
import { ScrollView, GestureHandlerRootView } from "react-native-gesture-handler";
import { useRouter } from 'expo-router';
import DebitCard from '../../src/components/DebitCard';
import TopBar from '../../src/components/TopBar';
import { icons } from '../../src/constants';
import { loadCards, saveCards } from '../../src/services/cardService';

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

const Cards = () => {
  const router = useRouter();
  const [cards, setCards] = useState<Card[]>([]);

  useEffect(() => {
    const fetchCards = async () => {
      const loadedCards = await loadCards();
      setCards(loadedCards);
    };
    
    fetchCards();
  }, []);

  const setActiveCard = async (cardId: string) => {
    const updatedCards = cards.map(card => ({
      ...card,
      isActive: card.id === cardId
    }));
    setCards(updatedCards);
    await saveCards(updatedCards);
  };

  const deleteCard = async (cardId: string) => {
    Alert.alert(
      'Delete Card',
      'Are you sure you want to delete this card?',
      [
        {
          text: 'Cancel',
          style: 'cancel'
        },
        {
          text: 'Delete',
          style: 'destructive',
          onPress: async () => {
            const updatedCards = cards.filter(card => card.id !== cardId);
            if (updatedCards.length > 0 && !updatedCards.some(card => card.isActive)) {
              updatedCards[0].isActive = true;
            }
            setCards(updatedCards);
            await saveCards(updatedCards);
          }
        }
      ]
    );
  };

  return (
    <GestureHandlerRootView className="flex-1">
      <SafeAreaView className="flex-1 bg-white">
        <TopBar title="Stokvels" />
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
            <View key={card.id} >
              <DebitCard
                bankName={card.bank}
                cardNumber={`•••• •••• •••• ${card.cardNumber.slice(-4)}`}
                cardHolderName={card.cardHolderName}
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
                    cardNumber={`•••• •••• •••• ${card.cardNumber.slice(-4)}`}
                    cardHolderName={card.cardHolderName}
                    expiryDate={`${card.expiryMonth}/${card.expiryYear}`}
                    cardType={card.cardType}
                  />
                  <View className="flex-row justify-center mt-1">
                    <TouchableOpacity
                      className="bg-[#0C0C0F] px-4 py-3 mx-2 rounded-3xl"
                      onPress={() => setActiveCard(card.id)}
                    >
                      <Text className="text-white">Set as Active</Text>
                    </TouchableOpacity>
                    <TouchableOpacity
                      className="bg-[#D10000] px-4 py-3 mx-2 rounded-3xl"
                      onPress={() => deleteCard(card.id)}
                    >
                      <Text className="text-white">Delete Card</Text>
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