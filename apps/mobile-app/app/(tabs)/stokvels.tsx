import React, { useState, useEffect } from 'react';
import { View, Text, ScrollView, TouchableOpacity, Image, ActivityIndicator, Alert } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import SearchBar from '../../src/components/SearchBar';
import StokvelCard from '../../src/components/StokvelCard';
import TopBar from '../../src/components/TopBar';
import AutoJoinPrompt from '../../src/components/AutoJoinPrompt';
import { icons } from '../../src/constants';
import { useTheme } from '../_layout';
import authService from '../../src/services/authService';
import userService from '../../src/services/userService';
import groupService from '../../src/services/groupService';

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
  const [showAutoJoinPrompt, setShowAutoJoinPrompt] = useState(false);
  const [userTier, setUserTier] = useState<number | null>(null);
  const { colors } = useTheme();

  // Tier names mapping
  const tierNames = [
    "Essential Savers",
    "Steady Builders", 
    "Balanced Savers",
    "Growth Investors",
    "Premium Accumulators",
    "Elite Circle"
  ];

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
        console.debug("User groups:", data);

        const transformedStokvels = data.map((group: any) => ({
          id: group._id || group.id,
          groupId: group.groupId,
          name: group.name,
          memberCount: group.members?.length || 0,
          balance: group.balance ? `R ${group.balance.toFixed(2)}` : "R 0.00",
          profileImage: group.profileImage || null,
          visibility: group.visibility
        }));

        setStokvels(transformedStokvels);

        // Check if user has no groups and we should show the prompt
        if (data.length === 0) {
          // Get user tier for the prompt
          let tier: number | null = null; // Initialize as null
          try {
            const profileResponse = await userService.getProfile();
            tier = profileResponse.affordability?.tier ?? null; // Use null coalescing
            console.debug("Retrieved user tier from backend:", tier);
          } catch (error) {
            console.warn("Could not fetch user tier:", error);
            // Generate random tier for the prompt
            tier = Math.floor(Math.random() * 6) + 1;
          }
          
          setUserTier(tier); // Now this accepts number | null
          if (tier !== null) {
            setShowAutoJoinPrompt(true);
          }
        }
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

  const handleAutoJoinAccept = async () => {
    try {
      if (!userTier) {
        Alert.alert('Error', 'Unable to determine your tier');
        return;
      }

      setShowAutoJoinPrompt(false);
      setLoading(true);

      console.debug("Attempting to join/create stokvel for tier:", userTier);
      const joinResult = await groupService.joinOrCreateStokvel(userTier);

      // Refetch user's groups after joining
      const response = await authService.apiRequest('/groups/user', {
        method: 'GET'
      });

      if (response.ok) {
        const data = await response.json();
        const transformedStokvels = data.map((group: any) => ({
          id: group._id || group.id,
          groupId: group.groupId,
          name: group.name,
          memberCount: group.members?.length || 0,
          balance: group.balance ? `R ${group.balance.toFixed(2)}` : "R 0.00",
          profileImage: group.profileImage || null,
          visibility: group.visibility
        }));

        setStokvels(transformedStokvels);
        
        Alert.alert('Success', `You've been added to a ${tierNames[userTier - 1]} stokvel`);
      }
    } catch (error) {
      console.error('Error joining stokvel:', error);
      Alert.alert('Error', 'Failed to join stokvel automatically');
    } finally {
      setLoading(false);
    }
  };

  const handleAutoJoinDecline = () => {
    setShowAutoJoinPrompt(false);
    Alert.alert(
      'No Problem!',
      'You can create your own stokvel or join public ones anytime.',
      [{ text: 'OK' }]
    );
  };

  const handleClosePrompt = () => {
    setShowAutoJoinPrompt(false);
  };

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

      <AutoJoinPrompt
        visible={showAutoJoinPrompt}
        onAccept={handleAutoJoinAccept}
        onDecline={handleAutoJoinDecline}
        onClose={handleClosePrompt}
        tierName={userTier ? tierNames[userTier - 1] : 'Recommended'}
      />


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
                        // Use groupId consistently for navigation
                        router.push({
                          pathname: '/stokvels/[id]',
                          params: { id: stokvel.groupId }
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
                        // Use groupId consistently for navigation
                        router.push({
                          pathname: '/stokvels/[id]',
                          params: { id: stokvel.groupId }
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
                      // Use groupId consistently for navigation
                      router.push({
                        pathname: '/stokvels/[id]',
                        params: { id: stokvel.groupId }
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