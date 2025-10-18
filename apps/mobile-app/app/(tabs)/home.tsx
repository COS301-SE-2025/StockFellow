import React, { useEffect, useState, useCallback } from 'react';
import { View, Text, ScrollView, ActivityIndicator, TouchableOpacity, Image, RefreshControl } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { StatusBar } from 'expo-status-bar';
import { useTheme } from '../_layout';
import { useFocusEffect } from '@react-navigation/native';
import SavingsCard from '../../src/components/SavingsCard';
import QuickActions from '../../src/components/QuickActions';
import ActivityItem from '../../src/components/ActivityItem';
import TopBar from '../../src/components/TopBar';
import GreetingCard from '../../src/components/GreetingCard';
import userService from '../../src/services/userService';
import groupService from '../../src/services/groupService';
import notificationService, { Notification } from '../../src/services/notificationService';
import { useNavigation } from '@react-navigation/native';
import icons from '../../src/constants/icons';
import NotificationItem from '../../src/components/NotificationItem';
import { router } from 'expo-router';

const Home = () => {
  const { colors, isDarkMode } = useTheme();
  const navigation = useNavigation<any>();

  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [profile, setProfile] = useState<any>(null);
  const [userGroups, setUserGroups] = useState<any[]>([]);
  const [selectedGroup, setSelectedGroup] = useState<any>(null);
  const [notifications, setNotifications] = useState<Notification[]>([]);

  const fetchData = useCallback(async (isRefresh = false) => {
    try {
      if (isRefresh) {
        setRefreshing(true);
      } else {
        setLoading(true);
      }

      // Fetch all data in parallel for better performance
      const [profileData, groupsData, notifRes] = await Promise.all([
        userService.getProfile(),
        groupService.getUserGroups(),
        notificationService.getUserNotifications()
      ]);

      setProfile(profileData);
      setUserGroups(groupsData);

      if (groupsData.length > 0) {
        const groupId = groupsData[0].groupId || groupsData[0].id;
        const groupDetail = await groupService.viewGroup(groupId);
        setSelectedGroup(groupDetail.group);
      } else {
        setSelectedGroup(null);
      }

      let notificationsList = notifRes.notifications || [];

      // Add mock notifications for testing display

      setNotifications(notificationsList);
    } catch (error) {
      console.error('Error fetching home data:', error);
      setProfile(null);
      setUserGroups([]);
      setSelectedGroup(null);
      setNotifications([]);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  // Initial data fetch
  useEffect(() => {
    fetchData();
  }, []);

  // Refresh data when screen comes into focus (when user switches tabs)
  useFocusEffect(
    useCallback(() => {
      // Only refresh if we have initial data (avoid double loading on first mount)
      if (profile || userGroups.length > 0 || notifications.length > 0) {
        fetchData(true);
      }
    }, [])
  );

//   // Optional: Auto-refresh every few minutes when screen is focused
//   useEffect(() => {
//     const interval = setInterval(() => {
//       // Only auto-refresh if the screen is likely focused and we have data
//       if (!loading && !refreshing) {
//         fetchData(true);
//       }
//     }, 3 * 60 * 1000); // Refresh every 3 minutes

//     return () => clearInterval(interval);
//   }, [loading, refreshing, fetchData]);

  const greetingUserName = profile?.user?.firstName || profile?.user?.username || 'User';
  const notInGroup = userGroups.length === 0;

  function formatDate(dateStr?: string) {
    if (!dateStr || dateStr === 'No groups yet' || dateStr === 'N/A') return dateStr || '';
    const date = new Date(dateStr);
    if (isNaN(date.getTime())) return dateStr;
    return date.toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  }

  const nextContributionDate = notInGroup
    ? 'No groups yet'
    : formatDate(selectedGroup?.contributionDate);

  const nextPayoutDateRaw = notInGroup
    ? undefined
    : selectedGroup?.payoutDate;

  const nextPayoutDate = notInGroup
    ? 'No groups yet'
    : formatDate(nextPayoutDateRaw);

  const contributionAmount = notInGroup
    ? '0.00'
    : selectedGroup?.minContribution?.toFixed(2) || '0.00';

  // Calculate days until next payout
  let daysUntilPayout = 0;
  if (!notInGroup && nextPayoutDateRaw) {
    const now = new Date();
    const payoutDate = new Date(nextPayoutDateRaw);
    const diff = payoutDate.getTime() - now.getTime();
    daysUntilPayout = diff > 0 ? Math.ceil(diff / (1000 * 60 * 60 * 24)) : 0;
  }

  // Show only the 3 most recent notifications
  const recentNotifications = notifications.slice(0, 3);

  // Pull-to-refresh handler
  const onRefresh = useCallback(() => {
    fetchData(true);
  }, []);

  return (
    <SafeAreaView style={{ backgroundColor: colors.background }} className="flex-1">
      <StatusBar style={isDarkMode ? 'light' : 'dark'} />
      <TopBar title="Home" />

      <ScrollView 
        className="flex-1 px-6"
        style={{ backgroundColor: colors.background }}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={onRefresh}
            colors={[colors.primary]}
            tintColor={colors.primary}
          />
        }
      >
        {loading ? (
          <View className="mt-10 items-center">
            <ActivityIndicator size="large" color={colors.primary} />
          </View>
        ) : (
          <>
            {/* Greeting */}
            <View className="mt-6">
              <GreetingCard userName={greetingUserName} />
            </View>

            {/* Savings Card */}
            <View style={{ marginBottom: 4 }}>
              <SavingsCard 
                userTier={profile?.affordability?.tierName || 'Unanalyzed'}
                nextContributionDate={nextContributionDate}
                contributionAmount={contributionAmount}
                nextPayoutDate={nextPayoutDate}
              />
            </View>

            {/* Quick Actions */}
            <View style={{ marginBottom: 4, marginTop: 4 }}>
              <QuickActions 
                contributionsLeft={notInGroup ? 0 : 3} 
                totalContributions={notInGroup ? 0 : 12} 
                daysUntilPayout={notInGroup ? 0 : daysUntilPayout}
                nextPayoutDate={notInGroup ? 'N/A' : nextPayoutDate}
              />
            </View>

            {/* Recent Activity from Notifications */}
            <View>
              <View className="flex-row justify-between items-center mb-3">
                <Text className="text-lg font-['PlusJakartaSans-SemiBold']" style={{ color: colors.text }}>
                  Recent Activity
                </Text>
                <TouchableOpacity onPress={() => navigation.navigate('Notifications')}>
                  <Text 
                  className="text-m font-['PlusJakartaSans-Medium'] text-[#1DA1FA]"
                  onPress={() => router.push('/notifications')}
>
                    View More...
                  </Text>
                </TouchableOpacity>
              </View>

              {recentNotifications.length === 0 ? (
                <View
                  style={{
                    backgroundColor: colors.card,
                    borderRadius: 14,
                    padding: 24,
                    alignItems: 'center',
                    marginVertical: 16,
                  }}
                >
                  <Text
                    style={{
                      color: colors.text,
                      opacity: 0.7,
                      fontSize: 16,
                      textAlign: 'center',
                      fontFamily: 'PlusJakartaSans-SemiBold',
                      marginBottom: 8,
                    }}
                  >
                    No activity yet
                  </Text>
                  <Text
                    style={{
                      color: colors.text,
                      opacity: 0.5,
                      fontSize: 14,
                      textAlign: 'center',
                      fontFamily: 'PlusJakartaSans-Regular',
                    }}
                  >
                    Join a group or take action to see your notifications here.
                  </Text>
                </View>
              ) : (
                recentNotifications.map((notif, idx) => (
                  <NotificationItem
                    key={notif.notificationId || idx}
                    type={notif.type}
                    title={notif.title}
                    message={notif.message}
                    timeAgo={formatDate(notif.createdAt)}
                    readStatus={notif.readStatus}
                    onPress={() => navigation.navigate('Notifications')}
                  />
                ))
              )}
            </View>
          </>
        )}
      </ScrollView>
    </SafeAreaView>
  );
};

export default Home;