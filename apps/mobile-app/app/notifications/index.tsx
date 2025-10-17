// app/notifications/index.tsx
import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, ScrollView, RefreshControl, Alert, ActivityIndicator } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useFocusEffect } from '@react-navigation/native';
import TopBar from '../../src/components/TopBar';
import NotificationItem from '../../src/components/NotificationItem';
import NotificationService, { Notification } from '../../src/services/notificationService';

const formatTimeAgo = (dateString: string) => {
  const date = new Date(dateString);
  const now = new Date();
  const seconds = Math.floor((now.getTime() - date.getTime()) / 1000);
  
  if (seconds < 60) return `${seconds}s`;
  if (seconds < 3600) return `${Math.floor(seconds / 60)}m`;
  if (seconds < 86400) return `${Math.floor(seconds / 3600)}h`;
  if (seconds < 604800) return `${Math.floor(seconds / 86400)}d`;
  if (seconds < 2592000) return `${Math.floor(seconds / 604800)}w`;
  return `${Math.floor(seconds / 2592000)}mo`;
};

const groupNotificationsByTime = (notifications: Notification[]) => {
  const now = new Date();
  const today = new Date(now.setHours(0, 0, 0, 0));
  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);
  const last7Days = new Date(today);
  last7Days.setDate(last7Days.getDate() - 7);
  const last30Days = new Date(today);
  last30Days.setDate(last30Days.getDate() - 30);

  const groups = {
    today: [] as Notification[],
    yesterday: [] as Notification[],
    last7Days: [] as Notification[],
    last30Days: [] as Notification[],
    older: [] as Notification[],
  };

  notifications.forEach(notification => {
    const createdAt = new Date(notification.createdAt);
    
    if (createdAt >= today) {
      groups.today.push(notification);
    } else if (createdAt >= yesterday) {
      groups.yesterday.push(notification);
    } else if (createdAt >= last7Days) {
      groups.last7Days.push(notification);
    } else if (createdAt >= last30Days) {
      groups.last30Days.push(notification);
    } else {
      groups.older.push(notification);
    }
  });

  return groups;
};

const Notifications = () => {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Load notifications from API
  const loadNotifications = async (showLoading = true) => {
    try {
      if (showLoading) setLoading(true);
      setError(null);
      
      const response = await NotificationService.getUserNotifications();
      setNotifications(response.notifications);
    } catch (error) {
      console.error('Failed to load notifications:', error);
      setError(error instanceof Error ? error.message : 'Failed to load notifications');
      Alert.alert('Error', 'Failed to load notifications. Please try again.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  // Load notifications when screen is focused
  useFocusEffect(
    useCallback(() => {
      loadNotifications();
    }, [])
  );

  // Pull-to-refresh handler
  const onRefresh = () => {
    setRefreshing(true);
    loadNotifications(false);
  };

  // Mark notification as read
  const markAsRead = async (notificationId: string) => {
    try {
      await NotificationService.markAsRead(notificationId);
      
      // Update local state
      setNotifications(prev => 
        prev.map(notif => 
          notif.notificationId === notificationId 
            ? { ...notif, readStatus: true, readAt: new Date().toISOString() } 
            : notif
        )
      );
    } catch (error) {
      console.error('Failed to mark notification as read:', error);
      Alert.alert('Error', 'Failed to mark notification as read');
    }
  };

  // Mark all notifications as read
  const markAllAsRead = async () => {
    try {
      await NotificationService.markAllAsRead();
      
      // Update local state
      setNotifications(prev => 
        prev.map(notif => ({ 
          ...notif, 
          readStatus: true, 
          readAt: notif.readAt || new Date().toISOString() 
        }))
      );
      
      Alert.alert('Success', 'All notifications marked as read');
    } catch (error) {
      console.error('Failed to mark all notifications as read:', error);
      Alert.alert('Error', 'Failed to mark all notifications as read');
    }
  };

  if (loading) {
    return (
      <SafeAreaView className="flex-1 bg-white">
        <TopBar title="Notifications" showBackButton />
        <View className="flex-1 justify-center items-center">
          <ActivityIndicator size="large" color="#0066CC" />
          <Text className="mt-4 text-gray-600 font-['PlusJakartaSans-Regular']">
            Loading notifications...
          </Text>
        </View>
      </SafeAreaView>
    );
  }

  if (error && notifications.length === 0) {
    return (
      <SafeAreaView className="flex-1 bg-white">
        <TopBar title="Notifications" showBackButton />
        <View className="flex-1 justify-center items-center px-8">
          <Text className="text-red-500 text-center font-['PlusJakartaSans-Medium'] mb-4">
            {error}
          </Text>
          <Text 
            className="text-blue-500 font-['PlusJakartaSans-Medium']"
            onPress={() => loadNotifications()}
          >
            Tap to retry
          </Text>
        </View>
      </SafeAreaView>
    );
  }

  const groupedNotifications = groupNotificationsByTime(notifications);
  const hasNotifications = notifications.length > 0;
  const hasUnreadNotifications = notifications.some(n => !n.readStatus);

  return (
    <SafeAreaView className="flex-1 bg-white">
      <TopBar 
        title="Notifications" 
        showBackButton 
        rightComponent={
          hasUnreadNotifications ? (
            <Text 
              className="text-blue-500 font-['PlusJakartaSans-Medium'] mr-4"
              onPress={markAllAsRead}
            >
              Mark All Read
            </Text>
          ) : undefined
        }
      />
      
      {!hasNotifications ? (
        <View className="flex-1 justify-center items-center px-8">
          <Text className="text-gray-500 text-center font-['PlusJakartaSans-Medium'] text-lg mb-2">
            No notifications yet
          </Text>
          <Text className="text-gray-400 text-center font-['PlusJakartaSans-Regular']">
            You'll see your notifications here when you receive them
          </Text>
        </View>
      ) : (
        <ScrollView 
          className="flex-1 mx-4"
          refreshControl={
            <RefreshControl 
              refreshing={refreshing} 
              onRefresh={onRefresh}
              colors={['#0066CC']}
            />
          }
        >
          {groupedNotifications.today.length > 0 && (
            <View>
              <Text className="px-4 py-2 font-['PlusJakartaSans-SemiBold'] text-gray-700">
                Today
              </Text>
              {groupedNotifications.today.map(notification => (
                <NotificationItem
                  key={notification.notificationId}
                  type={notification.type}
                  title={notification.title}
                  message={notification.message}
                  timeAgo={formatTimeAgo(notification.createdAt)}
                  readStatus={notification.readStatus}
                  onPress={() => markAsRead(notification.notificationId)}
                />
              ))}
            </View>
          )}

          {groupedNotifications.yesterday.length > 0 && (
            <View>
              <Text className="px-4 py-2 font-['PlusJakartaSans-SemiBold'] text-gray-700">
                Yesterday
              </Text>
              {groupedNotifications.yesterday.map(notification => (
                <NotificationItem
                  key={notification.notificationId}
                  type={notification.type}
                  title={notification.title}
                  message={notification.message}
                  timeAgo={formatTimeAgo(notification.createdAt)}
                  readStatus={notification.readStatus}
                  onPress={() => markAsRead(notification.notificationId)}
                />
              ))}
            </View>
          )}

          {groupedNotifications.last7Days.length > 0 && (
            <View>
              <Text className="px-4 py-2 font-['PlusJakartaSans-SemiBold'] text-gray-700">
                Last 7 Days
              </Text>
              {groupedNotifications.last7Days.map(notification => (
                <NotificationItem
                  key={notification.notificationId}
                  type={notification.type}
                  title={notification.title}
                  message={notification.message}
                  timeAgo={formatTimeAgo(notification.createdAt)}
                  readStatus={notification.readStatus}
                  onPress={() => markAsRead(notification.notificationId)}
                />
              ))}
            </View>
          )}

          {groupedNotifications.last30Days.length > 0 && (
            <View>
              <Text className="px-4 py-2 font-['PlusJakartaSans-SemiBold'] text-gray-700">
                Last 30 Days
              </Text>
              {groupedNotifications.last30Days.map(notification => (
                <NotificationItem
                  key={notification.notificationId}
                  type={notification.type}
                  title={notification.title}
                  message={notification.message}
                  timeAgo={formatTimeAgo(notification.createdAt)}
                  readStatus={notification.readStatus}
                  onPress={() => markAsRead(notification.notificationId)}
                />
              ))}
            </View>
          )}

          {groupedNotifications.older.length > 0 && (
            <View>
              <Text className="px-4 py-2 font-['PlusJakartaSans-SemiBold'] text-gray-700">
                Older Notifications
              </Text>
              {groupedNotifications.older.map(notification => (
                <NotificationItem
                  key={notification.notificationId}
                  type={notification.type}
                  title={notification.title}
                  message={notification.message}
                  timeAgo={formatTimeAgo(notification.createdAt)}
                  readStatus={notification.readStatus}
                  onPress={() => markAsRead(notification.notificationId)}
                />
              ))}
            </View>
          )}
        </ScrollView>
      )}
    </SafeAreaView>
  );
};

export default Notifications;