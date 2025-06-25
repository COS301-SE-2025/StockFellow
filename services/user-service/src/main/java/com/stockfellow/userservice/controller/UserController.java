package com.stockfellow.userservice.controller;

import com.stockfellow.userservice.dto.RegisterUserRequest;
import com.stockfellow.userservice.model.User;
import com.stockfellow.userservice.service.ReadModelService;
import com.stockfellow.userservice.service.RegisterUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private ReadModelService readModelService;

    @Autowired
    private RegisterUserService registerUserService;

    @GetMapping
    public ResponseEntity<?> getServiceInfo() {
        try {
            return ResponseEntity.ok(Map.of(
                "service", "User Service",
                "version", "1.0.0",
                "endpoints", List.of(
                    "GET /api/users/profile - Get user profile (requires auth)",
                    "POST /api/users/register - Register new user (requires Keycloak token)",
                    "GET /api/users/:id - Get user by ID (requires auth)"
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            // Mocked userId since jwtMiddleware is commented out
            String userId = "e20f93e2-d283-4100-a5fa-92c61d85b4f4";
            User user = readModelService.getUser(userId);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        try {
            User user = readModelService.getUser(id);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterUserRequest request) {
        try {
            if (request.getName() == null || request.getEmail() == null || 
                request.getSaId() == null || request.getMobileNumber() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
            }

            // Mocked userId since jwtMiddleware is commented out
            String userId = "e20f93e2-d283-4100-a5fa-92c61d85b4f4";
            Map<String, Object> event = registerUserService.execute(userId, request);

            return ResponseEntity.status(201).body(Map.of(
                "message", "User registered successfully",
                "userId", userId,
                "eventId", event.get("_id")
            ));
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}