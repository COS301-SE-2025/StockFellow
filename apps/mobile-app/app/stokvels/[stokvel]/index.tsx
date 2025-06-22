import React, { useState, useEffect } from "react";
import { Image, Text, View, TouchableOpacity, ActivityIndicator, Alert } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { ScrollView, GestureHandlerRootView } from "react-native-gesture-handler";
import TopBar from '../../../src/components/TopBar';
import { icons } from "../../../src/constants";
import CustomButton from "../../../src/components/CustomButton";
import MemberCard from "../../../src/components/MemberCard";
import StokvelActivity from "../../../src/components/StokvelActivity";
import { useRouter, useLocalSearchParams } from "expo-router";
import * as SecureStore from 'expo-secure-store';

interface Member {
  userId: string;
  role: string;
  contribution: number;
  joinedAt?: Date;
  lastActive?: Date;
}

interface Event {
  id: string;
  type: string;
  payload: any;
  timestamp: Date;
}

interface StokvelDetails {
  id: string;
  groupId: string;
  name: string;
  balance: number;
  profileImage?: string;
  members: Member[];
  events: Event[];
  userPermissions: {
    isMember: boolean;
    isAdmin: boolean;
    canViewRequests: boolean;
  };
}

const Stokvel = () => {
  const router = useRouter();
  const { stokvel: stokvelId } = useLocalSearchParams();

  const [stokvel, setStokvel] = useState<StokvelDetails | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    console.log('[DEBUG] Initializing stokvel details fetch');
    console.log('[DEBUG] Received stokvel ID:', stokvelId);
    console.log('[DEBUG] Type of stokvelId:', typeof stokvelId);

    if (!stokvelId) {
      console.error('[ERROR] No stokvel ID provided');
      Alert.alert('Error', 'No stokvel ID provided');
      setLoading(false);
      return;
    }

    const fetchStokvelDetails = async () => {
      try {
        console.log('[DEBUG] Starting token retrieval');
        const token = await SecureStore.getItemAsync('access_token');
        console.log('[DEBUG] Token retrieved:', token ? '***REDACTED***' : 'NULL');

        if (!token) {
          throw new Error('Authentication required');
        }

        const apiUrl = `http://10.0.2.2:4040/api/groups/${stokvelId}/view`;
        console.log('[DEBUG] Making API call to:', apiUrl);

        const response = await fetch(apiUrl, {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });

        console.log('[DEBUG] API response status:', response.status);
        
        if (!response.ok) {
          const errorData = await response.json();
          console.error('[ERROR] API error response:', {
            status: response.status,
            errorData
          });
          throw new Error(errorData.error || 'Failed to fetch stokvel');
        }

        const data = await response.json();
        console.log('[DEBUG] API raw response data:', JSON.stringify(data, null, 2));

        // Transformation debugging
        console.log('[DEBUG] Transforming API data...');
        const transformedData: StokvelDetails = {
          id: data.group.id,
          groupId: data.group.groupId || data.group.id,
          name: data.group.name,
          balance: data.group.balance || 0,
          profileImage: data.group.profileImage,
          members: data.group.members?.map((member: any) => ({
            userId: member.userId,
            role: member.role,
            contribution: member.contribution || 0,
            joinedAt: member.joinedAt ? new Date(member.joinedAt) : new Date(),
            lastActive: member.lastActive ? new Date(member.lastActive) : new Date()
          })) || [],
          events: data.events?.map((event: any) => ({
            id: event.id || event._id,
            type: event.type,
            payload: event.payload,
            timestamp: new Date(event.timestamp)
          })) || [],
          userPermissions: data.userPermissions || {
            isMember: false,
            isAdmin: false,
            canViewRequests: false
          }
        };

        console.log('[DEBUG] Transformed data:', JSON.stringify(transformedData, null, 2));
        setStokvel(transformedData);
        console.log('[DEBUG] Stokvel data set successfully');

      } catch (error) {
        console.error('[ERROR] Fetch error details:', {
          error: error instanceof Error ? {
            message: error.message,
            stack: error.stack
          } : error,
          timestamp: new Date().toISOString()
        });
        Alert.alert('Error', error instanceof Error ? error.message : 'Unknown error');
      } finally {
        console.log('[DEBUG] Loading complete');
        setLoading(false);
      }
    };

    fetchStokvelDetails();
  }, [stokvelId]);

  if (loading) {
    return (
      <GestureHandlerRootView className="flex-1">
        <SafeAreaView className="flex-1 bg-white">
          <TopBar title="Stokvel Details" />
          <View className="flex-1 justify-center items-center">
            <ActivityIndicator size="large" color="#1DA1FA" />
          </View>
        </SafeAreaView>
      </GestureHandlerRootView>
    );
  }

  if (!stokvel) {
    return (
      <GestureHandlerRootView className="flex-1">
        <SafeAreaView className="flex-1 bg-white">
          <TopBar title="Stokvel Details" />
          <View className="flex-1 justify-center items-center">
            <Text className="text-lg font-['PlusJakartaSans-SemiBold'] mb-4">
              Stokvel not found or access denied
            </Text>
            <CustomButton
              title="Back to Stokvels"
              containerStyles="bg-[#1DA1FA] rounded-full py-3 px-6"
              textStyles="text-white text-base"
              handlePress={() => router.push('/stokvels')}
            />
          </View>
        </SafeAreaView>
      </GestureHandlerRootView>
    );
  }

  // Helper function to format currency
  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-ZA', {
      style: 'currency',
      currency: 'ZAR',
      minimumFractionDigits: 2,
    }).format(amount);
  };

  return (
    <GestureHandlerRootView className="flex-1">
      <SafeAreaView className="flex-1 bg-white">
        <TopBar title="Stokvels" />

        <ScrollView
          contentContainerStyle={{ flexGrow: 1, paddingTop: 20 }}
          nestedScrollEnabled={true}
          keyboardShouldPersistTaps="handled"
        >
          <View className="w-full flex-1 justify-start items-center h-full">
            <Text className="w-full px-6 text-left text-2xl font-['PlusJakartaSans-SemiBold'] mb-2 mt-4">
                {stokvel.name}
              </Text>

            {/* Profile Image */}
            <Image
              source={stokvel.profileImage ? { uri: stokvel.profileImage } : icons.stokvelpfp}
              className="w-40 h-40 my-5 rounded-full shadow-2xl shadow-[#1DA1FA]/90"
              resizeMode="contain"
            />

            {/* Balance */}
            <Text className="text-3xl font-['PlusJakartaSans-Bold'] text-[#03DE58]">
              {formatCurrency(stokvel.balance)}
            </Text>

            {/* Requests Button (only shown if user has permission) */}
            {stokvel.userPermissions.canViewRequests && (
              <TouchableOpacity
                className="flex-col items-center mt-4"
                onPress={() => router.push(`/stokvels/${stokvelId}/requests`)}
              >
                <Image
                  source={icons.request}
                  className="w-10 h-10 mr-1"
                  resizeMode="contain"
                />
                <Text className="text-xs font-['PlusJakartaSans-Regular'] text-[#1DA1FA]">
                  Requests
                </Text>
              </TouchableOpacity>
            )}

            <CustomButton
              title="Manage"
              containerStyles="bg-[#0C0C0F] rounded-full py-4 px-12 my-6 self-center"
              textStyles="text-white text-base font-['PlusJakartaSans-SemiBold']"
              handlePress={() => { }}
            />

            <View className="w-full py-3 pl-5">
              <Text className="w-full pl-2 text-left text-base font-['PlusJakartaSans-SemiBold'] mb-2">
                Members ({stokvel.members.length})
              </Text>
              <ScrollView
                horizontal
                showsHorizontalScrollIndicator={false}
                contentContainerStyle={{ paddingHorizontal: 0 }}
                className="w-full"
              >
                {stokvel.members.map((member) => (
                  <View key={member.userId} className="mr-4">
                    <MemberCard
                      name={member.userId} // You might want to fetch user names separately
                      role={member.role}
                      contribution={formatCurrency(member.contribution)}
                      tier={member.role === 'admin' ? 3 : member.role === 'founder' ? 4 : 1}
                      profileImage={null} // You might want to fetch user profile images separately
                    />
                  </View>
                ))}
              </ScrollView>

              {/* Activity */}
              <Text className="w-full px-2 text-left text-base font-['PlusJakartaSans-SemiBold'] mb-2 mt-4">
                Activity
              </Text>

              {/* <View className="w-full">
                {stokvel.events.map((event) => (
                  <StokvelActivity 
                    key={event.id} 
                    activity={{
                      id: event.id,
                      type: event.type,
                      memberName: event.payload?.userId || "System",
                      amount: event.payload?.amount,
                      timestamp: event.timestamp
                    }} 
                  />
                ))}
              </View> */}
            </View>
          </View>
        </ScrollView>
      </SafeAreaView>
    </GestureHandlerRootView>
  );
};

export default Stokvel;