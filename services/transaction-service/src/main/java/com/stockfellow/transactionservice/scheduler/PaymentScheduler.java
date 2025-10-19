package com.stockfellow.transactionservice.scheduler;

import com.stockfellow.transactionservice.dto.CreateTransactionDto;
import com.stockfellow.transactionservice.model.GroupCycle;
import com.stockfellow.transactionservice.model.PayerDetails;
import com.stockfellow.transactionservice.model.Rotation;
import com.stockfellow.transactionservice.model.Transaction;
import com.stockfellow.transactionservice.model.User;
import com.stockfellow.transactionservice.service.UserService;
import com.stockfellow.transactionservice.service.RotationService;
import com.stockfellow.transactionservice.service.TransactionService;
import com.stockfellow.transactionservice.repository.GroupCycleRepository;
import com.stockfellow.transactionservice.repository.PayerDetailsRepository;
import com.stockfellow.transactionservice.repository.TransactionRepository;
import com.stockfellow.transactionservice.repository.UserRepository;
import com.stockfellow.transactionservice.repository.RotationRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Component
public class PaymentScheduler {

    private final RotationService rotationService;

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

    @Autowired
    private RotationRepository rotationRepository;

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(PaymentScheduler.class);

    public PaymentScheduler(RotationService rotationService) {
        this.rotationService = rotationService;
    }

    @Scheduled(cron = "0 * * * * ?") // Run every minute for testing (Run daily at 9 AM)
    @Transactional
    public void processScheduledPayments() {
        logger.info("Starting scheduled payment processing");
        
        LocalDate today = LocalDate.now(ZoneOffset.ofHours(2));
        List<GroupCycle> dueCycles = groupCycleRepository.findByStatusAndCollectionStartDateLessThanEqual("pending", today);

        logger.info("{} Pending cycles found", dueCycles.size());
        for (GroupCycle cycle : dueCycles) {
            if ("pending".equals(cycle.getStatus()) && cycle.getCollectionStartDate().isBefore(today.plusDays(1))) {
                
                logger.info("Processing payments for cycle {} with recipient {}", 
                    cycle.getCycleId(), cycle.getRecipientUserId());
                
                cycle.setStatus("processing");
                groupCycleRepository.save(cycle);
                
                processPaymentsForCycle(cycle);
            } else {
                logger.info("Cycle doesnt meet criteria. Status: {} Check date: {} Actual_Date: {}",
                    cycle.getStatus(), today.plusDays(1), cycle.getCollectionStartDate());                
            }
        }
    }

    @Scheduled(cron = "0 * * * * ?") // Run every minute for testing (Run daily at 9 AM: "0 0 9 * * ?")
    @Transactional
    public void processRotations() {
        logger.info("Starting rotation processing");
        
        List<Rotation> rotations = rotationRepository.findAll();
        logger.info("{} rotations found for processing", rotations.size());
        
        for (Rotation rotation : rotations) {
            try {
                String status = rotation.getStatus();
                
                if ("complete".equalsIgnoreCase(status)) {
                    logger.debug("Rotation {} is already complete, skipping", rotation.getId());
                    // Nothing to do for completed rotations
                } else if ("inactive".equalsIgnoreCase(status) || "pending".equalsIgnoreCase(status)) {
                    processInactiveRotation(rotation);
                } else if ("active".equalsIgnoreCase(status)) {
                    processActiveRotation(rotation);
                }
            } catch (Exception e) {
                logger.error("Error processing rotation {}: {}", rotation.getId(), e.getMessage(), e);
                // Continue processing other rotations even if one fails
            }
        }
        
        logger.info("Completed rotation processing");
    }

    /**
     * Processes inactive/pending rotations - activates them if they have enough members
     */
    private void processInactiveRotation(Rotation rotation) {
        logger.info("Processing inactive rotation {} for group {}", 
            rotation.getId(), rotation.getGroupId());
        
        // Check if rotation has at least 2 members
        if (rotation.getMemberIds().length >= 2) {
            logger.info("Rotation {} has {} members, activating and creating first cycle", 
                rotation.getId(), rotation.getMemberIds().length);
            
            // Change status to ACTIVE
            rotation.setStatus("active");
            
            try {
                GroupCycle cycle = rotationService.createGroupCycle(rotation);
                logger.info("Successfully created first group cycle {} for rotation {}", 
                    cycle.getCycleId(), rotation.getId());
                
                rotationRepository.save(rotation);
            } catch (Exception e) {
                logger.error("Failed to create group cycle for rotation {}: {}", 
                    rotation.getId(), e.getMessage());
                throw new RuntimeException("Failed to activate rotation", e);
            }
        } else {
            logger.info("Rotation {} only has {} member(s), needs at least 2 to activate", 
                rotation.getId(), rotation.getMemberIds().length);
        }
    }

    /**
     * Processes active rotations - checks if all members have received payout
     */
    private void processActiveRotation(Rotation rotation) {
        logger.info("Processing active rotation {} for group {}", 
            rotation.getId(), rotation.getGroupId());
        
        // Check if all members have received their payout
        if (rotation.getPosition() >= rotation.getMemberIds().length - 1) {
            logger.info("Rotation {} has completed all payouts (position: {}, members: {})", 
                rotation.getId(), rotation.getPosition(), rotation.getMemberIds().length);
            
            rotation.setStatus("complete");
            rotationRepository.save(rotation);
            
            logger.info("Rotation {} marked as COMPLETE", rotation.getId());
        } else {
            logger.debug("Rotation {} still has {} members remaining", 
                rotation.getId(), 
                rotation.getMemberIds().length - rotation.getPosition() - 1);
        }
    }


    private void processPaymentsForCycle(GroupCycle cycle) {
        logger.info("Processing payments for cycle: {}", cycle.getCycleId());

        try {
            // Remove the recipient from the list (they don't pay, they receive)
            UUID[] memberIdsArray = cycle.getMemberIds();
            
            if (memberIdsArray == null || memberIdsArray.length == 0) {
                logger.warn("No member IDs found for cycle {}", cycle.getCycleId());
                return;
            }

            List<UUID> payingUserIds = Arrays.stream(memberIdsArray)
            .filter(memberId -> !memberId.equals(cycle.getRecipientUserId()))
            .collect(Collectors.toList());
            
            logger.info("Found {} users to charge for cycle {} (excluding recipient)", 
                payingUserIds.size(), cycle.getCycleId());
            

            List<User> payingUsers = userRepository.findAllById(payingUserIds);

            if (payingUsers.isEmpty()) {
                logger.warn("No paying users found in database for cycle {}", cycle.getCycleId());
                return;
            }
            
            logger.info("Successfully fetched {} user records", payingUsers.size());
            
            // Process each paying user
            for (User user : payingUsers) {
                try {
                    processUserPayment(cycle, user);
                } catch (Exception e) {
                    logger.error("Failed to process payment for user {} in cycle {}: {}", 
                        user.getUserId(), cycle.getCycleId(), e.getMessage());
                }
            }
            
            // Update cycle status after processing all users
            updateCycleStatus(cycle);
            
        } catch (Exception e) {
            logger.error("Error processing payments for cycle {}: {}", cycle.getCycleId(), e.getMessage());
            cycle.setStatus("error");
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
            cycle.setStatus("completed");
            updateRotation(cycle.getRotationId()); 
            
        } else {
            cycle.setStatus("processing");
        }
        
        groupCycleRepository.save(cycle);
    }

    private void updateRotation(UUID id) {
        rotationService.updateRotation(id);
    }
}