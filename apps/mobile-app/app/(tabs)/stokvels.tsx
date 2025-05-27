import React, { useState } from 'react';
import { View, Text, ScrollView, TouchableOpacity } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import { StyleSheet } from 'react-native';

import SearchBar from '../../src/components/SearchBar';
import StokvelCard from '../../src/components/StokvelCard';
// import CustomButton from '../../src/components/CustomButton';

const Stokvels = () => {
  const router = useRouter();
  const [searchQuery, setSearchQuery] = useState('');

  const mockStokvels = [
    { id: 1, name: 'Stokvel Group 1', members: 23, balance: '15435.95' },
    { id: 2, name: 'Stokvel Group 2', members: 15, balance: '8570.00' },
  ];

  return (
    <SafeAreaView className="flex-1 bg-white">
      <ScrollView className="flex-1 px-6">
        <View className="py-4">

          <SearchBar
            value={searchQuery}
            onChangeText={setSearchQuery}
            placeholder="Search for a Stokvel"
          />

          <Text className="text-base font-['PlusJakartaSans-SemiBold'] mb-4 mt-4">
            Your Stokvels
          </Text>

          {mockStokvels.map((stokvel) => (
            <StokvelCard
              key={stokvel.id}
              name={stokvel.name}
              memberCount={stokvel.members}
              balance={stokvel.balance}
              onPress={() => router.push(`/stokvel/${stokvel.id}`)}
            />
          ))}

          <Text className="text-base font-['PlusJakartaSans-SemiBold'] mb-4 mt-4">
            Joined Stokvels
          </Text>

          {mockStokvels.map((stokvel) => (
            <StokvelCard
              key={stokvel.id}
              name={stokvel.name}
              memberCount={stokvel.members}
              balance={stokvel.balance}
              onPress={() => router.push(`/stokvel/${stokvel.id}`)}
            />
          ))}

        </View>
      </ScrollView>

      <View className="p-6 border-t border-gray-100">
        {/* <CustomButton
          title="Create"
          containerStyles="bg-[#1DA1FA] rounded-xl px-8 py-4"
          textStyles="text-white text-lg font-['PlusJakartaSans-SemiBold']"
          handlePress={() => router.push('/create-stokvel')}
        /> */}        rm -rf node_modules/.cache
      </View>
    </SafeAreaView>
  );
};

export default Stokvels;

const styles = StyleSheet.create({});