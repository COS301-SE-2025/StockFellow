import { Text, View, Alert } from 'react-native';
import React, { useState, useRef } from 'react';
import { SafeAreaView } from 'react-native-safe-area-context';
import { ScrollView, GestureHandlerRootView } from 'react-native-gesture-handler';
import FormInput from '../../src/components/FormInput';
import PDFUpload from '../../src/components/pdfUpload';
import CustomButton from '../../src/components/CustomButton';
import { Link, useRouter } from "expo-router";
import { StatusBar } from 'expo-status-bar';
import authService from '../../src/services/authService';
import * as DocumentPicker from 'expo-document-picker';

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

  const [bankStatement, setBankStatement] = useState<DocumentPicker.DocumentPickerAsset | null>(null);
  const [payslip, setPayslip] = useState<DocumentPicker.DocumentPickerAsset | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [uploadingDoc, setUploadingDoc] = useState<string | null>(null);
  const router = useRouter();
  const passwordRef = useRef<string>('');

  const validateForm = () => {
    let valid = true;
    const newErrors = { ...errors };

    if (!form.username.trim()) {
      newErrors.username = 'Username is required';
      valid = false;
    }

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
        // Update the form with the cleaned number
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

     if (!bankStatement) {
      newErrors.password = 'Please upload your 3-month bank statement';
      valid = false;
    }

    if (!payslip) {
      newErrors.password = 'Please upload your latest payslip';
      valid = false;
    }


    setErrors(newErrors);
    return valid;
  };

  const handleDocumentUpload = async (type: 'bankStatement' | 'payslip') => {
    try {
      setUploadingDoc(type);
      const res = await DocumentPicker.getDocumentAsync({
        type: 'application/pdf',
      });

      if (res.canceled) {
        console.log('File selection canceled');
        return;
      }

      if (res.assets && res.assets[0]) {
        if (type === 'bankStatement') {
          setBankStatement(res.assets[0]);
        } else {
          setPayslip(res.assets[0]);
        }
      }
    } catch (err) {
      console.error('Error during document picker:', err);
      Alert.alert('Error', 'Failed to select document. Please try again.');
    } finally {
      setUploadingDoc(null);
    }
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
        console.log('Attempting registration...');

        // Prepare form data with documents
        const formData = new FormData();
        formData.append('username', form.username);
        formData.append('firstName', form.firstName);
        formData.append('lastName', form.lastName);
        formData.append('email', form.email);
        formData.append('password', form.password);
        formData.append('contactNumber', form.contactNumber);
        formData.append('idNumber', form.idNumber);
        
        if (bankStatement) {
          formData.append('bankStatement', {
            uri: bankStatement.uri,
            name: bankStatement.name,
            type: 'application/pdf',
          } as any);
        }
        
        if (payslip) {
          formData.append('payslip', {
            uri: payslip.uri,
            name: payslip.name,
            type: 'application/pdf',
          } as any);
        }

        const registrationResult = await authService.register(formData);

        if (registrationResult.success) {
          console.log('Registration successful');
          
          // Automatically login after successful registration
          const loginResult = await authService.login(form.username, form.password);
          
          if (loginResult.success) {
            console.log('Auto-login successful');
            router.push('/(tabs)/home');
          } else {
            // Registration successful but auto-login failed
            console.log('Registration successful, redirecting to login');
            router.push('/login');
          }
        } else {
          // Handle registration failure
          console.error('Registration failed:', registrationResult.error);
          
          // Show error on appropriate field
          if (registrationResult.error.includes('already exists') || 
              registrationResult.error.includes('User already exists')) {
            setErrors({
              ...errors,
              username: 'Username or email already exists',
            });
          } else {
            setErrors({
              ...errors,
              email: registrationResult.error,
            });
          }
        }
      } catch (error) {
        console.error('Registration error:', error);
        const errorMessage = error instanceof Error ? error.message : 'Registration failed. Please try again.';
        setErrors({
          ...errors,
          email: errorMessage,
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
                  title="Username"
                  value={form.username}
                  handleChangeText={(e) => setForm({ ...form, username: e })}
                  otherStyles="mt-3"
                  placeholder='johndoe'
                  error={errors.username}
                />

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
                  title="Contact Number"
                  value={form.contactNumber}
                  handleChangeText={(e) => {
                    setForm({ ...form, contactNumber: e });
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
                  error={errors.password}
                />

                <FormInput
                  title="Confirm Password"
                  value={form.confirmPassword}
                  handleChangeText={handleConfirmPasswordChange}
                  otherStyles="mt-3"
                  placeholder='Confirm your password'
                  error={errors.confirmPassword}
                />
                {/* 3 Month Bank Statement Upload */}
                <PDFUpload 
                  heading="3 Month Bank Statement"
                  onDocumentSelect={(doc) => setBankStatement(doc)}
                />

                {/* Payslip Upload */}
                <PDFUpload 
                  heading="Latest Payslip"
                  onDocumentSelect={(doc) => setPayslip(doc)}
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