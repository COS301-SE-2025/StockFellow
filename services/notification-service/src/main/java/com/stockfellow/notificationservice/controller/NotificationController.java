package com.stockfellow.notificationservice.controller;

import com.stockfellow.notificationservice.model.Notification;
import com.stockfellow.notificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public Map<String, Object> getServiceInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Notification Service");
        response.put("version", "1.0.0");
        response.put("endpoints", List.of(
                "POST /api/notifications/system - Send system notification",
                "POST /api/notifications/group - Send group notification",
                "POST /api/notifications/transaction - Send transaction notification",
                "GET /api/notifications/user - Get user notifications"
        ));
        return response;
    }

    @PostMapping("/system")
    public ResponseEntity<?> sendSystemNotification(@RequestBody Map<String, Object> request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getPrincipal() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required"));
            }
            String title = (String) request.get("title");
            String message = (String) request.get("message");
            String channel = (String) request.get("channel");

            if (title == null || message == null || channel == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
            }
            if (!List.of("IN_APP", "EMAIL", "PUSH").contains(channel)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid channel"));
            }

            notificationService.sendSystemNotification(title, message, channel);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "System notification sent"));
        } catch (Exception e) {
            logger.error("Error sending system notification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/group")
    public ResponseEntity<?> sendGroupNotification(@RequestBody Map<String, Object> request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getPrincipal() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required"));
            }
            String groupId = (String) request.get("groupId");
            String title = (String) request.get("title");
            String message = (String) request.get("message");
            String channel = (String) request.get("channel");

            if (groupId == null || title == null || message == null || channel == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
            }
            if (!List.of("IN_APP", "EMAIL", "PUSH").contains(channel)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid channel"));
            }

            notificationService.sendGroupNotification(groupId, title, message, channel);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Group notification sent"));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid group notification: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error sending group notification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/transaction")
    public ResponseEntity<?> sendTransactionNotification(@RequestBody Map<String, Object> request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getPrincipal() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required"));
            }
            String recipientId = (String) request.get("recipientId");
            String groupId = (String) request.get("groupId");
            String transactionId = (String) request.get("transactionId");
            String title = (String) request.get("title");
            String message = (String) request.get("message");
            String channel = (String) request.get("channel");

            if (recipientId == null || transactionId == null || title == null || message == null || channel == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
            }
            if (!List.of("IN_APP", "EMAIL", "PUSH").contains(channel)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid channel"));
            }

            notificationService.sendTransactionNotification(recipientId, groupId, transactionId, title, message, channel);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Transaction notification sent"));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid transaction notification: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error sending transaction notification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserNotifications() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getPrincipal() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required"));
            }
            String userId = auth.getPrincipal().toString();

            List<Notification> notifications = notificationService.getUserNotifications(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error fetching user notifications: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }
}
