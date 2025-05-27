import React, { useState } from 'react';
import { View, Text, ScrollView, TouchableOpacity, Image } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import { StyleSheet } from 'react-native';

import SearchBar from '../../src/components/SearchBar';
import StokvelCard from '../../src/components/StokvelCard';
import CustomButton from '../../src/components/CustomButton';
import { icons } from '../../src/constants';

const Stokvels = () => {
  const router = useRouter();
  const [searchQuery, setSearchQuery] = useState('');

  const mockStokvels = [
    { id: 1, name: 'Stokvel Group 1', members: 23, balance: '15435.95' },
    { id: 2, name: 'Stokvel Group 2', members: 15, balance: '8570.00' },
  ];

  return (
    <SafeAreaView className="flex-1 bg-white">
      <View className="px-6">
        <SearchBar
          value={searchQuery}
          onChangeText={setSearchQuery}
          placeholder="Search for a Stokvel"
        />
      </View>
      
      <ScrollView className="flex-1 px-6">
        <View className="py-2">
          <Text className="text-base font-['PlusJakartaSans-SemiBold'] mb-4 mt-2">
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

      <View className="p-6 border-t border-gray-100 items-center">
        <TouchableOpacity 
          className="bg-[#1DA1FA] rounded-full px-5 py-3 flex-row items-center justify-center gap-2"
          onPress={() => router.push('/create-stokvel')}
        >
          <Image 
            source={icons.plus} 
            style={{ width: 19, height: 19, tintColor: 'white' }}
            resizeMode="contain"
          />
          <Text className="text-white text-base font-['PlusJakartaSans-Regular']">Create</Text>
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
};

export default Stokvels;

const styles = StyleSheet.create({});