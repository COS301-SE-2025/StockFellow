// apps/mobile-app/app/transactions/cardform.tsx
import React, { useState } from 'react';
import { View, Text, TouchableOpacity, Image, Alert, ActivityIndicator, Linking } from 'react-native';
import { useRouter } from 'expo-router';
import { icons } from '../../src/constants';
import TopBar from '../../src/components/TopBar';
import { SafeAreaView } from "react-native-safe-area-context";
import { ScrollView, GestureHandlerRootView } from "react-native-gesture-handler";
import cardService from '../../src/services/cardService';
import authService from '../../src/services/authService';

const CardForm = () => {
    const router = useRouter();
    const [isInitializing, setIsInitializing] = useState(false);

    const handleAddCard = async () => {
        try {
            setIsInitializing(true);
            
            // Get Paystack authorization URL (user info extracted from JWT token)
            const authUrl = await cardService.openPaystackAuthorization();
            
            // Open Paystack in browser/webview
            const canOpen = await Linking.canOpenURL(authUrl);
            if (canOpen) {
                await Linking.openURL(authUrl);
                
                // Show instructions to user
                Alert.alert(
                    'Complete Card Authorization',
                    'You will now be redirected to Paystack to securely add your card. After completing the process, return to this app and your card will be available.',
                    [
                        {
                            text: 'Continue',
                            onPress: () => {
                                // Navigate back to cards list
                                router.back();
                            }
                        }
                    ]
                );
            } else {
                Alert.alert('Error', 'Cannot open Paystack authorization page');
            }
        } catch (error) {
            console.error('Error initializing card authorization:', error);
            Alert.alert(
                'Error', 
                error instanceof Error ? error.message : 'Failed to initialize card authorization'
            );
        } finally {
            setIsInitializing(false);
        }
    };

    return (
        <GestureHandlerRootView className="flex-1">
            <SafeAreaView className="flex-1 bg-white">
                <TopBar title="Add New Card" />
                <ScrollView className="flex-1 bg-white p-6" contentContainerStyle={{ flexGrow: 1 }}>
                    
                    {/* Header */}
                    <View className="items-center mb-8">
                        <Image 
                            source={icons.creditCard} // You might need to add this icon
                            className="w-20 h-20 mb-4"
                            resizeMode="contain"
                        />
                        <Text className="text-2xl font-['PlusJakartaSans-Bold'] text-center mb-2">
                            Add Your Card Securely
                        </Text>
                        <Text className="text-gray-600 text-center text-base leading-6">
                            We use Paystack's secure payment system to safely store your card details. 
                            Your card information is never stored on our servers.
                        </Text>
                    </View>

                    {/* Security Features */}
                    <View className="mb-8">
                        <Text className="text-lg font-['PlusJakartaSans-SemiBold'] mb-4">
                            Why this is secure:
                        </Text>
                        
                        <View className="space-y-3">
                            <View className="flex-row items-start">
                                <View className="w-6 h-6 bg-green-100 rounded-full items-center justify-center mr-3 mt-0.5">
                                    <Text className="text-green-600 text-xs">✓</Text>
                                </View>
                                <View className="flex-1">
                                    <Text className="font-['PlusJakartaSans-SemiBold'] text-gray-800">
                                        PCI DSS Compliant
                                    </Text>
                                    <Text className="text-gray-600 text-sm">
                                        Paystack meets the highest security standards for handling card data
                                    </Text>
                                </View>
                            </View>

                            <View className="flex-row items-start">
                                <View className="w-6 h-6 bg-green-100 rounded-full items-center justify-center mr-3 mt-0.5">
                                    <Text className="text-green-600 text-xs">✓</Text>
                                </View>
                                <View className="flex-1">
                                    <Text className="font-['PlusJakartaSans-SemiBold'] text-gray-800">
                                        End-to-End Encryption
                                    </Text>
                                    <Text className="text-gray-600 text-sm">
                                        Your card details are encrypted during transmission and storage
                                    </Text>
                                </View>
                            </View>

                            <View className="flex-row items-start">
                                <View className="w-6 h-6 bg-green-100 rounded-full items-center justify-center mr-3 mt-0.5">
                                    <Text className="text-green-600 text-xs">✓</Text>
                                </View>
                                <View className="flex-1">
                                    <Text className="font-['PlusJakartaSans-SemiBold'] text-gray-800">
                                        Small Authorization Charge
                                    </Text>
                                    <Text className="text-gray-600 text-sm">
                                        We'll charge R1.00 to verify your card (this will be refunded)
                                    </Text>
                                </View>
                            </View>
                        </View>
                    </View>

                    {/* Supported Banks */}
                    <View className="mb-8">
                        <Text className="text-lg font-['PlusJakartaSans-SemiBold'] mb-4">
                            Supported Banks:
                        </Text>
                        <View className="flex-row flex-wrap justify-between">
                            {[
                                { name: 'Standard Bank', logo: icons.standardbank },
                                { name: 'Absa', logo: icons.absa },
                                { name: 'Capitec Bank', logo: icons.capitec },
                                { name: 'First National Bank', logo: icons.fnb },
                                { name: 'Nedbank', logo: icons.nedbank }
                            ].map((bank) => (
                                <View key={bank.name} className="w-[30%] items-center mb-4">
                                    <Image 
                                        source={bank.logo} 
                                        className="w-12 h-8 mb-1" 
                                        resizeMode="contain" 
                                    />
                                    <Text className="text-xs text-gray-600 text-center">
                                        {bank.name}
                                    </Text>
                                </View>
                            ))}
                        </View>
                    </View>

                    {/* Spacer to push button to bottom */}
                    <View className="flex-1" />

                    {/* Add Card Button */}
                    <View className="mb-6">
                        <TouchableOpacity
                            className="bg-[#0C0C0F] p-4 rounded-3xl items-center justify-center min-h-[56px]"
                            onPress={handleAddCard}
                            disabled={isInitializing}
                        >
                            {isInitializing ? (
                                <View className="flex-row items-center">
                                    <ActivityIndicator color="white" className="mr-2" />
                                    <Text className="text-white font-['PlusJakartaSans-SemiBold']">
                                        Initializing...
                                    </Text>
                                </View>
                            ) : (
                                <Text className="text-white font-['PlusJakartaSans-SemiBold'] text-lg">
                                    Add Card Securely with Paystack
                                </Text>
                            )}
                        </TouchableOpacity>
                        
                        <TouchableOpacity
                            className="mt-4 p-3 items-center"
                            onPress={() => router.back()}
                            disabled={isInitializing}
                        >
                            <Text className="text-gray-600 font-['PlusJakartaSans-Medium']">
                                Cancel
                            </Text>
                        </TouchableOpacity>
                    </View>

                    {/* Footer Info */}
                    <View className="bg-gray-50 p-4 rounded-lg">
                        <Text className="text-sm text-gray-600 text-center">
                            By adding your card, you agree to our Terms of Service and Privacy Policy. 
                            Your card will be used for automatic contributions to your savings groups.
                        </Text>
                    </View>
                </ScrollView>
            </SafeAreaView>
        </GestureHandlerRootView>
    );
};

export default CardForm;