package com.stockfellow.transactionservice.controller;

import com.stockfellow.transactionservice.dto.CreateBankDetailRequest;
import com.stockfellow.transactionservice.dto.BankDetailResponse;
import com.stockfellow.transactionservice.model.BankDetails;
import com.stockfellow.transactionservice.service.BankDetailService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@Tag(name = "Bank Details", description = "Operations related to user banking details and payment methods")
@RequestMapping("/api/bank-details")
public class BankDetailController {

    private static final Logger logger = LoggerFactory.getLogger(BankDetailController.class);
    private final BankDetailService bankDetailService;

    public BankDetailController(BankDetailService bankDetailService) {
        this.bankDetailService = bankDetailService;
    }

    @PostMapping
    @Operation(summary = "Add new bank details", 
               description = "Adds new banking details for a user. If this is the user's first bank detail, it will automatically be set as active.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Bank details created successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = BankDetailResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "409", description = "Business logic conflict (e.g., duplicate bank details)"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BankDetailResponse> addBankDetails(
            @Valid @RequestBody 
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Bank details creation request", 
                required = true,
                content = @Content(schema = @Schema(implementation = CreateBankDetailRequest.class))
            ) CreateBankDetailRequest request) {
        
        logger.info("Received request to add bank details for user: {}", request.getUserId());
        
        try {
            BankDetails bankDetails = bankDetailService.addBankDetails(request);
            BankDetailResponse response = BankDetailResponse.from(bankDetails);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            logger.warn("Business logic error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            logger.error("Unexpected error adding bank details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user")
    @Operation(summary = "Get all bank details for authenticated user", 
               description = "Retrieves all banking details (active and inactive) for the authenticated user from gateway headers")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User bank details retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = BankDetailResponse.class))),
        @ApiResponse(responseCode = "401", description = "User not authenticated - missing X-User-Id header"),
        @ApiResponse(responseCode = "404", description = "User not found or no bank details exist")
    })
    @Parameter(name = "X-User-Id", description = "User ID from gateway authentication", 
               in = ParameterIn.HEADER, required = true, example = "123e4567-e89b-12d3-a456-426614174000")

    public ResponseEntity<?> getUserBankDetails(HttpServletRequest request) {
        try {
            // Extract user ID from gateway headers
            String userIdHeader = request.getHeader("X-User-Id");
            
            if (userIdHeader == null || userIdHeader.isEmpty()) {
                logger.warn("Missing X-User-Id header in request");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
            }
            
            UUID userId;
            try {
                userId = UUID.fromString(userIdHeader);
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid UUID format in X-User-Id header: {}", userIdHeader);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid user ID format"));
            }
            
            logger.info("Getting all bank details for user: {}", userId);
            
            long count = bankDetailService.getBankDetailsCount(userId);
            return ResponseEntity.ok(count);

        } catch (Exception e) {
            logger.error("Unexpected error getting bank details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/user/active")
    @Operation(summary = "Get active bank details for a user", 
               description = "Retrieves the currently active (primary) banking details for a user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active bank details retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = BankDetailResponse.class))),
        @ApiResponse(responseCode = "401", description = "User not authenticated - missing X-User-Id header"),
        @ApiResponse(responseCode = "404", description = "No active bank details found for user")
    })
    @Parameter(name = "X-User-Id", description = "User ID from gateway authentication", 
               in = ParameterIn.HEADER, required = true, example = "123e4567-e89b-12d3-a456-426614174000")

    public ResponseEntity<BankDetailResponse> getActiveBankDetails(HttpServletRequest request) {
        try {
           // Extract user ID from gateway headers
            String userIdHeader = request.getHeader("X-User-Id");
            
            if (userIdHeader == null || userIdHeader.isEmpty()) {
                logger.warn("Missing X-User-Id header in request");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
            }
            
            UUID userId;
            try {
                userId = UUID.fromString(userIdHeader);
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid UUID format in X-User-Id header: {}", userIdHeader);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid user ID format"));
            }
            
            logger.info("Getting active bank details for user: {}", userId);
            
            BankDetailResponse bankDetails = bankDetailService.getActiveBankDetails(userId);
            return ResponseEntity.ok(bankDetails);

        } catch (IllegalArgumentException e) {
            logger.warn("User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "User not found or no active bank details exist"));
        } catch (Exception e) {
            logger.error("Unexpected error getting active bank details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/{bankDetailsId}")
    @Operation(summary = "Get bank details by ID", 
               description = "Retrieves specific banking details by ID. Includes user validation for security.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bank details retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = BankDetailResponse.class))),
        @ApiResponse(responseCode = "404", description = "Bank details not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - bank details belong to different user")
    })
    public ResponseEntity<BankDetailResponse> getBankDetailsById(
            @Parameter(description = "The unique identifier of the bank details", required = true)
            @PathVariable UUID bankDetailsId,
            @Parameter(description = "The user ID for security validation", required = true)
            @RequestParam UUID userId) {
        logger.info("Getting bank details by ID: {} for user: {}", bankDetailsId, userId);
        
        try {
            BankDetailResponse bankDetails = bankDetailService.getBankDetailsById(bankDetailsId, userId);
            return ResponseEntity.ok(bankDetails);
        } catch (IllegalArgumentException e) {
            logger.warn("Bank details not found or access denied: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            logger.warn("Access denied for bank details: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/{bankDetailsId}/activate")
    @Operation(summary = "Set bank details as active", 
               description = "Sets the specified bank details as the active/primary payment method. Automatically deactivates any other active bank details for the user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bank details activated successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = BankDetailResponse.class))),
        @ApiResponse(responseCode = "404", description = "Bank details not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - bank details belong to different user")
    })
    public ResponseEntity<BankDetailResponse> activateBankDetails(
            @Parameter(description = "The unique identifier of the bank details to activate", required = true)
            @PathVariable UUID bankDetailsId,
            @Parameter(description = "The user ID for security validation", required = true)
            @RequestParam UUID userId) {
        logger.info("Activating bank details: {} for user: {}", bankDetailsId, userId);
        
        try {
            BankDetailResponse activatedBankDetails = bankDetailService.activateBankDetails(bankDetailsId, userId);
            return ResponseEntity.ok(activatedBankDetails);
        } catch (IllegalArgumentException e) {
            logger.warn("Bank details not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            logger.warn("Access denied for bank details activation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/{bankDetailsId}/deactivate")
    @Operation(summary = "Deactivate bank details", 
               description = "Deactivates the specified bank details. Note: Users should have at least one active bank detail.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bank details deactivated successfully"),
        @ApiResponse(responseCode = "404", description = "Bank details not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - bank details belong to different user"),
        @ApiResponse(responseCode = "409", description = "Cannot deactivate - this is the user's only active bank detail")
    })
    public ResponseEntity<Void> deactivateBankDetails(
            @Parameter(description = "The unique identifier of the bank details to deactivate", required = true)
            @PathVariable UUID bankDetailsId,
            @Parameter(description = "The user ID for security validation", required = true)
            @RequestParam UUID userId) {
        logger.info("Deactivating bank details: {} for user: {}", bankDetailsId, userId);
        
        try {
            bankDetailService.deactivateBankDetails(bankDetailsId, userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Bank details not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            logger.warn("Access denied for bank details deactivation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalStateException e) {
            logger.warn("Cannot deactivate bank details: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @DeleteMapping("/{bankDetailsId}")
    @Operation(summary = "Delete bank details", 
               description = "Permanently deletes bank details. This is a hard delete operation - use with caution.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Bank details deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Bank details not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - bank details belong to different user"),
        @ApiResponse(responseCode = "409", description = "Cannot delete - this is the user's only active bank detail")
    })
    public ResponseEntity<Void> deleteBankDetails(
            @Parameter(description = "The unique identifier of the bank details to delete", required = true)
            @PathVariable UUID bankDetailsId,
            @Parameter(description = "The user ID for security validation", required = true)
            @RequestParam UUID userId) {
        logger.info("Deleting bank details: {} for user: {}", bankDetailsId, userId);
        
        try {
            bankDetailService.deleteBankDetails(bankDetailsId, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Bank details not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            logger.warn("Access denied for bank details deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalStateException e) {
            logger.warn("Cannot delete bank details: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping("/user/count")
    @Operation(summary = "Get bank details count for user", 
               description = "Returns the total count of active bank details for a user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bank details count retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated - missing X-User-Id header")
    })
    @Parameter(name = "X-User-Id", description = "User ID from gateway authentication", 
               in = ParameterIn.HEADER, required = true, example = "123e4567-e89b-12d3-a456-426614174000")
    public ResponseEntity<Long> getBankDetailsCount(HttpServletRequest request) {
        try {
            Srting userIdHeader = request.getHeader("X-User-Id");

            if (iserIdHeader == null || userIdHeader.isEmpty()){
                logger.warn("Missing X-User-Id header in request");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
            }

            UUID userId;
            try {
                userId = UUID.fromString(userIdHeader);
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid UUID format in X-User-Id header: {}", userIdHeader);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid user ID format"));
            }
            
            logger.info("Getting all number of details for user: {}", userId);
            
            List<BankDetailResponse> bankDetails = bankDetailService.countByUserIdAndIsActiveTrue(userId);
            return ResponseEntity.ok(bankDetails);
            
        } catch (IllegalArgumentException e) {
            logger.warn("User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "User not found or no bank details exist"));
        } catch (Exception e) {
            logger.error("Unexpected error getting bank details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
        }
    }

    /* TODO: Not sure if this is needed given that we have an endpoint to fetch active

    // @GetMapping("/user/{userId}/has-active")
    // @Operation(summary = "Check if user has active bank details", 
    //            description = "Returns true if the user has at least one active bank detail")
    // @ApiResponses(value = {
    //     @ApiResponse(responseCode = "200", description = "Check completed successfully")
    // })
    // public ResponseEntity<Boolean> hasActiveBankDetails(
    //         @Parameter(description = "The unique identifier of the user", required = true)
    //         @PathVariable UUID userId) {
    //     logger.info("Checking if user has active bank details: {}", userId);
        
    //     boolean hasActive = bankDetailService.hasActiveBankDetails(userId);
    //     return ResponseEntity.ok(hasActive);
    }  */
}