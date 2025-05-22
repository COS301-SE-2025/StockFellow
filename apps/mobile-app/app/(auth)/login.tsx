import { Image, Text, View } from 'react-native';
import React, { useState } from 'react';
import { SafeAreaView } from 'react-native-safe-area-context';
import { ScrollView, GestureHandlerRootView } from 'react-native-gesture-handler';
import FormInput from '../../src/components/FormInput';
import CustomButton from '../../src/components/CustomButton';
import { Link, useRouter } from "expo-router";
import { images } from '../../src/constants';
import { StatusBar } from 'expo-status-bar';

const Login = () => {
  const [form, setForm] = useState({
    email: '',
    password: ''
  });

  const [isSubmitting, setIsSubmitting] = useState(false);
  const router = useRouter();

  const handleLogin = async () => {
    // Your login logic here
  };

  return (
    <GestureHandlerRootView className='flex-1'>
      <StatusBar style="light" />
      {/* Blue header section */}
      <View className='h-40 bg-[#1DA1FA] w-full absolute top-0' />
      <SafeAreaView className='flex-1  '>
        {/* Blue background with white "sheet" */}
        <View className='flex-1 pt-20 bg-[#1DA1FA]'>
          {/* White "bottom sheet" container */}
          <View className='flex-1 bg-white rounded-t-[60px] mt-20 p-8'>
            <ScrollView 
              contentContainerStyle={{ flexGrow: 1 }}
              showsVerticalScrollIndicator={false}
            >
              <View className='flex-1 justify-start'>
                <Text className="text-2xl text-left font-semibold my-3">Login</Text>
                <Text className="text-m text-left mb-3 text-[#71727A] font-light">Access your existing account</Text>

                <FormInput
                  title="Email"
                  value={form.email}
                  handleChangeText={(e) => setForm({...form, email: e})}
                  otherStyles="mt-3"
                  keyboardType="email-address"
                  placeholder='Email Address'
                />

                <FormInput
                  title="Password"
                  value={form.password}
                  handleChangeText={(e) => setForm({...form, password: e})}
                  otherStyles="mt-3"
                  placeholder='Password'
                />

                <Link href="/forgot-password" className="text-[#1DA1FA] font-medium self-start mt-4 mb-4 text-sm">
                  Forgot Password?
                </Link>

                <CustomButton
                  title="Login"
                  containerStyles="bg-[#1DA1FA] rounded-xl px-8 py-4 my-4"
                  textStyles="text-white text-lg"
                  handlePress={handleLogin}
                  isLoading={isSubmitting}
                />

                <View className="flex-row justify-center gap-2 mt-1">
                  <Text className="text-sm text-[#71727A]">Not a Member?</Text>
                  <Link href="/signup" className="text-[#1DA1FA] font-semibold text-sm">
                    Register Now
                  </Link>
                </View>
              </View>

              {/* <View className="flex-row justify-center gap-4 mt-8 pb-10">
                <Link href="/terms" className="text-blue-400 font-semibold text-sm">
                  Terms of Use
                </Link>
                <Link href="/privacy" className="text-blue-400 font-semibold text-sm">
                  Privacy Policy
                </Link>
              </View> */}
            </ScrollView>
          </View>
        </View>
      </SafeAreaView>
    </GestureHandlerRootView>
  );
};

export default Login;