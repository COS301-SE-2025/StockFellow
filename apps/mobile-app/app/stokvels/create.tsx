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
    setIsSubmitting(true);
    try {
      if (Number(form.maxMembers) > 30) {
        Alert.alert("Error", "Maximum members cannot exceed 30");
        return;
      }

      setIsSubmitting(true);

        // const payload = {
        // ...form,
        // contributionDate: form.contributionDate?.toISOString(),
        // payoutDate: form.payoutDate?.toISOString(),
        // };

        // const response = await fetch("http://localhost::3000/api/groups/create", {
        // method: "POST",
        // headers: {
        //     "Content-Type": "application/json",
        // },
        // body: JSON.stringify(payload),
        // });

        // if (!response.ok) {
        // throw new Error("Failed to create stokvel");
        // }


      
      router.push('/stokvels');
    } catch (error) {
      Alert.alert("Error", "Failed to create stokvel");
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
            <Text className="text-xl font-semibold my-7">Create a New Stokvel</Text>

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
              handleChangeText={(text) => handleChangeText('name', text)}
              helperText="Maximum of 60 characters"
            />

            <View className="flex-row justify-between">
              <FormInputFlat
                title="Min Monthly Contribution"
                value={form.minContribution}
                placeholder="200.00"
                handleChangeText={(text) => handleChangeText('minContribution', text)}
                keyboardType="numeric"
              />

              <FormInputFlat
                title="Max Members"
                value={form.maxMembers}
                placeholder="10"
                handleChangeText={(text) => handleChangeText('maxMembers', text)}
                keyboardType="numeric"
                helperText="Cannot exceed 30 members"
                helperTextColor={Number(form.maxMembers) > 30 ? "#EF4444" : undefined}
              />
            </View>

            <FormInputFlat
              title="Description"
              value={form.description}
              placeholder="Enter stokvel description"
              handleChangeText={(text) => handleChangeText('description', text)}
              multiline
              numberOfLines={4}
              helperText="Maximum of 255 characters"
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
              containerStyles="bg-[#1DA1FA] rounded-full py-4 px-8 my-4 self-center"
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