import { Text, View, Image, TouchableOpacity, ScrollView, Modal, Alert, Switch } from 'react-native'
import React, { useState } from 'react'
import { SafeAreaView } from 'react-native-safe-area-context';
import TopBar from '../../src/components/TopBar';
import { icons, images } from '../../src/constants';
import { useTheme } from '../../app/_layout';
import { useTutorial } from '../../src/components/help/TutorialContext';
import { useRouter } from 'expo-router';
import HelpMenu from '../../src/components/help/HelpMenu';
import { useEffect } from 'react';
import userService from '../../src/services/userService'; 
import { StatusBar } from 'expo-status-bar'; // added
import authService from '../../src/services/authService';

const profile = () => {
  const { isDarkMode, toggleTheme, colors } = useTheme();
  const { startTutorial } = useTutorial();
  const router = useRouter();

  // Modal states
  const [settingsVisible, setSettingsVisible] = useState(false);
  const [helpMenuVisible, setHelpMenuVisible] = useState(false);
  const [selectedBadge, setSelectedBadge] = useState<any>(null);
  const [badgeModalVisible, setBadgeModalVisible] = useState(false);
  
  // Profile data state
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

  const handleSettings = () => {
    setSettingsVisible(true);
  };

  const badgeData = [
    {
      id: 1,
      name: "First Steps",
      description: "Welcome to the community! You've successfully created your account and taken the first step towards building your financial future.",
      icon: icons.badgeOne,
      earned: true 
    },
    {
      id: 2,
      name: "Email Verified",
      description: "Great job! You've verified your email address, making your account more secure and enabling important notifications.",
      icon: icons.badgeTwo,
      earned: userProfile?.emailVerified ?? false // Use nullish coalescing
    },
    {
      id: 3,
      name: "ID Verified",
      description: "Your identity has been verified! This enables higher contribution limits and builds trust within the community.",
      icon: icons.badgeThree,
      earned: userProfile?.idVerified ?? false // Use nullish coalescing
    },
    {
      id: 4,
      name: "Stokvel Member",
      description: "You're now part of a stokvel group! Start contributing and saving together with your fellow members.",
      icon: icons.badgeFour,
      earned: false
    }
  ];

  useEffect(() => {
    const fetchUserProfile = async () => {
      try {
        setLoading(true);

        // Always fetch real data
        const response = await userService.getProfile();

        setUserProfile(response.user);
        setAffordabilityInfo(response.affordability);
        setProfileData({
          name: `${response.user.firstName || ''} ${response.user.lastName || ''}`.trim() || response.user.username,
          email: response.user.email,
          profileImage: null 
        });
        setError(null);
      } catch (err: any) {
        console.error('Failed to load profile:', err);
        setError(err?.message || 'Failed to load profile');
      } finally {
        setLoading(false);
      }
    };

    fetchUserProfile();
  }, []);

  const handleLogout = async () => {
    setSettingsVisible(false);
    try {
      await authService.logout?.(); // call if available
    } catch (e) {
      // ignore logout errors
    }
    router.replace('/login');
  };

  if (loading) {
    return (
      <SafeAreaView className="flex-1 bg-white" style={{ backgroundColor: colors.background }}>
        <StatusBar style={isDarkMode ? 'light' : 'dark'} />
        <TopBar title="Your Profile" />
        <View className="flex-1 justify-center items-center">
          <Text style={{ color: colors.text }}>Loading profile...</Text>
        </View>
      </SafeAreaView>
    );
  }

  if (error) {
    return (
      <SafeAreaView className="flex-1 bg-white" style={{ backgroundColor: colors.background }}>
        <StatusBar style={isDarkMode ? 'light' : 'dark'} />
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

  // Tier config and helpers
  const tierIcons = [icons.levelOne, icons.levelTwo, icons.levelThree, icons.levelFour, icons.levelFive];
  const tierNames = ['Essential Savers', 'Steady Builders', 'Balanced Savers', 'Growth Investors', 'Premium Accumulators'];
  const rawTier = affordabilityInfo?.tier;
  const hasTier = typeof rawTier === 'number' && rawTier >= 0;
  const tierIndex = hasTier ? Math.min(rawTier, tierIcons.length - 1) : null;
  const displayTier = hasTier ? Math.max(1, Math.min((rawTier as number) + 1, 5)) : null;

  return (
    <SafeAreaView className="flex-1 bg-white" style={{ backgroundColor: colors.background }}>
      <StatusBar style={isDarkMode ? 'light' : 'dark'} />
      <TopBar title="Your Profile" />
      
      <ScrollView className="flex-1 px-6" style={{ backgroundColor: colors.background }}>
        {/* Profile Section */}
        <View className="items-center mt-4 mb-6">
          {/* Profile picture */}
          <View className="relative mb-3">
            <View
              className="w-32 h-32 bg-slate-200 rounded-full items-center justify-center mb-3 overflow-hidden"
              style={isDarkMode ? { backgroundColor: colors.card } : undefined}
            >
              {profileData.profileImage ? (
                <Image 
                  source={{ uri: profileData.profileImage }}
                  className='w-full h-full'
                  resizeMode="cover"
                />
              ) : (
                <Image 
                  className='w-full h-full'
                  source={images.user}
                  resizeMode="cover"
                />
              )}
            </View>
          </View>

          <Text
            className="text-3xl font-['PlusJakartaSans-Bold'] mb-3"
            style={{ color: colors.text }}
          >
            {profileData.name}
          </Text>

          {/* Buttons */}
          <View className="flex-row gap-3">
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
          <Text
            className="text-lg font-['PlusJakartaSans-SemiBold'] mb-4"
            style={{ color: colors.text }}
          >
            Badges
          </Text>
          
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
                {badgeData.map((badge) => (
                  <TouchableOpacity
                    key={badge.id}
                    className="w-16 h-16 items-center justify-center"
                    onPress={() => {
                      setSelectedBadge(badge);
                      setBadgeModalVisible(true);
                    }}
                  >
                    <Image 
                      source={badge.icon}
                      className="w-16 h-16"
                      resizeMode="contain"
                      style={{ 
                        opacity: badge.earned ? 1 : 0.4
                      }}
                    />
                  </TouchableOpacity>
                ))}
              </View>
            </ScrollView>
          </View>

          {/* Your Tier */}
          <Text
            className="text-lg font-['PlusJakartaSans-SemiBold'] mb-4"
            style={{ color: colors.text }}
          >
            Your Tier
          </Text>

          <View
            className="rounded-lg p-4 mb-2"
            style={isDarkMode ? { backgroundColor: colors.card } : { backgroundColor: '#F0F7FA' }}
          >
            <View className="items-center mb-3">
              <Text
                className="text-xl font-['PlusJakartaSans-Bold']"
                style={{ color: colors.text }}
              >
                {hasTier ? `Tier ${displayTier}` : 'No Tier Yet'}
              </Text>
              <Text
                className="text-sm mt-1 font-['PlusJakartaSans-Regular']"
                style={{ color: colors.text, opacity: 0.7 }}
              >
                {'Keep contributing to advance to the next tier.'}
              </Text>
            </View>

            <View className="flex-row justify-between items-center px-4 mt-2">
              {tierIcons.map((icn, i) => {
                const active = hasTier && i <= (tierIndex as number);
                const isCurrent = hasTier && i === (tierIndex as number);
                const iconSize = isCurrent ? 62 : 40;
                const numberTop = isCurrent ? 16 : 10;
                const numberFont = isCurrent ? 24 : 12;

                return (
                  <View key={i} className="items-center">
                    <View style={{ position: 'relative' }}>
                      <Image
                        source={icn}
                        className="mb-1"
                        resizeMode="contain"
                        style={[
                          { width: iconSize, height: iconSize },
                          active ? { tintColor: colors.primary, opacity: 1 } : { opacity: 0.35 }
                        ]}
                      />
                      <Text
                        style={{
                          position: 'absolute',
                          top: numberTop,
                          left: 0,
                          right: 0,
                          textAlign: 'center',
                          fontWeight: '700',
                          fontSize: numberFont,
                          color: colors.text,
                          opacity: active ? 0.95 : 0.5
                        }}
                      >
                        {i + 1}
                      </Text>
                    </View>
                  </View>
                );
              })}
            </View>
          </View>

          {/* Optional hint */}
          <Text
            className="text-sm ml-1 mt-1 font-['PlusJakartaSans-Regular']"
            style={{ color: colors.text, opacity: 0.6, textAlign: 'center' }}
          >
            Advancing tiers unlocks better group opportunities.
          </Text>
        </View>
      </ScrollView>

      {/* Settings Modal */}
      <Modal
        animationType="slide"
        transparent={true}
        visible={settingsVisible}
        onRequestClose={() => setSettingsVisible(false)}
      >
        <View className="flex-1 justify-center items-center bg-black/50">
          <View
            className="rounded-2xl p-6 w-11/12"
            style={{ backgroundColor: colors.card }}
          >
            <View className="flex-row justify-between items-center mb-6">
              <Text className="text-2xl font-['PlusJakartaSans-Bold']" style={{ color: colors.text }}>Settings</Text>
              <TouchableOpacity onPress={() => setSettingsVisible(false)}>
                <Image 
                  source={icons.close}
                  className="w-6 h-6"
                  style={{ tintColor: isDarkMode ? '#AAA' : '#666' }}
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
                    <Text className="font-['PlusJakartaSans-SemiBold'] text-base" style={{ color: colors.text }}>Dark Mode</Text>
                    <Text className="text-sm" style={{ color: colors.text, opacity: 0.7 }}>Switch to dark theme</Text>
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
                    <Text className="font-['PlusJakartaSans-SemiBold'] text-base" style={{ color: colors.text }}>Notifications</Text>
                    <Text className="text-sm" style={{ color: colors.text, opacity: 0.7 }}>Receive app notifications</Text>
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
                  <Text className="font-['PlusJakartaSans-SemiBold'] text-base" style={{ color: colors.text }}>Help & Support</Text>
                  <Text className="text-sm" style={{ color: colors.text, opacity: 0.7 }}>Tutorial, FAQ, and contact support</Text>
                </View>
                <Image 
                  source={icons.right}
                  className="w-5 h-5"
                  style={{ tintColor: isDarkMode ? '#AAA' : '#666' }}
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
                  <Text className="font-['PlusJakartaSans-SemiBold'] text-base" style={{ color: colors.text }}>View Notifications</Text>
                  <Text className="text-sm" style={{ color: colors.text, opacity: 0.7 }}>See your notification history</Text>
                </View>
                <Image 
                  source={icons.right}
                  className="w-5 h-5"
                  style={{ tintColor: isDarkMode ? '#AAA' : '#666' }}
                />
              </TouchableOpacity>

              {/* Logout */}
              <TouchableOpacity
                className="flex-row items-center py-3"
                onPress={() =>
                  Alert.alert('Log out', 'Are you sure you want to log out?', [
                    { text: 'Cancel', style: 'cancel' },
                    { text: 'Log out', style: 'destructive', onPress: handleLogout },
                  ])
                }
              >
                <Image 
                  source={icons.close}
                  className="w-4 h-4 mr-4"
                  style={{ tintColor: '#EF4444' }}
                />
                <View className="flex-1">
                  <Text className="font-['PlusJakartaSans-SemiBold'] text-base text-red-500">
                    Log Out
                  </Text>
                  <Text className="text-sm" style={{ color: colors.text, opacity: 0.7 }}>
                    End your session and return to login
                  </Text>
                </View>
                <Image 
                  source={icons.right}
                  className="w-5 h-5"
                  style={{ tintColor: isDarkMode ? '#AAA' : '#666' }}
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

      {/* Badge Description Modal */}
      <Modal
        animationType="fade"
        transparent={true}
        visible={badgeModalVisible}
        onRequestClose={() => setBadgeModalVisible(false)}
      >
        <View className="flex-1 justify-center items-center bg-black/50">
          <View className="rounded-2xl p-6 w-11/12 max-w-sm" style={{ backgroundColor: colors.card }}>
            {selectedBadge && (
              <>
                <View className="items-center mb-4">
                  <Image 
                    source={selectedBadge.icon}
                    className="w-20 h-20 mb-3"
                    resizeMode="contain"
                  />
                  <Text className="text-xl font-['PlusJakartaSans-Bold'] text-center" style={{ color: colors.text }}>
                    {selectedBadge.name}
                  </Text>
                  {!selectedBadge.earned && (
                    <Text className="text-sm mt-1" style={{ color: colors.text, opacity: 0.6 }}>Not yet earned</Text>
                  )}
                </View>
                
                <Text className="font-['PlusJakartaSans-Regular'] text-center mb-6" style={{ color: colors.text }}>
                  {selectedBadge.description}
                </Text>
                
                <TouchableOpacity 
                  className="py-3 rounded-lg"
                  onPress={() => setBadgeModalVisible(false)}
                  style={{ backgroundColor: '#1DA1FA' }}
                >
                  <Text className="text-center text-white font-['PlusJakartaSans-Medium']">
                    Close
                  </Text>
                </TouchableOpacity>
              </>
            )}
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  )
}

export default profile