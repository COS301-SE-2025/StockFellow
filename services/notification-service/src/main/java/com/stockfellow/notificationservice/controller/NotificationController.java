package com.stockfellow.notificationservice.controller;

import com.stockfellow.notificationservice.model.Notification;
import com.stockfellow.notificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    
    @Autowired
    private NotificationService notificationService;
    
    @GetMapping
    public Map<String, Object> getServiceInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Notification Service");
        response.put("version", "1.0.0");
        response.put("endpoints", Arrays.asList(
                "POST /api/notifications/send - Send a notification",
                "GET /api/notifications/user - Get user notifications",
                "GET /api/notifications/user/unread - Get unread notifications",
                "PUT /api/notifications/{notificationId}/read - Mark notification as read",
                "GET /api/notifications/user/count - Get unread count"
        ));
        return response;
    }
    
    /**
     * Send a notification
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            // Extract required fields
            String userId = (String) request.get("userId");
            String groupId = (String) request.get("groupId");
            String type = (String) request.get("type");
            String title = (String) request.get("title");
            String message = (String) request.get("message");
            String channel = (String) request.get("channel");
            String priority = (String) request.getOrDefault("priority", "NORMAL");
            Map<String, Object> metadata = (Map<String, Object>) request.get("metadata");
            
            // Validate required fields
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User ID is required"));
            }
            if (type == null || type.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Notification type is required"));
            }
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Title is required"));
            }
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Message is required"));
            }
            if (channel == null || channel.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Channel is required"));
            }
            
            // Validate channel
            if (!Arrays.asList("EMAIL", "SMS", "PUSH", "IN_APP").contains(channel.toUpperCase())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid channel. Must be: EMAIL, SMS, PUSH, or IN_APP"));
            }
            
            // Validate priority
            if (!Arrays.asList("LOW", "NORMAL", "HIGH", "URGENT").contains(priority.toUpperCase())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid priority. Must be: LOW, NORMAL, HIGH, or URGENT"));
            }
            
            // Validate type
            if (!Arrays.asList("GROUP_INVITE", "GROUP_JOIN", "PAYMENT_DUE", "PAYMENT_RECEIVED", 
                              "PAYOUT_READY", "SYSTEM_UPDATE", "REMINDER", "WELCOME").contains(type.toUpperCase())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid notification type"));
            }
            
            String notificationId = notificationService.sendNotification(
                userId, groupId, type.toUpperCase(), title, message, 
                channel.toUpperCase(), priority.toUpperCase(), metadata
            );
            
            logger.info("Notification sent successfully: {}", notificationId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Notification sent successfully");
            response.put("notificationId", notificationId);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            logger.error("Error sending notification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to send notification: " + e.getMessage()));
        }
    }
    
    /**
     * Get notifications for authenticated user
     */
    @GetMapping("/user")
    public ResponseEntity<?> getUserNotifications(HttpServletRequest httpRequest) {
        try {
            String userId = httpRequest.getHeader("X-User-Id");
            String username = httpRequest.getHeader("X-User-Name");
            
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User ID not found in request"));
            }
            
            List<Notification> notifications = notificationService.getUserNotifications(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("notifications", notifications);
            response.put("count", notifications.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching user notifications: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Get unread notifications for authenticated user
     */
    @GetMapping("/user/unread")
    public ResponseEntity<?> getUnreadNotifications(HttpServletRequest httpRequest) {
        try {
            String userId = httpRequest.getHeader("X-User-Id");
            String username = httpRequest.getHeader("X-User-Name");
            
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User ID not found in request"));
            }
            
            List<Notification> unreadNotifications = notificationService.getUnreadNotifications(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("notifications", unreadNotifications);
            response.put("count", unreadNotifications.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching unread notifications: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Mark notification as read
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable String notificationId, HttpServletRequest httpRequest) {
        try {
            String userId = httpRequest.getHeader("X-User-Id");
            String username = httpRequest.getHeader("X-User-Name");
            
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User ID not found in request"));
            }
            
            boolean success = notificationService.markAsRead(notificationId, userId);
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Notification marked as read");
                response.put("notificationId", notificationId);
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Failed to mark notification as read"));
            }
            
        } catch (IllegalArgumentException e) {
            logger.error("Error marking notification as read: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error marking notification as read: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Get unread notification count for user
     */
    @GetMapping("/user/count")
    public ResponseEntity<?> getUnreadCount(HttpServletRequest httpRequest) {
        try {
            String userId = httpRequest.getHeader("X-User-Id");
            String username = httpRequest.getHeader("X-User-Name");
            
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User ID not found in request"));
            }
            
            Long unreadCount = notificationService.getUnreadCount(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("unreadCount", unreadCount);
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching unread count: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Mark all notifications as read for user
     */
    @PutMapping("/user/read-all")
    public ResponseEntity<?> markAllAsRead(HttpServletRequest httpRequest) {
        try {
            String userId = httpRequest.getHeader("X-User-Id");
            String username = httpRequest.getHeader("X-User-Name");
            
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User ID not found in request"));
            }
            
            List<Notification> unreadNotifications = notificationService.getUnreadNotifications(userId);
            int markedCount = 0;
            
            for (Notification notification : unreadNotifications) {
                if (notificationService.markAsRead(notification.getNotificationId(), userId)) {
                    markedCount++;
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "All notifications marked as read");
            response.put("markedCount", markedCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error marking all notifications as read: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Send bulk notifications (admin endpoint)
     */
    @PostMapping("/bulk")
    public ResponseEntity<?> sendBulkNotifications(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            String adminUserId = httpRequest.getHeader("X-User-Id");
            
            if (adminUserId == null || adminUserId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User ID not found in request"));
            }
            
            List<String> userIds = (List<String>) request.get("userIds");
            String type = (String) request.get("type");
            String title = (String) request.get("title");
            String message = (String) request.get("message");
            String channel = (String) request.get("channel");
            String priority = (String) request.getOrDefault("priority", "NORMAL");
            String groupId = (String) request.get("groupId");
            Map<String, Object> metadata = (Map<String, Object>) request.get("metadata");
            
            // Validate required fields
            if (userIds == null || userIds.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User IDs list is required"));
            }
            if (type == null || title == null || message == null || channel == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Type, title, message, and channel are required"));
            }
            
            List<String> notificationIds = new java.util.ArrayList<>();
            int successCount = 0;
            int failureCount = 0;
            
            for (String userId : userIds) {
                try {
                    String notificationId = notificationService.sendNotification(
                        userId, groupId, type.toUpperCase(), title, message, 
                        channel.toUpperCase(), priority.toUpperCase(), metadata
                    );
                    notificationIds.add(notificationId);
                    successCount++;
                } catch (Exception e) {
                    logger.error("Failed to send notification to user {}: {}", userId, e.getMessage());
                    failureCount++;
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Bulk notifications processed");
            response.put("totalRequested", userIds.size());
            response.put("successCount", successCount);
            response.put("failureCount", failureCount);
            response.put("notificationIds", notificationIds);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error sending bulk notifications: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to send bulk notifications"));
        }
    }
    
    /**
     * Get notification by ID (for authenticated user)
     */
    @GetMapping("/{notificationId}")
    public ResponseEntity<?> getNotification(@PathVariable String notificationId, HttpServletRequest httpRequest) {
        try {
            String userId = httpRequest.getHeader("X-User-Id");
            
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User ID not found in request"));
            }
            
            List<Notification> userNotifications = notificationService.getUserNotifications(userId);
            Notification notification = userNotifications.stream()
                    .filter(n -> n.getNotificationId().equals(notificationId))
                    .findFirst()
                    .orElse(null);
            
            if (notification == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Notification not found"));
            }
            
            return ResponseEntity.ok(notification);
            
        } catch (Exception e) {
            logger.error("Error fetching notification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }
}