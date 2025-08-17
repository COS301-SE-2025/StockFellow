package com.stockfellow.userservice.controller;

import com.stockfellow.userservice.model.User;
import com.stockfellow.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public ResponseEntity<?> login(HttpServletRequest request, HttpSession session) {
        try {
            // Extract user ID from gateway headers
            String userId = request.getHeader("X-User-Id");
            String username = request.getHeader("X-User-Name");
            
            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            
            User user = userService.getUserByUserId(userId);

            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found in database"));
            }

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userId", userId);
            userMap.put("username", username != null ? username : user.getUsername());
            userMap.put("email", user.getEmail());
            userMap.put("name", (user.getFirstName() != null ? user.getFirstName() : "") + " " + (user.getLastName() != null ? user.getLastName() : ""));
            userMap.put("saId", user.getIdNumber() != null ? user.getIdNumber() : "");
            userMap.put("mobileNumber", user.getContactNumber() != null ? user.getContactNumber() : "");
            userMap.put("idVerified", user.isIdVerified());
            userMap.put("emailVerified", user.isEmailVerified());
            userMap.put("profileComplete", isProfileComplete(user));
            userMap.put("dateOfBirth", user.getDateOfBirth() != null ? user.getDateOfBirth() : "");
            userMap.put("gender", user.getGender() != null ? user.getGender() : "");
            userMap.put("citizenship", user.getCitizenship() != null ? user.getCitizenship() : "");

            return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "user", userMap
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        try {
            session.invalidate();
            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
        }
    }
    
    private boolean isProfileComplete(User user) {
        return user.getFirstName() != null && !user.getFirstName().trim().isEmpty() &&
               user.getLastName() != null && !user.getLastName().trim().isEmpty() &&
               user.getContactNumber() != null && !user.getContactNumber().trim().isEmpty();
    }
}