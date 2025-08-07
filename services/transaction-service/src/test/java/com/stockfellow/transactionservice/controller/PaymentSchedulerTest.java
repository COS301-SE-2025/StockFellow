// package com.stockfellow.transactionservice.scheduler;

// import com.stockfellow.transactionservice.model.GroupCycle;
// import com.stockfellow.transactionservice.model.Mandate;
// import com.stockfellow.transactionservice.model.Transaction;
// import com.stockfellow.transactionservice.repository.GroupCycleRepository;
// import com.stockfellow.transactionservice.repository.MandateRepository;
// import com.stockfellow.transactionservice.repository.TransactionRepository;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import java.math.BigDecimal;
// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.util.Arrays;
// import java.util.Collections;
// import java.util.List;
// import java.util.UUID;

// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// @DisplayName("PaymentScheduler Tests")
// class PaymentSchedulerTest {

//     @Mock
//     private GroupCycleRepository groupCycleRepository;

//     @Mock
//     private MandateRepository mandateRepository;

//     @Mock
//     private TransactionRepository transactionRepository;

//     @InjectMocks
//     private PaymentScheduler paymentScheduler;

//     private UUID testCycleId;
//     private UUID testGroupId;
//     private UUID testMandateId;
//     private UUID testPayerUserId;
//     private UUID testRecipientUserId;
//     private UUID testPaymentMethodId;
//     private UUID testTransactionId;
    
//     private GroupCycle testCycle;
//     private Mandate testMandate;
//     private Transaction testTransaction;

//     @BeforeEach
//     void setUp() {
//         testCycleId = UUID.randomUUID();
//         testGroupId = UUID.randomUUID();
//         testMandateId = UUID.randomUUID();
//         testPayerUserId = UUID.randomUUID();
//         testRecipientUserId = UUID.randomUUID();
//         testPaymentMethodId = UUID.randomUUID();
//         testTransactionId = UUID.randomUUID();

//         // Setup test cycle
//         testCycle = new GroupCycle();
//         testCycle.setCycleId(testCycleId);
//         testCycle.setGroupId(testGroupId);
//         testCycle.setRecipientUserId(testRecipientUserId);
//         testCycle.setRecipientPaymentMethodId(testPaymentMethodId);
//         testCycle.setContributionAmount(new BigDecimal("100.00"));
//         testCycle.setCollectionDate(LocalDate.now().minusDays(1)); // Due yesterday
//         testCycle.setStatus("PENDING");
//         testCycle.setSuccessfulPayments(0);
//         testCycle.setFailedPayments(0);

//         // Setup test mandate
//         testMandate = new Mandate();
//         testMandate.setMandateId(testMandateId);
//         testMandate.setPayerUserId(testPayerUserId);
//         testMandate.setGroupId(testGroupId);
//         testMandate.setPaymentMethodId(testPaymentMethodId);
//         testMandate.setStatus("ACTIVE");

//         // Setup test transaction
//         testTransaction = new Transaction();
//         testTransaction.setTransactionId(testTransactionId);
//         testTransaction.setCycleId(testCycleId);
//         testTransaction.setMandateId(testMandateId);
//         testTransaction.setPayerUserId(testPayerUserId);
//         testTransaction.setRecipientUserId(testRecipientUserId);
//         testTransaction.setGroupId(testGroupId);
//         testTransaction.setAmount(new BigDecimal("100.00"));
//         testTransaction.setStatus("PENDING");
//         testTransaction.setRetryCount(0);
//         testTransaction.setCreatedAt(LocalDateTime.now());
//     }

//     @Test
//     @DisplayName("Should process scheduled payments for due cycles")
//     void processScheduledPayments_WithDueCycles_ShouldProcessPayments() {
//         // Given
//         List<GroupCycle> dueCycles = Arrays.asList(testCycle);
//         List<Mandate> activeMandates = Arrays.asList(testMandate);
        
//         when(groupCycleRepository.findByStatusAndCollectionDateLessThanEqual(eq("PENDING"), any(LocalDate.class)))
//             .thenReturn(dueCycles);
//         when(mandateRepository.findActiveMandatesByGroupId(testGroupId))
//             .thenReturn(activeMandates);
//         when(transactionRepository.save(any(Transaction.class)))
//             .thenReturn(testTransaction);
//         when(transactionRepository.findByCycleId(testCycleId))
//             .thenReturn(Arrays.asList(testTransaction));

//         // When
//         paymentScheduler.processScheduledPayments();

//         // Then
//         verify(groupCycleRepository).findByStatusAndCollectionDateLessThanEqual(eq("PENDING"), any(LocalDate.class));
//         verify(groupCycleRepository, atLeast(2)).save(any(GroupCycle.class)); // Once for PROCESSING, once for final status
//         verify(mandateRepository).findActiveMandatesByGroupId(testGroupId);
//         verify(transactionRepository, atLeastOnce()).save(any(Transaction.class));
//     }

//     @Test
//     @DisplayName("Should not process payments when no due cycles exist")
//     void processScheduledPayments_WithNoDueCycles_ShouldNotProcessPayments() {
//         // Given
//         when(groupCycleRepository.findByStatusAndCollectionDateLessThanEqual(eq("PENDING"), any(LocalDate.class)))
//             .thenReturn(Collections.emptyList());

//         // When
//         paymentScheduler.processScheduledPayments();

//         // Then
//         verify(groupCycleRepository).findByStatusAndCollectionDateLessThanEqual(eq("PENDING"), any(LocalDate.class));
//         verify(mandateRepository, never()).findActiveMandatesByGroupId(any(UUID.class));
//         verify(transactionRepository, never()).save(any(Transaction.class));
//     }

//     @Test
//     @DisplayName("Should skip cycle when collection date is not due")
//     void processScheduledPayments_WithFutureCycle_ShouldSkipProcessing() {
//         // Given
//         testCycle.setCollectionDate(LocalDate.now().plusDays(2)); // Future date
//         List<GroupCycle> dueCycles = Arrays.asList(testCycle);
        
//         when(groupCycleRepository.findByStatusAndCollectionDateLessThanEqual(eq("PENDING"), any(LocalDate.class)))
//             .thenReturn(dueCycles);

//         // When
//         paymentScheduler.processScheduledPayments();

//         // Then
//         verify(groupCycleRepository).findByStatusAndCollectionDateLessThanEqual(eq("PENDING"), any(LocalDate.class));
//         verify(mandateRepository, never()).findActiveMandatesByGroupId(any(UUID.class));
//         verify(transactionRepository, never()).save(any(Transaction.class));
//     }

//     @Test
//     @DisplayName("Should exclude recipient from payment processing")
//     void processScheduledPayments_ShouldExcludeRecipientFromPayments() {
//         // Given
//         Mandate recipientMandate = new Mandate();
//         recipientMandate.setMandateId(UUID.randomUUID());
//         recipientMandate.setPayerUserId(testRecipientUserId); // Same as recipient
//         recipientMandate.setGroupId(testGroupId);
//         recipientMandate.setStatus("ACTIVE");

//         List<GroupCycle> dueCycles = Arrays.asList(testCycle);
//         List<Mandate> activeMandates = Arrays.asList(testMandate, recipientMandate);
        
//         when(groupCycleRepository.findByStatusAndCollectionDateLessThanEqual(eq("PENDING"), any(LocalDate.class)))
//             .thenReturn(dueCycles);
//         when(mandateRepository.findActiveMandatesByGroupId(testGroupId))
//             .thenReturn(activeMandates);
//         when(transactionRepository.save(any(Transaction.class)))
//             .thenReturn(testTransaction);
//         when(transactionRepository.findByCycleId(testCycleId))
//             .thenReturn(Arrays.asList(testTransaction));

//         // When
//         paymentScheduler.processScheduledPayments();

//         // Then
//         // Transaction is saved twice: once initially, once after payment processing
//         verify(transactionRepository, times(2)).save(any(Transaction.class)); 
//         // Verify only one unique transaction was created (recipient excluded)
//         verify(transactionRepository, times(2)).save(argThat(tx -> 
//             tx.getPayerUserId().equals(testPayerUserId) && 
//             !tx.getPayerUserId().equals(testRecipientUserId)
//         ));
//     }

//     @Test
//     @DisplayName("Should retry failed payments for partially completed cycles")
//     void retryFailedPayments_WithPartiallyCompletedCycles_ShouldRetryFailedTransactions() {
//         // Given
//         testCycle.setStatus("PARTIALLY_COMPLETED");
//         testTransaction.setStatus("FAILED");
//         testTransaction.setRetryCount(1);
        
//         List<GroupCycle> processingCycles = Arrays.asList(testCycle);
//         List<Transaction> failedTransactions = Arrays.asList(testTransaction);
        
//         when(groupCycleRepository.findByStatus("PARTIALLY_COMPLETED"))
//             .thenReturn(processingCycles);
//         when(transactionRepository.findByCycleIdAndStatus(testCycleId, "FAILED"))
//             .thenReturn(failedTransactions);
//         when(transactionRepository.findByCycleId(testCycleId))
//             .thenReturn(failedTransactions);

//         // When
//         paymentScheduler.retryFailedPayments();

//         // Then
//         verify(groupCycleRepository).findByStatus("PARTIALLY_COMPLETED");
//         verify(transactionRepository).findByCycleIdAndStatus(testCycleId, "FAILED");
//         verify(transactionRepository, atLeastOnce()).save(any(Transaction.class));
//         verify(groupCycleRepository, atLeast(2)).save(any(GroupCycle.class)); // Status updates
//     }

//     @Test
//     @DisplayName("Should not retry transactions that have reached max retry count")
//     void retryFailedPayments_WithMaxRetries_ShouldNotRetryTransaction() {
//         // Given
//         testCycle.setStatus("PARTIALLY_COMPLETED");
//         testTransaction.setStatus("FAILED");
//         testTransaction.setRetryCount(3); // Max retries reached
        
//         List<GroupCycle> processingCycles = Arrays.asList(testCycle);
//         List<Transaction> failedTransactions = Arrays.asList(testTransaction);
        
//         when(groupCycleRepository.findByStatus("PARTIALLY_COMPLETED"))
//             .thenReturn(processingCycles);
//         when(transactionRepository.findByCycleIdAndStatus(testCycleId, "FAILED"))
//             .thenReturn(failedTransactions);
//         when(transactionRepository.findByCycleId(testCycleId))
//             .thenReturn(failedTransactions);

//         // When
//         paymentScheduler.retryFailedPayments();

//         // Then
//         verify(groupCycleRepository).findByStatus("PARTIALLY_COMPLETED");
//         verify(transactionRepository).findByCycleIdAndStatus(testCycleId, "FAILED");
//     }

//     @Test
//     @DisplayName("Should handle retry logic for failed transactions")
//     void retryFailedTransactionsForCycle_WithFailedTransactions_ShouldProcessRetries() {
//         // Given
//         testTransaction.setStatus("FAILED");
//         testTransaction.setRetryCount(1);
        
//         List<Transaction> failedTransactions = Arrays.asList(testTransaction);
        
//         when(transactionRepository.findByCycleIdAndStatus(testCycleId, "FAILED"))
//             .thenReturn(failedTransactions);
//         when(transactionRepository.findByCycleId(testCycleId))
//             .thenReturn(failedTransactions);

//         // When
//         paymentScheduler.retryFailedTransactionsForCycle(testCycle);

//         // Then
//         verify(transactionRepository).findByCycleIdAndStatus(testCycleId, "FAILED");
//         verify(transactionRepository, atLeastOnce()).save(any(Transaction.class));
//         verify(groupCycleRepository).save(any(GroupCycle.class));
//     }

//     @Test
//     @DisplayName("Should update cycle status based on transaction results")
//     void updateCycleStatus_WithMixedTransactions_ShouldCalculateCorrectStatus() {
//         // Given
//         Transaction completedTx = new Transaction();
//         completedTx.setStatus("COMPLETED");
        
//         Transaction failedTx = new Transaction();
//         failedTx.setStatus("FAILED");
        
//         List<Transaction> transactions = Arrays.asList(completedTx, failedTx);
        
//         when(transactionRepository.findByCycleId(testCycleId))
//             .thenReturn(transactions);

//         // When
//         paymentScheduler.retryFailedTransactionsForCycle(testCycle);

//         // Then
//         verify(groupCycleRepository).save(argThat(cycle -> 
//             cycle.getSuccessfulPayments() == 1 &&
//             cycle.getFailedPayments() == 1 &&
//             "PARTIALLY_COMPLETED".equals(cycle.getStatus())
//         ));
//     }

//     @Test
//     @DisplayName("Should mark cycle as completed when all transactions succeed")
//     void updateCycleStatus_WithAllCompletedTransactions_ShouldMarkCompleted() {
//         // Given
//         Transaction completedTx1 = new Transaction();
//         completedTx1.setStatus("COMPLETED");
        
//         Transaction completedTx2 = new Transaction();
//         completedTx2.setStatus("COMPLETED");
        
//         List<Transaction> transactions = Arrays.asList(completedTx1, completedTx2);
        
//         when(transactionRepository.findByCycleId(testCycleId))
//             .thenReturn(transactions);

//         // When
//         paymentScheduler.retryFailedTransactionsForCycle(testCycle);

//         // Then
//         verify(groupCycleRepository).save(argThat(cycle -> 
//             cycle.getSuccessfulPayments() == 2 &&
//             cycle.getFailedPayments() == 0 &&
//             "COMPLETED".equals(cycle.getStatus())
//         ));
//     }

//     @Test
//     @DisplayName("Should handle empty mandate list gracefully")
//     void processPaymentsForCycle_WithNoActiveMandates_ShouldCompleteGracefully() {
//         // Given
//         List<Mandate> activeMandates = Collections.emptyList();
//         List<Transaction> transactions = Collections.emptyList();
        
//         when(transactionRepository.findByCycleId(testCycleId))
//             .thenReturn(transactions);

//         // When
//         paymentScheduler.retryFailedTransactionsForCycle(testCycle);

//         // Then
//         verify(transactionRepository, never()).save(any(Transaction.class));
//         verify(groupCycleRepository).save(any(GroupCycle.class)); // Status update should still happen
//     }

//     @Test
//     @DisplayName("Should handle empty failed transactions list")
//     void retryFailedTransactionsForCycle_WithNoFailedTransactions_ShouldCompleteGracefully() {
//         // Given
//         when(transactionRepository.findByCycleIdAndStatus(testCycleId, "FAILED"))
//             .thenReturn(Collections.emptyList());
//         when(transactionRepository.findByCycleId(testCycleId))
//             .thenReturn(Collections.emptyList());

//         // When
//         paymentScheduler.retryFailedTransactionsForCycle(testCycle);

//         // Then
//         verify(transactionRepository).findByCycleIdAndStatus(testCycleId, "FAILED");
//         verify(transactionRepository, never()).save(any(Transaction.class));
//         verify(groupCycleRepository).save(any(GroupCycle.class)); // Status update should still happen
//     }

//     @Test
//     @DisplayName("Should update payment counts correctly")
//     void updateCycleStatus_ShouldCalculatePaymentCountsCorrectly() {
//         // Given
//         Transaction completedTx1 = new Transaction();
//         completedTx1.setStatus("COMPLETED");
        
//         Transaction completedTx2 = new Transaction();
//         completedTx2.setStatus("COMPLETED");
        
//         Transaction failedTx = new Transaction();
//         failedTx.setStatus("FAILED");
        
//         Transaction permanentlyFailedTx = new Transaction();
//         permanentlyFailedTx.setStatus("PERMANENTLY_FAILED");
        
//         List<Transaction> transactions = Arrays.asList(completedTx1, completedTx2, failedTx, permanentlyFailedTx);
        
//         when(transactionRepository.findByCycleId(testCycleId))
//             .thenReturn(transactions);

//         // When
//         paymentScheduler.retryFailedTransactionsForCycle(testCycle);

//         // Then
//         verify(groupCycleRepository).save(argThat(cycle -> 
//             cycle.getSuccessfulPayments() == 2 &&
//             cycle.getFailedPayments() == 2
//         ));
//     }

//     @Test
//     @DisplayName("Should set cycle status to PROCESSING when not all transactions are complete")
//     void updateCycleStatus_WithIncompleteTransactions_ShouldSetProcessingStatus() {
//         // Given
//         Transaction completedTx = new Transaction();
//         completedTx.setStatus("COMPLETED");
        
//         Transaction pendingTx = new Transaction();
//         pendingTx.setStatus("PENDING");
        
//         List<Transaction> transactions = Arrays.asList(completedTx, pendingTx);
        
//         when(transactionRepository.findByCycleId(testCycleId))
//             .thenReturn(transactions);

//         // When
//         paymentScheduler.retryFailedTransactionsForCycle(testCycle);

//         // Then
//         verify(groupCycleRepository).save(argThat(cycle -> 
//             "PROCESSING".equals(cycle.getStatus())
//         ));
//     }

//     @Test
//     @DisplayName("Should handle no partially completed cycles in retry job")
//     void retryFailedPayments_WithNoPartiallyCompletedCycles_ShouldCompleteGracefully() {
//         // Given
//         when(groupCycleRepository.findByStatus("PARTIALLY_COMPLETED"))
//             .thenReturn(Collections.emptyList());

//         // When
//         paymentScheduler.retryFailedPayments();

//         // Then
//         verify(groupCycleRepository).findByStatus("PARTIALLY_COMPLETED");
//         verify(transactionRepository, never()).findByCycleIdAndStatus(any(UUID.class), anyString());
//         verify(transactionRepository, never()).save(any(Transaction.class));
//     }
// }