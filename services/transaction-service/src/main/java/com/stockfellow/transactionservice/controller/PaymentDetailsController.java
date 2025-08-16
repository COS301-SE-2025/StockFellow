package com.stockfellow.transactionservice.controller;

import com.stockfellow.transactionservice.dto.*;
import com.stockfellow.transactionservice.model.*;
import com.stockfellow.transactionservice.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.Map;

import io.swagger.v3.oas.annotations.tags.*;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@Tag(name = "Payment Details", description = "Operations related to payment details (card and account info)")
@RequestMapping("/api/transaction/payment-methods")
@CrossOrigin(origins = "*")
public class PaymentDetailsController {
    
    @Autowired
    private PaymentDetailsService paymentDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(PaymentDetailsController.class);
    
    // @Autowired
    // private ActivityLogService activityLogService;

    /**
     * Helper method to extract and validate user ID from headers
     */
    private ResponseEntity<?> extractUserIdFromHeaders(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");
        
        if (userIdHeader == null || userIdHeader.trim().isEmpty()) {
            logger.warn("Missing X-User-Id header in request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "User ID not found in request headers"));
        }
        
        try {
            UUID userId = UUID.fromString(userIdHeader);
            return ResponseEntity.ok(userId);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format in X-User-Id header: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid user ID format"));
        }
    }

    /**
     * ===================================================
     * |   Callback (Redirect from Paystack pay page)    |
     * ===================================================
     */
    @GetMapping("/payer/callback")
    @Operation(summary = "Handle Paystack callback", 
               description = "Process callback from Paystack after user completes card authorization")
    public ResponseEntity<?> handlePaystackCallback(@RequestParam String reference) {
        try {
            Map<String, Object> result = paymentDetailsService.processPaystackCallback(reference);
            
            // Get the success status from the consistent field
            boolean success = (Boolean) result.getOrDefault("success", false);
            String status = success ? "success" : "failed";
            
            String redirectUrl = String.format("stockfellow://cards/callback?reference=%s&status=%s", 
                                            reference, status);
            
            return ResponseEntity.status(HttpStatus.FOUND)
                            .header("Location", redirectUrl)
                            .build();
                            
        } catch (Exception e) {
            logger.error("Error processing callback", e);
            String redirectUrl = String.format("stockfellow://cards/callback?reference=%s&status=failed&error=%s", 
                                            reference, e.getMessage());
            return ResponseEntity.status(HttpStatus.FOUND)
                            .header("Location", redirectUrl)
                            .build();
        }
    }

    /**
     * ===================================================
     * |      PAYER DETAILS (Card/Payment Methods)       |
     * ===================================================
     */
    @PostMapping("/payer/initialize")
    @Operation(summary = "Initialize card authorization", 
               description = "Initialize Paystack payment to capture and save user's card details")
    public ResponseEntity<?> initializeCardAuthorization(
            @Valid @RequestBody InitializeCardAuthDto initializeDto, 
            HttpServletRequest httpRequest) {
        try {
            // Extract user ID from headers
            ResponseEntity<?> userIdResponse = extractUserIdFromHeaders(httpRequest);
            if (userIdResponse.getStatusCode() != HttpStatus.OK) {
                return userIdResponse;
            }
            
            UUID userId = (UUID) userIdResponse.getBody();
            logger.info("Initializing payer details for user: {}", userId);
            
            initializeDto.setUserId(userId);
            return ResponseEntity.ok(paymentDetailsService.initializeCardAuth(initializeDto));
            
        } catch (Exception e) {
            logger.error("Error initializing card details for user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }
    
    @GetMapping("/payer/user")
    @Operation(summary = "Get card details", 
                description = "Get card details for authenticated user (user ID from gateway headers)")
    public ResponseEntity<?> getPayerDetailsByUser(HttpServletRequest httpRequest) {
        try {
            // Extract user ID from headers
            ResponseEntity<?> userIdResponse = extractUserIdFromHeaders(httpRequest);
            if (userIdResponse.getStatusCode() != HttpStatus.OK) {
                return userIdResponse;
            }
            
            UUID userId = (UUID) userIdResponse.getBody();
            logger.info("Retrieving payer details for user: {}", userId);
            
            List<PayerDetails> payerDetails = paymentDetailsService.findPayerDetailsByUserId(userId);
            List<PayerDetailsResponseDto> response = payerDetails.stream()
                    .map(PayerDetailsResponseDto::fromEntity)
                    .toList();
                    
            logger.info("Found {} payer details for user: {}", response.size(), userId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving card details for user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }
    
    // Deactivate card (instead of delete)
    @PutMapping("/payer/{payerId}/deactivate")
    @Operation(summary = "Deactivate card", 
                description = "Sets isActive of PayerDetails to false")
    public ResponseEntity<?> deactivateCard(
            @PathVariable UUID payerId, 
            HttpServletRequest httpRequest) {
        try {
            // Extract user ID from headers
            ResponseEntity<?> userIdResponse = extractUserIdFromHeaders(httpRequest);
            if (userIdResponse.getStatusCode() != HttpStatus.OK) {
                return userIdResponse;
            }
            
            UUID userId = (UUID) userIdResponse.getBody();
            logger.info("Deactivating card {} for user: {}", payerId, userId);
            
            // You might want to verify that the payer belongs to this user
            PayerDetails deactivated = paymentDetailsService.deactivateCard(payerId);
            return ResponseEntity.ok(PayerDetailsResponseDto.fromEntity(deactivated));
            
        } catch (Exception e) {
            logger.error("Error deactivating card for user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }
    
    /**
     * ===================================================
     * |      PAYOUT DETAILS (Bank/Recipient Methods)    |
     * ===================================================
     */
    @PostMapping("/payout")
    @Operation(summary = "Add new payout details (bank account)", 
                description = "Add new payout details (bank account)")
    public ResponseEntity<?> addPayoutDetails(
            @Valid @RequestBody CreatePayoutDetailsDto createDto, 
            HttpServletRequest httpRequest) {
        try {
            // Extract user ID from headers
            ResponseEntity<?> userIdResponse = extractUserIdFromHeaders(httpRequest);
            if (userIdResponse.getStatusCode() != HttpStatus.OK) {
                return userIdResponse;
            }
            
            UUID userId = (UUID) userIdResponse.getBody();
            logger.info("Adding payout details for user: {}", userId);
            
            // Set the user ID in the DTO
            createDto.setUserId(userId);
            
            PayoutDetails payoutDetails = paymentDetailsService.addPayoutDetails(createDto);
            // activityLogService.logActivity(payoutDetails.getUserId(), null, 
            //                              ActivityLog.EntityType.PAYOUT_DETAILS, payoutDetails.getPayoutId(), 
            //                              "PAYOUT_DETAILS_ADDED", null, null);
            return ResponseEntity.status(HttpStatus.CREATED)
                               .body(PayoutDetailsResponseDto.fromEntity(payoutDetails));
                               
        } catch (Exception e) {
            logger.error("Error adding payout details for user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }
    
    @GetMapping("/payout/user")
    @Operation(summary = "Get Payout Details (bank details)", 
                description = "Get Payout details for authenticated user (user ID from gateway headers)")
    public ResponseEntity<?> getPayoutDetailsByUser(HttpServletRequest httpRequest) {
        try {
            // Extract user ID from headers
            ResponseEntity<?> userIdResponse = extractUserIdFromHeaders(httpRequest);
            if (userIdResponse.getStatusCode() != HttpStatus.OK) {
                return userIdResponse;
            }
            
            UUID userId = (UUID) userIdResponse.getBody();
            logger.info("Retrieving payout details for user: {}", userId);
            
            List<PayoutDetails> payoutDetails = paymentDetailsService.findPayoutDetailsByUserId(userId);
            List<PayoutDetailsResponseDto> response = payoutDetails.stream()
                                   .map(PayoutDetailsResponseDto::fromEntity)
                                   .toList();
                                   
            logger.info("Found {} payout details for user: {}", response.size(), userId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving payout details for user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    //TODO: How to handle this with paystack?    
    // @PutMapping("/payer/{payerId}")
    // public ResponseEntity<PayerDetailsResponseDto> updatePayerDetails(
    //         @PathVariable UUID payerId,
    //         @Valid @RequestBody UpdatePayerDetailsDto updateDto) {
    //     PayerDetails payerDetails = paymentDetailsService.updatePayerDetails(payerId, updateDto);
    //     return ResponseEntity.ok(PayerDetailsResponseDto.fromEntity(payerDetails));
    // }

    //TODO: How to handle this with paystack?    
    // @PutMapping("/payout/{payoutId}")
    // public ResponseEntity<PayoutDetailsResponseDto> updatePayoutDetails(
    //         @PathVariable UUID payoutId,
    //         @Valid @RequestBody UpdatePayoutDetailsDto updateDto) {
    //     PayoutDetails payoutDetails = paymentDetailsService.updatePayoutDetails(payoutId, updateDto);
    //     return ResponseEntity.ok(PayoutDetailsResponseDto.fromEntity(payoutDetails));
    // }
    
    //TODO: not needed. A user should only have 1 payout method
    // @PutMapping("/payout/{payoutId}/set-default")
    // public ResponseEntity<PayoutDetailsResponseDto> setActivePayoutMethod(@PathVariable UUID payoutId) {
    //     PayoutDetails payoutDetails = paymentDetailsService.setAsActive(payoutId);
    //     return ResponseEntity.ok(PayoutDetailsResponseDto.fromEntity(payoutDetails));
    // }
    
    // @DeleteMapping("/payout/{payoutId}")
    // public ResponseEntity<Void> deletePayoutDetails(@PathVariable UUID payoutId) {
    //     paymentDetailsService.deletePayoutDetails(payoutId);
    //     return ResponseEntity.noContent().build();
    // }
}