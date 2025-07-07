package com.stockfellow.transactionservice.controller;

import com.stockfellow.transactionservice.model.Transaction;
import com.stockfellow.transactionservice.repository.TransactionRepository;
import com.stockfellow.transactionservice.dto.TransactionResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Transactions", description = "Operations related to financial transactions")
@RequestMapping("/api/transactions")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionRepository transactionRepository;

    public TransactionController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping
    @Operation(summary = "Get all transactions", 
               description = "Retrieves a list of all transactions in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of transactions retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = Transaction.class)))
    })
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        logger.info("Getting all transactions");
        List<Transaction> transactions = transactionRepository.findAll();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{transactionId}")
    @Operation(summary = "Get transaction by ID", 
               description = "Retrieves a specific transaction by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = Transaction.class))),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<Transaction> getTransaction(
            @Parameter(description = "The unique identifier of the transaction", required = true)
            @PathVariable UUID transactionId) {
        logger.info("Getting transaction: {}", transactionId);
        return transactionRepository.findById(transactionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cycle/{cycleId}")
    @Operation(summary = "Get transactions by cycle", 
               description = "Retrieves all transactions associated with a specific group cycle, ordered by creation date (newest first)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cycle transactions retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = Transaction.class)))
    })
    public ResponseEntity<List<Transaction>> getTransactionsByCycle(
            @Parameter(description = "The unique identifier of the cycle", required = true)
            @PathVariable UUID cycleId) {
        logger.info("Getting transactions for cycle: {}", cycleId);
        List<Transaction> transactions = transactionRepository.findByCycleIdOrderByCreatedAtDesc(cycleId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/payer/{payerUserId}")
    @Operation(summary = "Get transactions by payer", 
               description = "Retrieves all transactions made by a specific payer, ordered by creation date (newest first)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payer transactions retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = Transaction.class)))
    })
    public ResponseEntity<List<Transaction>> getTransactionsByPayer(
            @Parameter(description = "The unique identifier of the payer user", required = true)
            @PathVariable UUID payerUserId) {
        logger.info("Getting transactions for payer: {}", payerUserId);
        List<Transaction> transactions = transactionRepository.findByPayerUserIdOrderByCreatedAtDesc(payerUserId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get transactions by status", 
               description = "Retrieves all transactions with a specific status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transactions with specified status retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = Transaction.class)))
    })
    public ResponseEntity<List<Transaction>> getTransactionsByStatus(
            @Parameter(description = "The status of the transactions to retrieve (PENDING, COMPLETED, FAILED, CANCELLED)", 
                      required = true)
            @PathVariable String status) {
        logger.info("Getting transactions with status: {}", status);
        List<Transaction> transactions = transactionRepository.findByStatus(status);
        return ResponseEntity.ok(transactions);
    }
}