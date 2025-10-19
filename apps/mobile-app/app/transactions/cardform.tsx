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
import { StatusBar } from 'expo-status-bar';
import { useTheme } from '../_layout';

const CardForm = () => {
    const router = useRouter();
    const [isInitializing, setIsInitializing] = useState(false);
    const { colors, isDarkMode } = useTheme();

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
            <SafeAreaView className="flex-1 bg-white" style={{ backgroundColor: colors.background }}>
                <StatusBar style={isDarkMode ? 'light' : 'dark'} />
                <TopBar title="Add New Card" />
                <ScrollView
                    className="flex-1 bg-white p-6"
                    contentContainerStyle={{ paddingBottom: 24 }}
                    style={{ backgroundColor: colors.background }}
                >
                    {/* Header */}
                    <View className="items-center mb-2">
                        <Image 
                            source={icons.debitcard}
                            className="w-20 h-20"
                            resizeMode="contain"
                            // keep icon brand color; no tint needed
                        />
                        <Text className="text-2xl font-['PlusJakartaSans-Bold'] text-center mb-2" style={{ color: colors.text }}>
                            Add Your Card Securely
                        </Text>
                        <Text className="text-center text-base leading-6" style={{ color: colors.text, opacity: 0.75 }}>
                            We use Paystack's secure payment system to safely store your card details. 
                            Your card information is never stored on our servers.
                        </Text>
                    </View>

                    {/* Security Features */}
                    <View className="mb-8">
                        <Text className="text-lg font-['PlusJakartaSans-SemiBold'] mb-4" style={{ color: colors.text }}>
                            Why this is secure:
                        </Text>
                        
                        <View className="space-y-3">
                            {/* item 1 */}
                            <View className="flex-row items-start">
                                <View className="w-6 h-6 bg-green-100 rounded-full items-center justify-center mr-3 mt-0.5">
                                    <Text className="text-green-600 text-xs">✓</Text>
                                </View>
                                <View className="flex-1">
                                    <Text className="font-['PlusJakartaSans-SemiBold']" style={{ color: colors.text }}>
                                        PCI DSS Compliant
                                    </Text>
                                    <Text className="text-sm" style={{ color: colors.text, opacity: 0.75 }}>
                                        Paystack meets the highest security standards for handling card data
                                    </Text>
                                </View>
                            </View>

                            {/* item 2 */}
                            <View className="flex-row items-start">
                                <View className="w-6 h-6 bg-green-100 rounded-full items-center justify-center mr-3 mt-0.5">
                                    <Text className="text-green-600 text-xs">✓</Text>
                                </View>
                                <View className="flex-1">
                                    <Text className="font-['PlusJakartaSans-SemiBold']" style={{ color: colors.text }}>
                                        End-to-End Encryption
                                    </Text>
                                    <Text className="text-sm" style={{ color: colors.text, opacity: 0.75 }}>
                                        Your card details are encrypted during transmission and storage
                                    </Text>
                                </View>
                            </View>

                            {/* item 3 */}
                            <View className="flex-row items-start">
                                <View className="w-6 h-6 bg-green-100 rounded-full items-center justify-center mr-3 mt-0.5">
                                    <Text className="text-green-600 text-xs">✓</Text>
                                </View>
                                <View className="flex-1">
                                    <Text className="font-['PlusJakartaSans-SemiBold']" style={{ color: colors.text }}>
                                        Small Authorization Charge
                                    </Text>
                                    <Text className="text-sm" style={{ color: colors.text, opacity: 0.75 }}>
                                        We'll charge R1.00 to verify your card (this will be refunded)
                                    </Text>
                                </View>
                            </View>
                        </View>
                    </View>

                    {/* Supported Banks */}
                    <View className="mb-8">
                        <Text className="text-lg font-['PlusJakartaSans-SemiBold'] mb-4" style={{ color: colors.text }}>
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
                                    <Text className="text-xs text-center" style={{ color: colors.text, opacity: 0.7 }}>
                                        {bank.name}
                                    </Text>
                                </View>
                            ))}
                        </View>
                    </View>

                    {/* Add Card Button */}
                    <View className="mb-6">
                        <TouchableOpacity
                            className="p-4 rounded-3xl items-center justify-center min-h=[56px] min-h-[56px]"
                            style={{ backgroundColor: colors.primary }}
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
                    <View className="p-4 rounded-lg" style={isDarkMode ? { backgroundColor: colors.card } : { backgroundColor: '#F9FAFB' }}>
                        <Text className="text-sm text-center" style={{ color: colors.text, opacity: 0.75 }}>
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