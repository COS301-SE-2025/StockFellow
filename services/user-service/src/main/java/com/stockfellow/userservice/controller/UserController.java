package com.stockfellow.userservice.controller;

import com.stockfellow.userservice.dto.RegisterUserRequest;
import com.stockfellow.userservice.model.User;
import com.stockfellow.userservice.service.ReadModelService;
import com.stockfellow.userservice.service.RegisterUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
                    "POST /api/users/verifyID - Verify user ID ",
                    "GET /api/users/:id - Get user by ID (requires auth)"
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        try {
            // Extract user ID from gateway headers
            String userId = request.getHeader("X-User-Id");
            String username = request.getHeader("X-User-Name");
            
            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            
            User user = readModelService.getUser(userId);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }
            
            // Optionally enhance user object with additional context
            if (username != null && !username.equals(user.getUsername())) {
                // Log discrepancy if needed
                System.out.println("Username from token: " + username + ", from DB: " + user.getUsername());
            }
            
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id, HttpServletRequest request) {
        try {
            // Extract requesting user ID from gateway headers for authorization
            String requestingUserId = request.getHeader("X-User-Id");
            String userRoles = request.getHeader("X-User-Roles");
            
            if (requestingUserId == null || requestingUserId.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            
            // Check if user is requesting their own data or has admin role
            boolean isOwnData = id.equals(requestingUserId);
            boolean isAdmin = userRoles != null && userRoles.contains("admin");
            
            if (!isOwnData && !isAdmin) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }
            
            User user = readModelService.getUser(id);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/verifyID")
    public ResponseEntity<?> verfiyID(@RequestBody RegisterUserRequest request, HttpServletRequest httpRequest) {
      // get pdf file from request
      // verify ID with smile ID or idfy 
      // after verification add user to document to alfersco 
    }
}