import React, { useState } from "react";
import { Text, View, Alert, TouchableOpacity, Image } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { ScrollView, GestureHandlerRootView } from "react-native-gesture-handler";
import StokvelAvatar from "../../src/components/StokvelAvatar";
import FormInputFlat from "../../src/components/FormInputFlat";
import CustomButton from "../../src/components/CustomButton";
import RadioBox from "../../src/components/RadioBox";
import DateTimeInput from "../../src/components/DateTimeInput";
import { router } from "expo-router";
import { icons } from "../../src/constants";
import authService from '../../src/services/authService';
//import { StatusBar } from 'expo-status-bar';
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

const StokvelForm: React.FC = () => {
    const [isSubmitting, setIsSubmitting] = useState(false);
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
        console.log("creating group.....");

        setIsSubmitting(true);
        try {
            const minContributionNum = parseFloat(form.minContribution);
            const maxMembersNum = parseInt(form.maxMembers, 10);

            if (Number.isNaN(minContributionNum) || minContributionNum <= 0) {
                Alert.alert("Error", "Invalid minimum contribution amount");
                return;
            }

            if (Number.isNaN(maxMembersNum) || maxMembersNum <= 0) {
                Alert.alert("Error", "Invalid maximum number of members");
                return;
            }

            if (maxMembersNum > 30) {
                Alert.alert("Error", "Maximum members cannot exceed 30");
                return;
            }

            const payload = {
                name: form.name,
                minContribution: minContributionNum,
                maxMembers: maxMembersNum,
                description: form.description,
                profileImage: form.profileImage,
                visibility: form.visibility,
                contributionFrequency: frequencyMap[form.contributionFrequency as FrequencyKey] || "Monthly",
                payoutFrequency: frequencyMap[form.payoutFrequency as FrequencyKey] || "Monthly",
                contributionDate: form.contributionDate?.toISOString() || null,
                payoutDate: form.payoutDate?.toISOString() || null,
                memberIds: []
            };

            const response = await authService.apiRequest('/groups/create', {
                method: 'POST',
                body: JSON.stringify(payload)
            });

            const responseData = await response.json();

            if (!response.ok) {
                throw new Error(responseData.error || "Failed to create stokvel");
            }

            Alert.alert("Success", "Stokvel created successfully!");
            router.push('/stokvels');
        } catch (error) {
            let errorMessage = "Failed to create stokvel";
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
                    <Text className="text-lg font-medium ml-4">Stokvels</Text>
                </View>

                <ScrollView
                    contentContainerStyle={{ flexGrow: 1, paddingBottom: 20 }}
                    nestedScrollEnabled={true}
                    keyboardShouldPersistTaps="handled"
                >
                    <View className="w-full flex-1 justify-start px-7">
                        <Text className="text-xl font-['PlusJakartaSans-SemiBold'] my-7">Create a New Stokvel</Text>

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
                                    // Allow only numbers and decimal point
                                    const sanitized = text.replace(/[^0-9.]/g, '');
                                    // Only allow one decimal point
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
                                    // Allow only numbers
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
                            title="Create"
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

export default StokvelForm;