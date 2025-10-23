import { Text, View, Alert } from 'react-native';
import React, { useState, useRef, useEffect } from 'react';
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

  const [touched, setTouched] = useState({
    username: false,
    firstName: false,
    lastName: false,
    contactNumber: false,
    idNumber: false,
    email: false,
    password: false,
    confirmPassword: false
  });

  const [bankStatement, setBankStatement] = useState<DocumentPicker.DocumentPickerAsset | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [uploadProgress, setUploadProgress] = useState<string>('');
  const router = useRouter();

  const [passwordRequirements, setPasswordRequirements] = useState({
    length: false,
    uppercase: false,
    digit: false,
    symbol: false
  });

  // Validation functions (keeping existing implementation)
  const validateUsername = (value: string): string => {
    if (!value.trim()) return 'Username is required';
    if (value.length < 3) return 'Username must be at least 3 characters';
    if (value.length > 20) return 'Username must not exceed 20 characters';
    if (!/^[a-zA-Z0-9_]+$/.test(value)) return 'Username can only contain letters, numbers, and underscores';
    return '';
  };

  const validateName = (value: string, fieldName: string): string => {
    if (!value.trim()) return `${fieldName} is required`;
    if (!/^[A-Z]/.test(value)) return `${fieldName} must start with a capital letter`;
    if (!/^[A-Za-z\s-']+$/.test(value)) return `${fieldName} can only contain letters, spaces, hyphens, and apostrophes`;
    if (value.length < 2) return `${fieldName} must be at least 2 characters`;
    if (value.length > 50) return `${fieldName} must not exceed 50 characters`;
    return '';
  };

  const validateContactNumber = (value: string): string => {
    if (!value) return 'Contact number is required';
    const cleanNumber = value.replace(/\s+/g, '');
    if (!/^\d+$/.test(cleanNumber)) return 'Contact number must contain only digits';
    if (!/^0[0-9]{9}$/.test(cleanNumber)) return 'Must be a valid 10-digit number starting with 0';
    return '';
  };

  const validateIdNumber = (value: string): string => {
    if (!value) return 'ID number is required';
    if (!/^\d+$/.test(value)) return 'ID number must contain only digits';
    if (value.length !== 13) return 'ID number must be exactly 13 digits';
    
    const year = parseInt(value.substring(0, 2));
    const month = parseInt(value.substring(2, 4));
    const day = parseInt(value.substring(4, 6));
    
    if (month < 1 || month > 12) return 'ID number contains invalid month';
    if (day < 1 || day > 31) return 'ID number contains invalid day';
    
    return '';
  };

  const validateEmail = (value: string): string => {
    if (!value) return 'Email is required';
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) return 'Please enter a valid email address';
    if (value.length > 100) return 'Email address is too long';
    return '';
  };

  const validatePassword = (value: string): string => {
    if (!value) return 'Password is required';
    
    const requirements = {
      length: value.length >= 6,
      uppercase: /[A-Z]/.test(value),
      digit: /\d/.test(value),
      symbol: /[!@#$%^&*(),.?":{}|<>]/.test(value)
    };

    const unmet = [];
    if (!requirements.length) unmet.push('at least 6 characters');
    if (!requirements.uppercase) unmet.push('one uppercase letter');
    if (!requirements.digit) unmet.push('one digit');
    if (!requirements.symbol) unmet.push('one symbol');

    if (unmet.length > 0) {
      return `Password must contain: ${unmet.join(', ')}`;
    }
    return '';
  };

  const validateConfirmPassword = (value: string, password: string): string => {
    if (!value) return 'Please confirm your password';
    if (value !== password) return 'Passwords do not match';
    return '';
  };

  useEffect(() => {
    const newRequirements = {
      length: form.password.length >= 6,
      uppercase: /[A-Z]/.test(form.password),
      digit: /\d/.test(form.password),
      symbol: /[!@#$%^&*(),.?":{}|<>]/.test(form.password)
    };
    setPasswordRequirements(newRequirements);
  }, [form.password]);

  const handleFieldChange = (field: keyof typeof form, value: string) => {
    let processedValue = value;

    switch (field) {
      case 'firstName':
      case 'lastName':
        if (value.length === 1) {
          processedValue = value.toUpperCase();
        }
        processedValue = processedValue.replace(/[^A-Za-z\s-']/g, '');
        break;
      
      case 'username':
        processedValue = value.replace(/[^a-zA-Z0-9_]/g, '');
        break;
      
      case 'contactNumber':
        processedValue = value.replace(/[^\d]/g, '');
        if (processedValue.length > 10) {
          processedValue = processedValue.substring(0, 10);
        }
        break;
      
      case 'idNumber':
        processedValue = value.replace(/[^\d]/g, '');
        if (processedValue.length > 13) {
          processedValue = processedValue.substring(0, 13);
        }
        break;
      
      case 'email':
        processedValue = value.replace(/\s/g, '');
        break;
    }

    setForm({ ...form, [field]: processedValue });

    if (touched[field]) {
      let error = '';
      switch (field) {
        case 'username':
          error = validateUsername(processedValue);
          break;
        case 'firstName':
          error = validateName(processedValue, 'First name');
          break;
        case 'lastName':
          error = validateName(processedValue, 'Last name');
          break;
        case 'contactNumber':
          error = validateContactNumber(processedValue);
          break;
        case 'idNumber':
          error = validateIdNumber(processedValue);
          break;
        case 'email':
          error = validateEmail(processedValue);
          break;
        case 'password':
          error = validatePassword(processedValue);
          if (form.confirmPassword) {
            const confirmError = validateConfirmPassword(form.confirmPassword, processedValue);
            setErrors(prev => ({ ...prev, confirmPassword: confirmError }));
          }
          break;
        case 'confirmPassword':
          error = validateConfirmPassword(processedValue, form.password);
          break;
      }
      setErrors({ ...errors, [field]: error });
    }
  };

  const handleFieldBlur = (field: keyof typeof form) => {
    setTouched({ ...touched, [field]: true });
    
    let error = '';
    switch (field) {
      case 'username':
        error = validateUsername(form[field]);
        break;
      case 'firstName':
        error = validateName(form[field], 'First name');
        break;
      case 'lastName':
        error = validateName(form[field], 'Last name');
        break;
      case 'contactNumber':
        error = validateContactNumber(form[field]);
        break;
      case 'idNumber':
        error = validateIdNumber(form[field]);
        break;
      case 'email':
        error = validateEmail(form[field]);
        break;
      case 'password':
        error = validatePassword(form[field]);
        break;
      case 'confirmPassword':
        error = validateConfirmPassword(form[field], form.password);
        break;
    }
    setErrors({ ...errors, [field]: error });
  };

  const validateForm = () => {
    const newErrors = {
      username: validateUsername(form.username),
      firstName: validateName(form.firstName, 'First name'),
      lastName: validateName(form.lastName, 'Last name'),
      contactNumber: validateContactNumber(form.contactNumber),
      idNumber: validateIdNumber(form.idNumber),
      email: validateEmail(form.email),
      password: validatePassword(form.password),
      confirmPassword: validateConfirmPassword(form.confirmPassword, form.password)
    };

    setErrors(newErrors);
    setTouched({
      username: true,
      firstName: true,
      lastName: true,
      contactNumber: true,
      idNumber: true,
      email: true,
      password: true,
      confirmPassword: true
    });

    return Object.values(newErrors).every(error => error === '');
  };

  const renderPasswordRequirements = () => {
    return (
      <View className="mt-2">
        <Text className="text-xs text-gray-900 font-['PlusJakartaSans-Regular']">Password must contain:</Text>
        <View className="ml-2 mt-1 flex-1 flex-row justify-around">
          <View>
            <Text className={`text-xs ${passwordRequirements.length ? 'text-green-500' : 'text-gray-400'} font-['PlusJakartaSans-Regular']`}>
              • At least 6 characters
            </Text>
            <Text className={`text-xs ${passwordRequirements.uppercase ? 'text-green-500' : 'text-gray-400'} font-['PlusJakartaSans-Regular']`}>
              • One uppercase letter
            </Text>
          </View>
          <View>
            <Text className={`text-xs ${passwordRequirements.digit ? 'text-green-500' : 'text-gray-400'} font-['PlusJakartaSans-Regular']`}>
              • One digit (0-9)
            </Text>
            <Text className={`text-xs ${passwordRequirements.symbol ? 'text-green-500' : 'text-gray-400'} font-['PlusJakartaSans-Regular']`}>
              • One symbol (!@#$%^&* etc.)
            </Text>
          </View>
        </View>
      </View>
    );
  };

  const handleConfirmPasswordChange = (text: string) => {
    if (text.length > form.confirmPassword.length + 1) {
      Alert.alert('Paste Not Allowed', 'Please type your password to confirm');
      return;
    }
    handleFieldChange('confirmPassword', text);
  };

  const handleSignup = async () => {
    if (validateForm()) {
      setIsSubmitting(true);
      setUploadProgress('Creating account...');
      
      try {
        console.log('Attempting registration...');

        const registrationData = {
          username: form.username,
          firstName: form.firstName,
          lastName: form.lastName,
          email: form.email,
          password: form.password,
          contactNumber: form.contactNumber,
          idNumber: form.idNumber,
          bankStatement: bankStatement || undefined, // Include bank statement if provided
        };

        console.log('Registration data:', {
          ...registrationData,
          password: '***',
          bankStatement: bankStatement ? 'Provided' : 'Not provided'
        });

        // Update progress message if bank statement is provided
        if (bankStatement) {
          setUploadProgress('Creating account and analyzing bank statement...');
        }

        const registrationResult = await authService.register(registrationData);

        if (registrationResult.success) {
          console.log('Registration successful');
          
          // Show success message with tier info if available
          let successMessage = 'Account created successfully!';
          if (registrationResult.data?.tierName) {
            successMessage += `\n\nYour affordability tier: ${registrationResult.data.tierName}`;
          } else if (bankStatement) {
            successMessage += '\n\nBank statement analysis is in progress.';
          }
          
          Alert.alert(
            'Success',
            successMessage,
            [
              {
                text: 'OK',
                onPress: () => {
                  router.push({
                    pathname: '/login',
                  });
                }
              }
            ]
          );
        } else {
          console.error('Registration failed:', registrationResult.error);
          
          const errorMessage = registrationResult.error || 'Registration failed. Please try again.';

          if (errorMessage.includes('already exists') ||
            errorMessage.includes('User already exists')) {
            setErrors({
              ...errors,
              username: 'Username or email already exists',
            });
          } else {
            setErrors({
              ...errors,
              email: errorMessage,
            });
          }
          
          Alert.alert('Registration Failed', errorMessage);
        }
      } catch (error) {
        console.error('Registration error:', error);
        const errorMessage = error instanceof Error ? error.message : 'Registration failed. Please try again.';
        Alert.alert('Registration Error', errorMessage);
        setErrors({
          ...errors,
          email: errorMessage,
        });
      } finally {
        setIsSubmitting(false);
        setUploadProgress('');
      }
    } else {
      Alert.alert('Validation Error', 'Please fix all errors before submitting');
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
                <Text className="text-xl text-left font-['PlusJakartaSans-SemiBold'] my-2">Sign Up</Text>
                <Text className="text-sm text-left mb-3 text-[#71727A] font-['PlusJakartaSans-Light']">
                  Create an account to get started
                </Text>

                <FormInput
                  title="Username"
                  value={form.username}
                  handleChangeText={(e) => handleFieldChange('username', e)}
                  onBlur={() => handleFieldBlur('username')}
                  otherStyles="mt-3"
                  placeholder='johndoe'
                  error={touched.username ? errors.username || '' : ''}
                  autoCapitalize="none"
                />

                <FormInput
                  title="First Name"
                  value={form.firstName}
                  handleChangeText={(e) => handleFieldChange('firstName', e)}
                  onBlur={() => handleFieldBlur('firstName')}
                  otherStyles="mt-3"
                  placeholder='Jane'
                  error={touched.firstName ? errors.firstName || '' : ''}
                  autoCapitalize="words"
                />

                <FormInput
                  title="Last Name"
                  value={form.lastName}
                  handleChangeText={(e) => handleFieldChange('lastName', e)}
                  onBlur={() => handleFieldBlur('lastName')}
                  otherStyles="mt-3"
                  placeholder='Doe'
                  error={touched.lastName ? errors.lastName || '' : ''}
                  autoCapitalize="words"
                />

                <FormInput
                  title="Contact Number"
                  value={form.contactNumber}
                  handleChangeText={(e) => handleFieldChange('contactNumber', e)}
                  onBlur={() => handleFieldBlur('contactNumber')}
                  otherStyles="mt-3"
                  keyboardType="phone-pad"
                  placeholder='0712345678'
                  error={touched.contactNumber ? errors.contactNumber || '' : ''}
                  maxLength={10}
                />

                <FormInput
                  title="ID Number"
                  value={form.idNumber}
                  handleChangeText={(e) => handleFieldChange('idNumber', e)}
                  onBlur={() => handleFieldBlur('idNumber')}
                  otherStyles="mt-3"
                  keyboardType="numeric"
                  placeholder='8601011234567'
                  error={touched.idNumber ? errors.idNumber || '' : ''}
                  maxLength={13}
                />

                <FormInput
                  title="Email Address"
                  value={form.email}
                  handleChangeText={(e) => handleFieldChange('email', e)}
                  onBlur={() => handleFieldBlur('email')}
                  otherStyles="mt-3"
                  keyboardType="email-address"
                  placeholder='jane@example.com'
                  error={touched.email ? errors.email || '' : ''}
                  autoCapitalize="none"
                />

                <FormInput
                  title="Password"
                  value={form.password}
                  handleChangeText={(e) => handleFieldChange('password', e)}
                  onBlur={() => handleFieldBlur('password')}
                  otherStyles="mt-3"
                  placeholder='Create a password'
                  error={touched.password ? errors.password || '' : ''}
                  autoCapitalize="none"
                />
                {renderPasswordRequirements()}

                <FormInput
                  title="Confirm Password"
                  value={form.confirmPassword}
                  handleChangeText={handleConfirmPasswordChange}
                  onBlur={() => handleFieldBlur('confirmPassword')}
                  otherStyles="mt-3"
                  placeholder='Confirm your password'
                  error={touched.confirmPassword ? errors.confirmPassword || '' : ''}
                  autoCapitalize="none"
                />

                <PDFUpload
                  heading="3 Month Bank Statement (Optional)"
                  onDocumentSelect={(doc) => {
                    setBankStatement(doc);
                    console.log('Bank statement selected:', doc?.name);
                  }}
                />

                {bankStatement && (
                  <View className="mt-2 p-3 bg-green-50 rounded-lg">
                    <Text className="text-sm text-green-700 font-['PlusJakartaSans-Medium']">
                      ✓ Bank statement ready for upload
                    </Text>
                    <Text className="text-xs text-green-600 font-['PlusJakartaSans-Regular'] mt-1">
                      {bankStatement.name}
                    </Text>
                  </View>
                )}

                {uploadProgress && (
                  <View className="mt-3 p-3 bg-blue-50 rounded-lg">
                    <Text className="text-sm text-blue-700 font-['PlusJakartaSans-Medium']">
                      {uploadProgress}
                    </Text>
                  </View>
                )}

                <CustomButton
                  title={bankStatement ? "Sign Up & Analyze Statement" : "Sign Up"}
                  containerStyles="bg-[#1DA1FA] rounded-xl px-8 py-4 my-4 mt-10"
                  textStyles="text-white text-lg"
                  handlePress={handleSignup}
                  isLoading={isSubmitting}
                  disabled={isSubmitting}
                />

                <View className="flex-row justify-center gap-2 mt-1">
                  <Text className="text-sm text-[#71727A] font-['PlusJakartaSans-Regular']">Already a Member?</Text>
                  <Link href="/login" className="text-[#1DA1FA] font-['PlusJakartaSans-SemiBold'] text-sm">
                    Login
                  </Link>
                </View>
              </View>

              <View className="flex-1 px-4 text-center self-end mt-4">
                <Text className="text-center text-sm font-['PlusJakartaSans-Light']">
                  By clicking Sign Up, you have read and agreed to our
                  <Link href='' className="text-[#1DA1FA] font-['PlusJakartaSans-SemiBold']"> Terms of Use </Link>
                  and
                  <Link href='' className="text-[#1DA1FA] font-['PlusJakartaSans-SemiBold']"> Privacy Policy </Link>
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