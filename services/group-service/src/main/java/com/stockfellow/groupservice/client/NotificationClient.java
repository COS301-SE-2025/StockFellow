package com.stockfellow.groupservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reusable client for sending notifications to the Notification Service
 * Copy this class to each microservice that needs to send notifications
 */
@Component
public class NotificationClient {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationClient.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${notification.service.url:http://notification-service:4050}")
    private String notificationServiceUrl;
    
    public NotificationClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Send a single notification
     */
    public void sendNotification(NotificationRequest request) {
        try {
            String url = notificationServiceUrl + "/api/notifications/send";
            
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("userId", request.getUserId());
            notificationData.put("type", request.getType());
            notificationData.put("title", request.getTitle());
            notificationData.put("message", request.getMessage());
            notificationData.put("channel", request.getChannel());
            notificationData.put("priority", request.getPriority());
            
            if (request.getGroupId() != null) {
                notificationData.put("groupId", request.getGroupId());
            }
            if (request.getMetadata() != null) {
                notificationData.put("metadata", request.getMetadata());
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(notificationData, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Notification sent successfully: {} to user {}", request.getType(), request.getUserId());
            } else {
                logger.error("Failed to send notification. Status: {}", response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error sending notification to user {}: {}", request.getUserId(), e.getMessage());
            // Don't throw - notification failure shouldn't break business logic
        }
    }
    
    /**
     * Send bulk notifications to multiple users
     */
    public void sendBulkNotifications(List<String> userIds, String type, String title, 
                                     String message, String channel, String priority, 
                                     String groupId, Map<String, Object> metadata) {
        try {
            String url = notificationServiceUrl + "/api/notifications/bulk";
            
            Map<String, Object> bulkRequest = new HashMap<>();
            bulkRequest.put("userIds", userIds);
            bulkRequest.put("type", type);
            bulkRequest.put("title", title);
            bulkRequest.put("message", message);
            bulkRequest.put("channel", channel);
            bulkRequest.put("priority", priority);
            
            if (groupId != null) {
                bulkRequest.put("groupId", groupId);
            }
            if (metadata != null) {
                bulkRequest.put("metadata", metadata);
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(bulkRequest, headers);
            
            restTemplate.postForEntity(url, entity, Map.class);
            logger.info("Bulk notifications sent to {} users", userIds.size());
            
        } catch (Exception e) {
            logger.error("Error sending bulk notifications: {}", e.getMessage());
        }
    }
    
    /**
     * Builder class for creating notification requests
     */
    public static class NotificationRequest {
        private String userId;
        private String groupId;
        private String type;
        private String title;
        private String message;
        private String channel = "IN_APP";
        private String priority = "NORMAL";
        private Map<String, Object> metadata;
        
        public NotificationRequest userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public NotificationRequest groupId(String groupId) {
            this.groupId = groupId;
            return this;
        }
        
        public NotificationRequest type(String type) {
            this.type = type;
            return this;
        }
        
        public NotificationRequest title(String title) {
            this.title = title;
            return this;
        }
        
        public NotificationRequest message(String message) {
            this.message = message;
            return this;
        }
        
        public NotificationRequest channel(String channel) {
            this.channel = channel;
            return this;
        }
        
        public NotificationRequest priority(String priority) {
            this.priority = priority;
            return this;
        }
        
        public NotificationRequest metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }
        
        // Getters
        public String getUserId() { return userId; }
        public String getGroupId() { return groupId; }
        public String getType() { return type; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public String getChannel() { return channel; }
        public String getPriority() { return priority; }
        public Map<String, Object> getMetadata() { return metadata; }
    }
}
