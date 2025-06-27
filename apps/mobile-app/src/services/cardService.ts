// apps/mobile-app/src/services/cardService.ts
import * as FileSystem from 'expo-file-system';
import { Alert } from 'react-native';

const CARDS_FILE = `${FileSystem.documentDirectory}mockCards.json`;

// Import using relative path from src
const defaultData = require('./mockData.json');

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

export const loadCards = async (): Promise<Card[]> => {
  try {
    const fileInfo = await FileSystem.getInfoAsync(CARDS_FILE);
    if (!fileInfo.exists) {
      await FileSystem.writeAsStringAsync(CARDS_FILE, JSON.stringify(defaultData));
      return defaultData;
    }
    
    const content = await FileSystem.readAsStringAsync(CARDS_FILE);
    return JSON.parse(content);
  } catch (error) {
    console.error('Error loading cards:', error);
    Alert.alert('Error', 'Failed to load cards');
    return [];
  }
};

export const saveCards = async (cards: Card[]): Promise<void> => {
  try {
    await FileSystem.writeAsStringAsync(CARDS_FILE, JSON.stringify(cards));
  } catch (error) {
    console.error('Error saving cards:', error);
    Alert.alert('Error', 'Failed to save cards');
  }
};

