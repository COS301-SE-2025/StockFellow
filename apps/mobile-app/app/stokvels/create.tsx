import React, { useState } from "react";
import { Text, View, Alert, TouchableOpacity, Image } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { ScrollView, GestureHandlerRootView } from "react-native-gesture-handler";
// import StokvelAvatar from "../../src/components/StokvelAvatar";
import FormInputFlat from "../../src/components/FormInputFlat";
import CustomButton from "../../src/components/CustomButton";
import RadioBox from "../../src/components/RadioBox";
import DateTimeInput from "../../src/components/DateTimeInput";
import { router } from "expo-router";
import { icons } from "../../src/constants";
import authService from '../../src/services/authService';
import userService from '../../src/services/userService';
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

    // Tier and recommendation state
    const [userTier, setUserTier] = useState<number>(1);
    const [recommended, setRecommended] = useState<{ min: number; max: number }>({ min: 50, max: 200 });

    const normalizeTier = (raw: any) => {
        const n = Number(raw);
        if (Number.isFinite(n)) {
            if (n >= 1 && n <= 5) return n;
            if (n >= 0 && n <= 4) return n + 1;
        }
        return 1;
    };
    const getRangeForTier = (tier: number) => {
        switch (tier) {
            case 1: return { min: 50, max: 200 };
            case 2: return { min: 200, max: 500 };
            case 3: return { min: 500, max: 1000 };
            case 4: return { min: 1000, max: 2500 };
            case 5: return { min: 2500, max: 5000 };
            default: return { min: 50, max: 200 };
        }
    };

    // Fetch tier once
    React.useEffect(() => {
        (async () => {
            try {
                const profile = await userService.getProfile();
                const t = normalizeTier(profile?.affordability?.tier);
                setUserTier(t);
                setRecommended(getRangeForTier(t));
            } catch {
                // fallback defaults already set
            }
        })();
    }, []);

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
            // Enforce tier max on submit
            if (minContributionNum > recommended.max) {
                Alert.alert("Error", `Min contribution cannot exceed your tier's max: R${recommended.max.toFixed(2)}`);
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

                        {/* Default Stokvel picture (no upload) */}
                        <Image
                            source={icons.stokvelpfp}
                            className="w-32 h-32 self-center my-2"
                            resizeMode="contain"
                        />

                        {/* Removed upload UI */}
                        {/* <StokvelAvatar
                            profileImage={form.profileImage}
                            onImageUpdate={handleImageUpdate}
                        /> */}

                        <RadioBox
                            options={["Private", "Public"]}
                            selectedOption={form.visibility}
                            onSelect={(option) => handleRadioSelect('visibility', option)}
                        />

                        {/* Stokvel Name */}
                        <Text className="mb-1 text-sm font-['PlusJakartaSans-SemiBold']" style={{ color: isDarkMode ? '#FFFFFF' : colors.text }}>
                          Stokvel Name
                        </Text>
                        <FormInputFlat
                            title=""
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
                                <Text className="mb-1 text-sm font-['PlusJakartaSans-SemiBold']" style={{ color: isDarkMode ? '#FFFFFF' : colors.text }}>
                                  Min Monthly Contribution
                                </Text>
                                <FormInputFlat
                                    title=""
                                    value={form.minContribution}
                                    placeholder="200.00"
                                    handleChangeText={(text) => {
                                        const sanitized = text.replace(/[^0-9.]/g, '');
                                        if ((sanitized.match(/\./g) || []).length > 1) return;
                                        // Enforce tier max
                                        const num = parseFloat(sanitized || '0');
                                        if (!Number.isNaN(num)) {
                                            if (num > recommended.max) {
                                                Alert.alert(
                                                    'Limit reached',
                                                    `Max allowed for your tier is R${recommended.max.toFixed(2)}`
                                                );
                                                return setForm(prev => ({ ...prev, minContribution: String(recommended.max) }));
                                            }
                                        }
                                        handleChangeText('minContribution', sanitized);
                                    }}
                                    keyboardType="numeric"
                                    helperText={`Suggested for you: R${recommended.min.toFixed(2)} - R${recommended.max.toFixed(2)}`}
                                    helperTextColor={
                                        parseFloat(form.minContribution || '0') > recommended.max ? '#EF4444' : undefined
                                    }
                                />
                            </View>

                            <View className="flex-1 ml-2">
                                <Text className="mb-1 text-sm font-['PlusJakartaSans-SemiBold']" style={{ color: isDarkMode ? '#FFFFFF' : colors.text }}>
                                  Max Members
                                </Text>
                                <FormInputFlat
                                    title=""
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
                        <Text className="mb-1 mt-2 text-sm font-['PlusJakartaSans-SemiBold']" style={{ color: isDarkMode ? '#FFFFFF' : colors.text }}>
                          Description
                        </Text>
                        <FormInputFlat
                            title=""
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
                        <RadioBox
                            options={["Monthly", "Bi-Weekly", "Weekly"]}
                            selectedOption={form.contributionFrequency}
                            onSelect={(option) => handleRadioSelect('contributionFrequency', option)}
                        />
                        
                        {/* First Contribution Date */}
                        <Text className="mb-1 text-sm font-['PlusJakartaSans-SemiBold']" style={{ color: isDarkMode ? '#FFFFFF' : colors.text }}>
                          First Contribution Date
                        </Text>
                        <DateTimeInput
                            label=""
                            onDateTimeChange={handleContributionDateChange}
                        />

                        <Text
                            className="text-lg mb-2 font-light"
                            style={{ color: isDarkMode ? '#FFFFFF' : colors.text }}
                        >
                            Payouts
                        </Text>
                        <RadioBox
                            options={["Monthly", "Bi-Weekly", "Weekly"]}
                            selectedOption={form.payoutFrequency}
                            onSelect={(option) => handleRadioSelect('payoutFrequency', option)}
                        />

                        {/* First Payout Date */}
                        <Text className="mb-1 text-sm font-['PlusJakartaSans-SemiBold']" style={{ color: isDarkMode ? '#FFFFFF' : colors.text }}>
                          First Payout Date
                        </Text>
                        <DateTimeInput
                            label=""
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