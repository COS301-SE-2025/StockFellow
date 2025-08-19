// src/services/NotificationService.ts
import authService from './authService';

export interface Notification {
  id?: number;
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
  retryCount?: number;
  metadata?: Record<string, any> | string | null;
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
  private readonly baseUrl = '/api/notifications';

  // Get all notifications for the authenticated user
  async getUserNotifications(): Promise<NotificationResponse> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/user`, {
        method: 'GET',
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error || errorData.message || 'Failed to fetch notifications');
      }

      const data = await response.json();
      
      // Ensure metadata is parsed if it's a string
      if (data.notifications) {
        data.notifications = data.notifications.map((notification: Notification) => ({
          ...notification,
          metadata: this.parseMetadata(notification.metadata)
        }));
      }

      return data;
    } catch (error) {
      console.error('Error fetching user notifications:', error);
      throw error;
    }
  }

  // Get unread notifications for the authenticated user
  async getUnreadNotifications(): Promise<NotificationResponse> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/user/unread`, {
        method: 'GET',
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error || errorData.message || 'Failed to fetch unread notifications');
      }

      const data = await response.json();
      
      // Ensure metadata is parsed if it's a string
      if (data.notifications) {
        data.notifications = data.notifications.map((notification: Notification) => ({
          ...notification,
          metadata: this.parseMetadata(notification.metadata)
        }));
      }

      return data;
    } catch (error) {
      console.error('Error fetching unread notifications:', error);
      throw error;
    }
  }

  // Get unread notification count
  async getUnreadCount(): Promise<UnreadCountResponse> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/user/count`, {
        method: 'GET',
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error || errorData.message || 'Failed to fetch unread count');
      }

      return await response.json();
    } catch (error) {
      console.error('Error fetching unread count:', error);
      // Return default count instead of throwing to prevent UI breaks
      return { unreadCount: 0, userId: '' };
    }
  }

  // Mark a specific notification as read
  async markAsRead(notificationId: string): Promise<{ message: string; notificationId: string }> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/${notificationId}/read`, {
        method: 'PUT',
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error || errorData.message || 'Failed to mark notification as read');
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
      const response = await authService.apiRequest(`${this.baseUrl}/user/read-all`, {
        method: 'PUT',
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error || errorData.message || 'Failed to mark all notifications as read');
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
      const response = await authService.apiRequest(`${this.baseUrl}/${notificationId}`, {
        method: 'GET',
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error || errorData.message || 'Failed to fetch notification');
      }

      const notification = await response.json();
      
      // Ensure metadata is parsed if it's a string
      notification.metadata = this.parseMetadata(notification.metadata);

      return notification;
    } catch (error) {
      console.error('Error fetching notification:', error);
      throw error;
    }
  }

  // Send a notification (admin function)
  async sendNotification(request: SendNotificationRequest): Promise<SendNotificationResponse> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/send`, {
        method: 'POST',
        body: JSON.stringify(request),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error || errorData.message || 'Failed to send notification');
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
      const response = await authService.apiRequest(`${this.baseUrl}/bulk`, {
        method: 'POST',
        body: JSON.stringify(request),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error || errorData.message || 'Failed to send bulk notifications');
      }

      return await response.json();
    } catch (error) {
      console.error('Error sending bulk notifications:', error);
      throw error;
    }
  }

  // Helper method to parse metadata
  private parseMetadata(metadata: any): Record<string, any> | null {
    if (!metadata) return null;
    
    if (typeof metadata === 'string') {
      try {
        return JSON.parse(metadata);
      } catch (error) {
        console.warn('Failed to parse notification metadata:', metadata);
        return null;
      }
    }
    
    return metadata;
  }

  // Test connection to notification service (useful for debugging)
  async testConnection(): Promise<{ status: string; message: string }> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/health`, {
        method: 'GET',
      });

      if (!response.ok) {
        throw new Error('Service health check failed');
      }

      return await response.json();
    } catch (error) {
      console.error('Notification service connection test failed:', error);
      throw error;
    }
  }
}

export default new NotificationService();