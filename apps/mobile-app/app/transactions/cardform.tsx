// apps/mobile-app/app/transactions/cardform.tsx
import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, Image, Alert } from 'react-native';
import { useRouter } from 'expo-router';
import { icons } from '../../src/constants';
import TopBar from '../../src/components/TopBar';
import { SafeAreaView } from "react-native-safe-area-context";
import { ScrollView, GestureHandlerRootView } from "react-native-gesture-handler";
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

interface CardData {
    cardNumber: string;
    cardHolderName: string;
    expiryMonth: string;
    expiryYear: string;
    cvv: string;
    bank: string;
    cardType: 'mastercard' | 'visa';
}

const banks = [
    { name: 'Standard Bank', logo: icons.standardbank },
    { name: 'Absa', logo: icons.absa },
    { name: 'Capitec Bank', logo: icons.capitec },
    { name: 'First National Bank', logo: icons.fnb },
    { name: 'Nedbank', logo: icons.nedbank }
];

const CardForm = () => {
    const router = useRouter();
    const [cards, setCards] = useState<Card[]>([]);

    const [cardData, setCardData] = useState<CardData>({
        cardNumber: '',
        cardHolderName: '',
        expiryMonth: '',
        expiryYear: '',
        cvv: '',
        bank: '',
        cardType: 'mastercard' // Default value
    });

    const [errors, setErrors] = useState({
        cardNumber: false,
        cardHolderName: false,
        expiryMonth: false,
        expiryYear: false,
        cvv: false,
        bank: false
    });

    const handleInputChange = (field: string, value: string) => {
        setCardData(prev => ({ ...prev, [field]: value }));
        setErrors(prev => ({ ...prev, [field]: false }));
    };

    const formatCardNumber = (input: string) => {
        const v = input.replace(/\s+/g, '').replace(/[^0-9]/gi, '');
        const matches = v.match(/\d{4,16}/g);
        const match = matches && matches[0] || '';
        const parts = [];

        for (let i = 0, len = match.length; i < len; i += 4) {
            parts.push(match.substring(i, i + 4));
        }

        if (parts.length) {
            return parts.join(' ');
        }
        return input;
    };

    const validateForm = () => {
        const newErrors = {
            cardNumber: !cardData.cardNumber || cardData.cardNumber.replace(/\s/g, '').length !== 16,
            cardHolderName: !cardData.cardHolderName,
            expiryMonth: !cardData.expiryMonth || parseInt(cardData.expiryMonth) < 1 || parseInt(cardData.expiryMonth) > 12,
            expiryYear: !cardData.expiryYear || cardData.expiryYear.length !== 2,
            cvv: !cardData.cvv || cardData.cvv.length !== 3,
            bank: !cardData.bank
        };

        setErrors(newErrors);
        return !Object.values(newErrors).some(error => error);
    };

    useEffect(() => {
        const fetchCards = async () => {
            const loadedCards = await loadCards();
            setCards(loadedCards);
        };

        fetchCards();
    }, []);

    const handleSubmit = async () => {
        if (validateForm()) {
            const newCard: Card = {
                id: Date.now().toString(),
                bank: cardData.bank,
                cardNumber: cardData.cardNumber.replace(/\s/g, ''),
                cardHolderName: cardData.cardHolderName,
                expiryMonth: cardData.expiryMonth,
                expiryYear: cardData.expiryYear,
                cardType: cardData.cardType,
                isActive: cards.length === 0
            };

            const updatedCards = [...cards, newCard];
            setCards(updatedCards);
            await saveCards(updatedCards);
            Alert.alert('Card Added', 'A new card has been added successfully', [
                { text: 'OK', onPress: () => router.push('(tabs)/transactions') }
            ]);
            router.push('(tabs)/transactions');
        }
    };

    return (
        <GestureHandlerRootView className="flex-1">
            <SafeAreaView className="flex-1 bg-white">
                <TopBar title="Stokvels" />
                <ScrollView className="flex-1 bg-white p-6">

                    <Text className="text-xl font-['PlusJakartaSans-SemiBold'] mb-6">Add New Card</Text>

                    {/* Bank Selection */}
                    <View className="mb-6">
                        <Text className=" text-gray-800 mb-2 font-['PlusJakartaSans-Medium'] ">Bank</Text>
                        <View className="flex-row flex-wrap justify-between">
                            {banks.map((bank) => (
                                <TouchableOpacity
                                    key={bank.name}
                                    className={`w-[48%] p-3 mb-3 rounded-lg border ${cardData.bank === bank.name ? 'border-blue-500 bg-blue-50' : 'border-gray-300'}`}
                                    onPress={() => handleInputChange('bank', bank.name)}
                                >
                                    <View className="flex-row items-center">
                                        <Image source={bank.logo} className="w-6 h-6 mr-2" resizeMode="contain" />
                                        <Text>{bank.name}</Text>
                                    </View>
                                </TouchableOpacity>
                            ))}
                        </View>
                        {errors.bank && <Text className="text-red-500 text-xs mt-1">Please select a bank</Text>}
                    </View>

                    {/* Card Number */}
                    <View className="mb-4">
                        <Text className="text-sm text-gray-800 mb-2 font-['PlusJakartaSans-Medium']">Card Number</Text>
                        <TextInput
                            className={`p-3 border rounded-lg ${errors.cardNumber ? 'border-red-500' : 'border-gray-300'}`}
                            placeholder="1234 5678 9012 3456"
                            value={formatCardNumber(cardData.cardNumber)}
                            onChangeText={(text) => handleInputChange('cardNumber', text.replace(/\D/g, ''))}
                            keyboardType="numeric"
                            maxLength={19}
                        />
                        {errors.cardNumber && <Text className="text-red-500 text-xs mt-1">Please enter a valid card number</Text>}
                    </View>

                    {/* Cardholder Name */}
                    <View className="mb-4">
                        <Text className="text-sm text-gray-800 mb-2 font-['PlusJakartaSans-Medium']">Cardholder Name</Text>
                        <TextInput
                            className={`p-3 border rounded-lg ${errors.cardHolderName ? 'border-red-500' : 'border-gray-300'}`}
                            placeholder="John Doe"
                            value={cardData.cardHolderName}
                            onChangeText={(text) => handleInputChange('cardHolderName', text)}
                        />
                        {errors.cardHolderName && <Text className="text-red-500 text-xs mt-1">Please enter cardholder name</Text>}
                    </View>

                    {/* Expiry Date and CVV */}
                    <View className="flex-row justify-between mb-4">
                        <View className="w-[48%]">
                            <Text className="text-sm text-gray-800 mb-2 font-['PlusJakartaSans-Medium']">Expiry Date</Text>
                            <View className="flex-row">
                                <TextInput
                                    className={`p-3 border rounded-lg w-[48%] mr-2 ${errors.expiryMonth ? 'border-red-500' : 'border-gray-300'}`}
                                    placeholder="MM"
                                    value={cardData.expiryMonth}
                                    onChangeText={(text) => handleInputChange('expiryMonth', text.replace(/\D/g, ''))}
                                    keyboardType="numeric"
                                    maxLength={2}
                                />
                                <TextInput
                                    className={`p-3 border rounded-lg w-[48%] ${errors.expiryYear ? 'border-red-500' : 'border-gray-300'}`}
                                    placeholder="YY"
                                    value={cardData.expiryYear}
                                    onChangeText={(text) => handleInputChange('expiryYear', text.replace(/\D/g, ''))}
                                    keyboardType="numeric"
                                    maxLength={2}
                                />
                            </View>
                            {(errors.expiryMonth || errors.expiryYear) && (
                                <Text className="text-red-500 text-xs mt-1">Please enter valid expiry date</Text>
                            )}
                        </View>
                        <View className="w-[48%]">
                            <Text className="text-sm text-gray-800 mb-2 font-['PlusJakartaSans-Medium']">CVV</Text>
                            <TextInput
                                className={`p-3 border rounded-lg ${errors.cvv ? 'border-red-500' : 'border-gray-300'}`}
                                placeholder="123"
                                value={cardData.cvv}
                                onChangeText={(text) => handleInputChange('cvv', text.replace(/\D/g, ''))}
                                keyboardType="numeric"
                                maxLength={3}
                                secureTextEntry
                            />
                            {errors.cvv && <Text className="text-red-500 text-xs mt-1">Please enter valid CVV</Text>}
                        </View>
                    </View>

                    {/* Card Type Selection */}
                    <View className="mb-6">
                        <Text className="text-sm text-gray-800 mb-2 font-['PlusJakartaSans-Medium']">Card Type</Text>
                        <View className="flex-row">
                            <TouchableOpacity
                                className={`flex-1 p-3 border rounded-lg mr-2 items-center ${cardData.cardType === 'visa' ? 'border-blue-500 bg-blue-50' : 'border-gray-300'
                                    }`}
                                onPress={() => handleInputChange('cardType', 'visa')}
                            >
                                <Image source={icons.visa} className="w-12 h-8" resizeMode="contain" />
                            </TouchableOpacity>
                            <TouchableOpacity
                                className={`flex-1 p-3 border rounded-lg items-center ${cardData.cardType === 'mastercard' ? 'border-blue-500 bg-blue-50' : 'border-gray-300'
                                    }`}
                                onPress={() => handleInputChange('cardType', 'mastercard')}
                            >
                                <Image source={icons.mastercard} className="w-12 h-8" resizeMode="contain" />
                            </TouchableOpacity>
                        </View>
                    </View>

                    {/* Submit Button */}
                    <TouchableOpacity
                        className="bg-blue-500 p-4 rounded-lg items-center mb-20"
                        onPress={handleSubmit}
                    >
                        <Text className="text-white font-bold">Add Card</Text>
                    </TouchableOpacity>
                </ScrollView>
            </SafeAreaView>
        </GestureHandlerRootView>
    );
};

export default CardForm;