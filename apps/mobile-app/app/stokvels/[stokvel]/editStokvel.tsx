import React, { useState, useEffect } from "react";
import { Text, View, Alert, TouchableOpacity, Image } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { ScrollView, GestureHandlerRootView } from "react-native-gesture-handler";
import StokvelAvatar from "../../../src/components/StokvelAvatar";
import FormInputFlat from "../../../src/components/FormInputFlat";
import CustomButton from "../../../src/components/CustomButton";
import RadioBox from "../../../src/components/RadioBox";
import DateTimeInput from "../../../src/components/DateTimeInput";
import { router, useLocalSearchParams } from "expo-router";
import { icons } from "../../../src/constants";
import groupService from '../../../src/services/groupService';


interface Group {
    id: string;
    groupId: string;
    name: string;
    description: string;
    profileImage: string | null;
    visibility: string;
    minContribution: number;
    maxMembers: number;
    contributionFrequency: string;
    payoutFrequency: string;
    balance: number;
    createdAt: string;
    members: Member[];
    payoutOrder?: string[];
    contributionDate?: string;
    payoutDate?: string;
}

interface Member {
    userId: string;
    username: string;
    role: string;
    joinedAt: string;
}

interface FormData {
    name: string;
    minContribution: string;
    maxMembers: string;
    description: string;
    profileImage: string | null;
    visibility: string;
    contributionFrequency: string;
    contributionDate: Date | null;
    payoutFrequency: string;
    payoutDate: Date | null;
}

type FrequencyKey = "Monthly" | "Bi-Weekly" | "Weekly";
type BackendFrequencyValue = "Monthly" | "Bi-weekly" | "Weekly";

interface GroupWithDates extends Omit<Group, 'contributionDate' | 'payoutDate'> {
    contributionDate?: string;
    payoutDate?: string;
}

const EditStokvelForm: React.FC = () => {
    const { groupId } = useLocalSearchParams();
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isLoading, setIsLoading] = useState(true);
    const [form, setForm] = useState<FormData>({
        name: "",
        minContribution: "",
        maxMembers: "",
        description: "",
        profileImage: null,
        visibility: "Private",
        contributionFrequency: "Monthly",
        contributionDate: null,
        payoutFrequency: "Monthly",
        payoutDate: null,
    });

    const frequencyMap: Record<FrequencyKey, BackendFrequencyValue> = {
        "Monthly": "Monthly",
        "Bi-Weekly": "Bi-weekly",
        "Weekly": "Weekly"
    };

    useEffect(() => {
        const fetchGroupDetails = async () => {
            try {
                if (!groupId) {
                    Alert.alert("Error", "No stokvel ID provided");
                    router.back();
                    return;
                }

                const response = await groupService.viewGroup(groupId as string);
                const group = response.group as GroupWithDates;

                setForm({
                    name: group.name,
                    minContribution: group.minContribution.toString(),
                    maxMembers: group.maxMembers.toString(),
                    description: group.description || "",
                    profileImage: group.profileImage || null,
                    visibility: group.visibility,
                    contributionFrequency: group.contributionFrequency,
                    contributionDate: group.contributionDate ? new Date(group.contributionDate) : null,
                    payoutFrequency: group.payoutFrequency,
                    payoutDate: group.payoutDate ? new Date(group.payoutDate) : null,
                });
            } catch (error) {
                Alert.alert("Error", "Failed to load stokvel details");
                console.error("Error fetching group details:", error);
                router.back();
            } finally {
                setIsLoading(false);
            }
        };

        fetchGroupDetails();
    }, [groupId]);

    const handleImageUpdate = (uri: string | null) => {
        setForm(prev => ({ ...prev, profileImage: uri }));
    };

    const handleChangeText = (field: keyof Omit<FormData, 'profileImage' | 'visibility' | 'contributionFrequency' | 'payoutFrequency'>, value: string) => {
        setForm(prev => ({ ...prev, [field]: value }));
    };

    const handleRadioSelect = (field: 'visibility' | 'contributionFrequency' | 'payoutFrequency', value: string) => {
        setForm(prev => ({ ...prev, [field]: value }));
    };

    const handleContributionDateChange = (datetime: Date | null) => {
        setForm(prev => ({ ...prev, contributionDate: datetime }));
    };

    const handlePayoutDateChange = (datetime: Date | null) => {
        setForm(prev => ({ ...prev, payoutDate: datetime }));
    };

    const submit = async () => {
        console.log("updating group.....");

        setIsSubmitting(true);
        try {
            const minContributionNum = parseFloat(form.minContribution);
            const maxMembersNum = parseInt(form.maxMembers, 10);

            if (Number.isNaN(minContributionNum)) {
                Alert.alert("Error", "Invalid minimum contribution amount");
                return;
            }

            if (Number.isNaN(maxMembersNum)) {
                Alert.alert("Error", "Invalid maximum number of members");
                return;
            }

            if (maxMembersNum > 30) {
                Alert.alert("Error", "Maximum members cannot exceed 30");
                return;
            }

            const payload = {
                name: form.name,
                description: form.description,
                profileImage: form.profileImage || undefined,
                minContribution: minContributionNum,
                maxMembers: maxMembersNum,
                visibility: form.visibility,
                contributionFrequency: frequencyMap[form.contributionFrequency as FrequencyKey] || "Monthly",
                payoutFrequency: frequencyMap[form.payoutFrequency as FrequencyKey] || "Monthly",
                ...(form.contributionDate && { contributionDate: form.contributionDate.toISOString() }),
                ...(form.payoutDate && { payoutDate: form.payoutDate.toISOString() }),
            };

            const updatedGroup = await groupService.updateGroup(groupId as string, payload);

            Alert.alert("Success", "Stokvel updated successfully!");
            router.push(`/stokvels/${groupId}`);
        } catch (error) {
            let errorMessage = "Failed to update stokvel";
            if (error instanceof Error) {
                errorMessage = error.message;
            } else if (typeof error === 'string') {
                errorMessage = error;
            }
            Alert.alert("Error", errorMessage);
        } finally {
            setIsSubmitting(false);
        }
    };

    if (isLoading) {
        return (
            <View className="flex-1 justify-center items-center">
                <Text>Loading stokvel details...</Text>
            </View>
        );
    }

    return (
        <GestureHandlerRootView className="flex-1">
            <SafeAreaView className="flex-1 bg-white">
                {/* Header with Back Button and Title */}
                <View className="flex-row items-center px-5 py-3 border-b border-gray-200">
                    <TouchableOpacity onPress={() => router.back()}>
                        <Image
                            source={icons.back}
                            className="w-6 h-6"
                            resizeMode="contain"
                        />
                    </TouchableOpacity>
                    <Text className="text-lg font-medium ml-4">Edit Stokvel</Text>
                </View>

                <ScrollView
                    contentContainerStyle={{ flexGrow: 1, paddingBottom: 20 }}
                    nestedScrollEnabled={true}
                    keyboardShouldPersistTaps="handled"
                >
                    <View className="w-full flex-1 justify-start px-7">
                        <Text className="text-xl font-['PlusJakartaSans-SemiBold'] my-7">Edit Stokvel Details</Text>

                        <StokvelAvatar
                            profileImage={form.profileImage}
                            onImageUpdate={handleImageUpdate}
                        />

                        <RadioBox
                            options={["Private", "Public"]}
                            selectedOption={form.visibility}
                            onSelect={(option) => handleRadioSelect('visibility', option)}
                        />

                        <FormInputFlat
                            title="Stokvel Name"
                            value={form.name}
                            placeholder="Enter stokvel name"
                            handleChangeText={(text) => {
                                if (text.length <= 60) handleChangeText('name', text);
                            }}
                            helperText={`${form.name.length}/60 characters`}
                        />

                        <View className="flex-row justify-between">
                            <FormInputFlat
                                title="Min Monthly Contribution"
                                value={form.minContribution}
                                placeholder="200.00"
                                handleChangeText={(text) => {
                                    const sanitized = text.replace(/[^0-9.]/g, '');
                                    if ((sanitized.match(/\./g) || []).length <= 1) {
                                        handleChangeText('minContribution', sanitized);
                                    }
                                }}
                                keyboardType="numeric"
                            />

                            <FormInputFlat
                                title="Max Members"
                                value={form.maxMembers}
                                placeholder="10"
                                handleChangeText={(text) => {
                                    const sanitized = text.replace(/[^0-9]/g, '');
                                    handleChangeText('maxMembers', sanitized);
                                }}
                                keyboardType="numeric"
                                helperText="Cannot exceed 30 members"
                                helperTextColor={Number(form.maxMembers) > 30 ? "#EF4444" : undefined}
                            />
                        </View>

                        <FormInputFlat
                            title="Description"
                            value={form.description}
                            placeholder="Enter stokvel description"
                            handleChangeText={(text) => {
                                if (text.length <= 255) handleChangeText('description', text);
                            }}
                            multiline
                            numberOfLines={4}
                            helperText={`${form.description.length}/255 characters`}
                        />

                        <Text className="text-lg mb-2 font-light">Contributions</Text>
                        <RadioBox
                            options={["Monthly", "Bi-Weekly", "Weekly"]}
                            selectedOption={form.contributionFrequency}
                            onSelect={(option) => handleRadioSelect('contributionFrequency', option)}
                        />

                        <DateTimeInput
                            label="First Contribution Date"
                            onDateTimeChange={handleContributionDateChange}
                        />

                        <Text className="text-lg mb-2 font-light">Payouts</Text>
                        <RadioBox
                            options={["Monthly", "Bi-Weekly", "Weekly"]}
                            selectedOption={form.payoutFrequency}
                            onSelect={(option) => handleRadioSelect('payoutFrequency', option)}
                        />

                        <DateTimeInput
                            label="First Payout Date"
                            onDateTimeChange={handlePayoutDateChange}
                        />

                        <CustomButton
                            title="Save Changes"
                            containerStyles="bg-[#006FFD] rounded-full py-4 px-8 my-4 self-center"
                            textStyles="text-white text-base font-normal"
                            handlePress={submit}
                            isLoading={isSubmitting}
                        />
                    </View>
                </ScrollView>
            </SafeAreaView>
        </GestureHandlerRootView>
    );
};

export default EditStokvelForm;