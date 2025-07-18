import { Text, View, Image, TouchableOpacity, ScrollView } from 'react-native'
import React from 'react'
import { SafeAreaView } from 'react-native-safe-area-context';
import TopBar from '../../src/components/TopBar';
import { icons } from '../../src/constants';

const profile = () => {
  return (
    <SafeAreaView className="flex-1 bg-white">
      <TopBar title="Your Profile" />
      
      <ScrollView className="flex-1 px-6">
        {/* Profile Section */}
        <View className="items-center mt-8 mb-8">
          {/* Profile picture */}
          <TouchableOpacity className="relative mb-4">
            <View className="w-32 h-32 bg-slate-200 rounded-full items-center justify-center mb-4 overflow-hidden">
              {/* actual profile image will go here */}
              <Image 
                className='w-full h-full'
                source={icons.profile}
                resizeMode="cover"
              />
            </View>
          </TouchableOpacity>

          <Text className="text-2xl font-['PlusJakartaSans-Bold'] text-black mb-4">Son Goku</Text>

          {/* Buttons */}
          <View className="flex-row gap-3">
            <TouchableOpacity className="bg-[#1DA1FA] px-6 py-3 rounded-full">
              <Text className="text-white font-['PlusJakartaSans-Medium'] text-m">Edit Profile</Text>
            </TouchableOpacity>
            <TouchableOpacity className="bg-[#1DA1FA] px-6 py-3 rounded-full">
              <Text className="text-white font-['PlusJakartaSans-Medium'] text-m">Settings</Text>
            </TouchableOpacity>
          </View>

        </View>
        <View className="mb-6">
          <Text className="text-lg font-['PlusJakartaSans-SemiBold'] text-black mb-4">Badges</Text>
          <View className="flex-row gap-4">
          
          </View>

          <Text className="text-lg font-['PlusJakartaSans-SemiBold'] text-black mb-4">Rank</Text>
          <View className="flex-row gap-4">
          
          </View>
        </View>
      </ScrollView>
      
    </SafeAreaView>
  )
}

export default profile