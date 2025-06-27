package com.stockfellow.userservice.controller;

import com.stockfellow.userservice.model.User;
import com.stockfellow.userservice.service.ReadModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private ReadModelService readModelService;

    @GetMapping("/login")
    public ResponseEntity<?> login(HttpServletRequest request, HttpSession session) {
        try {
            // Extract user ID from gateway headers
            String userId = request.getHeader("X-User-Id");
            String username = request.getHeader("X-User-Name");
            
            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            
            User user = readModelService.getUser(userId);

            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found in database"));
            }

            return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "user", Map.of(
                    "userId", userId,
                    "username", username != null ? username : user.getUsername(),
                    "email", user.getEmail(),
                    "name", user.getFirstName() + " " + user.getLastName(),
                    "saId", user.getIdNumber() != null ? user.getIdNumber() : "",
                    "mobileNumber", user.getContactNumber() != null ? user.getContactNumber() : ""
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}