package com.stockfellow.userservice.controller;

import com.stockfellow.userservice.model.User;
import com.stockfellow.userservice.service.UserService;
import com.stockfellow.userservice.service.SouthAfricanIdValidationService;
import com.stockfellow.userservice.service.PdfIdExtractionService;
import com.stockfellow.userservice.service.AlfrescoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SouthAfricanIdValidationService idValidationService;
    
    @Autowired
    private PdfIdExtractionService pdfExtractionService;
    
    @Autowired
    private AlfrescoService alfrescoService;

    @GetMapping
    public ResponseEntity<?> getServiceInfo() {
        try {
            return ResponseEntity.ok(Map.of(
                "service", "User Service",
                "version", "2.0.0",
                "database", "PostgreSQL",
                "endpoints", List.of(
                    "GET /api/users/profile - Get user profile (requires auth)",
                    "POST /api/users/verifyID - Verify user ID document",
                    "GET /api/users/{id} - Get user by ID (requires auth)",
                    "GET /api/users/search?name={name} - Search users by name",
                    "GET /api/users/verified - Get verified users",
                    "GET /api/users/stats - Get user statistics"
                )
            ));
        } catch (Exception e) {
            logger.error("Error getting service info", e);
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        try {
            String userId = request.getHeader("X-User-Id");
            String username = request.getHeader("X-User-Name");
            
            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            
            User user = userService.getUserByUserId(userId);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }
            
            if (username != null && !username.equals(user.getUsername())) {
                logger.warn("Username from token: {} differs from DB: {}", username, user.getUsername());
            }
            
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Error getting user profile", e);
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id, HttpServletRequest request) {
        try {
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
            
            User user = userService.getUserByUserId(id);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Error getting user by ID", e);
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String name, HttpServletRequest request) {
        try {
            String userRoles = request.getHeader("X-User-Roles");
            if (userRoles == null || !userRoles.contains("admin")) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
            }
            
            List<User> users = userService.searchUsersByName(name);
            return ResponseEntity.ok(Map.of(
                "users", users,
                "count", users.size()
            ));
        } catch (Exception e) {
            logger.error("Error searching users", e);
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/verified")
    public ResponseEntity<?> getVerifiedUsers(HttpServletRequest request) {
        try {
            String userRoles = request.getHeader("X-User-Roles");
            if (userRoles == null || !userRoles.contains("admin")) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
            }
            
            List<User> verifiedUsers = userService.getVerifiedUsers();
            return ResponseEntity.ok(Map.of(
                "verifiedUsers", verifiedUsers,
                "count", verifiedUsers.size()
            ));
        } catch (Exception e) {
            logger.error("Error getting verified users", e);
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats(HttpServletRequest request) {
        try {
            String userRoles = request.getHeader("X-User-Roles");
            if (userRoles == null || !userRoles.contains("admin")) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
            }
            
            List<User> allUsers = userService.getAllUsers();
            List<User> verifiedUsers = userService.getVerifiedUsers();
            List<User> incompleteUsers = userService.getUsersWithIncompleteProfiles();
            
            return ResponseEntity.ok(Map.of(
                "totalUsers", allUsers.size(),
                "verifiedUsers", verifiedUsers.size(),
                "unverifiedUsers", allUsers.size() - verifiedUsers.size(),
                "incompleteProfiles", incompleteUsers.size(),
                "verificationRate", allUsers.size() > 0 ? 
                    Math.round((double) verifiedUsers.size() / allUsers.size() * 100) : 0
            ));
        } catch (Exception e) {
            logger.error("Error getting user stats", e);
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/verifyID")
    public ResponseEntity<?> verifyID(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) String userId,
            HttpServletRequest httpRequest) {
        
        try {
            if (userId == null || userId.isEmpty()) {
                userId = httpRequest.getHeader("X-User-Id");
            }
            
            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "User not authenticated",
                    "message", "User ID is required for ID verification"
                ));
            }
            
            // Validate file
            if (file == null || file.isEmpty()) {
                return ResponseEntity.status(400).body(Map.of(
                    "error", "Invalid file",
                    "message", "PDF file is required for ID verification"
                ));
            }
            
            String contentType = file.getContentType();
            if (contentType == null || !contentType.equals("application/pdf")) {
                return ResponseEntity.status(400).body(Map.of(
                    "error", "Invalid file type",
                    "message", "Only PDF files are accepted for ID verification"
                ));
            }
            
            // Check if user exists
            User user = userService.getUserByUserId(userId);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "User not found",
                    "message", "User must be registered before ID verification"
                ));
            }
            
            // Check if user is already verified
            if (user.isIdVerified()) {
                return ResponseEntity.status(409).body(Map.of(
                    "error", "Already verified",
                    "message", "User ID is already verified",
                    "verifiedAt", user.getUpdatedAt()
                ));
            }
            
            logger.info("Starting ID verification process for user: {}", userId);
            
            // Extract ID number from PDF
            String extractedIdNumber;
            try {
                extractedIdNumber = pdfExtractionService.extractIdNumberFromPdf(file);
            } catch (Exception e) {
                logger.error("Error extracting ID from PDF for user: {}", userId, e);
                return ResponseEntity.status(400).body(Map.of(
                    "error", "PDF processing failed",
                    "message", "Could not extract text from the provided PDF"
                ));
            }
            
            if (extractedIdNumber == null) {
                return ResponseEntity.status(400).body(Map.of(
                    "error", "ID not found",
                    "message", "Could not extract a valid South African ID number from the document"
                ));
            }
            
            logger.info("Extracted ID number for user {}: {}", userId, 
                       extractedIdNumber.substring(0, 6) + "XXXXXXX");
            
            // Check if ID number is already used by another user
            User existingUser = userService.getUserByIdNumber(extractedIdNumber);
            if (existingUser != null && !existingUser.getUserId().equals(userId)) {
                return ResponseEntity.status(409).body(Map.of(
                    "error", "ID already registered",
                    "message", "This ID number is already registered to another user"
                ));
            }
            
            // Validate ID number using Luhn algorithm
            boolean isValidId = idValidationService.validateSouthAfricanId(extractedIdNumber);
            if (!isValidId) {
                return ResponseEntity.status(400).body(Map.of(
                    "error", "Invalid ID number",
                    "message", "The extracted ID number is not a valid South African ID"
                ));
            }
            
            // Extract additional information from valid ID
            SouthAfricanIdValidationService.SouthAfricanIdInfo idInfo = 
                idValidationService.extractIdInfo(extractedIdNumber);
            
            // Store document in Alfresco
            String documentId;
            try {
                documentId = alfrescoService.uploadDocument(file, userId, "ID_VERIFICATION");
                logger.info("Document uploaded to Alfresco with ID: {} for user: {}", documentId, userId);
            } catch (Exception e) {
                logger.error("Error uploading document to Alfresco for user: {}", userId, e);
                return ResponseEntity.status(500).body(Map.of(
                    "error", "Document storage failed",
                    "message", "ID verification successful but document storage failed"
                ));
            }
            
            // Update user record with verification status
            User updatedUser = userService.updateIdVerificationStatus(userId, extractedIdNumber, documentId, idInfo);
            
            if (updatedUser == null) {
                return ResponseEntity.status(500).body(Map.of(
                    "error", "Update failed",
                    "message", "Could not update user verification status"
                ));
            }
            
            // Return success response with extracted information
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "ID verification completed successfully",
                "idNumber", extractedIdNumber,
                "extractedInfo", Map.of(
                    "dateOfBirth", idInfo.getDateOfBirth(),
                    "gender", idInfo.getGender(),
                    "citizenship", idInfo.getCitizenship()
                ),
                "documentId", documentId,
                "verificationTimestamp", System.currentTimeMillis(),
                "user", Map.of(
                    "id", updatedUser.getId(),
                    "userId", updatedUser.getUserId(),
                    "email", updatedUser.getEmail(),
                    "idVerified", updatedUser.isIdVerified(),
                    "updatedAt", updatedUser.getUpdatedAt()
                )
            ));
            
        } catch (Exception e) {
            logger.error("Unexpected error during ID verification for user: {}", 
                        httpRequest.getHeader("X-User-Id"), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Verification failed",
                "message", "An unexpected error occurred during ID verification"
            ));
        }
    }
}