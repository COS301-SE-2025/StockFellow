package com.stockfellow.userservice.controller;

import com.stockfellow.userservice.model.User;
import com.stockfellow.userservice.service.UserService;
import com.stockfellow.userservice.service.SouthAfricanIdValidationService;
import com.stockfellow.userservice.service.PdfIdExtractionService;
import com.stockfellow.userservice.service.AlfrescoService;
import com.stockfellow.userservice.service.AffordabilityTierService;
import com.stockfellow.userservice.service.BankStatementExtractionService;
import com.stockfellow.userservice.dto.AffordabilityTierResult;
import com.stockfellow.userservice.dto.BankStatementUploadRequest;
import com.stockfellow.userservice.model.BankTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private AlfrescoService alfrescoService;    @Autowired
    private AffordabilityTierService affordabilityTierService;
    
    @Autowired
    private BankStatementExtractionService bankStatementExtractionService;

     @GetMapping
    public ResponseEntity<?> getServiceInfo() {
        try {
            return ResponseEntity.ok(Map.of(
                    "service", "User Service",
                    "version", "2.1.0",
                    "database", "PostgreSQL",
                    "endpoints", List.of(
                            "POST /api/users/register - Register new user in database",
                            "GET /api/users/profile - Get user profile (requires auth)",
                            "POST /api/users/verifyID - Verify user ID document",
                            "POST /api/users/affordability/analyze - Analyze user affordability",
                            "POST /api/users/affordability/analyze-pdf - Analyze bank statement PDF",
                            "GET /api/users/{id} - Get user by ID (requires auth)",
                            "GET /api/users/{id}/affordability - Get user affordability tier",
                            "GET /api/users/search?name={name} - Search users by name",
                            "GET /api/users/verified - Get verified users",
                            "GET /api/users/stats - Get user statistics",
                            "GET /api/users/affordability/stats - Get affordability statistics",
                            "GET /api/users/admin/analytics - Get user analytics (admin only)",
                            "POST /api/users/admin/requests/leave-group - Request to leave group (creates admin request)",
                            "POST /api/users/admin/requests/delete-card - Request to delete card (creates admin request)"
                            )));
        } catch (Exception e) {
            logger.error("Error getting service info", e);
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

     /**
     * Registration endpoint - Creates user in database after Keycloak registration
     * Called by API Gateway after successful Keycloak user creation
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterUserRequest request, HttpServletRequest httpRequest) {
        try {
            logger.info("=== USER SERVICE REGISTER ENDPOINT HIT ===");
            logger.info("User registration request received for userId: {}, username: {}", 
                    request.getUserId(), request.getUsername());
            
            // Debug: Log all headers
            logger.info("=== REQUEST HEADERS ===");
            httpRequest.getHeaderNames().asIterator().forEachRemaining(headerName -> {
                logger.info("Header: {} = {}", headerName, httpRequest.getHeader(headerName));
            });
            
            // Debug: Log authentication info
            logger.info("=== AUTHENTICATION INFO ===");
            logger.info("Remote address: {}", httpRequest.getRemoteAddr());
            logger.info("Request URI: {}", httpRequest.getRequestURI());
            logger.info("Request method: {}", httpRequest.getMethod());
            logger.info("Content type: {}", httpRequest.getContentType());
            
            // Check authentication context
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null) {
                    logger.info("Authentication present: {}", authentication.getClass().getSimpleName());
                    logger.info("Principal: {}", authentication.getPrincipal());
                    logger.info("Authorities: {}", authentication.getAuthorities());
                    logger.info("Is authenticated: {}", authentication.isAuthenticated());
                } else {
                    logger.info("No authentication found in SecurityContext");
                }
            } catch (Exception e) {
                logger.error("Error getting authentication context", e);
            }

            // Validate required fields
            if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
                logger.error("Registration failed: User ID is missing");
                return ResponseEntity.status(400).body(Map.of(
                    "error", "Invalid request",
                    "message", "User ID is required"
                ));
            }

            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                logger.error("Registration failed: Username is missing");
                return ResponseEntity.status(400).body(Map.of(
                    "error", "Invalid request", 
                    "message", "Username is required"
                ));
            }

            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                logger.error("Registration failed: Email is missing");
                return ResponseEntity.status(400).body(Map.of(
                    "error", "Invalid request",
                    "message", "Email is required"
                ));
            }

            logger.info("=== PROCEEDING WITH USER CREATION ===");

            // Check if user already exists
            User existingUser = userService.getUserByUserId(request.getUserId());
            if (existingUser != null) {
                logger.warn("User already exists in database: {}", request.getUserId());
                return ResponseEntity.status(409).body(Map.of(
                    "error", "User already exists",
                    "message", "User is already registered in the system",
                    "userId", request.getUserId()
                ));
            }

            // Check if email is already used
            User existingEmailUser = userService.getUserByEmail(request.getEmail());
            if (existingEmailUser != null) {
                logger.warn("Email already in use: {}", request.getEmail());
                return ResponseEntity.status(409).body(Map.of(
                    "error", "Email already in use",
                    "message", "This email address is already registered"
                ));
            }

            // Create new user
            User newUser = new User();
            newUser.setUserId(request.getUserId());
            newUser.setUsername(request.getUsername());
            newUser.setEmail(request.getEmail());
            newUser.setFirstName(request.getFirstName());
            newUser.setLastName(request.getLastName());
            
            // Set default values
            newUser.setEmailVerified(false); // Will be verified by Keycloak
            newUser.setIdVerified(false);
            newUser.setAffordabilityTier(0); // Unanalyzed
            newUser.setAffordabilityConfidence(0.0);

            // Save user to database
            User savedUser = userService.createUser(newUser);

            if (savedUser == null) {
                logger.error("Failed to save user to database: {}", request.getUserId());
                return ResponseEntity.status(500).body(Map.of(
                    "error", "Database error",
                    "message", "Failed to create user in database"
                ));
            }

            logger.info("=== USER SUCCESSFULLY CREATED ===");
            logger.info("User successfully registered in database: userId={}, id={}", 
                    savedUser.getUserId(), savedUser.getId());

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User registered successfully in database");
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", savedUser.getId());
            userMap.put("userId", savedUser.getUserId());
            userMap.put("username", savedUser.getUsername());
            userMap.put("email", savedUser.getEmail());
            userMap.put("firstName", savedUser.getFirstName() != null ? savedUser.getFirstName() : "");
            userMap.put("lastName", savedUser.getLastName() != null ? savedUser.getLastName() : "");
            userMap.put("emailVerified", savedUser.isEmailVerified());
            userMap.put("idVerified", savedUser.isIdVerified());
            userMap.put("affordabilityTier", savedUser.getAffordabilityTier());
            userMap.put("createdAt", savedUser.getCreatedAt());
            userMap.put("updatedAt", savedUser.getUpdatedAt());
            response.put("user", userMap);            // Add next steps for user
            response.put("nextSteps", Map.of(
                "message", "Complete your profile to access all features",
                "steps", List.of(
                    "1. Verify your ID document",
                    "2. Upload bank statement PDF for affordability analysis",
                    "3. Complete additional profile information"
                ),
                "endpoints", Map.of(
                    "idVerification", "/api/users/verifyID",
                    "affordabilityAnalysisPDF", "/api/users/affordability/analyze-pdf",
                    "affordabilityAnalysisJSON", "/api/users/affordability/analyze",
                    "profile", "/api/users/profile"
                )
            ));

            response.put("timestamp", System.currentTimeMillis());

            logger.info("=== RETURNING SUCCESS RESPONSE ===");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("=== UNEXPECTED ERROR IN USER REGISTRATION ===");
            logger.error("Unexpected error during user registration for userId: {}", 
                        request != null ? request.getUserId() : "null", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Registration failed",
                "message", "An unexpected error occurred during user registration",
                "details", e.getMessage()
            ));
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

            logger.info("fetching profile reponse for user: {}", userId);
            // Include affordability information in profile response
            Map<String, Object> affordabilityMap = new HashMap<>();
            affordabilityMap.put("tier", user.getAffordabilityTier());
            // affordabilityMap.put("tierName", getTierName(user.getAffordabilityTier()));
            // affordabilityMap.put("contributionRange",
            // getContributionRange(user.getAffordabilityTier()));
            // affordabilityMap.put("confidence", user.getAffordabilityConfidence());
            // affordabilityMap.put("lastAnalyzed", user.getAffordabilityAnalyzedAt());
            // affordabilityMap.put("needsReanalysis",
            // isReanalysisNeeded(user.getAffordabilityAnalyzedAt()));

            Map<String, Object> profileResponse = new HashMap<>();
            profileResponse.put("user", user);
            profileResponse.put("affordability", affordabilityMap);

            logger.info("User profile fetched successfully for user: {}", user.getUserId());

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

    @GetMapping("/affordability")
    public ResponseEntity<?> getUserAffordabilityTier(HttpServletRequest request) {
        try {
            String requestingUserId = request.getHeader("X-User-Id");
            logger.info("Attempting to get affordability tier for user: {}", requestingUserId);

            User user = userService.getUserByUserId(requestingUserId);
            if (user == null) {
                logger.warn("User not found: {}", requestingUserId);
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }

            logger.info("User found, affordability tier: {}", user.getAffordabilityTier());
            return ResponseEntity.ok(Map.of(
                    "userId", user.getUserId(),
                    "tier", user.getAffordabilityTier() != null ? user.getAffordabilityTier() : 0));

        } catch (Exception e) {
            logger.error("Database error getting affordability tier for user: {}. Error type: {}. Message: {}",
                    request.getHeader("X-User-Id"),
                    e.getClass().getName(),
                    e.getMessage(),
                    e);

            return ResponseEntity.status(500).body(Map.of(
                    "error", "Database operation failed",
                    "message", e.getMessage(),
                    "exceptionType", e.getClass().getName()));
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
                        "message", "User ID is required for affordability analysis"));
            }

            // Validate request
            if (request.getTransactions() == null || request.getTransactions().isEmpty()) {
                return ResponseEntity.status(400).body(Map.of(
                        "error", "Invalid request",
                        "message", "Bank transactions are required for analysis"));
            }

            if (request.getTransactions().size() < 50) {
                return ResponseEntity.status(400).body(Map.of(
                        "error", "Insufficient data",
                        "message", "Minimum 50 transactions required for reliable analysis. Provided: " +
                                request.getTransactions().size()));
            }

            // Check if user exists
            if (userService.getUserByUserId(userId) == null) {
                return ResponseEntity.status(404).body(Map.of(
                        "error", "User not found",
                        "message", "User must be registered before affordability analysis"));
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
                            "analysisDetails", result),
                    "timestamp", System.currentTimeMillis()));

        } catch (IllegalArgumentException e) {
            logger.error("Invalid request for affordability analysis: {}", e.getMessage());
            return ResponseEntity.status(400).body(Map.of(
                    "error", "Invalid request",
                    "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error during affordability analysis for user: {}",
                    httpRequest.getHeader("X-User-Id"), e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Analysis failed",
                    "message", "An unexpected error occurred during affordability analysis"));
        }
    }

    @PostMapping("/affordability/analyze-pdf")
    public ResponseEntity<?> analyzeBankStatementPdf(
            @RequestParam("bankStatement") MultipartFile bankStatementFile,
            HttpServletRequest httpRequest) {

        try {
            String userId = httpRequest.getHeader("X-User-Id");
            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of(
                        "error", "User not authenticated",
                        "message", "User ID is required for affordability analysis"));
            }

            // Validate file
            if (bankStatementFile == null || bankStatementFile.isEmpty()) {
                return ResponseEntity.status(400).body(Map.of(
                        "error", "Invalid request",
                        "message", "Bank statement PDF file is required"));
            }

            if (!bankStatementFile.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
                return ResponseEntity.status(400).body(Map.of(
                        "error", "Invalid file type",
                        "message", "Only PDF files are supported"));
            }

            // Check if user exists
            if (userService.getUserByUserId(userId) == null) {
                return ResponseEntity.status(404).body(Map.of(
                        "error", "User not found",
                        "message", "User must be registered before affordability analysis"));
            }

            logger.info("Starting PDF bank statement analysis for user: {} with file: {}",
                    userId, bankStatementFile.getOriginalFilename());

            // Extract transactions from PDF
            List<BankTransaction> transactions;
            BankStatementExtractionService.BankStatementAnalysisResult extractionResult;
            
            try {
                transactions = bankStatementExtractionService.extractTransactionsFromPdf(bankStatementFile);
                extractionResult = bankStatementExtractionService.analyzeExtractionQuality(
                    transactions, "PDF extraction");
            } catch (Exception e) {
                logger.error("Failed to extract transactions from PDF for user: {}", userId, e);
                return ResponseEntity.status(400).body(Map.of(
                        "error", "PDF extraction failed",
                        "message", "Unable to extract transactions from the provided PDF. Please ensure it's a valid bank statement.",
                        "details", e.getMessage()));
            }

            // Validate extracted transactions
            if (transactions.isEmpty()) {
                return ResponseEntity.status(400).body(Map.of(
                        "error", "No transactions found",
                        "message", "No valid transactions could be extracted from the PDF. Please check that it's a valid bank statement.",
                        "extractionAnalysis", extractionResult));
            }

            if (transactions.size() < 50) {
                return ResponseEntity.status(400).body(Map.of(
                        "error", "Insufficient data",
                        "message", "Minimum 50 transactions required for reliable analysis. Extracted: " + 
                                transactions.size(),
                        "extractionAnalysis", extractionResult,
                        "recommendation", "Please upload a statement covering at least 3 months of transactions"));
            }

            logger.info("Successfully extracted {} transactions from PDF for user: {}", 
                    transactions.size(), userId);

            // Perform affordability analysis
            AffordabilityTierResult result = affordabilityTierService.analyzeBankStatements(
                    userId, transactions);

            // Update user's affordability tier in database
            userService.updateUserAffordabilityTier(userId, result.getTier(), result.getConfidence());

            logger.info("PDF affordability analysis completed for user: {} - Tier: {}, Confidence: {}%",
                    userId, result.getTier(), Math.round(result.getConfidence() * 100));

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Bank statement PDF analyzed successfully",
                    "extractionResult", Map.of(
                            "transactionsExtracted", transactions.size(),
                            "qualityScore", extractionResult.getQualityScore(),
                            "dateRange", extractionResult.getDateRange(),
                            "warnings", extractionResult.getWarnings(),
                            "recommendations", extractionResult.getRecommendations()),
                    "affordabilityResult", Map.of(
                            "tier", result.getTier(),
                            "tierName", getTierName(result.getTier()),
                            "confidence", result.getConfidence(),
                            "contributionRange", getContributionRange(result.getTier()),
                            "analysisDetails", result),
                    "timestamp", System.currentTimeMillis()));

        } catch (IllegalArgumentException e) {
            logger.error("Invalid request for PDF affordability analysis: {}", e.getMessage());
            return ResponseEntity.status(400).body(Map.of(
                    "error", "Invalid request",
                    "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error during PDF affordability analysis for user: {}",
                    httpRequest.getHeader("X-User-Id"), e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Analysis failed",
                    "message", "An unexpected error occurred during PDF affordability analysis",
                    "details", e.getMessage()));
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
                    "count", users.size()));
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
                    "count", verifiedUsers.size()));
        } catch (Exception e) {
            logger.error("Error getting verified users", e);
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats(HttpServletRequest request) {
        try {
            String userRoles = request.getHeader("X-User-Roles");
            // if (userRoles == null || !userRoles.contains("admin")) {
            //     return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
            // }

            // String userRoles = request.getHeader("X-User-Roles");
            // String serviceOrigin = request.getHeader("X-Service-Origin");
            
            // // Allow admin-service to access without user roles
            // boolean isAuthorized = (userRoles != null && userRoles.contains("admin")) ||
            //                     "admin-service".equals(serviceOrigin);
                                
            // if (!isAuthorized) {
            //     return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
            // }

            List<User> allUsers = userService.getAllUsers();
            List<User> verifiedUsers = userService.getVerifiedUsers();
            List<User> incompleteUsers = userService.getUsersWithIncompleteProfiles();

            return ResponseEntity.ok(Map.of(
                    "totalUsers", allUsers.size(),
                    "verifiedUsers", verifiedUsers.size(),
                    "unverifiedUsers", allUsers.size() - verifiedUsers.size(),
                    "incompleteProfiles", incompleteUsers.size(),
                    "verificationRate",
                    allUsers.size() > 0 ? Math.round((double) verifiedUsers.size() / allUsers.size() * 100) : 0));
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
                    1,
                    allUsers.stream().filter(u -> u.getAffordabilityTier() != null && u.getAffordabilityTier() == 1)
                            .count(),
                    2,
                    allUsers.stream().filter(u -> u.getAffordabilityTier() != null && u.getAffordabilityTier() == 2)
                            .count(),
                    3,
                    allUsers.stream().filter(u -> u.getAffordabilityTier() != null && u.getAffordabilityTier() == 3)
                            .count(),
                    4,
                    allUsers.stream().filter(u -> u.getAffordabilityTier() != null && u.getAffordabilityTier() == 4)
                            .count(),
                    5,
                    allUsers.stream().filter(u -> u.getAffordabilityTier() != null && u.getAffordabilityTier() == 5)
                            .count(),
                    6, allUsers.stream().filter(u -> u.getAffordabilityTier() != null && u.getAffordabilityTier() == 6)
                            .count());

            long analyzedUsers = allUsers.stream().filter(u -> u.getAffordabilityTier() != null).count();
            long unanalyzedUsers = allUsers.size() - analyzedUsers;

            return ResponseEntity.ok(Map.of(
                    "totalUsers", allUsers.size(),
                    "analyzedUsers", analyzedUsers,
                    "unanalyzedUsers", unanalyzedUsers,
                    "analysisRate",
                    allUsers.size() > 0 ? Math.round((double) analyzedUsers / allUsers.size() * 100) : 0,
                    "tierDistribution", tierCounts,
                    "tierDistributionPercentage", calculateTierPercentages(tierCounts, analyzedUsers)));

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
                        "message", "User ID is required for ID verification"));
            }

            // Validate file
            if (file == null || file.isEmpty()) {
                return ResponseEntity.status(400).body(Map.of(
                        "error", "Invalid file",
                        "message", "PDF file is required for ID verification"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.equals("application/pdf")) {
                return ResponseEntity.status(400).body(Map.of(
                        "error", "Invalid file type",
                        "message", "Only PDF files are accepted for ID verification"));
            }

            // Check if user exists
            User user = userService.getUserByUserId(userId);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of(
                        "error", "User not found",
                        "message", "User must be registered before ID verification"));
            }

            // Check if user is already verified
            if (user.isIdVerified()) {
                return ResponseEntity.status(409).body(Map.of(
                        "error", "Already verified",
                        "message", "User ID is already verified",
                        "verifiedAt", user.getUpdatedAt()));
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
                        "message", "Could not extract text from the provided PDF"));
            }

            if (extractedIdNumber == null) {
                return ResponseEntity.status(400).body(Map.of(
                        "error", "ID not found",
                        "message", "Could not extract a valid South African ID number from the document"));
            }

            logger.info("Extracted ID number for user {}: {}", userId,
                    extractedIdNumber.substring(0, 6) + "XXXXXXX");

            // Check if ID number is already used by another user
            User existingUser = userService.getUserByIdNumber(extractedIdNumber);
            if (existingUser != null && !existingUser.getUserId().equals(userId)) {
                return ResponseEntity.status(409).body(Map.of(
                        "error", "ID already registered",
                        "message", "This ID number is already registered to another user"));
            }

            // Validate ID number using Luhn algorithm
            boolean isValidId = idValidationService.validateSouthAfricanId(extractedIdNumber);
            if (!isValidId) {
                return ResponseEntity.status(400).body(Map.of(
                        "error", "Invalid ID number",
                        "message", "The extracted ID number is not a valid South African ID"));
            }

            // Extract additional information from valid ID
            SouthAfricanIdValidationService.SouthAfricanIdInfo idInfo = idValidationService
                    .extractIdInfo(extractedIdNumber);

            // Store document in Alfresco
            String documentId = "Test";
            // try {
            // documentId = alfrescoService.uploadDocument(file, userId, "ID_VERIFICATION");
            // logger.info("Document uploaded to Alfresco with ID: {} for user: {}",
            // documentId, userId);
            // } catch (Exception e) {
            // logger.error("Error uploading document to Alfresco for user: {}", userId, e);
            // return ResponseEntity.status(500).body(Map.of(
            // "error", "Document storage failed",
            // "message", "ID verification successful but document storage failed"
            // ));
            // }

            // Update user record with verification status
            User updatedUser = userService.updateIdVerificationStatus(userId, extractedIdNumber, documentId, idInfo);

            if (updatedUser == null) {
                return ResponseEntity.status(500).body(Map.of(
                        "error", "Update failed",
                        "message", "Could not update user verification status"));
            }

            // Return success response with extracted information
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "ID verification completed successfully",
                    "idNumber", extractedIdNumber,
                    "extractedInfo", Map.of(
                            "dateOfBirth", idInfo.getDateOfBirth(),
                            "gender", idInfo.getGender(),
                            "citizenship", idInfo.getCitizenship()),
                    "documentId", documentId,
                    "verificationTimestamp", System.currentTimeMillis(),
                    "user", Map.of(
                            "id", updatedUser.getId(),
                            "userId", updatedUser.getUserId(),
                            "email", updatedUser.getEmail(),
                            "idVerified", updatedUser.isIdVerified(),
                            "updatedAt", updatedUser.getUpdatedAt()),
                    "nextSteps", Map.of(
                            "message", "Complete your profile by uploading bank statements for affordability analysis",
                            "endpoint", "/api/users/affordability/analyze")));

        } catch (Exception e) {
            logger.error("Unexpected error during ID verification for user: {}",
                    httpRequest.getHeader("X-User-Id"), e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Verification failed",
                    "message", "An unexpected error occurred during ID verification"));
        }
    }

    // Admin-only analytics endpoint
    @GetMapping("/admin/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserAnalytics() {
        try {
            List<User> allUsers = userService.getAllUsers();
            List<User> verifiedUsers = userService.getVerifiedUsers();
            
            // Calculate registration trends (last 30 days)
            Map<String, Long> registrationTrends = calculateRegistrationTrends(allUsers);
            
            // Calculate verification rates by tier
            Map<Integer, Map<String, Object>> tierAnalysis = calculateTierAnalysis(allUsers);
            
            // Get verification completion rates
            Map<String, Object> verificationMetrics = calculateVerificationMetrics(allUsers);
            
            Map<String, Object> analytics = new HashMap<>();
            analytics.put("totalUsers", allUsers.size());
            analytics.put("verifiedUsers", verifiedUsers.size());
            analytics.put("verificationRate", allUsers.size() > 0 ? 
                (double) verifiedUsers.size() / allUsers.size() * 100 : 0);
            analytics.put("registrationTrends", registrationTrends);
            analytics.put("tierAnalysis", tierAnalysis);
            analytics.put("verificationMetrics", verificationMetrics);
            analytics.put("generatedAt", new Date());
            
            return ResponseEntity.ok(analytics);
            
        } catch (Exception e) {
            logger.error("Error getting user analytics", e);
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    // Request to leave group (creates admin request)
    @PostMapping("/admin/requests/leave-group")
    public ResponseEntity<?> requestLeaveGroup(@RequestBody LeaveGroupRequest request, HttpServletRequest httpRequest) {
        try {
            String userId = httpRequest.getHeader("X-User-Id");
            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }

            // Validate request
            if (request.getGroupId() == null || request.getGroupId().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid request",
                    "message", "Group ID is required"
                ));
            }

            // Create admin request via admin service
            Map<String, Object> adminRequest = new HashMap<>();
            adminRequest.put("userId", userId);
            adminRequest.put("requestType", "LEAVE_GROUP");
            adminRequest.put("groupId", request.getGroupId());
            adminRequest.put("reason", request.getReason());

            // Call admin service to create request
            try {
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<Map> response = restTemplate.postForEntity(
                    "http://admin-service:4060/api/admin/requests/create",
                    adminRequest,
                    Map.class
                );

                if (response.getStatusCode().is2xxSuccessful()) {
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Leave group request submitted successfully",
                        "requestId", response.getBody().get("requestId"),
                        "status", "pending_admin_approval"
                    ));
                } else {
                    return ResponseEntity.status(500).body(Map.of(
                        "error", "Failed to create admin request"
                    ));
                }
            } catch (Exception e) {
                logger.error("Error calling admin service for leave group request", e);
                return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to submit request"
                ));
            }

        } catch (Exception e) {
            logger.error("Error processing leave group request", e);
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/admin/requests/delete-card")
    public ResponseEntity<?> requestDeleteCard(@RequestBody DeleteCardRequest request, HttpServletRequest httpRequest) {
        try {
            String userId = httpRequest.getHeader("X-User-Id");
            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }

            // Validate request
            if (request.getCardId() == null || request.getCardId().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid request",
                    "message", "Card ID is required"
                ));
            }

            // Create admin request
            Map<String, Object> adminRequest = new HashMap<>();
            adminRequest.put("userId", userId);
            adminRequest.put("requestType", "DELETE_CARD");
            adminRequest.put("cardId", request.getCardId());
            adminRequest.put("reason", request.getReason());

            try {
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<Map> response = restTemplate.postForEntity(
                    "http://admin-service:4060/api/admin/requests/create",
                    adminRequest,
                    Map.class
                );

                if (response.getStatusCode().is2xxSuccessful()) {
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Delete card request submitted successfully",
                        "requestId", response.getBody().get("requestId"),
                        "status", "pending_admin_approval"
                    ));
                } else {
                    return ResponseEntity.status(500).body(Map.of(
                        "error", "Failed to create admin request"
                    ));
                }
            } catch (Exception e) {
                logger.error("Error calling admin service for delete card request", e);
                return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to submit request"
                ));
            }

        } catch (Exception e) {
            logger.error("Error processing delete card request", e);
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    // Helper methods for analytics
   // Helper methods for analytics
    private Map<String, Long> calculateRegistrationTrends(List<User> users) {
        Map<String, Long> trends = new HashMap<>();
        LocalDate now = LocalDate.now();

        for (int i = 0; i < 30; i++) {
            LocalDate date = now.minusDays(i);
            String dateKey = date.toString();

            long count = users.stream()
                .filter(user -> user.getCreatedAt() != null)
                .filter(user -> {
                    LocalDate userDate = user.getCreatedAt()
                        .atZone(ZoneId.systemDefault()) // attach zone
                        .toLocalDate();                 // strip time
                    return userDate.equals(date);
                })
                .count();

            trends.put(dateKey, count);
        }

        return trends;
    }

    private Map<Integer, Map<String, Object>> calculateTierAnalysis(List<User> users) {
        Map<Integer, Map<String, Object>> tierAnalysis = new HashMap<>();
        
        
        for (int tier = 1; tier <= 6; tier++) {
            final int currentTier = tier;
            List<User> tierUsers = users.stream()
                .filter(user -> user.getAffordabilityTier() != null && user.getAffordabilityTier() == currentTier)
                .collect(Collectors.toList());
            
            long verifiedCount = tierUsers.stream()
                .filter(User::isIdVerified)
                .count();
            
            Map<String, Object> tierData = new HashMap<>();
            tierData.put("totalUsers", tierUsers.size());
            tierData.put("verifiedUsers", verifiedCount);
            tierData.put("verificationRate", tierUsers.size() > 0 ? 
                (double) verifiedCount / tierUsers.size() * 100 : 0);
            
            tierAnalysis.put(tier, tierData);
        }
        
        return tierAnalysis;
    }

    private Map<String, Object> calculateVerificationMetrics(List<User> users) {
        Map<String, Object> metrics = new HashMap<>();
        
        long totalUsers = users.size();
        long idVerified = users.stream().filter(User::isIdVerified).count();
        long emailVerified = users.stream().filter(User::isEmailVerified).count();
        long affordabilityAnalyzed = users.stream()
            .filter(user -> user.getAffordabilityTier() != null && user.getAffordabilityTier() > 0)
            .count();
        
        metrics.put("totalUsers", totalUsers);
        metrics.put("idVerified", idVerified);
        metrics.put("emailVerified", emailVerified);
        metrics.put("affordabilityAnalyzed", affordabilityAnalyzed);
        metrics.put("idVerificationRate", totalUsers > 0 ? (double) idVerified / totalUsers * 100 : 0);
        metrics.put("emailVerificationRate", totalUsers > 0 ? (double) emailVerified / totalUsers * 100 : 0);
        metrics.put("affordabilityAnalysisRate", totalUsers > 0 ? (double) affordabilityAnalyzed / totalUsers * 100 : 0);
        
        return metrics;
    }


    // Helper methods for affordability functionality
    private String getTierName(Integer tier) {
        if (tier == null)
            return "Unanalyzed";
        switch (tier) {
            case 1:
                return "Essential Savers";
            case 2:
                return "Steady Builders";
            case 3:
                return "Balanced Savers";
            case 4:
                return "Growth Investors";
            case 5:
                return "Premium Accumulators";
            case 6:
                return "Elite Circle";
            default:
                return "Unknown Tier";
        }
    }

    private Map<String, Integer> getContributionRange(Integer tier) {
        if (tier == null)
            return Map.of("min", 0, "max", 0);

        Map<Integer, int[]> ranges = Map.of(
                1, new int[] { 50, 200 },
                2, new int[] { 200, 500 },
                3, new int[] { 500, 1000 },
                4, new int[] { 1000, 2500 },
                5, new int[] { 2500, 5000 },
                6, new int[] { 5000, 10000 });

        int[] range = ranges.getOrDefault(tier, new int[] { 0, 0 });
        return Map.of("min", range[0], "max", range[1]);
    }

    private boolean isReanalysisNeeded(java.util.Date lastAnalyzed) {
        if (lastAnalyzed == null)
            return true;

        long daysSinceAnalysis = java.time.temporal.ChronoUnit.DAYS.between(
                lastAnalyzed.toInstant(), java.time.Instant.now());

        return daysSinceAnalysis > 90; // Recommend reanalysis after 3 months
    }

    private Map<Integer, Double> calculateTierPercentages(Map<Integer, Long> counts, long total) {
        if (total == 0)
            return Map.of(
                    1, 0.0, 2, 0.0, 3, 0.0, 4, 0.0, 5, 0.0, 6, 0.0);

        return Map.of(
                1, ((double) counts.get(1) / total * 100.0),
                2, ((double) counts.get(2) / total * 100.0),
                3, ((double) counts.get(3) / total * 100.0),
                4, ((double) counts.get(4) / total * 100.0),
                5, ((double) counts.get(5) / total * 100.0),
                6, ((double) counts.get(6) / total * 100.0));
    }

    // DTO for registration request
    public static class RegisterUserRequest {
        private String userId;        // Keycloak user ID
        private String username;
        private String email;
        private String firstName;
        private String lastName;

        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }

    // DTO classes for the new endpoints
    public static class LeaveGroupRequest {
        private String groupId;
        private String reason;

        public String getGroupId() { return groupId; }
        public void setGroupId(String groupId) { this.groupId = groupId; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class DeleteCardRequest {
        private String cardId;
        private String reason;

        public String getCardId() { return cardId; }
        public void setCardId(String cardId) { this.cardId = cardId; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
   
}