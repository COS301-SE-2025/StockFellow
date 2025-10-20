import React, { useState, useEffect, useCallback } from "react";
import { Image, Text, View, TouchableOpacity, ActivityIndicator, Alert, RefreshControl } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { ScrollView, GestureHandlerRootView } from "react-native-gesture-handler";
import TopBar from '../../../src/components/TopBar';
import { icons } from "../../../src/constants";
import CustomButton from "../../../src/components/CustomButton";
import MemberCard from "../../../src/components/MemberCard";
import StokvelActivity from "../../../src/components/StokvelActivity";
import { useRouter, useLocalSearchParams } from "expo-router";
import authService from '../../../src/services/authService';
import StokvelMandate from "../../../src/components/StokvelMandate";
import { useTheme } from "../../_layout";
import { StatusBar } from "expo-status-bar";

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

interface JoinRequest {
  requestId: string;
  userId: string;
  state: string;
  timestamp: Date;
  profileName?: string;
  profileImage?: string | null;
}

interface StokvelDetails {
  id: string;
  name: string;
  balance: string;
  description: string | null;
  members: Member[];
  activities: ActivityItem[];
  requests?: JoinRequest[];
  userPermissions?: {
    canViewRequests: boolean;
    isAdmin: boolean;
    isMember: boolean;
  };
}

const Stokvel = () => {
  const router = useRouter();
  const [stokvel, setStokvel] = useState<StokvelDetails | null>(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [showMandateModal, setShowMandateModal] = useState(false);
  const [requestSent, setRequestSent] = useState(false);

  const params = useLocalSearchParams();
  const id = params.id || params.stokvel;

  const { colors, isDarkMode } = useTheme();

  if (!id) {
    console.error('No ID found in params');
    Alert.alert('Error', 'Stokvel ID missing');
    router.back();
    return null;
  }

  useEffect(() => {
    console.log('Received ID:', id);
    if (!id) {
      console.error('No ID in route params');
      Alert.alert('Error', 'Missing stokvel ID');
      router.back();
      return;
    }

    fetchStokvelDetails();
  }, [id]);

  const fetchStokvelDetails = async () => {
    try {
      const response = await authService.apiRequest(`/groups/${id}/view`, {
        method: 'GET'
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      console.log('API Response:', data);

      // Get current user ID to check for existing requests
      let currentUserId: string | null = null;
      try {
        currentUserId = await authService.getCurrentUserId();
      } catch (error) {
        console.error('Error getting current user ID:', error);
      }

      // Check if user has a pending join request (only if not already a member)
      let hasPendingRequest = false;
      if (currentUserId && data.group.requests && !data.userPermissions?.isMember) {
        hasPendingRequest = data.group.requests.some(
          (request: JoinRequest) => 
            request.userId === currentUserId && 
            request.state === 'PENDING'
        );
      }

      // Set the request sent state based on existing requests
      setRequestSent(hasPendingRequest);

      // Transform to match your frontend interface
      const transformedData: StokvelDetails = {
        id: data.group.id || data.group._id,
        name: data.group.name,
        balance: data.group.balance ? `R ${data.group.balance.toFixed(2)}` : "R 0.00",
        description: data.group.description,
        members: data.group.members?.map((member: any) => ({
          id: member.userId,
          name: member.username,
          role: member.role,
          contribution: member.contribution ? `R ${member.contribution.toFixed(2)}` : "R 0.00",
          tier: member.role === 'admin' ? 3 : 1,
          profileImage: null
        })) || [],
        requests: data.group.requests || [],
        activities: [
          // {
          //   id: '1',
          //   type: 'joined' as const,
          //   memberName: 'John Doe',
          //   stokvelName: data.group.name,
          //   timestamp: new Date(Date.now() - 1000 * 60 * 60 * 24 * 2),
          //   profileImage: null
          // },
          // {
          //   id: '2',
          //   type: 'contribution' as const,
          //   memberName: 'Jane Smith',
          //   amount: 500,
          //   timestamp: new Date(Date.now() - 1000 * 60 * 60 * 5),
          //   profileImage: null
          // },
          // {
          //   id: '3',
          //   type: 'contribution_change' as const,
          //   memberName: 'Mike Johnson',
          //   previousAmount: 300,
          //   newAmount: 500,
          //   timestamp: new Date(Date.now() - 1000 * 60 * 60 * 24),
          //   profileImage: null
          // },
          // {
          //   id: '4',
          //   type: 'payout' as const,
          //   memberName: 'Mike Johnson',
          //   amount: 2000,
          //   recipientName: 'Sarah Williams',
          //   timestamp: new Date(Date.now() - 1000 * 60 * 60 * 24 * 3),
          //   profileImage: null
          // },
          // {
          //   id: '5',
          //   type: 'missed_contribution' as const,
          //   memberName: 'Robert Brown',
          //   amount: 500,
          //   timestamp: new Date(Date.now() - 1000 * 60 * 60 * 24 * 4),
          //   profileImage: null
          // }
        ],
        userPermissions: data.userPermissions
      };

      setStokvel(transformedData);
    } catch (error) {
      console.error('Fetch error:', error);
      Alert.alert('Error', 'Failed to load stokvel details');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  // Pull to refresh handler
  const onRefresh = useCallback(async () => {
    setRefreshing(true);
    await fetchStokvelDetails();
  }, [id]);

  const handleManageButtonPress = async () => {
    if (!stokvel?.userPermissions) return;

    try {
      if (stokvel.userPermissions.isAdmin) {
        // Pass both id and groupId as params
        router.push({
          pathname: `/stokvels/${id}/editStokvel`,
          params: {
            id: id,
            groupId: id // Make sure we're passing the correct ID
          }
        });
      } else if (stokvel.userPermissions.isMember) {
        Alert.alert('Info', 'Leave functionality coming soon');
      } else if (!requestSent) {
        setShowMandateModal(true);
      }
    } catch (error) {
      console.error('Manage button error:', error);
      Alert.alert('Error', 'An error occurred');
    }
  };

  // Add this function to handle the actual join request
  const handleJoinRequest = async () => {
    try {
      setShowMandateModal(false);
      
      const response = await authService.apiRequest(`/groups/${id}/join`, {
        method: 'GET'
      });

      if (response.ok) {
        setRequestSent(true);
        Alert.alert('Success', 'Join request sent successfully');
        // Optionally refresh the stokvel data
        // fetchStokvelDetails();
      } else {
        const errorData = await response.json();
        Alert.alert('Error', errorData.error || 'Failed to send join request');
      }
    } catch (error) {
      console.error('Join request error:', error);
      Alert.alert('Error', 'An error occurred while sending the request');
    }
  };

  const getButtonTitle = () => {
    if (stokvel?.userPermissions?.isAdmin) return "Manage";
    if (stokvel?.userPermissions?.isMember) return "Leave";
    if (requestSent) return "Request Sent";
    return "Send Join Request";
  };

  const isButtonDisabled = () => {
    return requestSent || (stokvel?.userPermissions?.isMember && !stokvel?.userPermissions?.isAdmin);
  };

  if (loading) {
    return (
      <GestureHandlerRootView className="flex-1">
        <SafeAreaView className="flex-1 bg-white" style={{ backgroundColor: colors.background }}>
          <StatusBar style={isDarkMode ? 'light' : 'dark'} />
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
        <SafeAreaView className="flex-1 bg-white" style={{ backgroundColor: colors.background }}>
          <StatusBar style={isDarkMode ? 'light' : 'dark'} />
          <TopBar title="Stokvels" />
          <View className="flex-1 justify-center items-center">
            <Text className="text-gray-700" style={{ color: colors.text, opacity: 0.8 }}>Failed to load stokvel details</Text>
          </View>
        </SafeAreaView>
      </GestureHandlerRootView>
    );
  }

  return (
    <GestureHandlerRootView className="flex-1">
      <SafeAreaView className="flex-1 bg-white" style={{ backgroundColor: colors.background }}>
        <StatusBar style={isDarkMode ? 'light' : 'dark'} />
        <TopBar title="Stokvels" />

        <ScrollView
          contentContainerStyle={{ paddingTop: 15 }}
          nestedScrollEnabled={true}
          keyboardShouldPersistTaps="handled"
          style={{ backgroundColor: colors.background }}
          refreshControl={
            <RefreshControl 
              refreshing={refreshing} 
              onRefresh={onRefresh}
              colors={['#1DA1FA']}
              tintColor="#1DA1FA"
            />
          }
        >
          <View className="w-full flex-1 justify-start items-center h-full">
            {/* Stokvel Name */}
            <View className="w-full flex-1 flex-row justify-between items-center px-5">
              <Text className="text-2xl font-['PlusJakartaSans-Bold']" style={{ color: colors.text }}>
                {stokvel.name}
              </Text>
              {stokvel.userPermissions?.canViewRequests && (
                <TouchableOpacity
                  className="flex-col items-center"
                  onPress={() => router.push(`/stokvels/${id}/requests`)}
                >
                  <Image
                    source={icons.request}
                    className="w-10 h-10 mr-1"
                    resizeMode="contain"
                    style={isDarkMode ? { tintColor: '#FFFFFF' } : undefined}
                  />
                  <Text className="text-xs font-['PlusJakartaSans-Regular'] text-[#1DA1FA]">
                    Requests
                  </Text>
                </TouchableOpacity>
              )}
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
              title={getButtonTitle()}
              containerStyles={`rounded-full py-4 px-12 my-6 self-center ${
                isButtonDisabled() ? "bg-gray-400" : "bg-[#0C0C0F]"
              }`}
              textStyles="text-white text-base font-['PlusJakartaSans-SemiBold']"
              handlePress={handleManageButtonPress}
              disabled={isButtonDisabled()}
            />

            {/* Description Section - Only show if description exists */}
            {stokvel.description && (
              <View className="w-full px-7 mb-4">
                <View className="bg-gray-50 rounded-3xl p-4 border border-gray-100">
                  <Text className="text-sm font-['PlusJakartaSans-SemiBold'] text-gray-800 mb-2">
                    About
                  </Text>
                  <Text className="text-sm font-['PlusJakartaSans-Regular'] text-gray-600 leading-5">
                    {stokvel.description}
                  </Text>
                </View>
              </View>
            )}

            <View className="w-full py-3 pl-5">
              <Text className="w-full pl-2 text-left text-base font-['PlusJakartaSans-SemiBold'] mb-2" style={{ color: colors.text }}>
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
              <Text className="w-full px-2 text-left text-base font-['PlusJakartaSans-SemiBold'] mb-2" style={{ color: colors.text }}>
                Activity
              </Text>

              <View className="w-full">
                {stokvel.activities && stokvel.activities.length > 0 ? (
                  stokvel.activities.map((activity) => (
                    <StokvelActivity key={activity.id} activity={activity} />
                  ))
                ) : (
                  <Text className="text-gray-500 text-center py-4 font-['PlusJakartaSans-Regular']" style={{ color: colors.text, opacity: 0.7 }}>
                    No recent activity
                  </Text>
                )}
              </View>
            </View>
          </View>
        </ScrollView>
      </SafeAreaView>
      <StokvelMandate
        visible={showMandateModal}
        onClose={() => setShowMandateModal(false)}
        onAccept={handleJoinRequest}
      />
    </GestureHandlerRootView>
  );
};

export default Stokvel;