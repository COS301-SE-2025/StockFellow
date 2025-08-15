package com.stockfellow.userservice.controller;

import com.stockfellow.userservice.dto.UserSyncRequest;
import com.stockfellow.userservice.model.User;
import com.stockfellow.userservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sync")
@Validated
public class SyncController {
    
    private static final Logger logger = LoggerFactory.getLogger(SyncController.class);

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> syncUser(@Valid @RequestBody UserSyncRequest request) {
        logger.info("Keycloak sync request: {}", request);
        
        try {
            // Validate required fields
            if (request.getKeycloakId() == null || request.getKeycloakId().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Keycloak ID is required"));
            }
            
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }

            // Create or update user
            User user = userService.createOrUpdateUser(request);
            boolean isNewUser = user.getCreatedAt().equals(user.getUpdatedAt());
            
            String message = isNewUser ? "User created successfully" : "User updated successfully";
            
            logger.info("User sync successful for: {} ({})", request.getEmail(), 
                       isNewUser ? "new" : "updated");
            
           Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("userId", user.getUserId());
            userData.put("email", user.getEmail());
            userData.put("username", user.getUsername());
            userData.put("firstName", user.getFirstName() != null ? user.getFirstName() : "");
            userData.put("lastName", user.getLastName() != null ? user.getLastName() : "");
            userData.put("emailVerified", user.isEmailVerified());
            userData.put("idVerified", user.isIdVerified());
            userData.put("contactNumber", user.getContactNumber() != null ? user.getContactNumber() : "");
            userData.put("createdAt", user.getCreatedAt());
            userData.put("updatedAt", user.getUpdatedAt());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", message,
                "userId", request.getKeycloakId(),
                "user", userData
            ));
            
        } catch (Exception e) {
            logger.error("User sync failed for request: {}, Error: {}", request, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Failed to sync user",
                "details", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/status/{userId}")
    public ResponseEntity<?> getSyncStatus(@PathVariable String userId) {
        try {
            User user = userService.getUserByUserId(userId);
            
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "User not found",
                    "userId", userId
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "userId", userId,
                "syncStatus", Map.of(
                    "exists", true,
                    "emailVerified", user.isEmailVerified(),
                    "idVerified", user.isIdVerified(),
                    "profileComplete", isProfileComplete(user),
                    "lastUpdated", user.getUpdatedAt()
                )
            ));
            
        } catch (Exception e) {
            logger.error("Error getting sync status for user: {}", userId, e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to get sync status",
                "userId", userId
            ));
        }
    }
    
    private boolean isProfileComplete(User user) {
        return user.getFirstName() != null && !user.getFirstName().trim().isEmpty() &&
               user.getLastName() != null && !user.getLastName().trim().isEmpty() &&
               user.getContactNumber() != null && !user.getContactNumber().trim().isEmpty();
    }
}