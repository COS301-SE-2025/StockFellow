package com.stockfellow.transactionservice.scheduler;

import com.stockfellow.transactionservice.model.GroupCycle;
import com.stockfellow.transactionservice.model.Mandate;
import com.stockfellow.transactionservice.model.Transaction;
import com.stockfellow.transactionservice.repository.GroupCycleRepository;
import com.stockfellow.transactionservice.repository.MandateRepository;
import com.stockfellow.transactionservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentScheduler {

    private final GroupCycleRepository groupCycleRepository;
    private final MandateRepository mandateRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Process payments due today - runs at 1 AM daily
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processPaymentsDueToday() {
        LocalDate today = LocalDate.now();
        log.info("Processing payments due on {}", today);

        List<GroupCycle> cyclesDue = groupCycleRepository.findAll().stream()
                .filter(cycle -> "PENDING".equals(cycle.getStatus()))
                .filter(cycle -> today.equals(cycle.getCollectionDate()))
                .collect(Collectors.toList());

        for (GroupCycle cycle : cyclesDue) {
            try {
                log.info("Processing cycle: {} - Recipient: {}", cycle.getCycleId(), cycle.getRecipientUserId());

                cycle.setStatus("PROCESSING");
                groupCycleRepository.save(cycle);

                List<Mandate> mandates = getMandatesForGroup(cycle);
                List<Transaction> transactions = createTransactions(cycle, mandates);
                int successful = processTransactions(transactions);

                updateCycleStatus(cycle, successful, transactions.size());

            } catch (Exception e) {
                log.error("Failed to process cycle {}: {}", cycle.getCycleId(), e.getMessage());
                cycle.setStatus("FAILED");
                groupCycleRepository.save(cycle);
            }
        }
    }

    /**
     * Retry failed payments - runs at 4 AM daily
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void retryFailedPayments() {
        log.info("Retrying failed payments");

        List<Transaction> failedTx = transactionRepository.findAll().stream()
                .filter(tx -> "FAILED".equals(tx.getStatus()))
                .filter(tx -> tx.getRetryCount() < 3)
                .collect(Collectors.toList());

        for (Transaction tx : failedTx) {
            if (retryTransaction(tx)) {
                log.info("Retry successful for transaction {}", tx.getTransactionId());
            }
        }
    }

    // Helper methods
    private List<Mandate> getMandatesForGroup(GroupCycle cycle) {
        return mandateRepository.findAll().stream()
                .filter(m -> m.getGroupId().equals(cycle.getGroupId()))
                .filter(m -> "ACTIVE".equals(m.getStatus()))
                .filter(m -> !m.getPayerUserId().equals(cycle.getRecipientUserId()))
                .collect(Collectors.toList());
    }

    private List<Transaction> createTransactions(GroupCycle cycle, List<Mandate> mandates) {
        return mandates.stream()
                .map(mandate -> {
                    Transaction tx = new Transaction();
                    tx.setTransactionId(UUID.randomUUID());
                    tx.setCycleId(cycle.getCycleId());
                    tx.setMandateId(mandate.getMandateId());
                    tx.setPayerUserId(mandate.getPayerUserId());
                    tx.setRecipientUserId(cycle.getRecipientUserId());
                    tx.setGroupId(cycle.getGroupId());
                    tx.setPayerPaymentMethodId(mandate.getPaymentMethodId());
                    tx.setRecipientPaymentMethodId(cycle.getRecipientPaymentMethodId());
                    tx.setAmount(cycle.getContributionAmount());
                    tx.setStatus("PENDING");
                    tx.setRetryCount(0);
                    tx.setCreatedAt(LocalDateTime.now());
                    return transactionRepository.save(tx);
                })
                .collect(Collectors.toList());
    }

    private int processTransactions(List<Transaction> transactions) {
        int successful = 0;
        for (Transaction tx : transactions) {
            if (simulatePayment()) {
                tx.setStatus("COMPLETED");
                tx.setCompletedAt(LocalDateTime.now());
                successful++;
            } else {
                tx.setStatus("FAILED");
                tx.setRetryCount(1);
                tx.setFailMessage("Payment failed");
            }
            transactionRepository.save(tx);
        }
        return successful;
    }

    private boolean retryTransaction(Transaction tx) {
        tx.setRetryCount(tx.getRetryCount() + 1);

        if (simulatePayment()) {
            tx.setStatus("COMPLETED");
            tx.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(tx);
            return true;
        } else {
            tx.setFailMessage("Retry " + tx.getRetryCount() + " failed");
            transactionRepository.save(tx);
            return false;
        }
    }

    private void updateCycleStatus(GroupCycle cycle, int successful, int total) {
        cycle.setSuccessfulPayments(successful);
        cycle.setFailedPayments(total - successful);

        cycle.setTotalCollectedAmount(
                cycle.getContributionAmount().multiply(java.math.BigDecimal.valueOf(successful)));

        if (successful == total) {
            cycle.setStatus("COMPLETED");
        } else if (successful > 0) {
            cycle.setStatus("PARTIAL");
        } else {
            cycle.setStatus("FAILED");
        }

        groupCycleRepository.save(cycle);
    }

    private boolean simulatePayment() {
        return Math.random() > 0.1; // 90% success rate
    }
}
