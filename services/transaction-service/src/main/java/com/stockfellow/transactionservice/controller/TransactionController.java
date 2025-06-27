package com.stockfellow.transactionservice.controller;

import com.stockfellow.transactionservice.model.Transaction;
import com.stockfellow.transactionservice.repository.TransactionRepository;
import com.stockfellow.transactionservice.dto.TransactionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionRepository transactionRepository;

    public TransactionController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    //Get all
    //Get by id
    //Get by cycle
    //Get by transactions by cycle
    //Get transactions by payer
    //Get by status

    // Get all transactions
    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        logger.info("Getting all transactions");
        List<Transaction> transactions = transactionRepository.findAll();
        return ResponseEntity.ok(transactions);
    }

    // Get transaction by ID
    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable UUID transactionId) {
        logger.info("Getting transaction: {}", transactionId);
        return transactionRepository.findById(transactionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get transactions by cycle
    @GetMapping("/cycle/{cycleId}")
    public ResponseEntity<List<Transaction>> getTransactionsByCycle(@PathVariable UUID cycleId) {
        logger.info("Getting transactions for cycle: {}", cycleId);
        List<Transaction> transactions = transactionRepository.findByCycleIdOrderByCreatedAtDesc(cycleId);
        return ResponseEntity.ok(transactions);
    }

    // Get transactions by payer
    @GetMapping("/payer/{payerUserId}")
    public ResponseEntity<List<Transaction>> getTransactionsByPayer(@PathVariable UUID payerUserId) {
        logger.info("Getting transactions for payer: {}", payerUserId);
        List<Transaction> transactions = transactionRepository.findByPayerUserIdOrderByCreatedAtDesc(payerUserId);
        return ResponseEntity.ok(transactions);
    }

    // Get transactions by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Transaction>> getTransactionsByStatus(@PathVariable String status) {
        logger.info("Getting transactions with status: {}", status);
        List<Transaction> transactions = transactionRepository.findByStatus(status);
        return ResponseEntity.ok(transactions);
    }
}