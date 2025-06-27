import { Image, Text, View } from 'react-native';
import React from 'react';
import { SafeAreaView } from 'react-native-safe-area-context';
import { ScrollView, TouchableOpacity, GestureHandlerRootView } from 'react-native-gesture-handler';
import { useRouter } from "expo-router";
import { StatusBar } from 'expo-status-bar';
import CustomButton from '../../src/components/CustomButton';
import { icons } from '../../src/constants';

const Verification = () => {
  const router = useRouter();

  const handleStartVerification = () => {
    // Verification logic here
  };

  const verificationSteps = [
    { icon: icons.noGlasses, text: 'Avoid Wearing Glasses' },
    { icon: icons.noHat, text: 'Avoid Wearing Hats' },
    { icon: icons.avoidObjects, text: 'Avoid Background Objects' },
    { icon: icons.light, text: 'Ensure Good Lighting in Room' }
  ];

  return (
    <GestureHandlerRootView className='flex-1'>
      <StatusBar style="light" />
      <SafeAreaView className='flex-1'>
        <View className='flex-1 bg-white p-8'>
          <View className='flex-1'>
            <TouchableOpacity onPress={() => router.back()}>
              <Image
                source={icons.back}
                style={{ 
                  width: 32,
                  height: 32,
                  tintColor: '#1DA1FA',
                  marginBottom: 24
                }}
              />
            </TouchableOpacity>

            <Text className="text-2xl text-center font-['PlusJakartaSans-SemiBold'] mb-2">
              Biometric Verification
            </Text>
            <Text className="text-base text-center font-['PlusJakartaSans-Regular'] mb-6 text-[#6b7280]">
              We will confirm your identity using biometrics
            </Text>

            <View className="items-center justify-center mb-6 flex-1">
              <Image
                source={icons.face}
                style={{ 
                  width: 180,
                  height: 180,
                  tintColor: '#1DA1FA'
                }}
                resizeMode="contain"
              />
              <Text className="text-lg text-center font-['PlusJakartaSans-SemiBold'] text-[#374151] mt-4">
                Position your face in the frame
              </Text>
            </View>

            <View className="mb-6">
              {verificationSteps.map((item, index) => (
                <View key={index} className="flex-row items-center mb-4">
                  <Image
                    source={item.icon}
                    style={{ width: 24, height: 24, tintColor: '#1DA1FA' }}
                    resizeMode="contain"
                  />
                  <Text className="text-base font-['PlusJakartaSans-Regular'] text-[#374151] ml-3">
                    {item.text}
                  </Text>
                </View>
              ))}
            </View>

            <View className="mt-auto">
              <CustomButton
                title="Start Verification"
                containerStyles="bg-[#1DA1FA] rounded-xl px-8 py-4"
                textStyles="text-white text-lg font-['PlusJakartaSans-SemiBold']"
                handlePress={handleStartVerification}
              />
            </View>
          </View>
        </View>
      </SafeAreaView>
    </GestureHandlerRootView>
  );
};

export default Verification;