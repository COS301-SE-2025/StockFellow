import { Text, View, Image, TouchableOpacity, ScrollView, Modal, TextInput, Alert } from 'react-native'
import React, { useState } from 'react'
import { SafeAreaView } from 'react-native-safe-area-context';
import TopBar from '../../src/components/TopBar';
import { icons } from '../../src/constants';
import * as ImagePicker from 'expo-image-picker';

const profile = () => {
  // Modal states
  const [editProfileVisible, setEditProfileVisible] = useState(false);
  
  // Edit Profile states
  const [profileData, setProfileData] = useState({
    name: 'Son Goku',
    email: 'songoku@dragonball.com',
    profileImage: null as string | null
  });

  const handleSaveProfile = () => {
    Alert.alert('Success', 'Profile updated successfully!');
    setEditProfileVisible(false);
  };

  const handleSettings = () => {
    console.log('Settings button pressed');
  };

  const handleImagePicker = async () => {
    Alert.alert(
      'Select Image',
      'Choose an option',
      [
        { text: 'Camera', onPress: openCamera },
        { text: 'Gallery', onPress: openGallery },
        { text: 'Cancel', style: 'cancel' }
      ]
    );
  };

  const openCamera = async () => {
    const permission = await ImagePicker.requestCameraPermissionsAsync();
    if (!permission.granted) {
      Alert.alert('Permission required', 'Camera access is required to take a photo.');
      return;
    }

    const result = await ImagePicker.launchCameraAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      allowsEditing: true,
      aspect: [1, 1],
      quality: 0.8,
    });

    if (!result.canceled) {
      setProfileData({...profileData, profileImage: result.assets[0].uri});
    }
  };

  const openGallery = async () => {
    const permission = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (!permission.granted) {
      Alert.alert('Permission required', 'Gallery access is required to select a photo.');
      return;
    }

    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      allowsEditing: true,
      aspect: [1, 1],
      quality: 0.8,
    });

    if (!result.canceled) {
      setProfileData({...profileData, profileImage: result.assets[0].uri});
    }
  };

  return (
    <SafeAreaView className="flex-1 bg-white">
      <TopBar title="Your Profile" />
      
      <ScrollView className="flex-1 px-6">
        {/* Profile Section */}
        <View className="items-center mt-4 mb-6">
          {/* Profile picture */}
          <TouchableOpacity className="relative mb-3">
            <View className="w-32 h-32 bg-slate-200 rounded-full items-center justify-center mb-3 overflow-hidden">
              {profileData.profileImage ? (
                <Image 
                  source={{ uri: profileData.profileImage }}
                  className='w-full h-full'
                  resizeMode="cover"
                />
              ) : (
                <Image 
                  className='w-full h-full'
                  source={icons.profile}
                  resizeMode="cover"
                />
              )}
            </View>
          </TouchableOpacity>

          <Text className="text-3xl font-['PlusJakartaSans-Bold'] text-black mb-3">{profileData.name}</Text>

          {/* Buttons */}
          <View className="flex-row gap-3">
            <TouchableOpacity 
              className="bg-[#1DA1FA] px-6 py-3 rounded-full"
              onPress={() => setEditProfileVisible(true)}
            >
              <Text className="text-white font-['PlusJakartaSans-Medium'] text-m">Edit Profile</Text>
            </TouchableOpacity>
            <TouchableOpacity 
              className="bg-[#1DA1FA] px-6 py-3 rounded-full"
              onPress={handleSettings}
            >
              <Text className="text-white font-['PlusJakartaSans-Medium'] text-m">Settings</Text>
            </TouchableOpacity>
          </View>
        </View>

        {/* Badges and Rank*/}
        <View className="mb-6">
          <Text className="text-lg font-['PlusJakartaSans-SemiBold'] text-black mb-4">Badges</Text>
          
          {/* Badges Container with Horizontal Scroll */}
          <View className="mb-6">
            <ScrollView 
              horizontal 
              showsHorizontalScrollIndicator={false}
              contentContainerStyle={{ 
                flexGrow: 1, 
                justifyContent: 'center',
                paddingHorizontal: 16
              }}
            >
              <View className="flex-row items-center justify-center" style={{ gap: 16 }}>
                {/* Badge 1 */}
                <View className="w-16 h-16 items-center justify-center">
                  <Image 
                    source={icons.badgeOne}
                    className="w-16 h-16"
                    resizeMode="contain"
                  />
                </View>
                
                {/* Badge 2 */}
                <View className="w-16 h-16 items-center justify-center">
                  <Image 
                    source={icons.badgeTwo}
                    className="w-16 h-16"
                    resizeMode="contain"
                  />
                </View>
                
                {/* Badge 3 */}
                <View className="w-16 h-16 items-center justify-center">
                  <Image 
                    source={icons.badgeThree}
                    className="w-16 h-16"
                    resizeMode="contain"
                  />
                </View>
                
                {/* Badge 4 */}
                <View className="w-16 h-16 items-center justify-center">
                  <Image 
                    source={icons.badgeFour}
                    className="w-16 h-16"
                    resizeMode="contain"
                  />
                </View>
              </View>
            </ScrollView>
          </View>

          <Text className="text-lg font-['PlusJakartaSans-SemiBold'] text-black mb-4">Rank</Text>
          
          {/* Rank Progress Section */}
          <View className="flex-row items-center mb-6">
            {/* Level 1 Icon with Number */}
            <View className="relative">
              <Image 
                source={icons.levelTwo}
                className="w-12 h-12"
                resizeMode="contain"
              />
              <View className="absolute inset-0 items-center justify-center">
                <Text className="text-white font-['PlusJakartaSans-Bold'] text-lg">2</Text>
              </View>
            </View>
            
            {/* Progress Bar */}
            <View className="flex-1 h-3 rounded-full mx-6" style={{ backgroundColor: '#F0F7FA' }}>
              <View className="h-3 bg-[#1DA1FA] rounded-full" style={{ width: '60%' }} />
            </View>
            
            {/* Level 2 Icon with Number */}
            <View className="relative">
              <Image 
                source={icons.levelThree}
                className="w-12 h-12"
                resizeMode="contain"
              />
              <View className="absolute inset-0 items-center justify-center">
                <Text className="text-white font-['PlusJakartaSans-Bold'] text-lg">3</Text>
              </View>
            </View>
          </View>
          
          {/* Tier tasks */}
          <View className="rounded-lg overflow-hidden">
            {/* Current Tier */}
            <View className="bg-[#1DA1FA] px-4 py-3 rounded-t-lg">
              <Text className="text-white font-['PlusJakartaSans-SemiBold'] text-2xl">Tier 1</Text>
            </View>
            
            {/* Tasks */}
            <View className="px-4 py-4 rounded-b-lg" style={{ backgroundColor: '#F0F7FA' }}>
              <View className="flex-row items-center mb-3">
                <View className="w-6 h-6 border-2 border-gray-400 rounded mr-3 mb-1" />
                <Text className="flex-1 text-black text-m font-['PlusJakartaSans-Regular'] mb-1">Join a stokvel group</Text>
              </View>
              
              <View className="flex-row items-center mb-3">
                <View className="w-6 h-6 border-2 border-gray-400 rounded mr-3 mb-1" />
                <Text className="flex-1 text-black text-m font-['PlusJakartaSans-Regular'] mb-1">Verify your account information</Text>
              </View>
              
              <View className="flex-row items-center">
                <View className="w-6 h-6 border-2 border-gray-400 rounded mr-3 mb-1" />
                <Text className="flex-1 text-black text-m font-['PlusJakartaSans-Regular'] mb-1">Complete the tutorial guide</Text>
              </View>
            </View>
          </View>

        </View>
      </ScrollView>

      {/* Edit Profile Modal */}
      <Modal
        animationType="slide"
        transparent={true}
        visible={editProfileVisible}
        onRequestClose={() => setEditProfileVisible(false)}
      >
        <View className="flex-1 justify-center items-center bg-black/50">
          <View className="bg-white rounded-2xl p-6 w-11/12 max-h-4/5">
            <View className="flex-row justify-between items-center mb-6">
              <Text className="text-2xl font-['PlusJakartaSans-Bold'] text-black">Edit Profile</Text>
              <TouchableOpacity onPress={() => setEditProfileVisible(false)}>
                <Image 
                  source={icons.close}
                  className="w-6 h-6"
                  style={{ tintColor: '#666' }}
                />
              </TouchableOpacity>
            </View>

            <ScrollView showsVerticalScrollIndicator={false}>
              {/* Profile Picture */}
              <View className="items-center mb-6">
                <TouchableOpacity onPress={handleImagePicker} className="relative">
                  <View className="w-24 h-24 bg-slate-200 rounded-full items-center justify-center overflow-hidden">
                    {profileData.profileImage ? (
                      <Image 
                        source={{ uri: profileData.profileImage }}
                        className="w-full h-full"
                        resizeMode="cover"
                      />
                    ) : (
                      <Image 
                        source={icons.profile}
                        className="w-full h-full"
                        resizeMode="cover"
                      />
                    )}
                  </View>
                  <View className="absolute -bottom-2 -right-2 bg-[#1DA1FA] rounded-full p-2">
                    <Image source={icons.camera} className="w-4 h-4" style={{ tintColor: 'white' }} />
                  </View>
                </TouchableOpacity>
                <Text className="text-gray-600 mt-2 text-sm">Tap to change photo</Text>
              </View>

              {/* Form Fields */}
              <View className="space-y-4">
                <View>
                  <Text className="text-gray-700 font-['PlusJakartaSans-Medium'] mb-2">Full Name</Text>
                  <TextInput
                    value={profileData.name}
                    onChangeText={(text) => setProfileData({...profileData, name: text})}
                    className="border border-gray-300 rounded-lg px-4 py-3 font-['PlusJakartaSans-Regular']"
                    placeholder="Enter your full name"
                  />
                </View>

                <View>
                  <Text className="text-gray-700 font-['PlusJakartaSans-Medium'] mb-2">Email</Text>
                  <TextInput
                    value={profileData.email}
                    onChangeText={(text) => setProfileData({...profileData, email: text})}
                    className="border border-gray-300 rounded-lg px-4 py-3 font-['PlusJakartaSans-Regular']"
                    placeholder="Enter your email"
                    keyboardType="email-address"
                  />
                </View>
              </View>

              {/* Action Buttons */}
              <View className="flex-row gap-3 mt-8">
                <TouchableOpacity 
                  className="flex-1 bg-gray-200 py-3 rounded-lg"
                  onPress={() => setEditProfileVisible(false)}
                >
                  <Text className="text-center text-gray-700 font-['PlusJakartaSans-Medium']">Cancel</Text>
                </TouchableOpacity>
                <TouchableOpacity 
                  className="flex-1 bg-[#1DA1FA] py-3 rounded-lg"
                  onPress={handleSaveProfile}
                >
                  <Text className="text-center text-white font-['PlusJakartaSans-Medium']">Save Changes</Text>
                </TouchableOpacity>
              </View>
            </ScrollView>
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  )
}

export default profile