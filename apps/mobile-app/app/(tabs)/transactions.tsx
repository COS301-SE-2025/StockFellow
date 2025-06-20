import React, { useState, useEffect } from 'react';
import { View, Text, ScrollView, TouchableOpacity, Image, ActivityIndicator, Alert } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { useRouter } from 'expo-router';
import DebitCard from '../../src/components/DebitCard';
import TopBar from '../../src/components/TopBar';
import { icons } from '../../src/constants';
import { images } from '../../src/constants';
import { useTheme } from '../_layout';
import * as SecureStore from 'expo-secure-store';

const transactions = () => {
  return (
    <GestureHandlerRootView className="flex-1">
      <SafeAreaView className="flex-1 bg-white">
        <TopBar title="Transactions" />

        <ScrollView
          contentContainerStyle={{ flexGrow: 1, paddingTop: 20 }}
          nestedScrollEnabled={true}
          keyboardShouldPersistTaps="handled"
        >
          <View className="w-full flex-1 justify-start items-center h-full px-6">
            <Text className=" text-base font-['PlusJakartaSans-SemiBold'] mb-4 mt-2 self-start">
              My Card
            </Text>


            <View className="w-full px-1">
              <DebitCard
                bankName="My Bank"
                cardNumber="•••• •••• •••• 1234"
                cardHolderName="L SMITH"
                expiryDate="10/26"
                cardType="mastercard"
              />
            </View>
          </View>
        </ScrollView>
      </SafeAreaView>
    </GestureHandlerRootView>
  )
}

export default transactions

