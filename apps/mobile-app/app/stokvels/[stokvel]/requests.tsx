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
    // Fixed: Handle both possible parameter names
    const params = useLocalSearchParams<{ stokvel?: string; id?: string }>();
    const id = params.stokvel || params.id;
    const router = useRouter();
    const [requests, setRequests] = useState<JoinRequest[]>([]);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);
    const [groupName, setGroupName] = useState<string>('');

    const fetchRequests = useCallback(async () => {
        try {
            if (!id) {
                throw new Error('No group ID provided');
            }

            console.log("Group ID received to check requests: " + id);

            const response = await authService.apiRequest(`/groups/${id}/requests`, {
                method: 'GET'
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'Failed to fetch requests');
            }

            const data = await response.json();
            console.log("API Response: ", data);

            const pendingRequests = data.requests?.map((req: any) => ({
                requestId: req.requestId,
                userId: req.userId,
                state: req.state,
                timestamp: new Date(req.timestamp),
                profileName: req.username, 
                profileImage: null
            })) || [];
            
            console.log("Pending Requests: ", pendingRequests);

            setRequests(pendingRequests);
            setGroupName(data.groupName || 'Group'); // Set group name from response
        } catch (error) {
            console.error('Error fetching requests:', error);
            
            // Better error handling
            if (error instanceof Error) {
                if (error.message.includes('403') || error.message.includes('Access denied')) {
                    Alert.alert('Access Denied', 'Only group admins can view join requests', [
                        { text: 'OK', onPress: () => router.back() }
                    ]);
                } else if (error.message.includes('404')) {
                    Alert.alert('Not Found', 'Group not found', [
                        { text: 'OK', onPress: () => router.back() }
                    ]);
                } else {
                    Alert.alert('Error', error.message || 'Failed to load join requests');
                }
            } else {
                Alert.alert('Error', 'Failed to load join requests');
            }
        } finally {
            setLoading(false);
            setRefreshing(false);
        }
    }, [id, router]);

    useEffect(() => {
        if (!id) {
            Alert.alert('Error', 'No group ID provided', [
                { text: 'OK', onPress: () => router.back() }
            ]);
            return;
        }
        fetchRequests();
    }, [fetchRequests, id, router]);

    const onRefresh = useCallback(() => {
        setRefreshing(true);
        fetchRequests();
    }, [fetchRequests]);

    const processRequest = async (requestId: string, action: 'accept' | 'reject') => {
        try {
            const response = await authService.apiRequest(`/groups/${id}/request`, {
                method: 'POST',
                body: JSON.stringify({
                    requestId,
                    action
                })
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || `Failed to ${action} request`);
            }

            // Remove the processed request from local state
            setRequests(prev => prev.filter(req => req.requestId !== requestId));

            Alert.alert('Success', `Request ${action}ed successfully`);
        } catch (error) {
            console.error(`Error ${action}ing request:`, error);
            Alert.alert('Error', error instanceof Error ? error.message : `Failed to ${action} request`);
        }
    };

    const handleAccept = (requestId: string) => {
        Alert.alert(
            'Accept Request',
            'Are you sure you want to accept this join request?',
            [
                { text: 'Cancel', style: 'cancel' },
                { text: 'Accept', onPress: () => processRequest(requestId, 'accept') }
            ]
        );
    };

    const handleReject = (requestId: string) => {
        Alert.alert(
            'Reject Request',
            'Are you sure you want to reject this join request?',
            [
                { text: 'Cancel', style: 'cancel' },
                { text: 'Reject', style: 'destructive', onPress: () => processRequest(requestId, 'reject') }
            ]
        );
    };

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
                <TopBar title="Join Requests" />
                <View className="mx-6 mt-6 mb-4">
                    <Text className="text-xl font-['PlusJakartaSans-SemiBold']">
                        Join Requests
                    </Text>
                    {groupName && (
                        <Text className="text-sm text-gray-600 font-['PlusJakartaSans-Regular'] mt-1">
                            {groupName}
                        </Text>
                    )}
                </View>

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
                                    <View className="w-14 h-14 rounded-full bg-white items-center justify-center mr-3 shadow-xl shadow-[#1DA1FA]/90">
                                        <Image
                                            source={request.profileImage ? { uri: request.profileImage } : icons.avatar}
                                            className="w-12 h-12 rounded-full"
                                            resizeMode="cover"
                                        />
                                    </View>
                                    <View className="flex-1">
                                        <Text className="font-['PlusJakartaSans-SemiBold'] text-base">
                                            {request.profileName || request.userId}
                                        </Text>
                                        <Text className="text-gray-600 text-sm">
                                            wants to join
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
                            <View className="items-center">
                                <Image
                                    source={icons.request}
                                    className="w-24 h-24 opacity-70 mb-4"
                                    resizeMode="contain"
                                />
                                <Text className="text-gray-600 font-['PlusJakartaSans-Medium']">
                                    No pending requests
                                </Text>
                                <Text className="text-gray-500 text-sm text-center mt-2">
                                    When users request to join this group,{'\n'}they will appear here
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