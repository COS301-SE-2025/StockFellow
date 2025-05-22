import { Text, View } from 'react-native';
import React, { useState, useRef } from 'react';
import { SafeAreaView } from 'react-native-safe-area-context';
import { ScrollView, GestureHandlerRootView } from 'react-native-gesture-handler';
import FormInput from '../../src/components/FormInput';
import CustomButton from '../../src/components/CustomButton';
import { Link, useRouter } from "expo-router";
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

  const [errors, setErrors] = useState({
    fullName: '',
    contactNumber: '',
    idNumber: '',
    email: '',
    password: '',
    confirmPassword: ''
  });

  const [isSubmitting, setIsSubmitting] = useState(false);
  const router = useRouter();
  const passwordRef = useRef<string>('');

  const validateForm = () => {
    let valid = true;
    const newErrors = {
      fullName: '',
      contactNumber: '',
      idNumber: '',
      email: '',
      password: '',
      confirmPassword: ''
    };

    if (!form.fullName.trim()) {
      newErrors.fullName = 'Full name is required';
      valid = false;
    }

    if (!form.contactNumber.trim()) {
      newErrors.contactNumber = 'Contact number is required';
      valid = false;
    } else if (!/^[0-9]{10,15}$/.test(form.contactNumber)) {
      newErrors.contactNumber = 'Invalid contact number';
      valid = false;
    }

    if (!form.idNumber.trim()) {
      newErrors.idNumber = 'ID number is required';
      valid = false;
    }

    if (!form.email) {
      newErrors.email = 'Email is required';
      valid = false;
    } else if (!/\S+@\S+\.\S+/.test(form.email)) {
      newErrors.email = 'Email is invalid';
      valid = false;
    }

    if (!form.password) {
      newErrors.password = 'Password is required';
      valid = false;
    } else if (form.password.length < 6) {
      newErrors.password = 'Password must be at least 6 characters';
      valid = false;
    } else {
      passwordRef.current = form.password;
    }

    if (!form.confirmPassword) {
      newErrors.confirmPassword = 'Please confirm your password';
      valid = false;
    } else if (form.password !== form.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
      valid = false;
    }

    setErrors(newErrors);
    return valid;
  };

  const handlePasswordChange = (text: string) => {
    setForm({...form, password: text});
    passwordRef.current = text;
  };

  const handleConfirmPasswordChange = (text: string) => {
    // Prevent paste by comparing with previous password value
    if (text.length > form.confirmPassword.length + 1) {
      // Likely a paste operation
      setForm({...form, confirmPassword: ''});
      setErrors({...errors, confirmPassword: 'Pasting is not allowed'});
    } else {
      setForm({...form, confirmPassword: text});
    }
  };

  const handleSignup = async () => {
    if (validateForm()) {
      setIsSubmitting(true);
      // Your signup logic here
      // router.push('/verify-email');
      setIsSubmitting(false);
    }
  };

  return (
    <GestureHandlerRootView className='flex-1'>
      <StatusBar style="light" />
      <View className='h-40 bg-[#1DA1FA] w-full absolute top-0' />
      <SafeAreaView className='flex-1'>
        <View className='flex-1 pt-2 bg-[#1DA1FA]'>
          <View className='flex-1 bg-white rounded-t-[60px] p-8'>
            <ScrollView
              contentContainerStyle={{ flexGrow: 1 }}
              showsVerticalScrollIndicator={false}
            >
              <View className='flex-1 justify-start'>
                <Text className="text-2xl text-left font-semibold my-3">Sign Up</Text>
                <Text className="text-m text-left mb-3 text-[#71727A] font-light">
                  Create an account to get started
                </Text>

                <FormInput
                  title="Full Name"
                  value={form.fullName}
                  handleChangeText={(e) => setForm({...form, fullName: e})}
                  otherStyles="mt-3"
                  placeholder='Jane Doe'
                  error={errors.fullName}
                />

                <FormInput
                  title="Contact Number"
                  value={form.contactNumber}
                  handleChangeText={(e) => setForm({...form, contactNumber: e})}
                  otherStyles="mt-3"
                  keyboardType="phone-pad"
                  placeholder='071 234 5678'
                  error={errors.contactNumber}
                />

                <FormInput
                  title="ID Number"
                  value={form.idNumber}
                  handleChangeText={(e) => setForm({...form, idNumber: e})}
                  otherStyles="mt-3"
                  keyboardType="numeric"
                  placeholder='8601011234567'
                  error={errors.idNumber}
                />

                <FormInput
                  title="Email Address"
                  value={form.email}
                  handleChangeText={(e) => setForm({...form, email: e})}
                  otherStyles="mt-3"
                  keyboardType="email-address"
                  placeholder='jane@example.com'
                  error={errors.email}
                />

                <FormInput
                  title="Password"
                  value={form.password}
                  handleChangeText={handlePasswordChange}
                  otherStyles="mt-3"
                  placeholder='Create a password'
                  secureTextEntry
                  error={errors.password}
                />

                <FormInput
                  title="Confirm Password"
                  value={form.confirmPassword}
                  handleChangeText={handleConfirmPasswordChange}
                  otherStyles="mt-3"
                  placeholder='Confirm your password'
                  secureTextEntry
                  error={errors.confirmPassword}
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

              <View className="flex-1 px-4 text-center self-end mt-4">
                <Text className="text-center text-sm font-light">
                  By clicking Sign Up, you have read and agreed to our
                  <Link href='' className="text-[#1DA1FA] font-semibold"> Terms of Use </Link>
                  and
                  <Link href='' className="text-[#1DA1FA] font-semibold"> Privacy Policy </Link>
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