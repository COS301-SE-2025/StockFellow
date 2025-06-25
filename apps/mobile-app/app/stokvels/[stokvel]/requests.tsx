import React, { useState, useEffect, useCallback } from "react";
import {
    View,
    Text,
    Image,
    TouchableOpacity,
    ScrollView,
    ActivityIndicator,
    Alert,
    RefreshControl
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useLocalSearchParams, useRouter } from "expo-router";
import { GestureHandlerRootView } from "react-native-gesture-handler";
import TopBar from "../../../src/components/TopBar";
import { icons } from "../../../src/constants";
import { formatDistanceToNow } from "date-fns";
import authService from "../../../src/services/authService";

interface JoinRequest {
    requestId: string;
    userId: string;
    state: string;
    timestamp: Date;
    profileName?: string;
    profileImage?: string | null;
}

const StokvelRequests = () => {
    const { stokvel: id } = useLocalSearchParams<{ stokvel: string }>();
    const router = useRouter();
    const [requests, setRequests] = useState<JoinRequest[]>([]);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);

    const fetchRequests = useCallback(async () => {
        try {
            if (!id) {
                throw new Error('No group ID provided');
            }

            console.log("Group ID recieved to check requests: " + id);

            const response = await authService.apiRequest(`/groups/${id}/view`, {
                method: 'GET'
            });

            if (!response.ok) {
                throw new Error('Failed to fetch group details');
            }

            const data = await response.json();

            console.log("Group Data: " + data);

            // Filter only waiting requests and transform to frontend format
            const pendingRequests = data.group.requests
                ?.filter((req: any) => req.state === "waiting")
                ?.map((req: any) => ({
                    requestId: req.requestId,
                    userId: req.userId,
                    state: req.state,
                    timestamp: new Date(req.timestamp),
                    profileName: req.userId, // You should fetch actual user names here
                    profileImage: null
                })) || [];
            
            console.log("Groups Requests: " + pendingRequests);

            setRequests(pendingRequests);
        } catch (error) {
            console.error('Error fetching requests:', error);
            Alert.alert('Error', 'Failed to load join requests');
        } finally {
            setLoading(false);
            setRefreshing(false);
        }
    }, [id]);

    useEffect(() => {
        fetchRequests();
    }, [fetchRequests]);

    const onRefresh = useCallback(() => {
        setRefreshing(true);
        fetchRequests();
    }, [fetchRequests]);

    const processRequest = async (requestId: string, action: 'accept' | 'reject') => {
        try {
            setLoading(true);

            const response = await authService.apiRequest(`/groups/${id}/request`, {
                method: 'POST',
                body: JSON.stringify({
                    requestId,
                    action
                })
            });

            if (!response.ok) {
                throw new Error(`Failed to ${action} request`);
            }

            // Remove the processed request from local state
            setRequests(prev => prev.filter(req => req.requestId !== requestId));

            Alert.alert('Success', `Request ${action}ed successfully`);
        } catch (error) {
            console.error(`Error ${action}ing request:`, error);
            Alert.alert('Error', `Failed to ${action} request`);
        } finally {
            setLoading(false);
        }
    };

    const handleAccept = (requestId: string) => processRequest(requestId, 'accept');
    const handleReject = (requestId: string) => processRequest(requestId, 'reject');

    if (loading && requests.length === 0) {
        return (
            <GestureHandlerRootView className="flex-1">
                <SafeAreaView className="flex-1 bg-white">
                    <TopBar title="Join Requests" />
                    <View className="flex-1 justify-center items-center">
                        <ActivityIndicator size="large" color="#1DA1FA" />
                    </View>
                </SafeAreaView>
            </GestureHandlerRootView>
        );
    }

    return (
        <GestureHandlerRootView className="flex-1">
            <SafeAreaView className="flex-1 bg-white">
                <TopBar title="Stokvels" />
                <Text className="m-6 text-xl font-['PlusJakartaSans-SemiBold']">
                    Join Requests
                </Text>

                <ScrollView
                    className="flex-1 px-5 py-3"
                    contentContainerStyle={{ flexGrow: 1 }}
                    refreshControl={
                        <RefreshControl
                            refreshing={refreshing}
                            onRefresh={onRefresh}
                            colors={['#1DA1FA']}
                            tintColor="#1DA1FA"
                        />
                    }
                >
                    {requests.length > 0 ? (
                        requests.map((request) => (
                            <View key={request.requestId} className="mb-6 p-4 bg-gray-50 rounded-lg">
                                <View className="flex-row items-center mb-3">
                                    <View className="w-14 h-14 rounded-full bg-white items-center justify-center mr-2 shadow-xl shadow-[#1DA1FA]/90">
                                        <Image
                                            source={request.profileImage ? { uri: request.profileImage } : icons.avatar}
                                            className="w-12 h-12 rounded-full"
                                            resizeMode="cover"
                                        />
                                    </View>
                                    <View className="flex-1">
                                        <Text className="font-['PlusJakartaSans-SemiBold'] text-base">
                                            {request.profileName || request.userId} wants to join
                                        </Text>
                                        <Text className="text-gray-500 text-xs mt-1">
                                            {formatDistanceToNow(request.timestamp, { addSuffix: true })}
                                        </Text>
                                    </View>
                                </View>

                                <View className="flex-row justify-end space-x-3">
                                    <TouchableOpacity
                                        className="px-4 py-2 bg-red-100 rounded-full"
                                        onPress={() => handleReject(request.requestId)}
                                        disabled={loading}
                                    >
                                        <Text className="text-red-600 font-['PlusJakartaSans-SemiBold']">
                                            Reject
                                        </Text>
                                    </TouchableOpacity>
                                    <TouchableOpacity
                                        className="px-4 py-2 bg-[#03DE58]/10 rounded-full"
                                        onPress={() => handleAccept(request.requestId)}
                                        disabled={loading}
                                    >
                                        <Text className="text-[#03DE58] font-['PlusJakartaSans-SemiBold']">
                                            Accept
                                        </Text>
                                    </TouchableOpacity>
                                </View>
                            </View>
                        ))
                    ) : (
                        <View className="flex-1 justify-center items-center">
                            <View className="items-center "> 
                                <Image
                                    source={icons.request}
                                    className="w-24 h-24 opacity-70 mb-4"
                                    resizeMode="contain"
                                />
                                <Text className="text-gray-600 font-['PlusJakartaSans-Medium']">
                                    No pending requests
                                </Text>
                            </View>
                        </View>
                    )}
                </ScrollView>
            </SafeAreaView>
        </GestureHandlerRootView>
    );
};

export default StokvelRequests;