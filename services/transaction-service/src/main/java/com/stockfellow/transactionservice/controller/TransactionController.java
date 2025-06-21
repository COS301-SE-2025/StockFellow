package com.stockfellow.transactionservice.controller;

import com.stockfellow.transactionservice.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionService transactionService;

    @GetMapping
    public Map<String, Object> getServiceInfo() {
        return Map.of(
            "service", "Transaction Service",
            "version", "1.0.0",
            "endpoints", new String[]{
                "POST /api/transactions/users - Create user",
                "POST /api/transactions/mandates - Create mandate",
                "POST /api/transactions/debit-orders - Process debit order",
                "POST /api/transactions/payouts - Process payout",
                "POST /api/transactions/schedules - Schedule transaction"
            }
        );
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            String email = request.get("email");
            String phone = request.get("phone");
            String idNumber = request.get("idNumber");
            String financialTier = transactionService.createUser(userId, email, phone, idNumber);
            return ResponseEntity.ok(Map.of("userId", userId, "financialTier", financialTier));
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/mandates")
    public ResponseEntity<?> createMandate(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            String bankAccount = request.get("bankAccount");
            String mandateId = transactionService.createMandate(userId, bankAccount);
            return ResponseEntity.ok(Map.of("mandateId", mandateId));
        } catch (Exception e) {
            logger.error("Error creating mandate: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/debit-orders")
    public ResponseEntity<?> processDebitOrder(@RequestBody Map<String, Object> request) {
        try {
            String userId = (String) request.get("userId");
            String groupId = (String) request.get("groupId");
            Double amount = Double.parseDouble(request.get("amount").toString());
            String transactionId = transactionService.processDebitOrder(userId, groupId, amount);
            return ResponseEntity.ok(Map.of("transactionId", transactionId));
        } catch (Exception e) {
            logger.error("Error processing debit order: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/payouts")
    public ResponseEntity<?> processPayout(@RequestBody Map<String, Object> request) {
        try {
            String userId = (String) request.get("userId");
            String groupId = (String) request.get("groupId");
            Double amount = Double.parseDouble(request.get("amount").toString());
            String transactionId = transactionService.processPayout(userId, groupId, amount);
            return ResponseEntity.ok(Map.of("transactionId", transactionId));
        } catch (Exception e) {
            logger.error("Error processing payout: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/schedules")
    public ResponseEntity<?> scheduleTransaction(@RequestBody Map<String, Object> request) {
        try {
            String userId = (String) request.get("userId");
            String groupId = (String) request.get("groupId");
            String type = (String) request.get("type");
            Double amount = Double.parseDouble(request.get("amount").toString());
            String frequency = (String) request.get("frequency");
            LocalDate nextRun = LocalDate.parse((String) request.get("nextRun"));
            String scheduleId = transactionService.scheduleTransaction(userId, groupId, type, amount, frequency, nextRun);
            return ResponseEntity.ok(Map.of("scheduleId", scheduleId));
        } catch (Exception e) {
            logger.error("Error scheduling transaction: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
