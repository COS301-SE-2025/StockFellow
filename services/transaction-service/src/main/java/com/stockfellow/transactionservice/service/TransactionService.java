package com.stockfellow.transactionservice.service;

import com.stockfellow.transactionservice.dto.CreateTransactionDto;
import com.stockfellow.transactionservice.dto.ProcessTransactionDto;
import com.stockfellow.transactionservice.model.*;
import com.stockfellow.transactionservice.repository.*;
import com.stockfellow.transactionservice.integration.PaystackService;
import com.stockfellow.transactionservice.integration.dto.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Service
@Transactional
public class TransactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private GroupCycleRepository groupCycleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PayerDetailsRepository payerDetailsRepository;
    
    @Autowired
    private ActivityLogService activityLogService;
    
    @Autowired
    private PaystackService paystackService;
    
    @Value("${app.transaction.max-retry-count:3}")
    private Integer maxRetryCount;
    
    @Value("${app.paystack.callback-url}")
    private String paystackCallbackUrl;

    /**
     * Create a new transaction and initiate payment
     */
    public Transaction createTransaction(CreateTransactionDto createDto) {
        logger.info("Creating transaction for cycle: {} and user: {}", createDto.getCycleId(), createDto.getUserId());
        
        // Validate cycle exists and is active
        GroupCycle cycle = validateCycleForTransaction(createDto.getCycleId());
        
        // Validate user exists
        User user = userRepository.findById(createDto.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + createDto.getUserId()));
        
        // Validate payer details exist and are active
        PayerDetails payerDetails = validatePayerDetails(createDto.getPayerId(), createDto.getUserId());
        
        // Validate amount matches cycle contribution amount
        if (!createDto.getAmount().equals(cycle.getContributionAmount())) {
            throw new RuntimeException("Transaction amount must match cycle contribution amount");
        }
        
        // Check if user already has a successful transaction for this cycle
        List<Transaction> existingTransactions = transactionRepository
            .findByCycleIdAndUserIdAndStatus(createDto.getCycleId(), createDto.getUserId(), Transaction.TransactionStatus.COMPLETED);
        if (!existingTransactions.isEmpty()) {
            throw new RuntimeException("User has already completed a transaction for this cycle");
        }
        
        // Create transaction entity
        Transaction transaction = new Transaction();
        transaction.setCycleId(createDto.getCycleId());
        transaction.setUserId(createDto.getUserId());
        transaction.setPayerId(createDto.getPayerId());
        transaction.setPaystackReference(createDto.getPaystackReference());
        transaction.setAmount(createDto.getAmount());
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        transaction.setRetryCount(0);
        
        // Save transaction
        transaction = transactionRepository.save(transaction);
        
        // Log activity
        // activityLogService.logActivity(
        //     createDto.getUserId(), 
        //     createDto.getCycleId(),
        //     ActivityLog.EntityType.TRANSACTION, 
        //     transaction.getTransactionId(),
        //     "TRANSACTION_CREATED", 
        //     null, 
        //     null
        // );
        
        // Initiate payment with Paystack
        try {
            initiatePaystackPayment(transaction, user, payerDetails, cycle);
        } catch (Exception e) {
            logger.error("Failed to initiate payment: {}", e.getMessage());
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setFailureReason("Failed to initiate payment: " + e.getMessage());
            transaction = transactionRepository.save(transaction);
        }
        
        logger.info("Transaction created successfully with ID: {}", transaction.getTransactionId());
        return transaction;
    }

    /**
     * Process transaction response from payment gateway
     */
    public Transaction processTransaction(UUID transactionId, ProcessTransactionDto processDto) {
        logger.info("Processing transaction: {} with status: {}", transactionId, processDto.getStatus());
        
        Transaction transaction = findById(transactionId);
        Transaction.TransactionStatus oldStatus = transaction.getStatus();
        
        // Update transaction with gateway response
        transaction.setPaystackTransId(processDto.getPaystackTransId());
        transaction.setStatus(processDto.getStatus());
        transaction.setGatewayStatus(processDto.getGatewayStatus());
        transaction.setFailureReason(processDto.getFailureReason());
        
        // Set completion time for successful transactions
        if (processDto.getStatus() == Transaction.TransactionStatus.COMPLETED) {
            transaction.setCompletedAt(LocalDateTime.now());
        }
        
        transaction = transactionRepository.save(transaction);
        
        // Handle status change actions
        handleTransactionStatusChange(transaction, oldStatus);
        
        // Log activity
        // activityLogService.logActivity(
        //     transaction.getUserId(), 
        //     transaction.getCycleId(),
        //     ActivityLog.EntityType.TRANSACTION, 
        //     transaction.getTransactionId(),
        //     "TRANSACTION_PROCESSED", 
        //     null, 
        //     null
        // );
        
        logger.info("Transaction processed successfully: {}", transactionId);
        return transaction;
    }

    /**
     * Verify transaction with Paystack
     */
    public Transaction verifyTransaction(String reference) {
        logger.info("Verifying transaction with reference: {}", reference);
        
        // Find transaction by reference
        Transaction transaction = transactionRepository.findByPaystackReference(reference)
            .orElseThrow(() -> new RuntimeException("Transaction not found with reference: " + reference));
        
        try {
            // Verify with Paystack
            PaystackTransactionVerificationResponse verification = paystackService.verifyTransaction(reference);
            
            if (verification.getStatus()) {
                PaystackTransactionVerificationResponse.PaystackTransactionVerificationData data = verification.getData();
                
                // Update transaction with verification data
                transaction.setPaystackTransId(data.getId().toString());
                transaction.setGatewayStatus(data.getGatewayResponse());
                
                if ("success".equals(data.getStatus())) {
                    transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
                    transaction.setCompletedAt(LocalDateTime.now());
                    
                    // Update payer details with authorization data if available
                    updatePayerDetailsFromAuthorization(transaction.getPayerId(), data.getAuthorization());
                    
                } else {
                    transaction.setStatus(Transaction.TransactionStatus.FAILED);
                    transaction.setFailureReason(data.getMessage());
                }
            } else {
                transaction.setStatus(Transaction.TransactionStatus.FAILED);
                transaction.setFailureReason(verification.getMessage());
            }
            
            transaction = transactionRepository.save(transaction);
            
            // Handle status changes
            handleTransactionStatusChange(transaction, Transaction.TransactionStatus.PENDING);
            
        } catch (Exception e) {
            logger.error("Failed to verify transaction: {}", e.getMessage());
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setFailureReason("Verification failed: " + e.getMessage());
            transaction = transactionRepository.save(transaction);
        }
        
        logger.info("Transaction verification completed: {}", reference);
        return transaction;
    }

    /**
     * Retry a failed transaction
     */
    public Transaction retryTransaction(UUID transactionId) {
        logger.info("Retrying transaction: {}", transactionId);
        
        Transaction transaction = findById(transactionId);
        
        // Validate transaction can be retried
        if (transaction.getStatus() != Transaction.TransactionStatus.FAILED) {
            throw new RuntimeException("Transaction is not in FAILED status");
        }
        
        if (transaction.getRetryCount() >= maxRetryCount) {
            throw new RuntimeException("Maximum retry count exceeded for transaction: " + transactionId);
        }
        
        // Increment retry count
        transaction.setRetryCount(transaction.getRetryCount() + 1);
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        transaction.setFailureReason(null);
        transaction.setGatewayStatus(null);
        
        transaction = transactionRepository.save(transaction);
        
        // Get necessary data for retry
        User user = userRepository.findById(transaction.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        PayerDetails payerDetails = payerDetailsRepository.findById(transaction.getPayerId())
            .orElseThrow(() -> new RuntimeException("Payer details not found"));
        GroupCycle cycle = groupCycleRepository.findById(transaction.getCycleId())
            .orElseThrow(() -> new RuntimeException("Cycle not found"));
        
        try {
            initiatePaystackPayment(transaction, user, payerDetails, cycle);
        } catch (Exception e) {
            logger.error("Failed to retry transaction: {}", e.getMessage());
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setFailureReason("Retry failed: " + e.getMessage());
            transaction = transactionRepository.save(transaction);
        }
        
        // Log activity
        // activityLogService.logActivity(
        //     transaction.getUserId(), 
        //     transaction.getCycleId(),
        //     ActivityLog.EntityType.TRANSACTION, 
        //     transaction.getTransactionId(),
        //     "TRANSACTION_RETRIED", 
        //     null, 
        //     null
        // );
        
        logger.info("Transaction retry initiated: {}", transactionId);
        return transaction;
    }

    /**
     * Find transaction by ID
     */
    @Transactional(readOnly = true)
    public Transaction findById(UUID transactionId) {
        return transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + transactionId));
    }

    /**
     * Find transactions by cycle ID (paginated)
     */
    @Transactional(readOnly = true)
    public Page<Transaction> findByCycleId(UUID cycleId, Pageable pageable) {
        return transactionRepository.findByCycleId(cycleId, pageable);
    }

    /**
     * Find transactions by user ID (paginated)
     */
    @Transactional(readOnly = true)
    public Page<Transaction> findByUserId(UUID userId, Pageable pageable) {
        return transactionRepository.findByUserId(userId, pageable);
    }

    /**
     * Find transactions by status
     */
    @Transactional(readOnly = true)
    public List<Transaction> findByStatus(Transaction.TransactionStatus status) {
        return transactionRepository.findByStatus(status);
    }

    /**
     * Get transaction statistics for a cycle
     */
    @Transactional(readOnly = true)
    public TransactionStatistics getTransactionStatistics(UUID cycleId) {
        List<Object[]> stats = transactionRepository.getTransactionStatisticsByCycle(cycleId);
        return TransactionStatistics.fromRepositoryResult(stats);
    }

    /**
     * Process automatic retries for failed transactions
     */
    @Transactional
    public void processAutomaticRetries() {
        logger.info("Processing automatic retries for failed transactions");
        
        List<Transaction> retryableTransactions = transactionRepository.findRetryableTransactions(maxRetryCount);
        
        for (Transaction transaction : retryableTransactions) {
            try {
                // Only retry if transaction failed more than 30 minutes ago (avoid rapid retries)
                if (transaction.getUpdatedAt() != null && 
                    transaction.getUpdatedAt().isBefore(LocalDateTime.now().minusMinutes(30))) {
                    retryTransaction(transaction.getTransactionId());
                }
            } catch (Exception e) {
                logger.error("Failed to retry transaction: {}", transaction.getTransactionId(), e);
            }
        }
    }

    /**
     * Handle stale pending transactions
     */
    @Transactional
    public void handleStalePendingTransactions() {
        logger.info("Handling stale pending transactions");
        
        // Find transactions pending for more than 1 hour
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);
        List<Transaction> staleTransactions = transactionRepository.findStalePendingTransactions(cutoffTime);
        
        for (Transaction transaction : staleTransactions) {
            try {
                // Verify the transaction status with Paystack
                verifyTransaction(transaction.getPaystackReference());
            } catch (Exception e) {
                logger.error("Failed to verify stale transaction: {}", transaction.getTransactionId(), e);
                // Mark as failed if verification fails
                transaction.setStatus(Transaction.TransactionStatus.FAILED);
                transaction.setFailureReason("Transaction verification timeout");
                transactionRepository.save(transaction);
            }
        }
    }

    // ===== PRIVATE HELPER METHODS =====

    /**
     * Validate cycle for transaction
     */
    private GroupCycle validateCycleForTransaction(UUID cycleId) {
        GroupCycle cycle = groupCycleRepository.findById(cycleId)
            .orElseThrow(() -> new RuntimeException("Cycle not found with ID: " + cycleId));
        
        if (cycle.getStatus() != GroupCycle.CycleStatus.ACTIVE && 
            cycle.getStatus() != GroupCycle.CycleStatus.COLLECTING) {
            throw new RuntimeException("Cycle is not accepting transactions. Status: " + cycle.getStatus());
        }
        
        // Check if cycle collection period is valid
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(cycle.getCollectionStartDate().atStartOfDay()) || 
            now.isAfter(cycle.getCollectionEndDate().atTime(23, 59, 59))) {
            throw new RuntimeException("Cycle is outside collection period");
        }
        
        return cycle;
    }

    /**
     * Validate payer details
     */
    private PayerDetails validatePayerDetails(UUID payerId, UUID userId) {
        PayerDetails payerDetails = payerDetailsRepository.findById(payerId)
            .orElseThrow(() -> new RuntimeException("Payer details not found with ID: " + payerId));
        
        if (!payerDetails.getUserId().equals(userId)) {
            throw new RuntimeException("Payer details do not belong to the specified user");
        }
        
        if (!payerDetails.getIsActive()) {
            throw new RuntimeException("Payer details are not active");
        }
        
        return payerDetails;
    }

    /**
     * Initiate payment with Paystack
     */
    private void initiatePaystackPayment(Transaction transaction, User user, PayerDetails payerDetails, GroupCycle cycle) {
        logger.info("Initiating Paystack payment for transaction: {}", transaction.getTransactionId());
        
        // Create Paystack transaction request
        PaystackTransactionRequest request = new PaystackTransactionRequest();
        request.setEmail(user.getEmail());
        request.setAmount(transaction.getAmount().multiply(new BigDecimal("100")).intValue()); // Convert to kobo
        request.setReference(transaction.getPaystackReference());
        request.setCallbackUrl(paystackCallbackUrl);
        
        // Add metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("transaction_id", transaction.getTransactionId().toString());
        metadata.put("cycle_id", cycle.getCycleId().toString());
        metadata.put("user_id", user.getUserId().toString());
        metadata.put("cycle_period", cycle.getCyclePeriod());
        request.setMetadata(metadata);
        
        try {
            PaystackTransactionResponse response = paystackService.initializeTransaction(request);
            
            if (response.getStatus()) {
                // Transaction initialized successfully
                transaction.setStatus(Transaction.TransactionStatus.PENDING);
                transaction.setInitiatedAt(LocalDateTime.now());
                
                transactionRepository.save(transaction);
                
                // Send notification with payment link
                // notificationService.sendPaymentInitiatedNotification(transaction, response.getData().getAuthorizationUrl());
                
                logger.info("Paystack payment initiated successfully: {}", transaction.getTransactionId());
            } else {
                throw new RuntimeException("Paystack initialization failed: " + response.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("Failed to initiate Paystack payment: {}", e.getMessage());
            throw new RuntimeException("Failed to initiate payment with Paystack: " + e.getMessage());
        }
    }

    /**
     * Update payer details with authorization data from successful payment
     */
    private void updatePayerDetailsFromAuthorization(UUID payerId, PaystackAuthorization authorization) {
        if (authorization == null) return;
        
        try {
            PayerDetails payerDetails = payerDetailsRepository.findById(payerId)
                .orElseThrow(() -> new RuntimeException("Payer details not found"));
            
            // Update card details from authorization
            payerDetails.setAuthCode(authorization.getAuthorizationCode());
            payerDetails.setLast4(authorization.getLast4());
            payerDetails.setExpMonth(authorization.getExpMonth());
            payerDetails.setExpYear(authorization.getExpYear());
            payerDetails.setCardType(authorization.getCardType());
            payerDetails.setBank(authorization.getBank());
            payerDetails.setBin(authorization.getBin());
            payerDetails.setSignature(authorization.getSignature());
            payerDetails.setIsAuthenticated(true);
            
            payerDetailsRepository.save(payerDetails);
            
            logger.info("Updated payer details with authorization data: {}", payerId);
            
        } catch (Exception e) {
            logger.error("Failed to update payer details with authorization: {}", e.getMessage());
        }
    }

    /**
     * Handle transaction status changes
     */
    private void handleTransactionStatusChange(Transaction transaction, Transaction.TransactionStatus oldStatus) {
        if (transaction.getStatus() == Transaction.TransactionStatus.COMPLETED && oldStatus != Transaction.TransactionStatus.COMPLETED) {
            // Update cycle totals
            updateCycleTotals(transaction.getCycleId());
            
            // Send success notification
            // notificationService.sendTransactionCompletedNotification(transaction);
            
        } else if (transaction.getStatus() == Transaction.TransactionStatus.FAILED) {
            // Send failure notification
            // notificationService.sendTransactionFailedNotification(transaction);
        }
    }

    /**
     * Update cycle totals after successful transaction
     */
    private void updateCycleTotals(UUID cycleId) {
        Optional<GroupCycle> cycleOpt = groupCycleRepository.findById(cycleId);
        if (cycleOpt.isPresent()) {
            GroupCycle cycle = cycleOpt.get();
            
            // Calculate totals
            BigDecimal completedAmount = transactionRepository.getTotalAmountByCycleAndStatus(
                cycleId, Transaction.TransactionStatus.COMPLETED);
            long successfulCount = transactionRepository.countByCycleIdAndStatus(
                cycleId, Transaction.TransactionStatus.COMPLETED);
            long failedCount = transactionRepository.countByCycleIdAndStatus(
                cycleId, Transaction.TransactionStatus.FAILED);
            long pendingCount = transactionRepository.countByCycleIdAndStatus(
                cycleId, Transaction.TransactionStatus.PENDING);
            
            // Update cycle
            cycle.setCurrentTotal(completedAmount);
            cycle.setSuccessfulCount((int) successfulCount);
            cycle.setFailedCount((int) failedCount);
            cycle.setPendingCount((int) pendingCount);
            
            // Check if collection is complete
            if (completedAmount.compareTo(cycle.getExpectedTotal()) >= 0) {
                cycle.setStatus(GroupCycle.CycleStatus.COLLECTION_COMPLETE);
            }
            
            groupCycleRepository.save(cycle);
        }
    }

    /**
     * Generate unique transaction reference
     */
    private String generateTransactionReference(Transaction transaction) {
        return "TXN-" + transaction.getTransactionId().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    // ===== INNER CLASSES =====

    /**
     * Transaction statistics wrapper
     */
    public static class TransactionStatistics {
        private long totalTransactions;
        private long completedTransactions;
        private long failedTransactions;
        private long pendingTransactions;
        private BigDecimal totalAmount;
        private BigDecimal completedAmount;
        
        public static TransactionStatistics fromRepositoryResult(List<Object[]> stats) {
            TransactionStatistics transactionStats = new TransactionStatistics();
            transactionStats.totalAmount = BigDecimal.ZERO;
            transactionStats.completedAmount = BigDecimal.ZERO;
            
            for (Object[] stat : stats) {
                Transaction.TransactionStatus status = (Transaction.TransactionStatus) stat[0];
                Long count = (Long) stat[1];
                BigDecimal amount = (BigDecimal) stat[2];
                
                transactionStats.totalTransactions += count;
                transactionStats.totalAmount = transactionStats.totalAmount.add(amount);
                
                switch (status) {
                    case COMPLETED:
                        transactionStats.completedTransactions = count;
                        transactionStats.completedAmount = amount;
                        break;
                    case FAILED:
                    case CANCELLED:
                        transactionStats.failedTransactions += count;
                        break;
                    case PENDING:
                    case PROCESSING:
                        transactionStats.pendingTransactions += count;
                        break;
                }
            }
            
            return transactionStats;
        }

        // Getters
        public long getTotalTransactions() { return totalTransactions; }
        public long getCompletedTransactions() { return completedTransactions; }
        public long getFailedTransactions() { return failedTransactions; }
        public long getPendingTransactions() { return pendingTransactions; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public BigDecimal getCompletedAmount() { return completedAmount; }
    }
}