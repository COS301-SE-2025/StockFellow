import { Text, View } from 'react-native';
import React, { useState } from 'react';
import { SafeAreaView } from 'react-native-safe-area-context';
import { ScrollView, GestureHandlerRootView } from 'react-native-gesture-handler';
import FormInput from '../../src/components/FormInput';
import CustomButton from '../../src/components/CustomButton';
import { Link, useRouter } from "expo-router";
import { StatusBar } from 'expo-status-bar';
import authService from '../../src/services/authService';

const Login = () => {
  const [form, setForm] = useState({
    username: '',
    password: ''
  });

  const [errors, setErrors] = useState({
    username: '',
    password: ''
  });

  const [isSubmitting, setIsSubmitting] = useState(false);
  const router = useRouter();

  const validateForm = () => {
    let valid = true;
    const newErrors = { username: '', password: '' };

    if (!form.username) {
      newErrors.username = 'Username is required';
      valid = false;
    }

    if (!form.password) {
      newErrors.password = 'Password is required';
      valid = false;
    } else if (form.password.length < 6) {
      newErrors.password = 'Password must be at least 6 characters';
      valid = false;
    }

    setErrors(newErrors);
    return valid;
  };

  // const handleLogin = async () => {
  //   if (validateForm()) {
  //     setIsSubmitting(true);
  //     try {
  //       console.log('Attempting login...');

  //       // Auth service instead of direct KC call
  //       const result = await authService.login(form.username, form.password);

  //       if (result.success) {
  //         console.log('Login successful');
  //         router.push('/(tabs)/home');
  //       } else {
  //         // Handle login failure
  //         console.error('Login failed:', result.error);
  //         setErrors({
  //           ...errors,
  //           username: result.error,
  //         });
  //       }
  //     } catch (error) {
  //       console.error('Login error:', error);
  //       setErrors({
  //         ...errors,
  //         username: error instanceof Error ?
  //           error.message :
  //           'Network error. Please check your connection.',
  //       });
  //     } finally {
  //       setIsSubmitting(false);
  //     }
  //   }
  // };

  const handleLogin = async () => {
    if (validateForm()) {
      setIsSubmitting(true);
      try {
        console.log('Attempting login...');

        const result = await authService.login(form.username, form.password);

        if (result.success) {
          if (result.mfaRequired) {
            // MFA is required - navigate to MFA verification
            console.log('MFA required, navigating to verification');
            router.push({
              pathname: '/mfaVerification',
              params: {
                email: result.email,
                tempSession: result.tempSession,
                message: result.message,
              },
            });
          } else {
            // No MFA required - login successful
            console.log('Login successful, navigating to home');
            router.replace('/(tabs)/home');
          }
        } else {
          // Handle login failure
          console.error('Login failed:', result.error);
          setErrors({
            ...errors,
            username: result.error,
          });
        }
      } catch (error) {
        console.error('Login error:', error);
        setErrors({
          ...errors,
          username: error instanceof Error ?
            error.message :
            'Network error. Please check your connection.',
        });
      } finally {
        setIsSubmitting(false);
      }
    }
  };


  return (
    <GestureHandlerRootView className='flex-1'>
      <StatusBar style="light" />
      <View className='h-40 bg-[#1DA1FA] w-full absolute top-0' />
      <SafeAreaView className='flex-1'>
        <View className='flex-1 pt-20 bg-[#1DA1FA]'>
          <View className='flex-1 bg-white rounded-t-[60px] mt-20 p-8'>
            <ScrollView
              contentContainerStyle={{ flexGrow: 1 }}
              showsVerticalScrollIndicator={false}
            >
              <View className='flex-1 justify-start'>
                <Text className="text-2xl text-left font-['PlusJakartaSans-SemiBold'] my-3">Login</Text>
                <Text className="text-m text-left mb-3 text-[#71727A] font-['PlusJakartaSans-Light']">
                  Access your existing account
                </Text>

                <FormInput
                  title="Username"
                  value={form.username}
                  handleChangeText={(e) => setForm({ ...form, username: e })}
                  otherStyles="mt-3"
                  placeholder='Username'
                  error={errors.username}
                />

                <FormInput
                  title="Password"
                  value={form.password}
                  handleChangeText={(e) => setForm({ ...form, password: e })}
                  otherStyles="mt-3"
                  placeholder='Password'
                  error={errors.password}
                />

                <Link href="/forgot-password" className="text-[#1DA1FA] font-['PlusJakartaSans-Medium'] self-start mt-4 mb-4 text-sm">
                  Forgot Password?
                </Link>


                {/* <Link
                  href={{
                    pathname: "/mfaVerification",
                    params: { email: form.username } // Pass the username/email as param
                  }}
                  className="text-[#1DA1FA] font-medium self-start mt-4 mb-4 text-sm"
                >
                  MFA Verification
                </Link> */}

                <CustomButton
                  title="Login"
                  containerStyles="bg-[#1DA1FA] rounded-xl px-8 py-4 my-4"
                  textStyles="text-white text-lg"
                  handlePress={handleLogin}
                  isLoading={isSubmitting}
                />

                <View className="flex-row justify-center gap-2 mt-1">
                  <Text className="text-sm text-[#71727A] font-['PlusJakartaSans-Regular']">Not a Member?</Text>
                  <Link href="/signup" className="text-[#1DA1FA] font-['PlusJakartaSans-SemiBold'] text-sm">
                    Register Now
                  </Link>
                </View>
              </View>
            </ScrollView>
          </View>
        </View>
      </SafeAreaView>
    </GestureHandlerRootView>
  );
};

export default Login;