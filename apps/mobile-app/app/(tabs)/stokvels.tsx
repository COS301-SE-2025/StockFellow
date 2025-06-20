import React, { useState } from 'react';
import { View, Text, ScrollView, TouchableOpacity, Image } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
//import { StatusBar } from 'expo-status-bar';
import SearchBar from '../../src/components/SearchBar';
import StokvelCard from '../../src/components/StokvelCard';
import TopBar from '../../src/components/TopBar';
import { icons } from '../../src/constants';
import { useTheme } from '../_layout';

interface Stokvel {
  id: number;
  name: string;
  members: number;
  balance: string;
}

const Stokvels = () => {
  const router = useRouter();
  const [searchQuery, setSearchQuery] = useState('');
  const { colors } = useTheme();

  const mockStokvels = [
    { id: 1, name: 'Stokvel Group 1', members: 23, balance: '15435.95' },
    { id: 2, name: 'Stokvel Group 2', members: 15, balance: '8570.00' },
  ];

  const joinedStokvels = [
    { id: 1, name: 'Stokvel Group 3', members: 23, balance: '15435.95' },
    { id: 2, name: 'Stokvel Group 4', members: 18, balance: '8570.00' },
  ];

  const filteredYourStokvels = mockStokvels.filter(stokvel =>
    stokvel.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const filteredJoinedStokvels = joinedStokvels.filter(stokvel =>
    stokvel.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleSearch = (text: string) => {
    setSearchQuery(text);
  };

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: colors.background }} className="pt-0">
      <TopBar title="Stokvels" />
      
      <View className="px-6 pt-4">
        <SearchBar
          value={searchQuery}
          onChangeText={handleSearch}
          placeholder="Search for a Stokvel"
        />
      </View>
      
      <ScrollView className="flex-1 px-6">
        <View className="py-2">
          <Text style={{ color: colors.text }} className="text-base font-['PlusJakartaSans-SemiBold'] mb-4 mt-2">
            Your Stokvels
          </Text>

          {filteredYourStokvels.length > 0 ? (
            filteredYourStokvels.map((stokvel) => (
              <StokvelCard
                key={stokvel.id}
                name={stokvel.name}
                memberCount={stokvel.members}
                balance={stokvel.balance}
                onPress={() => router.push(`/stokvel/${stokvel.id}`)}
              />
            ))
          ) : (
            <Text style={{ color: colors.text, textAlign: 'center', padding: 20 }} className="text-sm">
              No matching stokvels found
            </Text>
          )}

          <Text style={{ color: colors.text }} className="text-base font-['PlusJakartaSans-SemiBold'] mb-4 mt-4">
            Joined Stokvels
          </Text>

          {filteredJoinedStokvels.map((stokvel) => (
            <StokvelCard
              key={stokvel.id}
              name={stokvel.name}
              memberCount={stokvel.members}
              balance={stokvel.balance}
              onPress={() => router.push(`/stokvel/${stokvel.id}`)}
            />
          ))}
        </View>

        <View style={{ borderTopColor: colors.border }} className="p-6 border-t items-center">
        <TouchableOpacity 
          style={{ backgroundColor: colors.primary }}
          className="rounded-full px-5 py-3 flex-row items-center justify-center gap-2"
          onPress={() => router.push('/stokvels/create')}
        >
          <Image 
            source={icons.plus} 
            style={{ width: 19, height: 19, tintColor: 'white' }}
            resizeMode="contain"
          />
          <Text className="text-white text-base font-['PlusJakartaSans-Regular']">Create</Text>
        </TouchableOpacity>
      </View>
      </ScrollView>

      
    </SafeAreaView>
  );
};

export default Stokvels;