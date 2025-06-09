import React, { useState, useEffect } from "react";
import { Image, Text, View, TouchableOpacity, ActivityIndicator } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { ScrollView, GestureHandlerRootView } from "react-native-gesture-handler";
import TopBar from '../../../src/components/TopBar';
import { Ionicons } from "@expo/vector-icons";
import axios from "axios";
import * as SecureStore from 'expo-secure-store';
import { icons } from "../../../src/constants";
import CustomButton from "../../../src/components/CustomButton";
import MemberCard from "../../../src/components/MemberCard";
import StokvelActivity from "../../../src/components/StokvelActivity";
import { useRouter } from "expo-router";

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

const Stokvel = () => {
    const router = useRouter();
    const [stokvel, setStokvel] = useState(null);
    const [loading, setLoading] = useState(true);
    const [members, setMembers] = useState([
        {
            name: "John Doe",
            role: "Founder",
            contribution: "R500",
            tier: 2,
            profileImage: null
        },
        {
            name: "Jane Smith",
            role: "Admin",
            contribution: "R500",
            tier: 2,
            profileImage: null
        },
        {
            name: "Mike Johnson",
            role: "Member",
            contribution: "R500",
            tier: 2,
            profileImage: null
        },
        {
            name: "Sarah Williams",
            role: "Member",
            contribution: "R500",
            tier: 2,
            profileImage: null
        }
    ]);

    const [activities, setActivities] = useState<ActivityItem[]>([
        {
            id: "1",
            type: "joined",
            memberName: "John Doe",
            stokvelName: "Savings Club",
            timestamp: new Date(Date.now() - 3600000), // 1 hour ago
            profileImage: null
        },
        {
            id: "2",
            type: "contribution_change",
            memberName: "Jane Smith",
            previousAmount: 200,
            newAmount: 300,
            timestamp: new Date(Date.now() - 86400000), // 1 day ago
            profileImage: null
        },
        {
            id: "3",
            type: "missed_contribution",
            memberName: "Mike Johnson",
            amount: 400,
            timestamp: new Date(Date.now() - 172800000), // 2 days ago
            profileImage: null
        }
    ]);

    useEffect(() => {
        // const fetchUser = async () => {
        //     try {
        //         const token = await SecureStore.getItemAsync("accessToken");
        //         if (!token) throw new Error("No token found");

        //         const response = await axios.get("https://localhost:", {
        //             headers: { Authorization: `Bearer ${token}` },
        //         });

        //         setStokvel(response.data);
        //     } catch (error) {
        //         console.error("Error fetching user:", error);
        //     } finally {
        //         setLoading(false);
        //     }
        // };

        // fetchUser();
    }, []);

    return (
        <GestureHandlerRootView className="flex-1">
            <SafeAreaView className="flex-1 bg-white">
                {/* Header */}
                <TopBar title="Stokvels" />

                {/* {loading ? (
                    <ActivityIndicator size="large" color="blue" className="mt-10" />
                ) : stokvel ? ( */}
                <ScrollView
                    contentContainerStyle={{ flexGrow: 1, paddingTop: 20 }}
                    nestedScrollEnabled={true}
                    keyboardShouldPersistTaps="handled"
                >
                    <View className="w-full flex-1 justify-start items-center h-full">

                        {/* Stokvel Name */}
                        <View className="w-full flex-1 flex-row justify-between items-center px-5">
                            <Text className="text-2xl font-['PlusJakartaSans-Bold'] ">{/*stokvel.name*/} Stokvel Name</Text>
                            <TouchableOpacity
                                className="flex-col items-center"
                                onPress={() => router.push(`/stokvels/1/requests`)}
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
                            className="w-40 h-40 my-5  rounded-full shadow-2xl shadow-[#1DA1FA]/90 "
                            resizeMode="contain"
                        />

                        <Text className="text-3xl font-['PlusJakartaSans-Bold'] text-[#03DE58]">{/*stokvel.name*/} R32000.00</Text>
                        <CustomButton
                            title="Manage"
                            containerStyles="bg-[#0C0C0F] rounded-full py-4 px-12 my-6 self-center"
                            textStyles="text-white text-base font-['PlusJakartaSans-SemiBold']"
                            handlePress={() => { }}

                        />
                        <View className="w-full py-3 pl-5">
                            <Text className="w-full pl-2 text-left text-base font-['PlusJakartaSans-SemiBold'] mb-2">
                                Members ({members.length})
                            </Text>
                            <ScrollView
                                horizontal
                                showsHorizontalScrollIndicator={false}
                                contentContainerStyle={{ paddingHorizontal: 0 }}
                                className="w-full"
                            >
                                {members.map((member, index) => (
                                    <View key={index} className="mr-4">
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
                            <Text className="w-full px-2 text-left text-base font-['PlusJakartaSans-SemiBold'] mb-2">Activity</Text>

                            <View className="w-full">
                                {activities.map((activity) => (
                                    <StokvelActivity key={activity.id} activity={activity} />
                                ))}
                            </View>

                        </View>



                    </View>
                </ScrollView>
                {/* ) : (
                    <Text className="text-center text-gray-700 mt-10">Failed to load Stokvel</Text>
                )} */}

            </SafeAreaView>
        </GestureHandlerRootView>
    );
};

export default Stokvel;