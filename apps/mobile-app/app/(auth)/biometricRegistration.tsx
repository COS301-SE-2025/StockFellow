import React, { useState, useEffect } from "react";
import { View, Text, TouchableOpacity, Alert, ActivityIndicator, Platform } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { router } from "expo-router";
import { Passkey } from "react-native-passkey";
import { StatusBar } from 'expo-status-bar';
import webAuthnService from "../../src/services/webauthnService";
import authService from "../../src/services/authService";

interface UserInfo {
  id: string;
  username: string;
}

export default function PasskeyScreen() {
  const [loading, setLoading] = useState(true);
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null);

  useEffect(() => {
    const getUserInfo = async () => {
      try {
        const user = await authService.getCurrentUserFull();
        setUserInfo({
          id: user.id,
          username: user.username
        });
      } catch (error) {
        console.error('Failed to get user info:', error);
      } finally {
        setLoading(false);
      }
    };

    getUserInfo();
  }, []);

  const register = async () => {
    if (!userInfo) {
      Alert.alert('Error', 'User information not available');
      return;
    }

    try {
      // Ask backend to start registration with correct user info
      const startResp = await webAuthnService.startRegistration({
        userId: userInfo.id,
        username: userInfo.username,
        authenticatorName: Platform.OS === "ios" ? "Face ID" : "Fingerprint",
      });

      // Call native passkey registration
      const result = await Passkey.create(startResp);
      console.log("Passkey.create result:", JSON.stringify(result, null, 2));


      // Send attestation result back to backend
      await webAuthnService.completeRegistration(startResp.challenge, {
        credentialId: result.id,
        credentialType: result.type ?? "public-key",
        clientDataJSON: result.response.clientDataJSON,
        attestationObject: result.response.attestationObject,
      });

      Alert.alert(
        "Success", 
        "Biometric authentication registered successfully!", 
        [{ text: "OK", onPress: () => router.replace("/(tabs)/home") }]
      );
    } catch (err: any) {
      Alert.alert("Error", err.message || "Registration failed");
    }
  };

  const login = async () => {
    if (!userInfo) {
      Alert.alert('Error', 'User information not available');
      return;
    }

    try {
      // Start authentication with correct username
      const startResp = await webAuthnService.startAuthentication({
        username: userInfo.username
      });

      // Call native passkey authentication
      const result = await Passkey.get(startResp);
      console.log("Passkey.get result:", JSON.stringify(result, null, 2));

      // Complete authentication
      const authResp = await webAuthnService.completeAuthentication(startResp.challenge, {
        credentialId: result.id,
        credentialType: result.type ?? "public-key",
        clientDataJSON: result.response.clientDataJSON,
        authenticatorData: result.response.authenticatorData,
        signature: result.response.signature,
        userHandle: result.response.userHandle,
      });

      Alert.alert(
        "Success", 
        `Welcome ${authResp.username}`,
        [{ text: "OK", onPress: () => router.replace("/(tabs)/home") }]
      );
    } catch (err: any) {
      Alert.alert("Error", err.message || "Login failed");
    }
  };

  if (loading) {
    return (
      <SafeAreaView className="flex-1 bg-white justify-center items-center">
        <ActivityIndicator size="large" color="#1DA1FA" />
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView className="flex-1 bg-white">
      <StatusBar style="dark" />

      <View className="flex-1 justify-center px-6">
        <View className="items-center mb-10">
          <Text className="text-[#1DA1FA] text-3xl font-['PlusJakartaSans-Bold'] mb-2">
            Biometric Setup
          </Text>
          <Text className="text-gray-600 text-center text-lg font-['PlusJakartaSans-Regular']">
            Secure your account with {Platform.OS === "ios" ? "Face ID" : "Fingerprint"}
          </Text>
        </View>

        <View className="gap-4">
          <TouchableOpacity
            onPress={register}
            className="bg-[#1DA1FA] py-4 rounded-full"
          >
            <Text className="text-white text-center text-lg font-['PlusJakartaSans-SemiBold']">
              Register Biometrics
            </Text>
          </TouchableOpacity>

          <TouchableOpacity
            onPress={login}
            className="border-2 border-[#1DA1FA] py-4 rounded-full"
          >
            <Text className="text-[#1DA1FA] text-center text-lg font-['PlusJakartaSans-SemiBold']">
              Login with Biometrics
            </Text>
          </TouchableOpacity>
        </View>

        <View className="mt-6">
          <Text className="text-gray-600 text-center font-['PlusJakartaSans-Regular']">
            Welcome back, {userInfo?.username}
          </Text>
        </View>
      </View>
    </SafeAreaView>
  );
}
