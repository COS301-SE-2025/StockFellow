package com.stockfellow.transactionservice.controller;

import com.stockfellow.transactionservice.dto.TransactionResponseDto;
import com.stockfellow.transactionservice.dto.CreateTransactionDto;
import com.stockfellow.transactionservice.model.Transaction;
import com.stockfellow.transactionservice.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import java.util.UUID;
import java.util.Map;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@Tag(name = "Transactions", description = "Operations related to transactions (contributions)")
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;
    
    // @Autowired
    // private ActivityLogService activityLogService;

    // Create transaction (called when user contributes to cycle)
    //TODO Remove this endpoint and replace with charge if possible
    @PostMapping
    @Operation(summary = "Create a new transaction", 
                description = "Creates a new transaction for a group cycle with specified parameters")
    public ResponseEntity<TransactionResponseDto> createTransaction(
            @Valid @RequestBody CreateTransactionDto createDto) {
        Transaction transaction = transactionService.createTransaction(createDto);
        // activityLogService.logActivity(transaction.getUserId(), transaction.getCycleId(), 
        //                              ActivityLog.EntityType.TRANSACTION, transaction.getTransactionId(), 
        //                              "TRANSACTION_CREATED", null, null);
        return ResponseEntity.status(HttpStatus.CREATED)
                           .body(TransactionResponseDto.fromEntity(transaction));
    }
    
    // Process transaction (handle payment gateway response)
    // @PostMapping("/{transactionId}/process")
    // @Operation(summary = "Process a transaction", 
    //            description = "Processes a transaction and handle payment gateway response")

    // public ResponseEntity<TransactionResponseDto> processTransaction(
    //         @PathVariable UUID transactionId,
    //         @RequestBody ProcessTransactionDto processDto) {
    //     Transaction transaction = transactionService.processTransaction(transactionId, processDto);
    //     return ResponseEntity.ok(TransactionResponseDto.fromEntity(transaction));
    // }

    @PostMapping("/charge-card")
    @Operation(summary = "Charge existing authorization", 
                description = "Creates a new transaction for a group cycle using an existing"
                + "payment authorization. Use in group cycle for recurring payments")
    public ResponseEntity<TransactionResponseDto> chargeTransaction(
            @Valid @RequestBody CreateTransactionDto createDto) {
        Transaction transaction = transactionService.chargeStoredCard(createDto);
        // activityLogService.logActivity(transaction.getUserId(), transaction.getCycleId(), 
        //                              ActivityLog.EntityType.TRANSACTION, transaction.getTransactionId(), 
        //                              "TRANSACTION_CREATED", null, null);
        return ResponseEntity.status(HttpStatus.CREATED)
                           .body(TransactionResponseDto.fromEntity(transaction));
    }
    
    // Get transaction details
    @GetMapping("/{transactionId}")
    @Operation(summary = "Fetch transaction details", 
               description = "Returns details about a particular transaction based on the provided transactionId")
    public ResponseEntity<TransactionResponseDto> getTransaction(@PathVariable UUID transactionId) {
        Transaction transaction = transactionService.findById(transactionId);
        return ResponseEntity.ok(TransactionResponseDto.fromEntity(transaction));
    }
    
    // Get transactions by cycle
    @GetMapping("/cycle/{cycleId}")
    public ResponseEntity<Page<TransactionResponseDto>> getTransactionsByCycle(
            @PathVariable UUID cycleId, 
            Pageable pageable) {
        Page<Transaction> transactions = transactionService.findByCycleId(cycleId, pageable);
        return ResponseEntity.ok(transactions.map(TransactionResponseDto::fromEntity));
    }
    
    // Get transactions by user
    @GetMapping("/user")
    public ResponseEntity<Page<TransactionResponseDto>> getTransactionsByUser(
            HttpServletRequest request,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            ResponseEntity<?> userIdResponse = extractUserIdFromHeaders(request); // Fixed variable name
            if (userIdResponse.getStatusCode() != HttpStatus.OK) {
                return (ResponseEntity<Page<TransactionResponseDto>>) userIdResponse;
            }

            UUID userId = (UUID) userIdResponse.getBody();
            
            Page<Transaction> transactions = transactionService.findByUserId(userId, pageable);
            Page<TransactionResponseDto> responseDto = transactions.map(TransactionResponseDto::fromEntity);
            
            return ResponseEntity.ok(responseDto);
            
        } catch (Exception e) {
            // logger.error("Error fetching transactions for user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Retry failed transaction
    @PostMapping("/{transactionId}/retry")
    public ResponseEntity<TransactionResponseDto> retryTransaction(@PathVariable UUID transactionId) {
        Transaction transaction = transactionService.retryTransaction(transactionId);
        return ResponseEntity.ok(TransactionResponseDto.fromEntity(transaction));
    }
    
    /**
     * Helper method to extract and validate user ID from headers
     */
    private ResponseEntity<?> extractUserIdFromHeaders(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");
        
        if (userIdHeader == null || userIdHeader.trim().isEmpty()) {
            // logger.warn("Missing X-User-Id header in request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "User ID not found in request headers"));
        }
        
        try {
            UUID userId = UUID.fromString(userIdHeader);
            return ResponseEntity.ok(userId);
        } catch (IllegalArgumentException e) {
            // logger.error("Invalid UUID format in X-User-Id header: {}", userIdHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid user ID format"));
        }
    }
}