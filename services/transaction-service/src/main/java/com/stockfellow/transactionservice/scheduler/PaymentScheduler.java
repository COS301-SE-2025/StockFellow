package com.stockfellow.transactionservice.scheduler;

import com.stockfellow.transactionservice.dto.CreateTransactionDto;
import com.stockfellow.transactionservice.model.GroupCycle;
import com.stockfellow.transactionservice.model.PayerDetails;
import com.stockfellow.transactionservice.model.Transaction;
import com.stockfellow.transactionservice.model.User;
import com.stockfellow.transactionservice.service.UserService;
import com.stockfellow.transactionservice.service.TransactionService;
import com.stockfellow.transactionservice.repository.GroupCycleRepository;
import com.stockfellow.transactionservice.repository.PayerDetailsRepository;
import com.stockfellow.transactionservice.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Component
public class PaymentScheduler {

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private GroupCycleRepository groupCycleRepository;

    @Autowired
    private PayerDetailsRepository payerDetailsRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private static final Logger logger = LoggerFactory.getLogger(PaymentScheduler.class);

    public PaymentScheduler() {}

    @Scheduled(cron = "0 * * * * ?") // Run every minute for testing (Run daily at 9 AM)
    @Transactional
    public void processScheduledPayments() {
        logger.info("Starting scheduled payment processing");
        
        List<GroupCycle> dueCycles = groupCycleRepository.findByStatusAndCollectionStartDateLessThanEqual("PENDING", LocalDate.now());
        
        logger.info("{} Pending cycles found", dueCycles.size());
        for (GroupCycle cycle : dueCycles) {
            if ("PENDING".equals(cycle.getStatus()) && cycle.getCollectionStartDate().isBefore(LocalDate.now().plusDays(1))) {
                
                logger.info("Processing payments for cycle {} with recipient {}", 
                    cycle.getCycleId(), cycle.getRecipientUserId());
                
                cycle.setStatus("PROCESSING");
                groupCycleRepository.save(cycle);
                
                processPaymentsForCycle(cycle);
            }
        }
    }

    private void processPaymentsForCycle(GroupCycle cycle) {
        logger.info("Processing payments for cycle: {}", cycle.getCycleId());

        try {
            // Fetch all users in the group
            List<User> users = userService.fetchUsers(cycle.getGroupId());
            
            // Remove the recipient from the list (they don't pay, they receive)
            List<User> payingUsers = users.stream()
                .filter(user -> !user.getUserId().equals(cycle.getRecipientUserId()))
                .collect(Collectors.toList());
            
            logger.info("Found {} users to charge for cycle {}", payingUsers.size(), cycle.getCycleId());
            
            // Process each paying user
            for (User user : payingUsers) {
                try {
                    processUserPayment(cycle, user);
                } catch (Exception e) {
                    logger.error("Failed to process payment for user {} in cycle {}: {}", 
                        user.getUserId(), cycle.getCycleId(), e.getMessage());
                    // Continue with other users even if one fails
                }
            }
            
            // Update cycle status after processing all users
            updateCycleStatus(cycle);
            
        } catch (Exception e) {
            logger.error("Error processing payments for cycle {}: {}", cycle.getCycleId(), e.getMessage());
            cycle.setStatus("ERROR");
            groupCycleRepository.save(cycle);
        }
    }

    private void processUserPayment(GroupCycle cycle, User user) {
        logger.info("Processing payment for user {} in cycle {}", user.getUserId(), cycle.getCycleId());
        
        // Check if user already has a completed transaction for this cycle
        List<Transaction> existingTransactions = transactionRepository
            .findByCycleIdAndUserIdAndStatus(cycle.getCycleId(), user.getUserId(), Transaction.TransactionStatus.COMPLETED);
        
        if (!existingTransactions.isEmpty()) {
            logger.info("User {} already has completed transaction for cycle {}", user.getUserId(), cycle.getCycleId());
            return;
        }
        
        // Find user's active payer details (stored card)
        List<PayerDetails> userPayerDetails = payerDetailsRepository.findByUserIdAndIsActiveTrue(user.getUserId());
        
        if (userPayerDetails.isEmpty()) {
            logger.warn("No active payer details found for user {} in cycle {}", user.getUserId(), cycle.getCycleId());
            createFailedTransaction(cycle, user, "No stored payment method found");
            return;
        }
        
        // Use the first active payer details (or implement logic to choose which card)
        PayerDetails payerDetails = userPayerDetails.get(0);
        
        // Check if the card has a valid authorization code
        if (payerDetails.getAuthCode() == null || payerDetails.getAuthCode().trim().isEmpty()) {
            logger.warn("No authorization code found for user {} in cycle {}", user.getUserId(), cycle.getCycleId());
            createFailedTransaction(cycle, user, "No valid authorization code");
            return;
        }
        
        // Create transaction DTO for charging stored card
        CreateTransactionDto createDto = new CreateTransactionDto();
        createDto.setCycleId(cycle.getCycleId());
        createDto.setUserId(user.getUserId());
        createDto.setPayerId(payerDetails.getPayerId());
        createDto.setAmount(cycle.getContributionAmount());
        // Don't need to set PaystackReference here - it's generated in chargeStoredCard
        
        try {
            // Use TransactionService to charge the stored card
            Transaction transaction = transactionService.chargeStoredCard(createDto);
            logger.info("Successfully processed automatic payment for user {} in cycle {}: Transaction {}", 
                user.getUserId(), cycle.getCycleId(), transaction.getTransactionId());
                
        } catch (Exception e) {
            logger.error("Failed to charge stored card for user {} in cycle {}: {}", 
                user.getUserId(), cycle.getCycleId(), e.getMessage());
            createFailedTransaction(cycle, user, "Charge failed: " + e.getMessage());
        }
    }

    // @Scheduled(cron = "*/30 * * * * ?") // Every 30 seconds for testing(Every 30 minutes)
    // @Transactional
    // public void retryFailedPayments() {
    //     logger.info("Starting retry of failed payments");
        
    //     List<GroupCycle> processingCycles = groupCycleRepository.findByStatus("PARTIALLY_COMPLETED");
        
    //     for (GroupCycle cycle : processingCycles) {
    //         logger.info("Retrying failed payments for cycle {}", cycle.getCycleId());
    //         cycle.setStatus("RETRYING");
    //         groupCycleRepository.save(cycle);
            
    //         retryFailedTransactionsForCycle(cycle);
    //     }
    // }

    // @Transactional
    // public void retryFailedTransactionsForCycle(GroupCycle cycle) {
    //     logger.info("Retrying failed transactions for cycle");
        
    //     List<Transaction> failedTransactions = transactionRepository.findByCycleIdAndStatus(cycle.getCycleId(), Transaction.TransactionStatus.FAILED);
        
    //     for (Transaction tx : failedTransactions) {
    //         if ("FAILED".equals(tx.getStatus()) && tx.getRetryCount() < 3) {
                
    //             logger.info("Retrying transaction {}", tx.getTransactionId());
                
    //             // Mock payment processing - replace with actual payment gateway integration
    //             boolean paymentSuccessful = processPayment(tx);
                
    //             if (paymentSuccessful) {
    //                 tx.setStatus(Transaction.TransactionStatus.COMPLETED);
    //                 tx.setCompletedAt(LocalDateTime.now());
    //             } else {
    //                 tx.setStatus(Transaction.TransactionStatus.FAILED);
    //                 tx.setRetryCount(tx.getRetryCount() + 1);
    //                 tx.setFailureReason("Payment failed after retry");
    //             }
                
    //             transactionRepository.save(tx);
                
    //             // If max retries reached, mark as permanently failed
    //             if (tx.getRetryCount() >= 3) {
    //                 tx.setStatus(Transaction.TransactionStatus.CANCELLED);
    //                 tx.setCompletedAt(LocalDateTime.now());
    //                 transactionRepository.save(tx);
    //             }
                
    //             // Small delay between retries
    //             try {
    //                 Thread.sleep(tx.getRetryCount() * 1000);
    //             } catch (InterruptedException e) {
    //                 Thread.currentThread().interrupt();
    //             }
    //         }
    //     }
        
    //     // Update cycle status based on final results
    //     updateCycleStatus(cycle);
    // }

    private void createFailedTransaction(GroupCycle cycle, User user, String reason) {
        // Create a failed transaction record for tracking
        Transaction failedTransaction = new Transaction();
        failedTransaction.setCycleId(cycle.getCycleId());
        failedTransaction.setUserId(user.getUserId());
        failedTransaction.setAmount(cycle.getContributionAmount());
        failedTransaction.setStatus(Transaction.TransactionStatus.FAILED);
        failedTransaction.setFailureReason(reason);
        failedTransaction.setInitiatedAt(LocalDateTime.now());
        failedTransaction.setRetryCount(0);
        
        transactionRepository.save(failedTransaction);
        logger.info("Created failed transaction record for user {} in cycle {}: {}", 
            user.getUserId(), cycle.getCycleId(), reason);
    }

    private void updateCycleStatus(GroupCycle cycle) {
        List<Transaction> cycleTransactions = transactionRepository.findByCycleId(cycle.getCycleId());
        
        BigDecimal received = new BigDecimal(0);
        
        for (Transaction tx : cycleTransactions) {
            if (Transaction.TransactionStatus.COMPLETED == tx.getStatus()) {
                received.add(tx.getAmount());
            }
        }
                
        BigDecimal expectedTotal = cycle.getExpectedTotal();
        
        if (received == expectedTotal) {
            cycle.setStatus("COMPLETED");
        } else {
            cycle.setStatus("PROCESSING");
        }
        
        groupCycleRepository.save(cycle);
    }
}