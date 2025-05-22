import { Image, Text, View } from 'react-native';
import React, { useState } from 'react';
import { SafeAreaView } from 'react-native-safe-area-context';
import { ScrollView, GestureHandlerRootView } from 'react-native-gesture-handler';
import FormInput from '../../src/components/FormInput';
import CustomButton from '../../src/components/CustomButton';
import { Link, useRouter } from "expo-router";
import { images } from '../../src/constants';
import { StatusBar } from 'expo-status-bar';

const SignUp = () => {
  const [form, setForm] = useState({
    fullName: '',
    contactNumber: '',
    idNumber: '',
    email: '',
    password: '',
    confirmPassword: ''
  });

  const [isSubmitting, setIsSubmitting] = useState(false);
  const router = useRouter();

  const handleSignup = async () => {
    // Your signup logic here
  };

  return (
    <GestureHandlerRootView className='flex-1'>
      <StatusBar style="light" />
      {/* Blue header section */}
      <View className='h-40 bg-[#1DA1FA] w-full absolute top-0' />
      <SafeAreaView className='flex-1  '>
        {/* Blue background with white "sheet" */}
        <View className='flex-1 pt-2 bg-[#1DA1FA]'>
          {/* White "bottom sheet" container */}
          <View className='flex-1 bg-white rounded-t-[60px] p-8'>
            <ScrollView
              contentContainerStyle={{ flexGrow: 1 }}
              showsVerticalScrollIndicator={false}
            >
              <View className='flex-1 justify-start'>
                <Text className="text-2xl text-left font-semibold my-3">Sign Up</Text>
                <Text className="text-m text-left mb-3 text-[#71727A] font-light">Create an account to get started </Text>

                <FormInput
                  title="Full Name"
                  value={form.fullName}
                  handleChangeText={(e) => setForm({ ...form, fullName: e })}
                  otherStyles="mt-3"
                  placeholder='Jane Doe'
                />

                <FormInput
                  title="Contact Number"
                  value={form.contactNumber}
                  handleChangeText={(e) => setForm({ ...form, contactNumber: e })}
                  otherStyles="mt-3"
                  keyboardType="phone-pad"
                  placeholder='071 234 5678'
                />

                <FormInput
                  title="ID Number"
                  value={form.idNumber}
                  handleChangeText={(e) => setForm({ ...form, idNumber: e })}
                  otherStyles="mt-3"
                  keyboardType="numeric"
                  placeholder='8601011234567'
                />

                <FormInput
                  title="Email Address"
                  value={form.email}
                  handleChangeText={(e) => setForm({ ...form, email: e })}
                  otherStyles="mt-3"
                  keyboardType="email-address"
                  placeholder='jane@example.com'
                />

                <FormInput
                  title="Password"
                  value={form.password}
                  handleChangeText={(e) => setForm({ ...form, password: e })}
                  otherStyles="mt-3"
                  placeholder='Create a password'
                  secureTextEntry
                />

                <FormInput
                  title="Confirm Password"
                  value={form.confirmPassword}
                  handleChangeText={(e) => setForm({ ...form, confirmPassword: e })}
                  otherStyles="mt-3"
                  placeholder='Confirm your password'
                  secureTextEntry
                />

                <CustomButton
                  title="Sign Up"
                  containerStyles="bg-[#1DA1FA] rounded-xl px-8 py-4 my-4 mt-10"
                  textStyles="text-white text-lg"
                  handlePress={handleSignup}
                  isLoading={isSubmitting}
                />

                <View className="flex-row justify-center gap-2 mt-1">
                  <Text className="text-sm text-[#71727A]">Already a Member?</Text>
                  <Link href="/login" className="text-[#1DA1FA] font-semibold text-sm">
                    Login
                  </Link>
                </View>
              </View>

              <View className="flex-1 px-4 text-center self-end ">
                <Text className="text-center text-sm font-light">
                  By clicking Sign Up, you have read and agreed to our
                  <Link href='' className="text-[#1DA1FA] font-semibold"> Terms of Use </Link>
                  and
                  <Link href='' className=" text-[#1DA1FA] font-semibold"> Privacy Policy </Link>
                </Text>
              </View>
            </ScrollView>
          </View>
        </View>
      </SafeAreaView>
    </GestureHandlerRootView>
  );
};

export default SignUp;