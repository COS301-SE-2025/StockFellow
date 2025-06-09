import React, { useState } from "react";
import { View, Text, Image, TouchableOpacity, ScrollView } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useLocalSearchParams, useRouter } from "expo-router";
import { GestureHandlerRootView } from "react-native-gesture-handler";
import TopBar from "../../../src/components/TopBar";
import { icons } from "../../../src/constants";
import { formatDistanceToNow } from "date-fns";

interface JoinRequest {
    id: string;
    profileName: string;
    profileImage?: string | null;
    timestamp: Date;
}

const StokvelRequests = () => {
    const { stokvel } = useLocalSearchParams();
    const router = useRouter();

    const [requests, setRequests] = useState<JoinRequest[]>([
        {
            id: "1",
            profileName: "John Mbeki",
            profileImage: null,
            timestamp: new Date(Date.now() - 3600000) // 1 hour ago
        },
        {
            id: "2",
            profileName: "Sarah Johnson",
            profileImage: null,
            timestamp: new Date(Date.now() - 86400000) // 1 day ago
        },
        {
            id: "3",
            profileName: "Mike Williams",
            profileImage: null,
            timestamp: new Date(Date.now() - 172800000) // 2 days ago
        }
    ]);

    const handleAccept = (requestId: string) => {
        // Handle accept logic
        setRequests(requests.filter(req => req.id !== requestId));
    };

    const handleReject = (requestId: string) => {
        // Handle reject logic
        setRequests(requests.filter(req => req.id !== requestId));
    };

    return (
        <GestureHandlerRootView className="flex-1">
            <SafeAreaView className="flex-1 bg-white">
                <TopBar title="Stokvels" />
                <Text className="m-4 text-xl font-['PlusJakartaSans-SemiBold'] ">{/*stokvel.name*/} Join Requests</Text>
                <ScrollView className="flex-1 px-5 py-3">
                    {requests.length > 0 ? (
                        requests.map((request) => (
                            <View key={request.id} className="mb-6 p-4 bg-gray-50 rounded-lg">
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
                                            {request.profileName} has sent a join request
                                        </Text>
                                        <Text className="text-gray-500 text-xs mt-1">
                                            {formatDistanceToNow(new Date(request.timestamp), { addSuffix: true })}
                                        </Text>
                                    </View>
                                </View>

                                <View className="flex-row justify-end space-x-3">
                                    <TouchableOpacity
                                        className="px-4 py-2 bg-red-100 rounded-full"
                                        onPress={() => handleReject(request.id)}
                                    >
                                        <Text className="text-red-600 font-['PlusJakartaSans-SemiBold']">
                                            Reject
                                        </Text>
                                    </TouchableOpacity>
                                    <TouchableOpacity
                                        className="px-4 py-2 bg-[#03DE58]/10 rounded-full"
                                        onPress={() => handleAccept(request.id)}
                                    >
                                        <Text className="text-[#03DE58] font-['PlusJakartaSans-SemiBold']">
                                            Accept
                                        </Text>
                                    </TouchableOpacity>
                                </View>
                            </View>
                        ))
                    ) : (
                        <View className="items-center justify-center py-10">
                            <Image
                                source={icons.request}
                                className="w-24 h-24 opacity-30 mb-4"
                                resizeMode="contain"
                            />
                            <Text className="text-gray-500 font-['PlusJakartaSans-Medium']">
                                No pending requests
                            </Text>
                        </View>
                    )}
                </ScrollView>
            </SafeAreaView>
        </GestureHandlerRootView>
    );
};

export default StokvelRequests;