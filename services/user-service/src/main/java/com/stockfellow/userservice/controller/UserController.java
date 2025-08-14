package com.stockfellow.userservice.controller;

import com.stockfellow.userservice.model.User;
import com.stockfellow.userservice.service.UserService;
import com.stockfellow.userservice.service.SouthAfricanIdValidationService;
import com.stockfellow.userservice.service.PdfIdExtractionService;
import com.stockfellow.userservice.service.AlfrescoService;
import com.stockfellow.userservice.service.AffordabilityTierService;
import com.stockfellow.userservice.dto.AffordabilityTierResult;
import com.stockfellow.userservice.dto.BankStatementUploadRequest;
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
@CrossOrigin(origins = "*")
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
    
    @Autowired
    private AffordabilityTierService affordabilityTierService;

    @GetMapping
    public ResponseEntity<?> getServiceInfo() {
        try {
            return ResponseEntity.ok(Map.of(
                "service", "User Service",
                "version", "2.1.0",
                "database", "PostgreSQL",
                "endpoints", List.of(
                    "GET /api/users/profile - Get user profile (requires auth)",
                    "POST /api/users/verifyID - Verify user ID document",
                    "POST /api/users/affordability/analyze - Analyze user affordability",
                    "GET /api/users/{id} - Get user by ID (requires auth)",
                    "GET /api/users/{id}/affordability - Get user affordability tier",
                    "GET /api/users/search?name={name} - Search users by name",
                    "GET /api/users/verified - Get verified users",
                    "GET /api/users/stats - Get user statistics",
                    "GET /api/users/affordability/stats - Get affordability statistics"
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
            
            // Include affordability information in profile response
            Map<String, Object> profileResponse = Map.of(
                "user", user,
                "affordability", Map.of(
                    "tier", user.getAffordabilityTier(),
                    "tierName", getTierName(user.getAffordabilityTier()),
                    "contributionRange", getContributionRange(user.getAffordabilityTier()),
                    "confidence", user.getAffordabilityConfidence(),
                    "lastAnalyzed", user.getAffordabilityAnalyzedAt(),
                    "needsReanalysis", isReanalysisNeeded(user.getAffordabilityAnalyzedAt())
                )
            );
            
            return ResponseEntity.ok(profileResponse);
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

    @GetMapping("/{id}/affordability")
    public ResponseEntity<?> getUserAffordabilityTier(@PathVariable String id, HttpServletRequest request) {
        try {
            String requestingUserId = request.getHeader("X-User-Id");
            String userRoles = request.getHeader("X-User-Roles");
            
            if (requestingUserId == null || requestingUserId.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            
            // Check if user can access this data
            boolean isOwnData = id.equals(requestingUserId);
            boolean isAdmin = userRoles != null && userRoles.contains("admin");
            
            if (!isOwnData && !isAdmin) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }
            
            // Get user with affordability data
            User user = userService.getUserByUserId(id);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }
            
            Map<String, Object> response = Map.of(
                "userId", user.getUserId(),
                "tier", user.getAffordabilityTier(),
                "tierName", getTierName(user.getAffordabilityTier()),
                "confidence", user.getAffordabilityConfidence(),
                "contributionRange", getContributionRange(user.getAffordabilityTier()),
                "lastAnalyzed", user.getAffordabilityAnalyzedAt(),
                "needsReanalysis", isReanalysisNeeded(user.getAffordabilityAnalyzedAt())
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting affordability tier for user: {}", id, e);
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/affordability/analyze")
    public ResponseEntity<?> analyzeAffordability(
            @RequestBody BankStatementUploadRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            String userId = httpRequest.getHeader("X-User-Id");
            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "User not authenticated",
                    "message", "User ID is required for affordability analysis"
                ));
            }
            
            // Validate request
            if (request.getTransactions() == null || request.getTransactions().isEmpty()) {
                return ResponseEntity.status(400).body(Map.of(
                    "error", "Invalid request",
                    "message", "Bank transactions are required for analysis"
                ));
            }
            
            if (request.getTransactions().size() < 50) {
                return ResponseEntity.status(400).body(Map.of(
                    "error", "Insufficient data",
                    "message", "Minimum 50 transactions required for reliable analysis. Provided: " + 
                              request.getTransactions().size()
                ));
            }
            
            // Check if user exists
            if (userService.getUserByUserId(userId) == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "User not found",
                    "message", "User must be registered before affordability analysis"
                ));
            }
            
            logger.info("Starting affordability analysis for user: {} with {} transactions", 
                       userId, request.getTransactions().size());
            
            // Perform analysis
            AffordabilityTierResult result = affordabilityTierService.analyzeBankStatements(
                userId, request.getTransactions());
            
            // Update user's affordability tier in database
            userService.updateUserAffordabilityTier(userId, result.getTier(), result.getConfidence());
            
            logger.info("Affordability analysis completed for user: {} - Tier: {}, Confidence: {}%", 
                       userId, result.getTier(), Math.round(result.getConfidence() * 100));
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Affordability analysis completed successfully",
                "result", Map.of(
                    "tier", result.getTier(),
                    "tierName", getTierName(result.getTier()),
                    "confidence", result.getConfidence(),
                    "contributionRange", getContributionRange(result.getTier()),
                    "analysisDetails", result
                ),
                "timestamp", System.currentTimeMillis()
            ));
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request for affordability analysis: {}", e.getMessage());
            return ResponseEntity.status(400).body(Map.of(
                "error", "Invalid request",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Error during affordability analysis for user: {}", 
                        httpRequest.getHeader("X-User-Id"), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Analysis failed",
                "message", "An unexpected error occurred during affordability analysis"
            ));
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

    @GetMapping("/affordability/stats")
    public ResponseEntity<?> getAffordabilityStats(HttpServletRequest request) {
        try {
            String userRoles = request.getHeader("X-User-Roles");
            if (userRoles == null || !userRoles.contains("admin")) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
            }
            
            List<User> allUsers = userService.getAllUsers();
            
            // Calculate tier distribution
            Map<Integer, Long> tierCounts = Map.of(
                1, allUsers.stream().filter(u -> u.getAffordabilityTier() != null && u.getAffordabilityTier() == 1).count(),
                2, allUsers.stream().filter(u -> u.getAffordabilityTier() != null && u.getAffordabilityTier() == 2).count(),
                3, allUsers.stream().filter(u -> u.getAffordabilityTier() != null && u.getAffordabilityTier() == 3).count(),
                4, allUsers.stream().filter(u -> u.getAffordabilityTier() != null && u.getAffordabilityTier() == 4).count(),
                5, allUsers.stream().filter(u -> u.getAffordabilityTier() != null && u.getAffordabilityTier() == 5).count(),
                6, allUsers.stream().filter(u -> u.getAffordabilityTier() != null && u.getAffordabilityTier() == 6).count()
            );
            
            long analyzedUsers = allUsers.stream().filter(u -> u.getAffordabilityTier() != null).count();
            long unanalyzedUsers = allUsers.size() - analyzedUsers;
            
            return ResponseEntity.ok(Map.of(
                "totalUsers", allUsers.size(),
                "analyzedUsers", analyzedUsers,
                "unanalyzedUsers", unanalyzedUsers,
                "analysisRate", allUsers.size() > 0 ? 
                    Math.round((double) analyzedUsers / allUsers.size() * 100) : 0,
                "tierDistribution", tierCounts,
                "tierDistributionPercentage", calculateTierPercentages(tierCounts, analyzedUsers)
            ));
            
        } catch (Exception e) {
            logger.error("Error getting affordability stats", e);
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
            String documentId = "Test";
            // try {
            //     documentId = alfrescoService.uploadDocument(file, userId, "ID_VERIFICATION");
            //     logger.info("Document uploaded to Alfresco with ID: {} for user: {}", documentId, userId);
            // } catch (Exception e) {
            //     logger.error("Error uploading document to Alfresco for user: {}", userId, e);
            //     return ResponseEntity.status(500).body(Map.of(
            //         "error", "Document storage failed",
            //         "message", "ID verification successful but document storage failed"
            //     ));
            // }
            
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
                ),
                "nextSteps", Map.of(
                    "message", "Complete your profile by uploading bank statements for affordability analysis",
                    "endpoint", "/api/users/affordability/analyze"
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

    // Helper methods for affordability functionality
    private String getTierName(Integer tier) {
        if (tier == null) return "Unanalyzed";
        switch (tier) {
            case 1: return "Essential Savers";
            case 2: return "Steady Builders";
            case 3: return "Balanced Savers";
            case 4: return "Growth Investors";
            case 5: return "Premium Accumulators";
            case 6: return "Elite Circle";
            default: return "Unknown Tier";
        }
    }
    
    private Map<String, Integer> getContributionRange(Integer tier) {
        if (tier == null) return Map.of("min", 0, "max", 0);
        
        Map<Integer, int[]> ranges = Map.of(
            1, new int[]{50, 200},
            2, new int[]{200, 500},
            3, new int[]{500, 1000},
            4, new int[]{1000, 2500},
            5, new int[]{2500, 5000},
            6, new int[]{5000, 10000}
        );
        
        int[] range = ranges.getOrDefault(tier, new int[]{0, 0});
        return Map.of("min", range[0], "max", range[1]);
    }
    
    private boolean isReanalysisNeeded(java.util.Date lastAnalyzed) {
        if (lastAnalyzed == null) return true;
        
        long daysSinceAnalysis = java.time.temporal.ChronoUnit.DAYS.between(
            lastAnalyzed.toInstant(), java.time.Instant.now());
        
        return daysSinceAnalysis > 90; // Recommend reanalysis after 3 months
    }
    
    private Map<Integer, Double> calculateTierPercentages(Map<Integer, Long> counts, long total) {
        if (total == 0) return Map.of(
            1, 0.0, 2, 0.0, 3, 0.0, 4, 0.0, 5, 0.0, 6, 0.0
        );
        
        return Map.of(
            1, ((double) counts.get(1) / total * 100.0),
            2, ((double) counts.get(2) / total * 100.0),
            3, ((double) counts.get(3) / total * 100.0),
            4, ((double) counts.get(4) / total * 100.0),
            5, ((double) counts.get(5) / total * 100.0),
            6, ((double) counts.get(6) / total * 100.0)
        );
    }
}