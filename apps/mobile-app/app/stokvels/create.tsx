import React, { useState } from "react";
import { Text, View, Alert } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { ScrollView, GestureHandlerRootView } from "react-native-gesture-handler";
import StokvelAvatar from "../../src/components/StokvelAvatar";
import FormInputFlat from "../../src/components/FormInputFlat";
import CustomButton from "../../src/components/CustomButton";
import { router } from "expo-router";

interface FormData {
  name: string;
  minContribution: string;
  maxMembers: string;
  description: string;
  profileImage: string | null;
}

const StokvelForm: React.FC = () => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [form, setForm] = useState<FormData>({
    name: "",
    minContribution: "",
    maxMembers: "",
    description: "",
    profileImage: null
  });

  const handleImageUpdate = (uri: string | null) => {
    setForm(prev => ({ ...prev, profileImage: uri }));
  };

  const handleChangeText = (field: keyof Omit<FormData, 'profileImage'>, value: string) => {
    setForm(prev => ({ ...prev, [field]: value }));
  };

  const submit = async () => {
    setIsSubmitting(true);
    try {
      if (Number(form.maxMembers) > 30) {
        Alert.alert("Error", "Maximum members cannot exceed 30");
        return;
      }

      // Your submission logic here
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
        <ScrollView
          contentContainerStyle={{ flexGrow: 1, paddingTop: 16 }}
          nestedScrollEnabled={true}
          keyboardShouldPersistTaps="handled"
        >
          <View className="w-full flex-1 justify-start px-7 gap-1">
            {/* Ensure all text is wrapped in Text components */}
            <Text className="text-left font-semibold text-2xl mb-4 mt-2">
              Create a new Stokvel
            </Text>

            <StokvelAvatar
              profileImage={form.profileImage}
              onImageUpdate={handleImageUpdate}
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

            <CustomButton
              title="Create"
              containerStyles="bg-[#1DA1FA] rounded-full py-4 px-6 my-4 self-center"
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