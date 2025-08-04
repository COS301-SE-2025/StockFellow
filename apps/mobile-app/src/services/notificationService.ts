// src/services/NotificationService.ts
import authService from './authService';

export interface Notification {
  notificationId: string;
  userId: string;
  groupId?: string;
  type: string;
  title: string;
  message: string;
  status: string;
  channel: string;
  priority: string;
  readStatus: boolean;
  createdAt: string;
  sentAt: string;
  readAt?: string;
  metadata?: Record<string, any>;
}

export interface NotificationResponse {
  notifications: Notification[];
  count: number;
}

export interface UnreadCountResponse {
  unreadCount: number;
  userId: string;
}

export interface SendNotificationRequest {
  userId: string;
  groupId?: string;
  type: string;
  title: string;
  message: string;
  channel: string;
  priority?: string;
  metadata?: Record<string, any>;
}

export interface SendNotificationResponse {
  message: string;
  notificationId: string;
}

class NotificationService {
  // Get all notifications for the authenticated user
  async getUserNotifications(): Promise<NotificationResponse> {
    try {
      const response = await authService.apiRequest('/notifications/user', {
        method: 'GET',
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to fetch notifications');
      }

      return await response.json();
    } catch (error) {
      console.error('Error fetching user notifications:', error);
      throw error;
    }
  }

  // Get unread notifications for the authenticated user
  async getUnreadNotifications(): Promise<NotificationResponse> {
    try {
      const response = await authService.apiRequest('/notifications/user/unread', {
        method: 'GET',
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to fetch unread notifications');
      }

      return await response.json();
    } catch (error) {
      console.error('Error fetching unread notifications:', error);
      throw error;
    }
  }

  // Get unread notification count
  async getUnreadCount(): Promise<UnreadCountResponse> {
    try {
      const response = await authService.apiRequest('/notifications/user/count', {
        method: 'GET',
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to fetch unread count');
      }

      return await response.json();
    } catch (error) {
      console.error('Error fetching unread count:', error);
      throw error;
    }
  }

  // Mark a specific notification as read
  async markAsRead(notificationId: string): Promise<{ message: string; notificationId: string }> {
    try {
      const response = await authService.apiRequest(`/notifications/${notificationId}/read`, {
        method: 'PUT',
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to mark notification as read');
      }

      return await response.json();
    } catch (error) {
      console.error('Error marking notification as read:', error);
      throw error;
    }
  }

  // Mark all notifications as read
  async markAllAsRead(): Promise<{ message: string; markedCount: number }> {
    try {
      const response = await authService.apiRequest('/notifications/user/read-all', {
        method: 'PUT',
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to mark all notifications as read');
      }

      return await response.json();
    } catch (error) {
      console.error('Error marking all notifications as read:', error);
      throw error;
    }
  }

  // Get a specific notification by ID
  async getNotification(notificationId: string): Promise<Notification> {
    try {
      const response = await authService.apiRequest(`/notifications/${notificationId}`, {
        method: 'GET',
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to fetch notification');
      }

      return await response.json();
    } catch (error) {
      console.error('Error fetching notification:', error);
      throw error;
    }
  }

  // Send a notification (admin function)
  async sendNotification(request: SendNotificationRequest): Promise<SendNotificationResponse> {
    try {
      const response = await authService.apiRequest('/notifications/send', {
        method: 'POST',
        body: JSON.stringify(request),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to send notification');
      }

      return await response.json();
    } catch (error) {
      console.error('Error sending notification:', error);
      throw error;
    }
  }

  // Send bulk notifications (admin function)
  async sendBulkNotifications(request: {
    userIds: string[];
    type: string;
    title: string;
    message: string;
    channel: string;
    priority?: string;
    groupId?: string;
    metadata?: Record<string, any>;
  }): Promise<{
    message: string;
    totalRequested: number;
    successCount: number;
    failureCount: number;
    notificationIds: string[];
  }> {
    try {
      const response = await authService.apiRequest('/notifications/bulk', {
        method: 'POST',
        body: JSON.stringify(request),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to send bulk notifications');
      }

      return await response.json();
    } catch (error) {
      console.error('Error sending bulk notifications:', error);
      throw error;
    }
  }
}

export default new NotificationService();