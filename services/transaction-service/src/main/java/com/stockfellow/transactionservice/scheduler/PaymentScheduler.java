package com.stockfellow.transactionservice.scheduler;

import com.stockfellow.transactionservice.model.GroupCycle;
import com.stockfellow.transactionservice.model.Transaction;
import com.stockfellow.transactionservice.repository.GroupCycleRepository;
import com.stockfellow.transactionservice.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

@Component
public class PaymentScheduler {

    // private static final Logger logger = LoggerFactory.getLogger(PaymentScheduler.class);
    // private final GroupCycleRepository groupCycleRepository;
    // private final MandateRepository mandateRepository;
    // private final TransactionRepository transactionRepository;

    // public PaymentScheduler(GroupCycleRepository groupCycleRepository,
    //                        MandateRepository mandateRepository,
    //                        TransactionRepository transactionRepository) {
    //     this.groupCycleRepository = groupCycleRepository;
    //     this.mandateRepository = mandateRepository;
    //     this.transactionRepository = transactionRepository;
    // }

    // @Scheduled(cron = "0 * * * * ?") // Run every minute for testing (Run daily at 9 AM)
    // @Transactional
    // public void processScheduledPayments() {
    //     logger.info("Starting scheduled payment processing");
        
    //     List<GroupCycle> dueCycles = groupCycleRepository
    //         .findByStatusAndCollectionDateLessThanEqual("PENDING", LocalDate.now());
        
    //     logger.info("{} Pending cycles found", dueCycles.size());
    //     for (GroupCycle cycle : dueCycles) {
    //         if ("PENDING".equals(cycle.getStatus()) && 
    //             cycle.getCollectionDate().isBefore(LocalDate.now().plusDays(1))) {
                
    //             logger.info("Processing payments for cycle {} with recipient {}", 
    //                 cycle.getCycleId(), cycle.getRecipientUserId());
                
    //             cycle.setStatus("PROCESSING");
    //             groupCycleRepository.save(cycle);
                
    //             processPaymentsForCycle(cycle);
    //         }
    //     }
    // }

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
        
    //     List<Transaction> failedTransactions = transactionRepository
    //         .findByCycleIdAndStatus(cycle.getCycleId(), "FAILED");
        
    //     for (Transaction tx : failedTransactions) {
    //         if ("FAILED".equals(tx.getStatus()) && tx.getRetryCount() < 3) {
                
    //             logger.info("Retrying transaction {}", tx.getTransactionId());
                
    //             // Mock payment processing - replace with actual payment gateway integration
    //             boolean paymentSuccessful = processPayment(tx);
                
    //             if (paymentSuccessful) {
    //                 tx.setStatus("COMPLETED");
    //                 tx.setCompletedAt(LocalDateTime.now());
    //             } else {
    //                 tx.setStatus("FAILED");
    //                 tx.setRetryCount(tx.getRetryCount() + 1);
    //                 tx.setFailMessage("Payment failed after retry");
    //             }
                
    //             transactionRepository.save(tx);
                
    //             // If max retries reached, mark as permanently failed
    //             if (tx.getRetryCount() >= 3) {
    //                 tx.setStatus("PERMANENTLY_FAILED");
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

    // private void processPaymentsForCycle(GroupCycle cycle) {
    //     logger.info("Porcessing payments for cycle: {}", cycle.getCycleId());
    //     List<Mandate> activeMandates = mandateRepository.findActiveMandatesByGroupId(cycle.getGroupId())
    //         .stream()
    //         .filter(m -> "ACTIVE".equals(m.getStatus()))
    //         .filter(m -> !m.getPayerUserId().equals(cycle.getRecipientUserId()))
    //         .toList();

    //     for (Mandate mandate : activeMandates) {
    //         // Create transaction record
    //         logger.info("Creating transaction");
    //         Transaction tx = new Transaction();
    //         tx.setTransactionId(UUID.randomUUID());
    //         tx.setCycleId(cycle.getCycleId());
    //         tx.setMandateId(mandate.getMandateId());
    //         tx.setPayerUserId(mandate.getPayerUserId());
    //         tx.setRecipientUserId(cycle.getRecipientUserId());
    //         tx.setGroupId(cycle.getGroupId());
    //         tx.setPayerPaymentMethodId(mandate.getPaymentMethodId());
    //         tx.setRecipientPaymentMethodId(cycle.getRecipientPaymentMethodId());
    //         tx.setAmount(cycle.getContributionAmount());
    //         tx.setStatus("PENDING");
    //         tx.setRetryCount(0);
    //         tx.setCreatedAt(LocalDateTime.now());
            
    //         logger.info("Saving transaction");
    //         transactionRepository.save(tx);
            
    //         // Process payment
    //         boolean paymentSuccessful = processPayment(tx);
            
    //         if (paymentSuccessful) {
    //             logger.info("Payment Successful");
    //             tx.setStatus("COMPLETED");
    //             tx.setCompletedAt(LocalDateTime.now());
    //         } else {
    //             logger.info("Payment Unsuccessful");
    //             tx.setStatus("FAILED");
    //             tx.setRetryCount(tx.getRetryCount() + 1);
    //             tx.setFailMessage("Initial payment failed");
    //         }
            
    //         transactionRepository.save(tx);
    //     }
        
    //     updateCycleStatus(cycle);
    // }

    // private boolean processPayment(Transaction transaction) {
    //     // Mock payment processing - replace with actual payment gateway integration
    //     // For demo purposes, randomly succeed/fail
    //     return Math.random() > 0.1; // 90% success rate
    // }

    // private void updateCycleStatus(GroupCycle cycle) {
    //     List<Transaction> cycleTransactions = transactionRepository.findByCycleId(cycle.getCycleId());
        
    //     int successful = 0;
    //     int failed = 0;
        
    //     for (Transaction tx : cycleTransactions) {
    //         if ("COMPLETED".equals(tx.getStatus())) {
    //             successful++;
    //         } else if ("FAILED".equals(tx.getStatus()) || "PERMANENTLY_FAILED".equals(tx.getStatus())) {
    //             failed++;
    //         }
    //     }
        
    //     cycle.setSuccessfulPayments(successful);
    //     cycle.setFailedPayments(failed);
        
    //     BigDecimal expectedTotal = cycle.getContributionAmount()
    //         .multiply(BigDecimal.valueOf(successful + failed));
        
    //     if (successful == cycleTransactions.size()) {
    //         cycle.setStatus("COMPLETED");
    //     } else if (failed > 0 && successful + failed == cycleTransactions.size()) {
    //         cycle.setStatus("PARTIALLY_COMPLETED");
    //     } else {
    //         cycle.setStatus("PROCESSING");
    //     }
        
    //     groupCycleRepository.save(cycle);
    // }
}