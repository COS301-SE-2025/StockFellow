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

          <Text className="text-3xl font-['PlusJakartaSans-Bold'] text-black mb-4">Son Goku</Text>

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

        {/* Badges and Rank*/}
        <View className="mb-6">
          <Text className="text-lg font-['PlusJakartaSans-SemiBold'] text-black mb-4">Badges</Text>
          <View className="flex-row gap-4">
          
          </View>

          <Text className="text-lg font-['PlusJakartaSans-SemiBold'] text-black mb-4">Rank</Text>
          <View className="flex-row gap-4">
          
          </View>
          
          {/* Tier tasks */}
          <View className="rounded-lg overflow-hidden">
            {/* Current Tier */}
            <View className="bg-[#1DA1FA] px-4 py-3 rounded-t-lg">
              <Text className="text-white font-['PlusJakartaSans-SemiBold'] text-2xl">Tier 1</Text>
            </View>
            
            {/* Tasks */}
            <View className="bg-gray-100 px-4 py-4 rounded-b-lg">
              <View className="flex-row items-center mb-3">
                <View className="w-6 h-6 border-2 border-gray-400 rounded mr-3 mb-3" />
                <Text className="flex-1 text-black text-m font-['PlusJakartaSans-Regular'] mb-1">Join a stokvel group</Text>
              </View>
              
              <View className="flex-row items-center mb-3">
                <View className="w-6 h-6 border-2 border-gray-400 rounded mr-3 mb-3" />
                <Text className="flex-1 text-black text-m font-['PlusJakartaSans-Regular'] mb-1">Verify your account information</Text>
              </View>
              
              <View className="flex-row items-center">
                <View className="w-6 h-6 border-2 border-gray-400 rounded mr-3 mb-3" />
                <Text className="flex-1 text-black text-m font-['PlusJakartaSans-Regular'] mb-1">Complete the tutorial guide</Text>
              </View>
            </View>
          </View>

        </View>
      </ScrollView>
    </SafeAreaView>
  )
}

export default profile