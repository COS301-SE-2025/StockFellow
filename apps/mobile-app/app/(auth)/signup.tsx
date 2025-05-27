import { Text, View } from 'react-native';
import React, { useState, useRef } from 'react';
import { SafeAreaView } from 'react-native-safe-area-context';
import { ScrollView, GestureHandlerRootView } from 'react-native-gesture-handler';
import FormInput from '../../src/components/FormInput';
import CustomButton from '../../src/components/CustomButton';
import { Link, useRouter } from "expo-router";
import { StatusBar } from 'expo-status-bar';
import * as SecureStore from 'expo-secure-store';

const SignUp = () => {
  const [form, setForm] = useState({
    username: '',
    firstName: '',
    lastName: '',
    contactNumber: '',
    idNumber: '',
    email: '',
    password: '',
    confirmPassword: ''
  });

  const [errors, setErrors] = useState({
    username: '',
    firstName: '',
    lastName: '',
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
    const newErrors = { ...errors };

    if (!form.firstName.trim()) {
      newErrors.firstName = 'First name is required';
      valid = false;
    }

    if (!form.lastName.trim()) {
      newErrors.lastName = 'Last name is required';
      valid = false;
    }

    if (!form.contactNumber) {
      newErrors.contactNumber = 'Contact number is required';
      valid = false;
    } else {

      const cleanNumber = form.contactNumber.replace(/\s+/g, '');
      if (!/^0[0-9]{9}$/.test(cleanNumber)) {
        newErrors.contactNumber = 'Please enter a valid 10-digit phone number';
        valid = false;
      } else {

        newErrors.contactNumber = '';
        // Update the form with the cleaned number before sending to Keycloak
        form.contactNumber = cleanNumber;
      }
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
    setForm({ ...form, password: text });
    passwordRef.current = text;
  };

  const handleConfirmPasswordChange = (text: string) => {

    if (text.length > form.confirmPassword.length + 1) {

      setForm({ ...form, confirmPassword: '' });
      setErrors({ ...errors, confirmPassword: 'Pasting is not allowed' });
    } else {
      setForm({ ...form, confirmPassword: text });
    }
  };

  const handleSignup = async () => {
    if (validateForm()) {
      setIsSubmitting(true);
      try {
        // Get admin token
        const tokenResponse = await fetch('http://10.0.2.2:8080/realms/master/protocol/openid-connect/token', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          body: new URLSearchParams({
            'grant_type': 'password',
            'client_id': 'admin-cli',
            'username': 'admin',
            'password': 'admin'
          }).toString()
        });

        const tokenData = await tokenResponse.json();

        if (!tokenResponse.ok) {
          throw new Error('Failed to get admin token');
        }


        const createUserResponse = await fetch('http://10.0.2.2:8080/admin/realms/stockfellow/users', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${tokenData.access_token}`
          },
          body: JSON.stringify({
            username: form.username,
            enabled: true,
            emailVerified: true,
            firstName: form.firstName,
            lastName: form.lastName,
            email: form.email,
            attributes: {
              contactNumber: [form.contactNumber],
              idNumber: [form.idNumber]
            },
            credentials: [{
              type: 'password',
              value: form.password,
              temporary: false
            }]
          })
        });

        if (!createUserResponse.ok) {
          const errorData = await createUserResponse.json();
          throw new Error(errorData.errorMessage || 'Registration failed');
        }


        const loginFormData = new URLSearchParams();
        loginFormData.append('grant_type', 'password');
        loginFormData.append('client_id', 'public-client');
        loginFormData.append('username', form.email);
        loginFormData.append('password', form.password);

        const loginResponse = await fetch('http://10.0.2.2:8080/realms/stockfellow/protocol/openid-connect/token', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          body: loginFormData.toString()
        });

        const loginData = await loginResponse.json();

        if (!loginResponse.ok) {
          throw new Error('Login after registration failed');
        }

        // Store tokens
        await SecureStore.setItemAsync('access_token', loginData.access_token);
        await SecureStore.setItemAsync('refresh_token', loginData.refresh_token);


        router.push('/login');
      } catch (error) {
        console.error('Registration error:', error);
        setErrors({
          ...errors,
          email: error instanceof Error ?
            error.message :
            'Registration failed. Please try again.',
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
                  title="First Name"
                  value={form.firstName}
                  handleChangeText={(e) => setForm({ ...form, firstName: e })}
                  otherStyles="mt-3"
                  placeholder='Jane'
                  error={errors.firstName}
                />

                <FormInput
                  title="Last Name"
                  value={form.lastName}
                  handleChangeText={(e) => setForm({ ...form, lastName: e })}
                  otherStyles="mt-3"
                  placeholder='Doe'
                  error={errors.lastName}
                />

                <FormInput
                  title="Username"
                  value={form.username}
                  handleChangeText={(e) => setForm({ ...form, username: e })}
                  otherStyles="mt-3"
                  placeholder='johndoe'
                  error={errors.username}
                />

                <FormInput
                  title="Contact Number"
                  value={form.contactNumber}
                  handleChangeText={(e) => {
                    setForm({ ...form, contactNumber: e });
                    // Clear the error when user starts typing
                    if (errors.contactNumber) {
                      setErrors({ ...errors, contactNumber: '' });
                    }
                  }}
                  otherStyles="mt-3"
                  keyboardType="phone-pad"
                  placeholder='071 234 5678'
                  error={errors.contactNumber}
                />

                <FormInput
                  title="ID Number"
                  value={form.idNumber}
                  handleChangeText={(e) => setForm({ ...form, idNumber: e })}
                  otherStyles="mt-3"
                  keyboardType="numeric"
                  placeholder='8601011234567'
                  error={errors.idNumber}
                />

                <FormInput
                  title="Email Address"
                  value={form.email}
                  handleChangeText={(e) => setForm({ ...form, email: e })}
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