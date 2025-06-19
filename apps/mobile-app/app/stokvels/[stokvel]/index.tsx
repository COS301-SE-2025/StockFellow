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
  id: string;
  name: string;
  role: string;
  contribution: string;
  tier: number;
  profileImage?: string | null;
}

interface ActivityItem {
  id: string;
  type: "joined" | "contribution_change" | "payout" | "contribution" | "missed_contribution";
  memberName: string;
  stokvelName?: string;
  previousAmount?: number;
  newAmount?: number;
  amount?: number;
  recipientName?: string;
  timestamp: Date;
  profileImage?: string | null;
}

interface StokvelDetails {
  id: string;
  name: string;
  balance: string;
  members: Member[];
  activities: ActivityItem[];
}

const Stokvel = () => {
  const router = useRouter();
  const { id } = useLocalSearchParams();
  const [stokvel, setStokvel] = useState<StokvelDetails | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStokvelDetails = async () => {
      try {
        const token = await SecureStore.getItemAsync('access_token');
        if (!token) {
          throw new Error('No authentication token found');
        }

        const response = await fetch(`http://10.0.2.2:4040/api/groups/${id}`, {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });

        if (!response.ok) {
          throw new Error('Failed to fetch stokvel details');
        }

        const data = await response.json();
        setStokvel(data);
      } catch (error) {
        console.error('Error fetching stokvel details:', error);
        Alert.alert('Error', 'Failed to load stokvel details');
      } finally {
        setLoading(false);
      }
    };

    fetchStokvelDetails();
  }, [id]);

  if (loading) {
    return (
      <GestureHandlerRootView className="flex-1">
        <SafeAreaView className="flex-1 bg-white">
          <TopBar title="Stokvels" />
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
          <TopBar title="Stokvels" />
          <View className="flex-1 justify-center items-center">
            <Text className="text-gray-700">Failed to load stokvel details</Text>
          </View>
        </SafeAreaView>
      </GestureHandlerRootView>
    );
  }

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
            {/* Stokvel Name */}
            <View className="w-full flex-1 flex-row justify-between items-center px-5">
              <Text className="text-2xl font-['PlusJakartaSans-Bold']">
                {stokvel.name}
              </Text>
              <TouchableOpacity
                className="flex-col items-center"
                onPress={() => router.push(`/stokvels/${id}/requests`)}
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
            </View>

            {/* Profile Image */}
            <Image
              source={icons.stokvelpfp}
              className="w-40 h-40 my-5 rounded-full shadow-2xl shadow-[#1DA1FA]/90"
              resizeMode="contain"
            />

            <Text className="text-3xl font-['PlusJakartaSans-Bold'] text-[#03DE58]">
              {stokvel.balance}
            </Text>
            
            <CustomButton
              title="Manage"
              containerStyles="bg-[#0C0C0F] rounded-full py-4 px-12 my-6 self-center"
              textStyles="text-white text-base font-['PlusJakartaSans-SemiBold']"
              handlePress={() => {}}
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
                  <View key={member.id} className="mr-4">
                    <MemberCard
                      name={member.name}
                      role={member.role}
                      contribution={member.contribution}
                      tier={member.tier}
                      profileImage={member.profileImage}
                    />
                  </View>
                ))}
              </ScrollView>

              {/* Activity */}
              <Text className="w-full px-2 text-left text-base font-['PlusJakartaSans-SemiBold'] mb-2">
                Activity
              </Text>

              <View className="w-full">
                {stokvel.activities.map((activity) => (
                  <StokvelActivity key={activity.id} activity={activity} />
                ))}
              </View>
            </View>
          </View>
        </ScrollView>
      </SafeAreaView>
    </GestureHandlerRootView>
  );
};

export default Stokvel;