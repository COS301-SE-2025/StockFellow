import { Text, View, Image, TouchableOpacity, ScrollView, Modal, TextInput, Alert, Switch } from 'react-native'
import React, { useState } from 'react'
import { SafeAreaView } from 'react-native-safe-area-context';
import TopBar from '../../src/components/TopBar';
import { icons, images } from '../../src/constants';
import { useTheme } from '../../app/_layout';
import { useTutorial } from '../../src/components/help/TutorialContext';
import { useRouter } from 'expo-router';
import * as ImagePicker from 'expo-image-picker';
import { Linking } from 'react-native';
import HelpMenu from '../../src/components/help/HelpMenu';
import { useEffect } from 'react';
import userService from '../../src/services/userService'; 

const profile = () => {
  const { isDarkMode, toggleTheme } = useTheme();
  const { startTutorial } = useTutorial();
  const router = useRouter();

  // Modal states
  const [editProfileVisible, setEditProfileVisible] = useState(false);
  const [settingsVisible, setSettingsVisible] = useState(false);
  const [helpMenuVisible, setHelpMenuVisible] = useState(false);
  
  // Edit Profile states
  const [profileData, setProfileData] = useState({
    name: '',
    email: '',
    profileImage: null as string | null
  });

  const [userProfile, setUserProfile] = useState<any>(null);
  const [affordabilityInfo, setAffordabilityInfo] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Settings states
  const [notificationsEnabled, setNotificationsEnabled] = useState(true);

  const handleSaveProfile = () => {
    Alert.alert('Success', 'Profile updated successfully!');
    setEditProfileVisible(false);
  };

  const handleSettings = () => {
    setSettingsVisible(true);
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

  useEffect(() => {
    const fetchUserProfile = async () => {
      try {
        setLoading(true);
        const response = await userService.getProfile();
        
        // Update profile data with real data
        setUserProfile(response.user);
        setAffordabilityInfo(response.affordability);
        
        // Update local state for display
        setProfileData({
          name: `${response.user.firstName || ''} ${response.user.lastName || ''}`.trim() || response.user.username,
          email: response.user.email,
          profileImage: null 
        });
        
      } catch (err: any) {
        setError(err.message);
        console.error('Failed to load profile:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchUserProfile();
  }, []);

  if (loading) {
    return (
      <SafeAreaView className="flex-1 bg-white">
        <TopBar title="Your Profile" />
        <View className="flex-1 justify-center items-center">
          <Text>Loading profile...</Text>
        </View>
      </SafeAreaView>
    );
  }

  if (error) {
    return (
      <SafeAreaView className="flex-1 bg-white">
        <TopBar title="Your Profile" />
        <View className="flex-1 justify-center items-center px-6">
          <Text className="text-red-500 text-center">Error loading profile: {error}</Text>
          <TouchableOpacity 
            className="mt-4 bg-[#1DA1FA] px-6 py-3 rounded-full"
            onPress={() => {
              setError(null);
              // fetchUserProfile();
            }}
          >
            <Text className="text-white">Retry</Text>
          </TouchableOpacity>
        </View>
      </SafeAreaView>
    );
  }

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
                  source={images.songoku}
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

        {/* ...existing code... */}
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
            {/* Level Icon with Number */}
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
            
            {/* Level Icon with Number */}
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
              <Text className="text-white font-['PlusJakartaSans-SemiBold'] text-2xl">Tier 2</Text>
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
                        source={images.songoku}
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

      {/* Settings Modal */}
      <Modal
        animationType="slide"
        transparent={true}
        visible={settingsVisible}
        onRequestClose={() => setSettingsVisible(false)}
      >
        <View className="flex-1 justify-center items-center bg-black/50">
          <View className="bg-white rounded-2xl p-6 w-11/12">
            <View className="flex-row justify-between items-center mb-6">
              <Text className="text-2xl font-['PlusJakartaSans-Bold'] text-black">Settings</Text>
              <TouchableOpacity onPress={() => setSettingsVisible(false)}>
                <Image 
                  source={icons.close}
                  className="w-6 h-6"
                  style={{ tintColor: '#666' }}
                />
              </TouchableOpacity>
            </View>

            <View className="space-y-6">
              {/* Dark Mode Setting */}
              <View className="flex-row justify-between items-center py-3">
                <View className="flex-row items-center flex-1">
                  <Image 
                    source={icons.light}
                    className="w-6 h-6 mr-3"
                    style={{ tintColor: '#1DA1FA' }}
                  />
                  <View className="flex-1">
                    <Text className="font-['PlusJakartaSans-SemiBold'] text-black text-base">Dark Mode</Text>
                    <Text className="text-gray-600 text-sm">Switch to dark theme</Text>
                  </View>
                </View>
                <Switch
                  value={isDarkMode}
                  onValueChange={toggleTheme}
                  trackColor={{ false: '#E5E5E5', true: '#1DA1FA' }}
                  thumbColor={isDarkMode ? '#FFFFFF' : '#f4f3f4'}
                />
              </View>

              {/* Notifications Setting */}
              <View className="flex-row justify-between items-center py-3">
                <View className="flex-row items-center flex-1">
                  <Image 
                    source={icons.bell}
                    className="w-6 h-6 mr-3"
                    style={{ tintColor: '#1DA1FA' }}
                  />
                  <View className="flex-1">
                    <Text className="font-['PlusJakartaSans-SemiBold'] text-black text-base">Notifications</Text>
                    <Text className="text-gray-600 text-sm">Receive app notifications</Text>
                  </View>
                </View>
                <Switch
                  value={notificationsEnabled}
                  onValueChange={setNotificationsEnabled}
                  trackColor={{ false: '#E5E5E5', true: '#1DA1FA' }}
                  thumbColor={notificationsEnabled ? '#FFFFFF' : '#f4f3f4'}
                />
              </View>

              {/* Help & Support - Opens HelpMenu with Tutorial, Contact Support, and FAQ */}
              <TouchableOpacity 
                className="flex-row items-center py-3"
                onPress={() => {
                  setSettingsVisible(false);
                  setHelpMenuVisible(true);
                }}
              >
                <Image 
                  source={icons.help}
                  className="w-6 h-6 mr-3"
                  style={{ tintColor: '#1DA1FA' }}
                />
                <View className="flex-1">
                  <Text className="font-['PlusJakartaSans-SemiBold'] text-black text-base">Help & Support</Text>
                  <Text className="text-gray-600 text-sm">Tutorial, FAQ, and contact support</Text>
                </View>
                <Image 
                  source={icons.right}
                  className="w-5 h-5"
                  style={{ tintColor: '#666' }}
                />
              </TouchableOpacity>

              {/* View Notifications */}
              <TouchableOpacity 
                className="flex-row items-center py-3"
                onPress={() => {
                  setSettingsVisible(false);
                  router.push('/notifications');
                }}
              >
                <Image 
                  source={icons.bell_filled}
                  className="w-6 h-6 mr-3"
                  style={{ tintColor: '#1DA1FA' }}
                />
                <View className="flex-1">
                  <Text className="font-['PlusJakartaSans-SemiBold'] text-black text-base">View Notifications</Text>
                  <Text className="text-gray-600 text-sm">See your notification history</Text>
                </View>
                <Image 
                  source={icons.right}
                  className="w-5 h-5"
                  style={{ tintColor: '#666' }}
                />
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>

      {/* Help Menu Modal */}
      <HelpMenu 
        isVisible={helpMenuVisible}
        onClose={() => setHelpMenuVisible(false)}
      />
    </SafeAreaView>
  )
}

export default profile