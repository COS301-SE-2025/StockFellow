// Update the notifications.tsx page
import React, { useState } from 'react';
import { View, Text, ScrollView } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import TopBar from '../../src/components/TopBar';
import NotificationItem from '../../src/components/NotificationItem';

// Mock data based on your schema
const mockNotifications = [
  {
    id: '1',
    notificationId: 'notif-001',
    userId: 'user-123',
    groupId: 'group-456',
    type: 'payment',
    title: 'Payment Received',
    message: 'You have received R250.00 from John Doe',
    status: 'DELIVERED',
    channel: 'APP',
    priority: 'NORMAL',
    readStatus: false,
    createdAt: new Date(Date.now() - 30000), // 30 seconds ago
    sentAt: new Date(Date.now() - 30000),
  },
  {
    id: '2',
    notificationId: 'notif-003',
    userId: 'user-123',
    groupId: null,
    type: 'alert',
    title: 'System Maintenance',
    message: 'There will be scheduled maintenance tonight at 2AM',
    status: 'DELIVERED',
    channel: 'APP',
    priority: 'NORMAL',
    readStatus: true,
    createdAt: new Date(Date.now() - 86400000), // 1 day ago
    sentAt: new Date(Date.now() - 86400000),
  },
  {
    id: '3',
    notificationId: 'notif-004',
    userId: 'user-123',
    groupId: 'group-789',
    type: 'reminder',
    title: 'Contribution Due',
    message: 'Your monthly contribution of R200 is due tomorrow',
    status: 'DELIVERED',
    channel: 'APP',
    priority: 'NORMAL',
    readStatus: true,
    createdAt: new Date(Date.now() - 172800000), // 2 days ago
    sentAt: new Date(Date.now() - 172800000),
  },
  {
    id: '4',
    notificationId: 'notif-005',
    userId: 'user-123',
    groupId: null,
    type: 'payment',
    title: 'Withdrawal Processed',
    message: 'Your withdrawal of R500 has been processed',
    status: 'DELIVERED',
    channel: 'APP',
    priority: 'NORMAL',
    readStatus: true,
    createdAt: new Date(Date.now() - 604800000), // 1 week ago
    sentAt: new Date(Date.now() - 604800000),
  },
];

const formatTimeAgo = (date: Date) => {
  const now = new Date();
  const seconds = Math.floor((now.getTime() - date.getTime()) / 1000);
  
  if (seconds < 60) return `${seconds}s`;
  if (seconds < 3600) return `${Math.floor(seconds / 60)}m`;
  if (seconds < 86400) return `${Math.floor(seconds / 3600)}h`;
  if (seconds < 604800) return `${Math.floor(seconds / 86400)}d`;
  if (seconds < 2592000) return `${Math.floor(seconds / 604800)}w`;
  return `${Math.floor(seconds / 2592000)}mo`;
};

const groupNotificationsByTime = (notifications: typeof mockNotifications) => {
  const now = new Date();
  const today = new Date(now.setHours(0, 0, 0, 0));
  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);
  const last7Days = new Date(today);
  last7Days.setDate(last7Days.getDate() - 7);
  const last30Days = new Date(today);
  last30Days.setDate(last30Days.getDate() - 30);

  const groups = {
    today: [] as typeof mockNotifications,
    yesterday: [] as typeof mockNotifications,
    last7Days: [] as typeof mockNotifications,
    last30Days: [] as typeof mockNotifications,
    older: [] as typeof mockNotifications,
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
  const [notifications, setNotifications] = useState(mockNotifications);
  const groupedNotifications = groupNotificationsByTime(notifications);

  const markAsRead = (id: string) => {
    setNotifications(prev => 
      prev.map(notif => 
        notif.id === id ? { ...notif, readStatus: true } : notif
      )
    );
  };

  return (
    <SafeAreaView className="flex-1 bg-white">
      <TopBar title="Notifications" showBackButton />
      
      <ScrollView className="flex-1 mx-4">
        {groupedNotifications.today.length > 0 && (
          <View>
            <Text className="px-4 py-2 font-['PlusJakartaSans-SemiBold']">Today</Text>
            {groupedNotifications.today.map(notification => (
              <NotificationItem
                key={notification.id}
                type={notification.type}
                title={notification.title}
                message={notification.message}
                timeAgo={formatTimeAgo(new Date(notification.createdAt))}
                readStatus={notification.readStatus}
                onPress={() => markAsRead(notification.id)}
              />
            ))}
          </View>
        )}

        {groupedNotifications.yesterday.length > 0 && (
          <View>
            <Text className="px-4 py-2 font-['PlusJakartaSans-SemiBold']">Yesterday</Text>
            {groupedNotifications.yesterday.map(notification => (
              <NotificationItem
                key={notification.id}
                type={notification.type}
                title={notification.title}
                message={notification.message}
                timeAgo={formatTimeAgo(new Date(notification.createdAt))}
                readStatus={notification.readStatus}
                onPress={() => markAsRead(notification.id)}
              />
            ))}
          </View>
        )}

        {groupedNotifications.last7Days.length > 0 && (
          <View>
            <Text className="px-4 py-2 font-['PlusJakartaSans-SemiBold']">Last 7 Days</Text>
            {groupedNotifications.last7Days.map(notification => (
              <NotificationItem
                key={notification.id}
                type={notification.type}
                title={notification.title}
                message={notification.message}
                timeAgo={formatTimeAgo(new Date(notification.createdAt))}
                readStatus={notification.readStatus}
                onPress={() => markAsRead(notification.id)}
              />
            ))}
          </View>
        )}

        {groupedNotifications.last30Days.length > 0 && (
          <View>
            <Text className="px-4 py-2 font-['PlusJakartaSans-SemiBold']">Last 30 Days</Text>
            {groupedNotifications.last30Days.map(notification => (
              <NotificationItem
                key={notification.id}
                type={notification.type}
                title={notification.title}
                message={notification.message}
                timeAgo={formatTimeAgo(new Date(notification.createdAt))}
                readStatus={notification.readStatus}
                onPress={() => markAsRead(notification.id)}
              />
            ))}
          </View>
        )}

        {groupedNotifications.older.length > 0 && (
          <View>
            <Text className="px-4 py-2 font-['PlusJakartaSans-SemiBold']">Older Notifications</Text>
            {groupedNotifications.older.map(notification => (
              <NotificationItem
                key={notification.id}
                type={notification.type}
                title={notification.title}
                message={notification.message}
                timeAgo={formatTimeAgo(new Date(notification.createdAt))}
                readStatus={notification.readStatus}
                onPress={() => markAsRead(notification.id)}
              />
            ))}
          </View>
        )}
      </ScrollView>
    </SafeAreaView>
  );
};

export default Notifications;