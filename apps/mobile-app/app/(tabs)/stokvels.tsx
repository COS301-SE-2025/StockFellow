import React, { useState, useEffect } from 'react';
import { View, Text, ScrollView, TouchableOpacity, Image, ActivityIndicator, Alert } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import SearchBar from '../../src/components/SearchBar';
import StokvelCard from '../../src/components/StokvelCard';
import TopBar from '../../src/components/TopBar';
import { icons } from '../../src/constants';
import { useTheme } from '../_layout';
import authService from '../../src/services/authService';

interface Stokvel {
  groupId: string;
  name: string;
  memberCount: number; // This should be derived from memberIds.length
  balance?: string; // Not in your schema, but keeping for UI
  profileImage?: string | null;
}

const Stokvels = () => {
  const router = useRouter();
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [stokvels, setStokvels] = useState<Stokvel[]>([]);
  const { colors } = useTheme();

  useEffect(() => {
    const fetchStokvels = async () => {
      try {
        const response = await authService.apiRequest('/groups/user', {
          method: 'GET'
        });

        if (!response.ok) {
          throw new Error('Failed to fetch stokvels');
        }

        const data = await response.json();

        // Transform the API response to match our frontend needs
        const transformedStokvels = data.map((group: any) => ({
          groupId: group._id || group.groupId, // Use _id if that's what backend returns
          name: group.name,
          memberCount: group.members?.length || 0, // Changed from memberIds to members
          balance: "0.00",
          profileImage: group.profileImage || null
        }));

        setStokvels(transformedStokvels);
      } catch (error) {
        console.error('Error fetching stokvels:', error);
        if (error instanceof Error && error.message.includes('Authentication failed')) {
          Alert.alert('Session Expired', 'Please login again', [
            { text: 'OK', onPress: () => router.push('/login') }
          ]);
        } else {
          Alert.alert('Error', 'Failed to load stokvels');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchStokvels();
  }, []);

  const handleSearch = (text: string) => {
    setSearchQuery(text);
  };

  const filteredStokvels = stokvels.filter(stokvel =>
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

          {filteredStokvels.length > 0 ? (
            filteredStokvels.map((stokvel) => (
              <StokvelCard
                key={stokvel.groupId}
                name={stokvel.name}
                memberCount={stokvel.memberCount}
                balance={stokvel.balance || "0.00"}
                profileImage={stokvel.profileImage} // Add this line
                onPress={() => router.push(`/stokvel/${stokvel.groupId}`)}
              />
            ))
          ) : (
            <Text style={{ color: colors.text, textAlign: 'center', padding: 20 }} className="text-sm">
              {searchQuery ? 'No matching stokvels found' : 'You have no stokvels yet'}
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