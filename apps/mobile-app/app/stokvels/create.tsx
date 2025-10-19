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
import { useTheme } from "../_layout";
import { StatusBar } from "expo-status-bar";
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

    const { colors, isDarkMode } = useTheme();

    // Helper: compare dates by start of day
    const toStartOfDay = (d: Date) => {
        const x = new Date(d);
        x.setHours(0, 0, 0, 0);
        return x;
    };
    // Earliest valid date is tomorrow (not today)
    const isBeforeTomorrow = (d: Date) => {
        const tomorrow = toStartOfDay(new Date());
        tomorrow.setDate(tomorrow.getDate() + 1);
        return toStartOfDay(d) < tomorrow;
    };
    // Helper: d is on or before ref (same day counts as invalid)
    const isOnOrBefore = (d: Date, ref: Date) => toStartOfDay(d) <= toStartOfDay(ref);

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
        if (datetime && isBeforeTomorrow(datetime)) {
            Alert.alert("Invalid date", "First contribution date must be from tomorrow onwards.");
            return;
        }
        setForm(prev => ({ ...prev, contributionDate: datetime }));
    };

    const handlePayoutDateChange = (datetime: Date | null) => {
        if (datetime) {
            // Must be at least tomorrow
            if (isBeforeTomorrow(datetime)) {
                Alert.alert("Invalid date", "First payout date must be from tomorrow onwards.");
                return;
            }
            // Must be strictly after contribution date (if set)
            if (form.contributionDate && isOnOrBefore(datetime, form.contributionDate)) {
                Alert.alert("Invalid date", "First payout date must be after the first contribution date.");
                return;
            }
        }
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

            if (form.contributionDate && isBeforeTomorrow(form.contributionDate)) {
                Alert.alert("Error", "First contribution date must be from tomorrow onwards.");
                return;
            }

            // Payout date must be from tomorrow and after contribution date
            if (form.payoutDate) {
                if (isBeforeTomorrow(form.payoutDate)) {
                    Alert.alert("Error", "First payout date must be from tomorrow onwards.");
                    return;
                }
                if (form.contributionDate && isOnOrBefore(form.payoutDate, form.contributionDate)) {
                    Alert.alert("Error", "First payout date must be after the first contribution date.");
                    return;
                }
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

    // Helper to render frequency options (fixes JSX parse issues)
    const renderFrequencyOptions = (
        field: 'contributionFrequency' | 'payoutFrequency',
        selectedValue: string
    ) => {
        if (isDarkMode) {
            return (
                <View className="flex-row justify-between mb-3">
                    {['Monthly', 'Bi-Weekly', 'Weekly'].map((opt) => {
                        const selected = selectedValue === opt;
                        return (
                            <TouchableOpacity
                                key={opt}
                                onPress={() => handleRadioSelect(field, opt)}
                                className="px-3 py-2 rounded-full"
                            >
                                <Text
                                    className="text-base"
                                    style={
                                        selected
                                            ? { color: colors.primary, fontWeight: '700' }
                                            : { color: colors.text, fontWeight: '700', opacity: 0.95 }
                                    }
                                >
                                    {opt}
                                </Text>
                            </TouchableOpacity>
                        );
                    })}
                </View>
            );
        }

        return (
            <RadioBox
                options={['Monthly', 'Bi-Weekly', 'Weekly']}
                selectedOption={selectedValue}
                onSelect={(option) => handleRadioSelect(field, option)}
            />
        );
    };

    return (
        <GestureHandlerRootView className="flex-1">
            <SafeAreaView className="flex-1 bg-white" style={{ backgroundColor: colors.background }}>
                <StatusBar style={isDarkMode ? 'light' : 'dark'} />
                {/* Header with Back Button and Title */}
                <View
                    className="flex-row items-center px-5 py-3 border-b border-gray-200"
                    style={isDarkMode ? { borderBottomColor: 'rgba(255,255,255,0.12)' } : undefined}
                >
                    <TouchableOpacity onPress={() => router.back()}>
                        <Image
                            source={icons.back}
                            className="w-6 h-6"
                            resizeMode="contain"
                            style={{ tintColor: isDarkMode ? '#FFFFFF' : colors.text }}
                        />
                    </TouchableOpacity>
                    <Text
                        className="text-lg font-medium ml-4"
                        style={{ color: isDarkMode ? '#FFFFFF' : colors.text }}
                    >
                        Stokvels
                    </Text>
                </View>

                <ScrollView
                    contentContainerStyle={{ flexGrow: 1, paddingBottom: 20 }}
                    nestedScrollEnabled={true}
                    keyboardShouldPersistTaps="handled"
                    style={{ backgroundColor: colors.background }}
                >
                    <View className="w-full flex-1 justify-start px-7">
                        <Text
                            className="text-xl font-['PlusJakartaSans-SemiBold'] my-7"
                            style={{ color: isDarkMode ? '#FFFFFF' : colors.text }}
                        >
                            Create a New Stokvel
                        </Text>

                        <StokvelAvatar
                            profileImage={form.profileImage}
                            onImageUpdate={handleImageUpdate}
                        />

                        <RadioBox
                            options={["Private", "Public"]}
                            selectedOption={form.visibility}
                            onSelect={(option) => handleRadioSelect('visibility', option)}
                        />

                        {/* Stokvel Name */}
                        {isDarkMode && (
                          <Text className="mb-1 text-sm font-['PlusJakartaSans-SemiBold']" style={{ color: '#FFFFFF' }}>
                            Stokvel Name
                          </Text>
                        )}
                        <FormInputFlat
                            title={isDarkMode ? "" : "Stokvel Name"}
                            value={form.name}
                            placeholder="Enter stokvel name"
                            handleChangeText={(text) => {
                                if (text.length <= 60) handleChangeText('name', text);
                            }}
                            helperText={`${form.name.length}/60 characters`}
                        />

                        {/* Row: Min Monthly Contribution & Max Members */}
                        <View className="flex-row justify-between">
                            <View className="flex-1 mr-2">
                                {isDarkMode && (
                                  <Text className="mb-1 text-sm font-['PlusJakartaSans-SemiBold']" style={{ color: '#FFFFFF' }}>
                                    Min Monthly Contribution
                                  </Text>
                                )}
                                <FormInputFlat
                                    title={isDarkMode ? "" : "Min Monthly Contribution"}
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
                            </View>

                            <View className="flex-1 ml-2">
                                {isDarkMode && (
                                  <Text className="mb-1 text-sm font-['PlusJakartaSans-SemiBold']" style={{ color: '#FFFFFF' }}>
                                    Max Members
                                  </Text>
                                )}
                                <FormInputFlat
                                    title={isDarkMode ? "" : "Max Members"}
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
                        </View>

                        {/* Description */}
                        {isDarkMode && (
                          <Text className="mb-1 mt-2 text-sm font-['PlusJakartaSans-SemiBold']" style={{ color: '#FFFFFF' }}>
                            Description
                          </Text>
                        )}
                        <FormInputFlat
                            title={isDarkMode ? "" : "Description"}
                            value={form.description}
                            placeholder="Enter stokvel description"
                            handleChangeText={(text) => {
                                if (text.length <= 255) handleChangeText('description', text);
                            }}
                            multiline
                            numberOfLines={4}
                            helperText={`${form.description.length}/255 characters`}
                        />

                        <Text
                            className="text-lg mb-2 font-light"
                            style={{ color: isDarkMode ? '#FFFFFF' : colors.text }}
                        >
                            Contributions
                        </Text>
                        {renderFrequencyOptions('contributionFrequency', form.contributionFrequency)}

                        {/* First Contribution Date */}
                        {isDarkMode && (
                          <Text className="mb-1 text-sm font-['PlusJakartaSans-SemiBold']" style={{ color: '#FFFFFF' }}>
                            First Contribution Date
                          </Text>
                        )}
                        <DateTimeInput
                            label={isDarkMode ? "" : "First Contribution Date"}
                            onDateTimeChange={handleContributionDateChange}
                        />

                        <Text
                            className="text-lg mb-2 font-light"
                            style={{ color: isDarkMode ? '#FFFFFF' : colors.text }}
                        >
                            Payouts
                        </Text>
                        {renderFrequencyOptions('payoutFrequency', form.payoutFrequency)}

                        {/* First Payout Date */}
                        {isDarkMode && (
                          <Text className="mb-1 text-sm font-['PlusJakartaSans-SemiBold']" style={{ color: '#FFFFFF' }}>
                            First Payout Date
                          </Text>
                        )}
                        <DateTimeInput
                            label={isDarkMode ? "" : "First Payout Date"}
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