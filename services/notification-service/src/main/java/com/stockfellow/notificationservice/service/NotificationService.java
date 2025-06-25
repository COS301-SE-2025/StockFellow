package com.stockfellow.notificationservice.service;

import com.stockfellow.notificationservice.model.Notification;
import com.stockfellow.notificationservice.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@EnableScheduling
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private JmsTemplate jmsTemplate;
    
    @Autowired
    private GroupServiceClient groupServiceClient;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // Queue names
    private static final String NOTIFICATION_QUEUE = "notification.queue";
    private static final String EMAIL_QUEUE = "email.queue";
    private static final String SMS_QUEUE = "sms.queue";
    private static final String PUSH_QUEUE = "push.queue";
    
    /**
     * Send notification to queue for processing
     */
    public String sendNotification(String userId, String groupId, String type, String title, 
                                 String message, String channel, String priority, Map<String, Object> metadata) {
        try {
            String notificationId = "notif_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
            
            Notification notification = new Notification(notificationId, userId, groupId, type, title, message, channel, priority);
            if (metadata != null) {
                notification.setMetadata(objectMapper.writeValueAsString(metadata));
            }
            
            // Save to database
            notificationRepository.save(notification);
            
            // Send to queue for processing
            Map<String, Object> notificationData = Map.of(
                "notificationId", notificationId,
                "userId", userId,
                "groupId", groupId != null ? groupId : "",
                "type", type,
                "title", title,
                "message", message,
                "channel", channel,
                "priority", priority,
                "metadata", metadata != null ? metadata : Map.of()
            );
            
            jmsTemplate.convertAndSend(NOTIFICATION_QUEUE, notificationData);
            logger.info("Notification queued: {}", notificationId);
            
            return notificationId;
        } catch (Exception e) {
            logger.error("Error sending notification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send notification: " + e.getMessage());
        }
    }
    
    /**
     * Listen for notifications from the main queue and route to appropriate channels
     */
    @JmsListener(destination = NOTIFICATION_QUEUE)
    public void processNotification(Map<String, Object> notificationData) {
        String notificationId = (String) notificationData.get("notificationId");
        String channel = (String) notificationData.get("channel");
        
        try {
            logger.info("Processing notification: {} for channel: {}", notificationId, channel);
            
            // Update status to processing
            updateNotificationStatus(notificationId, "PROCESSING");
            
            // Route to appropriate channel queue
            switch (channel.toUpperCase()) {
                case "EMAIL":
                    jmsTemplate.convertAndSend(EMAIL_QUEUE, notificationData);
                    break;
                case "SMS":
                    jmsTemplate.convertAndSend(SMS_QUEUE, notificationData);
                    break;
                case "PUSH":
                    jmsTemplate.convertAndSend(PUSH_QUEUE, notificationData);
                    break;
                case "IN_APP":
                    // For in-app notifications, just mark as sent since they're stored in DB
                    updateNotificationStatus(notificationId, "SENT");
                    updateNotificationSentTime(notificationId);
                    break;
                default:
                    logger.warn("Unknown notification channel: {}", channel);
                    updateNotificationStatus(notificationId, "FAILED");
            }
            
        } catch (Exception e) {
            logger.error("Error processing notification {}: {}", notificationId, e.getMessage(), e);
            updateNotificationStatus(notificationId, "FAILED");
            incrementRetryCount(notificationId);
        }
    }
    
    /**
     * Process email notifications
     */
    @JmsListener(destination = EMAIL_QUEUE)
    public void processEmailNotification(Map<String, Object> notificationData) {
        String notificationId = (String) notificationData.get("notificationId");
        
        try {
            logger.info("Sending email notification: {}", notificationId);
            
            // Simulate email sending (integrate with actual email service)
            String userId = (String) notificationData.get("userId");
            String title = (String) notificationData.get("title");
            String message = (String) notificationData.get("message");
            
            // TODO: Integrate with actual email service (SendGrid, AWS SES, etc.)
            boolean emailSent = sendEmail(userId, title, message);
            
            if (emailSent) {
                updateNotificationStatus(notificationId, "SENT");
                updateNotificationSentTime(notificationId);
                logger.info("Email notification sent successfully: {}", notificationId);
            } else {
                updateNotificationStatus(notificationId, "FAILED");
                incrementRetryCount(notificationId);
                logger.error("Failed to send email notification: {}", notificationId);
            }
            
        } catch (Exception e) {
            logger.error("Error processing email notification {}: {}", notificationId, e.getMessage(), e);
            updateNotificationStatus(notificationId, "FAILED");
            incrementRetryCount(notificationId);
        }
    }
    
    /**
     * Process SMS notifications
     */
    @JmsListener(destination = SMS_QUEUE)
    public void processSmsNotification(Map<String, Object> notificationData) {
        String notificationId = (String) notificationData.get("notificationId");
        
        try {
            logger.info("Sending SMS notification: {}", notificationId);
            
            // Simulate SMS sending (integrate with actual SMS service)
            String userId = (String) notificationData.get("userId");
            String message = (String) notificationData.get("message");
            
            // TODO: Integrate with actual SMS service (Twilio, AWS SNS, etc.)
            boolean smsSent = sendSms(userId, message);
            
            if (smsSent) {
                updateNotificationStatus(notificationId, "SENT");
                updateNotificationSentTime(notificationId);
                logger.info("SMS notification sent successfully: {}", notificationId);
            } else {
                updateNotificationStatus(notificationId, "FAILED");
                incrementRetryCount(notificationId);
                logger.error("Failed to send SMS notification: {}", notificationId);
            }
            
        } catch (Exception e) {
            logger.error("Error processing SMS notification {}: {}", notificationId, e.getMessage(), e);
            updateNotificationStatus(notificationId, "FAILED");
            incrementRetryCount(notificationId);
        }
    }
    
    /**
     * Process push notifications
     */
    @JmsListener(destination = PUSH_QUEUE)
    public void processPushNotification(Map<String, Object> notificationData) {
        String notificationId = (String) notificationData.get("notificationId");
        
        try {
            logger.info("Sending push notification: {}", notificationId);
            
            // Simulate push notification sending (integrate with FCM, APNS, etc.)
            String userId = (String) notificationData.get("userId");
            String title = (String) notificationData.get("title");
            String message = (String) notificationData.get("message");
            
            // TODO: Integrate with actual push notification service (FCM, APNS, etc.)
            boolean pushSent = sendPushNotification(userId, title, message);
            
            if (pushSent) {
                updateNotificationStatus(notificationId, "SENT");
                updateNotificationSentTime(notificationId);
                logger.info("Push notification sent successfully: {}", notificationId);
            } else {
                updateNotificationStatus(notificationId, "FAILED");
                incrementRetryCount(notificationId);
                logger.error("Failed to send push notification: {}", notificationId);
            }
            
        } catch (Exception e) {
            logger.error("Error processing push notification {}: {}", notificationId, e.getMessage(), e);
            updateNotificationStatus(notificationId, "FAILED");
            incrementRetryCount(notificationId);
        }
    }
    
    /**
     * Get notifications for a user
     */
    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Get unread notifications for a user
     */
    public List<Notification> getUnreadNotifications(String userId) {
        return notificationRepository.findByUserIdAndReadStatusOrderByCreatedAtDesc(userId, false);
    }
    
    /**
     * Mark notification as read
     */
    @Transactional
    public boolean markAsRead(String notificationId, String userId) {
        try {
            Notification notification = notificationRepository.findByNotificationId(notificationId)
                    .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
            
            if (!notification.getUserId().equals(userId)) {
                throw new IllegalArgumentException("Unauthorized access to notification");
            }
            
            notification.setReadStatus(true);
            notificationRepository.save(notification);
            return true;
        } catch (Exception e) {
            logger.error("Error marking notification as read: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get unread count for user
     */
    public Long getUnreadCount(String userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }
    
    /**
     * Retry failed notifications (scheduled task)
     */
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void retryFailedNotifications() {
        List<Notification> failedNotifications = notificationRepository.findFailedNotificationsForRetry();
        
        for (Notification notification : failedNotifications) {
            try {
                logger.info("Retrying failed notification: {}", notification.getNotificationId());
                
                Map<String, Object> notificationData = Map.of(
                    "notificationId", notification.getNotificationId(),
                    "userId", notification.getUserId(),
                    "groupId", notification.getGroupId() != null ? notification.getGroupId() : "",
                    "type", notification.getType(),
                    "title", notification.getTitle(),
                    "message", notification.getMessage(),
                    "channel", notification.getChannel(),
                    "priority", notification.getPriority(),
                    "metadata", notification.getMetadata() != null ? 
                        objectMapper.readValue(notification.getMetadata(), Map.class) : Map.of()
                );
                
                jmsTemplate.convertAndSend(NOTIFICATION_QUEUE, notificationData);
                
            } catch (Exception e) {
                logger.error("Error retrying notification {}: {}", notification.getNotificationId(), e.getMessage());
            }
        }
    }
    
    // Helper methods for external service integration (to be implemented)
    private boolean sendEmail(String userId, String title, String message) {
        // TODO: Implement actual email sending logic
        logger.info("Simulating email send to user: {} with title: {}", userId, title);
        return true; // Simulate success
    }
    
    private boolean sendSms(String userId, String message) {
        // TODO: Implement actual SMS sending logic
        logger.info("Simulating SMS send to user: {} with message: {}", userId, message);
        return true; // Simulate success
    }
    
    private boolean sendPushNotification(String userId, String title, String message) {
        // TODO: Implement actual push notification logic
        logger.info("Simulating push notification to user: {} with title: {}", userId, title);
        return true; // Simulate success
    }
    
    // Database update helpers
    @Transactional
    private void updateNotificationStatus(String notificationId, String status) {
        notificationRepository.findByNotificationId(notificationId)
                .ifPresent(notification -> {
                    notification.setStatus(status);
                    notificationRepository.save(notification);
                });
    }
    
    @Transactional
    private void updateNotificationSentTime(String notificationId) {
        notificationRepository.findByNotificationId(notificationId)
                .ifPresent(notification -> {
                    notification.setSentAt(LocalDateTime.now());
                    notificationRepository.save(notification);
                });
    }
    
    @Transactional
    private void incrementRetryCount(String notificationId) {
        notificationRepository.findByNotificationId(notificationId)
                .ifPresent(notification -> {
                    notification.setRetryCount(notification.getRetryCount() + 1);
                    notificationRepository.save(notification);
                });
    }
}