// apps/mobile-app/app/transactions/cards.tsx
import React, { useState } from 'react';
import { View, Text, ScrollView, TouchableOpacity, Image, Alert } from 'react-native';
import { useRouter } from 'expo-router';
import DebitCard from '../../src/components/DebitCard';
import { icons } from '../../src/constants';

const Cards = () => {
  const router = useRouter();
  const [cards, setCards] = useState([
    {
      id: '1',
      bank: 'First National Bank',
      cardNumber: '1234567890123456',
      cardHolderName: 'L SMITH',
      expiryMonth: '10',
      expiryYear: '26',
      cardType: 'mastercard',
      isActive: true
    },
    {
      id: '2',
      bank: 'Standard Bank',
      cardNumber: '9876543210987654',
      cardHolderName: 'L SMITH',
      expiryMonth: '05',
      expiryYear: '25',
      cardType: 'visa',
      isActive: false
    }
  ]);

  const setActiveCard = (cardId: string) => {
    setCards(cards.map(card => ({
      ...card,
      isActive: card.id === cardId
    })));
  };

  const deleteCard = (cardId: string) => {
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
          onPress: () => {
            const newCards = cards.filter(card => card.id !== cardId);
            setCards(newCards);
            if (newCards.length > 0 && !newCards.some(card => card.isActive)) {
              setActiveCard(newCards[0].id);
            }
          }
        }
      ]
    );
  };

  return (
    <View className="flex-1 bg-white">
      <ScrollView className="flex-1 p-6" contentContainerStyle={{ paddingBottom: 80 }}>
        <Text className="text-xl font-bold mb-6">My Cards</Text>

        {/* Current Active Card */}
        <Text className="text-base font-semibold mb-4">Current Active Card</Text>
        {cards.filter(card => card.isActive).map(card => (
          <View key={card.id} className="mb-6">
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
            <Text className="text-base font-semibold mb-4 mt-6">Other Cards</Text>
            {cards.filter(card => !card.isActive).map(card => (
              <View key={card.id} className="mb-6">
                <DebitCard
                  bankName={card.bank}
                  cardNumber={`•••• •••• •••• ${card.cardNumber.slice(-4)}`}
                  cardHolderName={card.cardHolderName}
                  expiryDate={`${card.expiryMonth}/${card.expiryYear}`}
                  cardType={card.cardType}
                />
                <View className="flex-row justify-between mt-3">
                  <TouchableOpacity
                    className="bg-blue-500 px-4 py-2 rounded-lg"
                    onPress={() => setActiveCard(card.id)}
                  >
                    <Text className="text-white">Set as Active</Text>
                  </TouchableOpacity>
                  <TouchableOpacity
                    className="bg-red-500 px-4 py-2 rounded-lg"
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

      {/* Fixed Add Card Button */}
      <View className="absolute bottom-5 left-0 right-0 px-6">
        <TouchableOpacity
          className="bg-blue-500 p-4 rounded-lg items-center"
          onPress={() => router.push('/transactions/cardform')}
        >
          <Text className="text-white font-bold">Add New Card</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};

export default Cards;