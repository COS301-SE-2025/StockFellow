// apps/mobile-app/app/transactions/cards.tsx

import React, { useState, useEffect } from 'react';
import { View, Text, TouchableOpacity, Image, Alert, ActivityIndicator, Linking } from 'react-native';
import { SafeAreaView } from "react-native-safe-area-context";
import { ScrollView, GestureHandlerRootView } from "react-native-gesture-handler";
import { useRouter, useFocusEffect } from 'expo-router';
import DebitCard from '../../src/components/DebitCard';
import TopBar from '../../src/components/TopBar';
import { icons } from '../../src/constants';
import cardService from '../../src/services/cardService';
import authService from '../../src/services/authService';
import { StatusBar } from 'expo-status-bar';
import { useTheme } from '../_layout';

const Cards = () => {
  const router = useRouter();
  const [cards, setCards] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [activatingCardId, setActivatingCardId] = useState<string | null>(null);
  const [deletingCardId, setDeletingCardId] = useState<string | null>(null);
  const [addingNewCard, setAddingNewCard] = useState(false);
  const { colors, isDarkMode } = useTheme();

  const fetchCards = async () => {
    try {
      const userCards = await cardService.getUserBankDetails();
      setCards(userCards);
    } catch (error) {
      console.error('Error fetching cards:', error);
      Alert.alert('Error', 'Failed to load cards');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCards();
  }, []);

  // Refresh cards when screen comes into focus (after returning from Paystack)
  useFocusEffect(
    React.useCallback(() => {
      if (!loading) {
        fetchCards();
      }
    }, [loading])
  );

	// Handle deep link when returning from Paystack
	useEffect(() => {
		const handleDeepLink = (url: string) => {
			console.log('Deep link received:', url);
			
			// Simple check - if URL contains our callback path, refresh
			if (url.includes('cards/callback')) {
				console.log('Paystack callback received, refreshing cards...');
				
				// Check for success/failure in the URL string
				if (url.includes('status=success')) {
					Alert.alert(
						'Card Added Successfully!',
						'Your card has been added and will be available shortly.',
						[
							{
								text: 'OK',
								onPress: () => {
									setLoading(true);
									fetchCards();
								}
							}
						]
					);
				} else if (url.includes('status=cancelled')) {
					Alert.alert('Card Addition Cancelled', 'You cancelled the card addition process.');
				} else if (url.includes('status=failed')) {
					Alert.alert('Card Addition Failed', 'There was an error adding your card. Please try again.');
				} else {
					// Generic callback - just refresh
					setLoading(true);
					fetchCards();
				}
			}
		};

		// Listen for deep links when app is already open
		const subscription = Linking.addEventListener('url', ({ url }) => {
			handleDeepLink(url);
		});

		// Check if app was opened via deep link
		Linking.getInitialURL().then((url) => {
			if (url) {
				handleDeepLink(url);
			}
		});

		return () => {
			subscription?.remove();
		};
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

  const addNewCard = async () => {
		try {
			setAddingNewCard(true);
			
			// Get Paystack authorization URL (user info extracted from token)
			const authUrl = await cardService.openPaystackAuthorization();
			
			// Open Paystack in browser
			const canOpen = await Linking.canOpenURL(authUrl);
			if (canOpen) {
				await Linking.openURL(authUrl);
				
				// Show a simple message that the user will be redirected back
				Alert.alert(
					'Redirecting to Paystack',
					'Complete your card authorization. You\'ll be automatically redirected back to the app when done.',
					[
						{
							text: 'OK'
						}
					]
				);
			} else {
				Alert.alert('Error', 'Cannot open Paystack authorization page');
			}
		} catch (error) {
			console.error('Error adding new card:', error);
			Alert.alert('Error', 'Failed to initialize card addition');
		} finally {
			setAddingNewCard(false);
		}
	};

  if (loading) {
    return (
      <GestureHandlerRootView className="flex-1">
        <SafeAreaView className="flex-1 bg-white" style={{ backgroundColor: colors.background }}>
          <StatusBar style={isDarkMode ? 'light' : 'dark'} />
          <TopBar title="My Cards" showBackButton />
          <View className="flex-1 justify-center items-center">
            <ActivityIndicator size="large" color="#0000ff" />
            <Text className="mt-4 text-gray-600" style={{ color: colors.text, opacity: 0.7 }}>Loading your cards...</Text>
          </View>
        </SafeAreaView>
      </GestureHandlerRootView>
    );
  }

  return (
    <GestureHandlerRootView className="flex-1">
      <SafeAreaView className="flex-1 bg-white" style={{ backgroundColor: colors.background }}>
        <StatusBar style={isDarkMode ? 'light' : 'dark'} />
        <TopBar
          title="My Cards"
          showBackButton
        />

        <ScrollView
          className="flex-1 p-6"
          contentContainerStyle={{ paddingBottom: 80 }}
          style={{ backgroundColor: colors.background }}
        >
          {/* Removed inner header row */}
          {/* Current Active Card */}
          {cards.filter(card => card.isActive).length > 0 && (
            <>
              <Text className="text-base font-['PlusJakartaSans-SemiBold'] mb-4" style={{ color: colors.text }}>
                Current Active Card
              </Text>
              {cards.filter(card => card.isActive).map(card => (
                <View key={card.id} className="mb-6">
                  <DebitCard
                    bankName={card.bank}
                    cardNumber={`•••• •••• •••• ${card.last4Digits}`}
                    cardHolder={card.cardHolder}
                    expiryDate={`${card.expiryMonth}/${card.expiryYear}`}
                    cardType={card.cardType}
                  />
                </View>
              ))}
            </>
          )}

          {/* Other Cards */}
          {cards.filter(card => !card.isActive).length > 0 && (
            <>
              <Text className="text-base font-['PlusJakartaSans-SemiBold'] mb-4" style={{ color: colors.text }}>
                Other Cards
              </Text>
              {cards.filter(card => !card.isActive).map(card => (
                <View key={card.id} className="mb-6">
                  <DebitCard
                    bankName={card.bank}
                    cardNumber={`•••• •••• •••• ${card.last4Digits}`}
                    cardHolder={card.cardHolder}
                    expiryDate={`${card.expiryMonth}/${card.expiryYear}`}
                    cardType={card.cardType}
                  />
                  <View className="flex-row justify-center mt-4">
                    <TouchableOpacity
                      className="bg-[#0C0C0F] px-4 py-3 mx-2 rounded-3xl items-center justify-center min-w-[120px]"
                      onPress={() => setActiveCard(card.id)}
                      disabled={!!activatingCardId}
                    >
                      {activatingCardId === card.id ? (
                        <ActivityIndicator color="white" />
                      ) : (
                        <Text className="text-white font-['PlusJakartaSans-SemiBold']">Set as Active</Text>
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
                        <Text className="text-white font-['PlusJakartaSans-SemiBold']">Delete Card</Text>
                      )}
                    </TouchableOpacity>
                  </View>
                </View>
              ))}
            </>
          )}

          {/* Empty State */}
          {cards.length === 0 && (
            <View className="items-center justify-center py-20">
              <Image
                source={icons.plus}
                className="w-16 h-16 mb-4"
                resizeMode="contain"
                style={isDarkMode ? { tintColor: colors.text, opacity: 0.3 } : { opacity: 0.3 }}
              />
              <Text className="text-lg mb-2" style={{ color: colors.text }}>No cards added yet</Text>
              <Text className="text-center px-8 mb-6" style={{ color: colors.text, opacity: 0.7 }}>
                Add your first card to start making payments
              </Text>
              <TouchableOpacity
                onPress={() => router.push('/transactions/cardform')}
                disabled={addingNewCard}
                className="px-6 py-3 rounded-3xl"
                style={{ backgroundColor: colors.primary }}
              >
                {addingNewCard ? (
                  <ActivityIndicator color="white" />
                ) : (
                  <Text className="text-white font-['PlusJakartaSans-SemiBold']">Add Your First Card</Text>
                )}
              </TouchableOpacity>
            </View>
          )}
        </ScrollView>
      </SafeAreaView>
    </GestureHandlerRootView>
  );
};

export default Cards;