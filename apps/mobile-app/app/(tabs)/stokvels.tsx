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
  id: string;
  groupId: string;
  name: string;
  memberCount: number;
  balance?: string;
  profileImage?: string | null;
  visibility?: 'Public' | 'Private';
}

const Stokvels = () => {
  const router = useRouter();
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [stokvels, setStokvels] = useState<Stokvel[]>([]);
  const [publicStokvels, setPublicStokvels] = useState<Stokvel[]>([]);
  const [searchLoading, setSearchLoading] = useState(false);
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

        const transformedStokvels = data.map((group: any) => ({
          id: group.id, // Keep the id for your own stokvels
          groupId: group.groupId || group.id || group._id, // Keep groupId for public stokvels
          name: group.name,
          memberCount: group.members?.length || 0,
          balance: group.balance ? `R ${group.balance.toFixed(2)}` : "R 0.00",
          profileImage: group.profileImage || null,
          visibility: group.visibility
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

  // Search for public groups with debounce
  useEffect(() => {
    const searchPublicGroups = async () => {
      if (searchQuery.trim().length < 2) {
        setPublicStokvels([]);
        return;
      }

      try {
        setSearchLoading(true);
        const response = await authService.apiRequest(
          `/groups/search?query=${encodeURIComponent(searchQuery)}`,
          { method: 'GET' }
        );

        if (!response.ok) {
          throw new Error('Search failed');
        }

        const data = await response.json();

        // Access the groups array from the response
        const groups = data.groups || [];

        const transformedResults = groups.map((group: any) => ({
          id: group.id, // Include id field
          groupId: group.groupId || group.id || group._id,
          name: group.name,
          memberCount: group.currentMembers || group.members?.length || 0,
          balance: group.balance ? `R ${group.balance.toFixed(2)}` : "R 0.00",
          profileImage: group.profileImage || null,
          visibility: group.visibility
        }));

        setPublicStokvels(transformedResults);
      } catch (error) {
        console.error('Search error:', error);
        Alert.alert('Error', 'Failed to search for groups');
      } finally {
        setSearchLoading(false);
      }
    };

    const debounceTimer = setTimeout(() => {
      searchPublicGroups();
    }, 500);

    return () => clearTimeout(debounceTimer);
  }, [searchQuery]);

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
          nativeID="search-bar"
          value={searchQuery}
          onChangeText={handleSearch}
          placeholder="Search for a Stokvel"
        />
      </View>

      {/* Main content with padding bottom for fixed button */}
      <View style={{ flex: 1, paddingBottom: 80 }}>
        <ScrollView className="flex-1 px-6">
          {searchQuery ? (
            <>
              {/* Search results section */}
              <View className="py-2">
                <Text style={{ color: colors.text }} className="text-base font-['PlusJakartaSans-SemiBold'] mb-4 mt-2">
                  Public Stokvels
                </Text>

                {searchLoading ? (
                  <ActivityIndicator size="small" color={colors.primary} />
                ) : publicStokvels.length > 0 ? (
                  publicStokvels.map((stokvel) => (
                    <StokvelCard
                      key={stokvel.groupId}
                      name={stokvel.name}
                      memberCount={stokvel.memberCount}
                      balance={stokvel.balance || "R 0.00"}
                      profileImage={stokvel.profileImage}
                      onPress={() => {
                        // Use id if available (for your own stokvels), otherwise use groupId (for public stokvels)
                        const routeId = stokvel.id || stokvel.groupId;
                        router.push({
                          pathname: '/stokvels/[id]',
                          params: { id: routeId }
                        });
                      }}
                    />
                  ))
                ) : (
                  <Text style={{ color: colors.text, textAlign: 'center', padding: 20 }} className="text-sm">
                    {searchQuery.length >= 2 ? 'No public stokvels found' : 'Type at least 2 characters to search'}
                  </Text>
                )}
              </View>

              {/* Your stokvels that match search */}
              {filteredStokvels.length > 0 && (
                <View className="py-2">
                  <Text style={{ color: colors.text }} className="text-base font-['PlusJakartaSans-SemiBold'] mb-4 mt-2">
                    Your Matching Stokvels
                  </Text>
                  {filteredStokvels.map((stokvel) => (
                    <StokvelCard
                      key={stokvel.groupId}
                      name={stokvel.name}
                      memberCount={stokvel.memberCount}
                      balance={stokvel.balance || "R 0.00"}
                      profileImage={stokvel.profileImage}
                      onPress={() => {
                        // Use id if available (for your own stokvels), otherwise use groupId (for public stokvels)
                        const routeId = stokvel.id || stokvel.groupId;
                        router.push({
                          pathname: '/stokvels/[id]',
                          params: { id: routeId }
                        });
                      }}
                    />
                  ))}
                </View>
              )}
            </>
          ) : (
            /* Default view when not searching */
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
                    balance={stokvel.balance || "R 0.00"}
                    profileImage={stokvel.profileImage}
                    onPress={() => {
                      // Use id if available (for your own stokvels), otherwise use groupId (for public stokvels)
                      const routeId = stokvel.id || stokvel.groupId;
                      router.push({
                        pathname: '/stokvels/[id]',
                        params: { id: routeId }
                      });
                    }}
                  />
                ))
              ) : (
                <Text style={{ color: colors.text, textAlign: 'center', padding: 20 }} className="text-sm">
                  You have no stokvels yet
                </Text>
              )}
            </View>
          )}
        </ScrollView>
      </View>

      {/* Fixed Create Button */}
      <View
        nativeID="create-stokvel-button"
        style={{
          position: 'absolute',
          bottom: 140,
          left: 0,
          right: 0,
          alignItems: 'center'
        }}
      >
        <TouchableOpacity
          style={{
            backgroundColor: colors.primary,
            width: '30%',
            maxWidth: 400
          }}
          className="rounded-full py-3 flex-row items-center justify-center gap-2"
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
    </SafeAreaView>
  );
};

export default Stokvels;