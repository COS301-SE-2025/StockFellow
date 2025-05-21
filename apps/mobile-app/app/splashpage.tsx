import '../global.css';
import { StatusBar } from 'expo-status-bar';
import { Text, View, Image, TouchableOpacity } from 'react-native';
import { useRouter, Link } from 'expo-router';
import { SafeAreaView } from 'react-native-safe-area-context';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { images } from '../src/constants';
import { useEffect } from 'react';

export default function SplashPage() {
  const router = useRouter();

  useEffect(() => {
    // Auto-navigate after delay
    const timer = setTimeout(() => {
      router.push('/(auth)/login');
    }, 5000);
    return () => clearTimeout(timer);
  }, []);

  return (
    <View className="flex-1 justify-center items-center">
      <Text className="text-2xl">Loading...</Text>
    </View>
  );
}