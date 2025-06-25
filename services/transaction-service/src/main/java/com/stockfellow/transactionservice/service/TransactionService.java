package com.stockfellow.transactionservice.service;

import com.stockfellow.transactionservice.integration.CreditCheckService;
import com.stockfellow.transactionservice.integration.DirectDebitClient;
import com.stockfellow.transactionservice.model.Mandate;
import com.stockfellow.transactionservice.model.Schedule;
import com.stockfellow.transactionservice.model.Transaction;
import com.stockfellow.transactionservice.model.User;
import com.stockfellow.transactionservice.repository.MandateRepository;
import com.stockfellow.transactionservice.repository.ScheduleRepository;
import com.stockfellow.transactionservice.repository.TransactionRepository;
import com.stockfellow.transactionservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MandateRepository mandateRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private DirectDebitClient directDebitClient;
    @Autowired
    private CreditCheckService creditCheckService;
    @Autowired
    private NotificationService notificationService;

    public String createUser(String userId, String email, String phone, String idNumber) {
        String financialTier = creditCheckService.performCreditCheck(userId, idNumber);
        User user = new User();
        user.setUserId(userId);
        user.setEmail(email);
        user.setPhone(phone);
        user.setFinancialTier(financialTier);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
        logger.info("User created: {}", userId);
        return financialTier;
    }

    public String createMandate(String userId, String bankAccount) {
        String mandateId = directDebitClient.createMandate(userId, bankAccount);
        Mandate mandate = new Mandate();
        mandate.setMandateId(mandateId);
        mandate.setUserId(userId);
        mandate.setBankAccount(bankAccount);
        mandate.setStatus("ACTIVE");
        mandate.setCreatedAt(LocalDateTime.now());
        mandateRepository.save(mandate);
        logger.info("Mandate created for user: {}", userId);
        return mandateId;
    }

    public String processDebitOrder(String userId, String groupId, Double amount) {
        Mandate mandate = mandateRepository.findByUserIdAndStatus(userId, "ACTIVE");
        if (mandate == null) {
            throw new RuntimeException("No active mandate found for user: " + userId);
        }
        String externalRef = directDebitClient.processDebitOrder(mandate.getMandateId(), amount, groupId);
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setUserId(userId);
        transaction.setGroupId(groupId);
        transaction.setType("DEBIT_ORDER");
        transaction.setAmount(amount);
        transaction.setStatus("COMPLETED");
        transaction.setExternalRef(externalRef);
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
        logger.info("Debit order processed for user: {}", userId);
        return transaction.getTransactionId();
    }

    public String processPayout(String userId, String groupId, Double amount) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        Mandate mandate = mandateRepository.findByUserIdAndStatus(userId, "ACTIVE");
        if (mandate == null) {
            throw new RuntimeException("No active mandate found for user: " + userId);
        }
        String externalRef = directDebitClient.processPayout(userId, mandate.getBankAccount(), amount, groupId);
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setUserId(userId);
        transaction.setGroupId(groupId);
        transaction.setType("PAYOUT");
        transaction.setAmount(amount);
        transaction.setStatus("COMPLETED");
        transaction.setExternalRef(externalRef);
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
        notificationService.sendPayoutNotification(userId, user.getEmail(), amount, "completed", transaction.getTransactionId());
        notificationService.sendSMS(user.getPhone(), String.format("Payout of R%.2f completed. Transaction ID: %s", amount, transaction.getTransactionId()));
        logger.info("Payout processed for user: {}", userId);
        return transaction.getTransactionId();
    }

    public String scheduleTransaction(String userId, String groupId, String type, Double amount, String frequency, LocalDate nextRun) {
        Schedule schedule = new Schedule();
        schedule.setScheduleId(UUID.randomUUID().toString());
        schedule.setUserId(userId);
        schedule.setGroupId(groupId);
        schedule.setType(type);
        schedule.setAmount(amount);
        schedule.setFrequency(frequency);
        schedule.setNextRun(nextRun);
        schedule.setStatus("ACTIVE");
        scheduleRepository.save(schedule);
        logger.info("Transaction scheduled for user: {}, type: {}", userId, type);
        return schedule.getScheduleId();
    }
}
