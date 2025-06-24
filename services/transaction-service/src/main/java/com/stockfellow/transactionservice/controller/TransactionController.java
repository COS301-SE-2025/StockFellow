package com.stockfellow.transactionservice.controller;

import com.stockfellow.transactionservice.model.Transaction;
import com.stockfellow.transactionservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionRepository transactionRepository;

    // Get all transactions

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        log.info("Getting all transactions");
        List<Transaction> transactions = transactionRepository.findAll();
        return ResponseEntity.ok(transactions);
    }

    // Get transaction by ID

    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable UUID transactionId) {
        log.info("Getting transaction: {}", transactionId);
        return transactionRepository.findById(transactionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get transactions by cycle

    @GetMapping("/cycle/{cycleId}")
    public ResponseEntity<List<Transaction>> getTransactionsByCycle(@PathVariable UUID cycleId) {
        log.info("Getting transactions for cycle: {}", cycleId);
        List<Transaction> transactions = transactionRepository.findByCycleIdOrderByCreatedAtDesc(cycleId);
        return ResponseEntity.ok(transactions);
    }

    // Get transactions by payer

    @GetMapping("/payer/{payerUserId}")
    public ResponseEntity<List<Transaction>> getTransactionsByPayer(@PathVariable UUID payerUserId) {
        log.info("Getting transactions for payer: {}", payerUserId);
        List<Transaction> transactions = transactionRepository.findByPayerUserIdOrderByCreatedAtDesc(payerUserId);
        return ResponseEntity.ok(transactions);
    }

    // Get transactions by status

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Transaction>> getTransactionsByStatus(@PathVariable String status) {
        log.info("Getting transactions with status: {}", status);
        List<Transaction> transactions = transactionRepository.findByStatus(status);
        return ResponseEntity.ok(transactions);
    }
}
