package com.stockfellow.userservice.controller;

import com.stockfellow.userservice.dto.RegisterUserRequest;
import com.stockfellow.userservice.model.User;
import com.stockfellow.userservice.service.ReadModelService;
import com.stockfellow.userservice.service.RegisterUserService;
import com.stockfellow.userservice.service.SouthAfricanIdValidationService;
import com.stockfellow.userservice.service.PdfIdExtractionService;
import com.stockfellow.userservice.service.AlfrescoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private ReadModelService readModelService;

    // @Autowired
    // private RegisterUserService registerUserService;
    
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
                "version", "1.0.0",
                "endpoints", List.of(
                    "GET /api/users/profile - Get user profile (requires auth)",
                    "POST /api/users/verifyID - Verify user ID document",
                    "GET /api/users/:id - Get user by ID (requires auth)"
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
            logger.error("Error getting user by ID", e);
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/verifyID")
    public ResponseEntity<?> verifyID(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) String userId,
            HttpServletRequest httpRequest) {
        
        try {
            // Get user ID from headers if not provided in request
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
            
            // Check if file is PDF
            String contentType = file.getContentType();
            if (contentType == null || !contentType.equals("application/pdf")) {
                return ResponseEntity.status(400).body(Map.of(
                    "error", "Invalid file type",
                    "message", "Only PDF files are accepted for ID verification"
                ));
            }
            
            logger.info("Starting ID verification process for user: {}", userId);
            
            // Step 1: Extract ID number from PDF
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
                       extractedIdNumber.substring(0, 6) + "XXXXXXX"); // Log partial ID for privacy
            
            // Step 2: Validate ID number using Luhn algorithm
            boolean isValidId = idValidationService.validateSouthAfricanId(extractedIdNumber);
            if (!isValidId) {
                return ResponseEntity.status(400).body(Map.of(
                    "error", "Invalid ID number",
                    "message", "The extracted ID number is not a valid South African ID"
                ));
            }
            
            // Step 3: Extract additional information from valid ID
            SouthAfricanIdValidationService.SouthAfricanIdInfo idInfo = 
                idValidationService.extractIdInfo(extractedIdNumber);
            
            // Step 4: Store document in Alfresco
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
            
            // Step 5: Update user record with verification status
            //try {
            //     registerUserService.updateUserVerificationStatus(userId, extractedIdNumber, documentId, idInfo);
            //     logger.info("User verification status updated for user: {}", userId);
            // } catch (Exception e) {
            //     logger.error("Error updating user verification status for user: {}", userId, e);
            //     // Don't fail the request if this step fails - the core verification is complete
            // }
            
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
                "verificationTimestamp", System.currentTimeMillis()
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