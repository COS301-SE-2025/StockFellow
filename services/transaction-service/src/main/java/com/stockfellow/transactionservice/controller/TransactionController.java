package com.stockfellow.transactionservice.controller;

import com.stockfellow.transactionservice.dto.*;
import com.stockfellow.transactionservice.model.*;
import com.stockfellow.transactionservice.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.UUID;

import io.swagger.v3.oas.annotations.tags.*;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@Tag(name = "Transactions", description = "Operations related to transactions (contributions)")
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private ActivityLogService activityLogService;

    // Create transaction (called when user contributes to cycle)
    @PostMapping
    @Operation(summary = "Create a new transaction", 
                description = "Creates a new transaction for a group cycle with specified parameters")
    public ResponseEntity<TransactionResponseDto> createTransaction(@Valid @RequestBody CreateTransactionDto createDto) {
        Transaction transaction = transactionService.createTransaction(createDto);
        activityLogService.logActivity(transaction.getUserId(), transaction.getCycleId(), 
                                     ActivityLog.EntityType.TRANSACTION, transaction.getTransactionId(), 
                                     "TRANSACTION_CREATED", null, null);
        return ResponseEntity.status(HttpStatus.CREATED)
                           .body(TransactionResponseDto.fromEntity(transaction));
    }
    
    // Process transaction (handle payment gateway response)
    @PostMapping("/{transactionId}/process")
    @Operation(summary = "Process a transaction", 
               description = "Processes a transaction and handle payment gateway response")

    public ResponseEntity<TransactionResponseDto> processTransaction(
            @PathVariable UUID transactionId,
            @RequestBody ProcessTransactionDto processDto) {
        Transaction transaction = transactionService.processTransaction(transactionId, processDto);
        return ResponseEntity.ok(TransactionResponseDto.fromEntity(transaction));
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
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<TransactionResponseDto>> getTransactionsByUser(
            @PathVariable UUID userId, 
            Pageable pageable) {
        Page<Transaction> transactions = transactionService.findByUserId(userId, pageable);
        return ResponseEntity.ok(transactions.map(TransactionResponseDto::fromEntity));
    }
    
    // Retry failed transaction
    @PostMapping("/{transactionId}/retry")
    public ResponseEntity<TransactionResponseDto> retryTransaction(@PathVariable UUID transactionId) {
        Transaction transaction = transactionService.retryTransaction(transactionId);
        return ResponseEntity.ok(TransactionResponseDto.fromEntity(transaction));
    }
}