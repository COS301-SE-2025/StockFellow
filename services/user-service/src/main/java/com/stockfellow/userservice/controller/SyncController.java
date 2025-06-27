package com.stockfellow.userservice.controller;

import com.stockfellow.userservice.dto.UserSyncRequest;
import com.stockfellow.userservice.model.Event;
import com.stockfellow.userservice.model.User;
import com.stockfellow.userservice.service.EventStoreService;
import com.stockfellow.userservice.service.ReadModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sync")
public class SyncController {
    private static final Logger logger = LoggerFactory.getLogger(SyncController.class);

    @Autowired
    private EventStoreService eventStoreService;

    @Autowired
    private ReadModelService readModelService;

    @PostMapping
    public ResponseEntity<?> syncUser(@RequestBody UserSyncRequest request) {
        logger.info("Keycloak sync request: {}", request);
        System.out.println("=== KEYCLOAK SYNC REQUEST ===");
        System.out.println("Body: " + request);

        try {
            if (request.getKeycloakId() == null || request.getUsername() == null || request.getEmail() == null ||
                request.getFirstName() == null || request.getLastName() == null || request.getIdNumber() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
            }

            User existingUser = readModelService.getUser(request.getKeycloakId());
            Event event;
            String eventType;
            String message;

            if (existingUser != null) {
                logger.info("Updating existing user: {}", request.getKeycloakId());
                eventType = "UserUpdated";
                Map<String, Object> eventData = new HashMap<>();
                eventData.put("userId", request.getKeycloakId());
                eventData.put("username", request.getUsername() != null ? request.getUsername() : existingUser.getUsername());
                eventData.put("email", request.getEmail() != null ? request.getEmail() : existingUser.getEmail());
                eventData.put("firstName", request.getFirstName() != null ? request.getFirstName() : existingUser.getFirstName());
                eventData.put("lastName", request.getLastName() != null ? request.getLastName() : existingUser.getLastName());
                eventData.put("emailVerified", request.getEmailVerified() != null ? request.getEmailVerified() : existingUser.isEmailVerified());
                eventData.put("contactNumber", request.getPhoneNumber() != null ? request.getPhoneNumber() : existingUser.getContactNumber());
                eventData.put("updatedAt", new Date());
                event = eventStoreService.appendEvent(eventType, eventData);
                message = "User updated successfully";
                logger.info("User {} updated successfully", request.getKeycloakId());
            } else {
                logger.info("Creating new user: {}", request.getKeycloakId());
                eventType = "UserRegistered";
                Map<String, Object> eventData = new HashMap<>();
                eventData.put("userId", request.getKeycloakId());
                eventData.put("username", request.getUsername() != null ? request.getUsername() : "");
                eventData.put("email", request.getEmail());
                eventData.put("firstName", request.getFirstName() != null ? request.getFirstName() : "");
                eventData.put("lastName", request.getLastName() != null ? request.getLastName() : "");
                eventData.put("emailVerified", request.getEmailVerified() != null ? request.getEmailVerified() : false);
                eventData.put("contactNumber", request.getPhoneNumber() != null ? request.getPhoneNumber() : "");
                eventData.put("idNumber", request.getIdNumber() != null ? request.getIdNumber() : "");
                eventData.put("createdAt", new Date());
                eventData.put("updatedAt", new Date());
                event = eventStoreService.appendEvent(eventType, eventData);
                message = "User registered successfully";
                logger.info("User {} registered successfully", request.getKeycloakId());
            }

            readModelService.rebuildState(request.getKeycloakId());
            System.out.println("User sync successful for: " + request.getEmail());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", message,
                "userId", request.getKeycloakId(),
                "eventId", event.getId(),
                "eventType", eventType
            ));
        } catch (Exception e) {
            logger.error("User sync failed: {}", e.getMessage(), e);
            System.out.println("User sync error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Failed to sync user",
                "details", e.getMessage()
            ));
        }
    }
}