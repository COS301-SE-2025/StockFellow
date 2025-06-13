import React, { useState, useEffect } from 'react';
import { View, Text, ScrollView, TouchableOpacity, Image, ActivityIndicator, Alert } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import SearchBar from '../../src/components/SearchBar';
import StokvelCard from '../../src/components/StokvelCard';
import TopBar from '../../src/components/TopBar';
import { icons } from '../../src/constants';
import { useTheme } from '../_layout';
import * as SecureStore from 'expo-secure-store';

interface Stokvel {
  id: string;
  name: string;
  memberCount: number;
  balance: string;
}

const Stokvels = () => {
  const router = useRouter();
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [yourStokvels, setYourStokvels] = useState<Stokvel[]>([]);
  const [joinedStokvels, setJoinedStokvels] = useState<Stokvel[]>([]);
  const { colors } = useTheme();

  useEffect(() => {
    const fetchStokvels = async () => {
      try {
        const token = await SecureStore.getItemAsync('accessToken');
        if (!token) {
          throw new Error('No authentication token found');
        }

        const response = await fetch('http://10.0.2.2:4040/api/groups/user', {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });

        if (!response.ok) {
          throw new Error('Failed to fetch stokvels');
        }

        const data = await response.json();
        // Assuming the API returns ownedGroups and joinedGroups
        setYourStokvels(data.ownedGroups || []);
        setJoinedStokvels(data.joinedGroups || []);
      } catch (error) {
        console.error('Error fetching stokvels:', error);
        Alert.alert('Error', 'Failed to load stokvels');
      } finally {
        setLoading(false);
      }
    };

    fetchStokvels();
  }, []);

  const handleSearch = (text: string) => {
    setSearchQuery(text);
  };

  const filteredYourStokvels = yourStokvels.filter(stokvel =>
    stokvel.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const filteredJoinedStokvels = joinedStokvels.filter(stokvel =>
    stokvel.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  if (loading) {
    return (
      <SafeAreaView style={{ flex: 1, backgroundColor: colors.background }}>
        <TopBar title="Stokvels" />
        <View className="flex-1 justify-center items-center">
          <ActivityIndicator size="large" color={colors.primary} />
        </View>
      </SafeAreaView>
    );
  }

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
                memberCount={stokvel.memberCount}
                balance={stokvel.balance}
                onPress={() => router.push(`/stokvel/${stokvel.id}`)}
              />
            ))
          ) : (
            <Text style={{ color: colors.text, textAlign: 'center', padding: 20 }} className="text-sm">
              No stokvels found
            </Text>
          )}

          <Text style={{ color: colors.text }} className="text-base font-['PlusJakartaSans-SemiBold'] mb-4 mt-4">
            Joined Stokvels
          </Text>

          {filteredJoinedStokvels.length > 0 ? (
            filteredJoinedStokvels.map((stokvel) => (
              <StokvelCard
                key={stokvel.id}
                name={stokvel.name}
                memberCount={stokvel.memberCount}
                balance={stokvel.balance}
                onPress={() => router.push(`/stokvel/${stokvel.id}`)}
              />
            ))
          ) : (
            <Text style={{ color: colors.text, textAlign: 'center', padding: 20 }} className="text-sm">
              No joined stokvels found
            </Text>
          )}
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