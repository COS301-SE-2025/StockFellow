import React, { useState, useEffect, useRef } from 'react';
import { View, Text, Alert, TextInput, TouchableOpacity, Image } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { StatusBar } from 'expo-status-bar';
import { useRouter, useLocalSearchParams } from 'expo-router';
import CustomButton from '../../src/components/CustomButton';
import authService from '../../src/services/authService';
import { icons } from '../../src/constants';

interface CustomButtonProps {
    title: string;
    containerStyles: string;
    textStyles: string;
    handlePress: () => Promise<void>;
    isLoading: boolean;
    disabled?: boolean;  // Changed from isDisabled to disabled to match likely component prop
}

const MfaVerification = () => {
    const [digits, setDigits] = useState<string[]>(Array(6).fill(''));
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [countdown, setCountdown] = useState(60);
    const [codeExpiryMinutes] = useState(10);
    const router = useRouter();
    const params = useLocalSearchParams();
    const email = params.email || '';
    const inputRefs = useRef<(TextInput | null)[]>(Array(6).fill(null));

    // Auto-focus first input on load
    useEffect(() => {
        inputRefs.current[0]?.focus();
    }, []);

    // Auto-submit when all digits are entered
    useEffect(() => {
        if (digits.every(d => d !== '') && digits.length === 6) {
            handleVerify();
        }
    }, [digits]);

    // Countdown timer
    useEffect(() => {
        const timer = setInterval(() => {
            setCountdown(prev => {
                if (prev <= 1) {
                    clearInterval(timer);
                    return 0;
                }
                return prev - 1;
            });
        }, 1000);

        return () => clearInterval(timer);
    }, []);

    const handleDigitChange = (text: string, index: number) => {
        // Only allow single digit input
        if (text.length > 1) {
            return;
        }

        const newDigits = [...digits];
        newDigits[index] = text;
        setDigits(newDigits);

        // Auto-focus next input if digit entered
        if (text && index < 5) {
            inputRefs.current[index + 1]?.focus();
        }

        // Auto-focus previous input if digit deleted
        if (!text && index > 0) {
            inputRefs.current[index - 1]?.focus();
        }
    };

    const handleKeyPress = (e: any, index: number) => {
        if (e.nativeEvent.key === 'Backspace' && !digits[index] && index > 0) {
            inputRefs.current[index - 1]?.focus();
        }
    };

    // const handleVerify = async () => {
    //     const verificationCode = digits.join('');
    //     if (verificationCode.length !== 6) {
    //         Alert.alert('Invalid Code', 'Please enter a complete 6-digit verification code');
    //         return;
    //     }

    //     setIsSubmitting(true);
    //     try {
    //         const result = await authService.verifyMfaCode(email.toString(), verificationCode);

    //         if (result.success) {
    //             router.push('/(tabs)/home');
    //         } else {
    //             let errorMessage = 'Verification failed. Please try again.';
    //             if (result.error?.includes('expired')) {
    //                 errorMessage = 'This code has expired. Please request a new one.';
    //             } else if (result.error?.includes('invalid')) {
    //                 errorMessage = 'Invalid verification code. Please try again.';
    //             }
    //             Alert.alert('Verification Error', errorMessage);
    //             setDigits(Array(6).fill(''));
    //             inputRefs.current[0]?.focus();
    //         }
    //     } catch (error) {
    //         let errorMessage = 'Unable to verify code. Please try again.';
    //         if (error instanceof Error) {
    //             errorMessage = error.message;
    //         }
    //         Alert.alert('Network Error', errorMessage);
    //         console.error('MFA verification error:', error);
    //         setDigits(Array(6).fill(''));
    //         inputRefs.current[0]?.focus();
    //     } finally {
    //         setIsSubmitting(false);
    //     }
    // };


    const handleVerify = async () => {
        const verificationCode = digits.join('');
        if (verificationCode.length !== 6) {
            Alert.alert('Invalid Code', 'Please enter a complete 6-digit verification code');
            return;
        }

        setIsSubmitting(true);
        try {

            const result = await authService.verifyMfaCode(
                email.toString(),
                verificationCode,
                params.tempSession?.toString() || ''
            );

            if (result.success) {
                router.push('/(tabs)/home');
            } else {
                let errorMessage = 'Verification failed. Please try again.';
                if (result.error?.includes('expired')) {
                    errorMessage = 'This code has expired. Please request a new one.';
                } else if (result.error?.includes('invalid')) {
                    errorMessage = 'Invalid verification code. Please try again.';
                }
                Alert.alert('Verification Error', errorMessage);
                setDigits(Array(6).fill(''));
                inputRefs.current[0]?.focus();
            }
        } catch (error) {
            let errorMessage = 'Unable to verify code. Please try again.';
            if (error instanceof Error) {
                errorMessage = error.message;
            }
            Alert.alert('Network Error', errorMessage);
            console.error('MFA verification error:', error);
            setDigits(Array(6).fill(''));
            inputRefs.current[0]?.focus();
        } finally {
            setIsSubmitting(false);
        }
    };

    // resend endpoint not implemented in backend 

    // const handleResendCode = async () => {
    //     if (countdown > 0) {
    //         Alert.alert(
    //             'Wait a moment',
    //             `Please wait ${countdown} seconds before requesting a new code.`
    //         );
    //         return;
    //     }

    //     try {
    //         setIsSubmitting(true);
    //         const result = await authService.resendMfaCode(email.toString());

    //         if (result.success) {
    //             setCountdown(60);
    //             setDigits(Array(6).fill(''));
    //             inputRefs.current[0]?.focus();
    //             Alert.alert(
    //                 'New Code Sent',
    //                 `A new 6-digit code has been sent to ${email}. It will expire in ${codeExpiryMinutes} minutes.`
    //             );
    //         } else {
    //             throw new Error(result.error || 'Failed to resend code');
    //         }
    //     } catch (error) {
    //         let errorMessage = 'Failed to send new code. Please try again later.';
    //         if (error instanceof Error) {
    //             errorMessage = error.message;
    //         }
    //         Alert.alert('Resend Failed', errorMessage);
    //     } finally {
    //         setIsSubmitting(false);
    //     }
    // };

    return (
        <GestureHandlerRootView className='flex-1'>
            <StatusBar style="light" />
            <View className='h-40 bg-[#1DA1FA] w-full absolute top-0' />
            <SafeAreaView className='flex-1'>
                <View className='flex-1 pt-20 bg-[#1DA1FA]'>
                    <View className='flex-1 bg-white rounded-t-[60px] p-8'>
                        <View className='flex-1 justify-start'>
                            {/* Back to Login Link */}
                            <View className='flex-row justify-start items-center mb-5'>
                                <TouchableOpacity
                                    onPress={() => router.push('login')}
                                    className=""
                                >
                                    <Image
                                        source={icons.back}
                                        className="w-8 h-8 mr-3"
                                        resizeMode="contain"
                                    />
                                </TouchableOpacity>
                                <Text className="text-xl font-['PlusJakartaSans-SemiBold'] my-3">Verify Your Identity</Text>
                            </View>

                            <Image
                                source={icons.email}
                                className="w-40 h-40 self-center"
                                resizeMode="contain"
                            />

                            <Text className="text-m text-left mb-3 text-[#0C0C0F] ffont-['PlusJakartaSans-Light']">
                                We've sent a 6-digit code to {email}
                            </Text>

                            {/* 6-Digit Input Boxes */}
                            <View className="flex-row justify-between my-6">
                                {Array.from({ length: 6 }).map((_, index) => (
                                    <View
                                        key={index}
                                        className={`w-12 h-16 border rounded-xl flex items-center justify-center 
                                            ${digits[index] ? 'border-[#1DA1FA]' : 'border-gray-300'}`}
                                    >
                                        <TextInput
                                            ref={(el) => {
                                                if (el) {
                                                    inputRefs.current[index] = el;
                                                }
                                            }}
                                            className="text-2xl text-center w-full h-full font-['PlusJakartaSans-Regular']"
                                            keyboardType="number-pad"
                                            maxLength={1}
                                            value={digits[index]}
                                            onChangeText={(text) => handleDigitChange(text, index)}
                                            onKeyPress={(e) => handleKeyPress(e, index)}
                                            selectTextOnFocus
                                        />
                                    </View>
                                ))}
                            </View>

                            <View className="flex-row items-center mt-4 justify-center">
                                <Text className="text-[#0C0C0F] text-sm font-['PlusJakartaSans-Regular']">
                                    Didn't receive a code? Return to login and try again.
                                </Text>
                                {/* <Text
                                    className={`text-sm ${countdown > 0 ? 'text-[#1DA1FA]' : 'text-[#1DA1FA] font-medium'}`}
                                    onPress={handleResendCode}
                                >
                                    Resend {countdown > 0 ? `(in ${countdown}s)` : ''}
                                </Text> */}
                            </View>

                            <CustomButton
                                title="Verify"
                                containerStyles="bg-[#1DA1FA] rounded-xl px-8 py-4 my-6"
                                textStyles="text-white text-lg"
                                handlePress={handleVerify}
                                isLoading={isSubmitting}
                                disabled={isSubmitting} 
                            />

                            <Text className="text-xs text-[#71727A]  text-center">
                                For security reasons, this code will expire in {codeExpiryMinutes} minutes.
                                {/* {countdown > 0 && ` You can request a new code in ${countdown} seconds.`} */}
                            </Text>
                        </View>
                    </View>
                </View>
            </SafeAreaView>
        </GestureHandlerRootView>
    );
};

export default MfaVerification;


