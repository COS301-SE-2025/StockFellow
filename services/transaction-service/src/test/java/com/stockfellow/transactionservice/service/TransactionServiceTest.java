package com.stockfellow.transactionservice.service;

import com.stockfellow.transactionservice.dto.CreateTransactionDto;
import com.stockfellow.transactionservice.dto.ProcessTransactionDto;
import com.stockfellow.transactionservice.integration.PaystackService;
import com.stockfellow.transactionservice.integration.dto.*;
import com.stockfellow.transactionservice.integration.dto.PaystackTransactionResponse.PaystackTransactionData;
import com.stockfellow.transactionservice.model.*;
import com.stockfellow.transactionservice.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private GroupCycleRepository groupCycleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PayerDetailsRepository payerDetailsRepository;

    @Mock
    private PaystackService paystackService;

    @InjectMocks
    private TransactionService transactionService;

    private UUID testUserId;
    private UUID testCycleId;
    private UUID testPayerId;
    private UUID testTransactionId;
    private CreateTransactionDto createTransactionDto;
    private Transaction transaction;
    private User user;
    private GroupCycle groupCycle;
    private PayerDetails payerDetails;
    private PaystackTransactionResponse paystackTransactionResponse;
    private PaystackTransactionData paystackResponseData;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testCycleId = UUID.randomUUID();
        testPayerId = UUID.randomUUID();
        testTransactionId = UUID.randomUUID();

        // Set up configuration values
        ReflectionTestUtils.setField(transactionService, "maxRetryCount", 3);
        ReflectionTestUtils.setField(transactionService, "paystackCallbackUrl", "http://localhost:4080/api/transactions/callback");

        // Create test DTOs
        createTransactionDto = new CreateTransactionDto();
        createTransactionDto.setCycleId(testCycleId);
        createTransactionDto.setUserId(testUserId);
        createTransactionDto.setPayerId(testPayerId);
        createTransactionDto.setAmount(new BigDecimal("1000.00"));
        createTransactionDto.setPaystackReference("TXN_test123");

        // Create test entities
        user = new User();
        user.setUserId(testUserId);
        user.setEmail("test@example.com");
        user.setStatus(User.UserStatus.active);

        groupCycle = new GroupCycle();
        groupCycle.setCycleId(testCycleId);
        groupCycle.setStatus("active");
        groupCycle.setContributionAmount(new BigDecimal("1000.00"));
        groupCycle.setExpectedTotal(new BigDecimal("5000.00"));
        groupCycle.setCurrentTotal(new BigDecimal("2000.00"));
        groupCycle.setCollectionStartDate(LocalDate.now().minusDays(1));
        groupCycle.setCollectionEndDate(LocalDate.now().plusDays(10));

        payerDetails = new PayerDetails();
        payerDetails.setPayerId(testPayerId);
        payerDetails.setUserId(testUserId);
        payerDetails.setIsActive(true);
        payerDetails.setIsAuthenticated(true);
        payerDetails.setAuthCode("AUTH_test123");

        transaction = new Transaction();
        transaction.setTransactionId(testTransactionId);
        transaction.setCycleId(testCycleId);
        transaction.setUserId(testUserId);
        transaction.setPayerId(testPayerId);
        transaction.setAmount(new BigDecimal("1000.00"));
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        transaction.setRetryCount(0);
        transaction.setPaystackReference("TXN_test123");

        // Create Paystack response
        paystackResponseData = new PaystackTransactionData();
        paystackResponseData.setReference("TXN_test123");
        paystackResponseData.setAuthorizationUrl("https://checkout.paystack.com/test123");

        paystackTransactionResponse = new PaystackTransactionResponse();
        paystackTransactionResponse.setStatus(true);
        paystackTransactionResponse.setData(paystackResponseData);
        paystackTransactionResponse.setMessage("Transaction initialized");
    }

    @Test
    void createTransaction_WhenAllValidationsPass_ShouldCreateSuccessfulTransaction() throws Exception {
        // Given
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(payerDetailsRepository.findById(testPayerId)).thenReturn(Optional.of(payerDetails));
        when(transactionRepository.findByCycleIdAndUserIdAndStatus(testCycleId, testUserId, Transaction.TransactionStatus.COMPLETED))
            .thenReturn(Collections.emptyList());
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(paystackService.initializeTransaction(any(PaystackTransactionRequest.class))).thenReturn(paystackTransactionResponse);

        // When
        Transaction result = transactionService.createTransaction(createTransactionDto);

        // Then
        assertNotNull(result);
        assertEquals(testCycleId, result.getCycleId());
        assertEquals(testUserId, result.getUserId());
        assertEquals(testPayerId, result.getPayerId());
        assertEquals(new BigDecimal("1000.00"), result.getAmount());

        verify(transactionRepository, times(2)).save(any(Transaction.class)); // Once for initial save, once after Paystack
        verify(paystackService).initializeTransaction(any(PaystackTransactionRequest.class));

        // Verify Paystack request
        ArgumentCaptor<PaystackTransactionRequest> requestCaptor = ArgumentCaptor.forClass(PaystackTransactionRequest.class);
        verify(paystackService).initializeTransaction(requestCaptor.capture());
        PaystackTransactionRequest capturedRequest = requestCaptor.getValue();
        assertEquals("test@example.com", capturedRequest.getEmail());
        assertEquals(100000, capturedRequest.getAmount()); // 1000 * 100 = 100000 kobo
        assertEquals("TXN_test123", capturedRequest.getReference());
    }

    @Test
    void createTransaction_WhenCycleNotFound_ShouldThrowException() {
        // Given
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> transactionService.createTransaction(createTransactionDto));

        assertEquals("Cycle not found with ID: " + testCycleId, exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_WhenCycleNotActive_ShouldThrowException() {
        // Given
        groupCycle.setStatus("completed");
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> transactionService.createTransaction(createTransactionDto));

        assertEquals("Cycle is not accepting transactions. Status: completed", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_WhenOutsideCollectionPeriod_ShouldThrowException() {
        // Given
        groupCycle.setCollectionStartDate(LocalDate.now().plusDays(1));
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> transactionService.createTransaction(createTransactionDto));

        assertEquals("Cycle is outside collection period", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_WhenUserNotFound_ShouldThrowException() {
        // Given
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> transactionService.createTransaction(createTransactionDto));

        assertEquals("User not found with ID: " + testUserId, exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_WhenAmountMismatch_ShouldThrowException() {
        // Given
        createTransactionDto.setAmount(new BigDecimal("500.00"));
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(payerDetailsRepository.findById(testPayerId)).thenReturn(Optional.of(payerDetails));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> transactionService.createTransaction(createTransactionDto));

        assertEquals("Transaction amount must match cycle contribution amount", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_WhenTransactionAlreadyExists_ShouldThrowException() {
        // Given
        Transaction existingTransaction = new Transaction();
        existingTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(payerDetailsRepository.findById(testPayerId)).thenReturn(Optional.of(payerDetails));
        when(transactionRepository.findByCycleIdAndUserIdAndStatus(testCycleId, testUserId, Transaction.TransactionStatus.COMPLETED))
            .thenReturn(List.of(existingTransaction));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> transactionService.createTransaction(createTransactionDto));

        assertEquals("User has already completed a transaction for this cycle", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_WhenPaystackFails_ShouldSetFailedStatus() throws Exception {
        // Given
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(payerDetailsRepository.findById(testPayerId)).thenReturn(Optional.of(payerDetails));
        when(transactionRepository.findByCycleIdAndUserIdAndStatus(testCycleId, testUserId, Transaction.TransactionStatus.COMPLETED))
            .thenReturn(Collections.emptyList());
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(paystackService.initializeTransaction(any(PaystackTransactionRequest.class)))
            .thenThrow(new RuntimeException("Network error"));

        // When
        Transaction result = transactionService.createTransaction(createTransactionDto);

        // Then
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(2)).save(transactionCaptor.capture());
        Transaction savedTransaction = transactionCaptor.getAllValues().get(1);
        assertEquals(Transaction.TransactionStatus.FAILED, savedTransaction.getStatus());
        assertTrue(savedTransaction.getFailureReason().contains("Failed to initiate payment"));
    }

    // @Test
    // void chargeStoredCard_WhenSuccessful_ShouldCompleteTransaction() throws Exception {
    //     // Given
    //     PaystackTransactionResponse chargeResponse = new PaystackTransactionResponse();
    //     PaystackTransactionData chargeData = new PaystackTransactionData();
    //     chargeData.setReference("CHARGE_test123");
    //     chargeResponse.setStatus(true);
    //     chargeResponse.setData(chargeData);
    //     chargeResponse.setMessage("Charge successful");

    //     when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
    //     when(payerDetailsRepository.findById(testPayerId)).thenReturn(Optional.of(payerDetails));
    //     when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
    //     when(transactionRepository.findByCycleIdAndUserIdAndStatus(testCycleId, testUserId, Transaction.TransactionStatus.COMPLETED))
    //         .thenReturn(Collections.emptyList());
    //     when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
    //     when(paystackService.chargeTransaction(any(PaystackChargeRequest.class))).thenReturn(chargeResponse);

    //     // When
    //     Transaction result = transactionService.chargeStoredCard(createTransactionDto);

    //     // Then
    //     assertNotNull(result);
    //     verify(paystackService).chargeTransaction(any(PaystackChargeRequest.class));
    //     verify(transactionRepository, atLeast(2)).save(any(Transaction.class));

    //     // Verify charge request
    //     ArgumentCaptor<PaystackChargeRequest> requestCaptor = ArgumentCaptor.forClass(PaystackChargeRequest.class);
    //     verify(paystackService).chargeTransaction(requestCaptor.capture());
    //     PaystackChargeRequest capturedRequest = requestCaptor.getValue();
    //     assertEquals("test@example.com", capturedRequest.getEmail());
    //     assertEquals(100000, capturedRequest.getAmount());
    //     assertEquals("AUTH_test123", capturedRequest.getAuthCode());
    // }

    @Test
    void chargeStoredCard_WhenNoAuthCode_ShouldThrowException() {
        // Given
        payerDetails.setAuthCode(null);
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
        when(payerDetailsRepository.findById(testPayerId)).thenReturn(Optional.of(payerDetails));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> transactionService.chargeStoredCard(createTransactionDto));

        assertEquals("No authorization code found for payer. Please complete initial payment first.", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void chargeStoredCard_WhenChargeFails_ShouldSetFailedStatus() throws Exception {
        // Given
        PaystackTransactionResponse chargeResponse = new PaystackTransactionResponse();
        chargeResponse.setStatus(false);
        chargeResponse.setMessage("Insufficient funds");

        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
        when(payerDetailsRepository.findById(testPayerId)).thenReturn(Optional.of(payerDetails));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(transactionRepository.findByCycleIdAndUserIdAndStatus(testCycleId, testUserId, Transaction.TransactionStatus.COMPLETED))
            .thenReturn(Collections.emptyList());
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(paystackService.chargeTransaction(any(PaystackChargeRequest.class))).thenReturn(chargeResponse);

        // When
        Transaction result = transactionService.chargeStoredCard(createTransactionDto);

        // Then
        verify(transactionRepository, atLeast(2)).save(any(Transaction.class));
        
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, atLeast(2)).save(transactionCaptor.capture());
        List<Transaction> savedTransactions = transactionCaptor.getAllValues();
        Transaction finalTransaction = savedTransactions.get(savedTransactions.size() - 1);
        assertEquals(Transaction.TransactionStatus.FAILED, finalTransaction.getStatus());
        assertEquals("Insufficient funds", finalTransaction.getFailureReason());
    }

    @Test
    void processTransaction_WhenSuccessful_ShouldUpdateTransactionStatus() {
        // Given
        ProcessTransactionDto processDto = new ProcessTransactionDto();
        processDto.setStatus(Transaction.TransactionStatus.COMPLETED);
        processDto.setPaystackTransId("PST_123");
        processDto.setGatewayStatus("success");

        when(transactionRepository.findById(testTransactionId)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
        when(groupCycleRepository.save(any(GroupCycle.class))).thenReturn(groupCycle);
        when(transactionRepository.getTotalAmountByCycleAndStatus(testCycleId, Transaction.TransactionStatus.COMPLETED))
            .thenReturn(new BigDecimal("3000.00"));
        when(transactionRepository.countByCycleIdAndStatus(eq(testCycleId), any(Transaction.TransactionStatus.class)))
            .thenReturn(1L);

        // When
        Transaction result = transactionService.processTransaction(testTransactionId, processDto);

        // Then
        assertNotNull(result);
        verify(transactionRepository).save(any(Transaction.class));
        
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction savedTransaction = transactionCaptor.getValue();
        assertEquals(Transaction.TransactionStatus.COMPLETED, savedTransaction.getStatus());
        assertEquals("PST_123", savedTransaction.getPaystackTransId());
        assertEquals("success", savedTransaction.getGatewayStatus());
        assertNotNull(savedTransaction.getCompletedAt());
    }

    @Test
    void verifyTransaction_WhenSuccessful_ShouldUpdateTransaction() throws Exception {
        // Given
        String reference = "TXN_test123";
        PaystackTransactionVerificationResponse.PaystackTransactionVerificationData verificationData = 
            new PaystackTransactionVerificationResponse.PaystackTransactionVerificationData();
        verificationData.setId(12345L);
        verificationData.setStatus("success");
        verificationData.setGatewayResponse("Successful");
        verificationData.setMessage("Payment successful");

        PaystackAuthorization authorization = new PaystackAuthorization();
        authorization.setAuthorizationCode("AUTH_updated");
        authorization.setLast4("1234");
        verificationData.setAuthorization(authorization);

        PaystackTransactionVerificationResponse verificationResponse = new PaystackTransactionVerificationResponse();
        verificationResponse.setStatus(true);
        verificationResponse.setData(verificationData);

        when(transactionRepository.findByPaystackReference(reference)).thenReturn(Optional.of(transaction));
        when(paystackService.verifyTransaction(reference)).thenReturn(verificationResponse);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(payerDetailsRepository.findById(testPayerId)).thenReturn(Optional.of(payerDetails));
        when(payerDetailsRepository.save(any(PayerDetails.class))).thenReturn(payerDetails);
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
        when(groupCycleRepository.save(any(GroupCycle.class))).thenReturn(groupCycle);
        when(transactionRepository.getTotalAmountByCycleAndStatus(testCycleId, Transaction.TransactionStatus.COMPLETED))
            .thenReturn(new BigDecimal("3000.00"));
        when(transactionRepository.countByCycleIdAndStatus(eq(testCycleId), any(Transaction.TransactionStatus.class)))
            .thenReturn(1L);

        // When
        Transaction result = transactionService.verifyTransaction(reference);

        // Then
        assertNotNull(result);
        verify(paystackService).verifyTransaction(reference);
        verify(transactionRepository).save(any(Transaction.class));
        verify(payerDetailsRepository).save(any(PayerDetails.class)); // Authorization update

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction savedTransaction = transactionCaptor.getValue();
        assertEquals(Transaction.TransactionStatus.COMPLETED, savedTransaction.getStatus());
        assertEquals("12345", savedTransaction.getPaystackTransId());
        assertEquals("Successful", savedTransaction.getGatewayStatus());
    }

    @Test
    void verifyTransaction_WhenNotFound_ShouldThrowException() {
        // Given
        String reference = "TXN_notfound";
        when(transactionRepository.findByPaystackReference(reference)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> transactionService.verifyTransaction(reference));

        assertEquals("Transaction not found with reference: " + reference, exception.getMessage());
        verify(paystackService, never()).verifyTransaction(any());
    }

    @Test
    void retryTransaction_WhenSuccessful_ShouldIncrementRetryCount() throws Exception {
        // Given
        transaction.setStatus(Transaction.TransactionStatus.FAILED);
        transaction.setRetryCount(1);

        when(transactionRepository.findById(testTransactionId)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(payerDetailsRepository.findById(testPayerId)).thenReturn(Optional.of(payerDetails));
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
        when(paystackService.initializeTransaction(any(PaystackTransactionRequest.class))).thenReturn(paystackTransactionResponse);

        // When
        Transaction result = transactionService.retryTransaction(testTransactionId);

        // Then
        assertNotNull(result);
        verify(transactionRepository, atLeast(2)).save(any(Transaction.class));
        
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, atLeast(2)).save(transactionCaptor.capture());
        Transaction savedTransaction = transactionCaptor.getAllValues().get(0);
        assertEquals(2, savedTransaction.getRetryCount());
        assertEquals(Transaction.TransactionStatus.PENDING, savedTransaction.getStatus());
    }

    @Test
    void retryTransaction_WhenNotFailed_ShouldThrowException() {
        // Given
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        when(transactionRepository.findById(testTransactionId)).thenReturn(Optional.of(transaction));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> transactionService.retryTransaction(testTransactionId));

        assertEquals("Transaction is not in FAILED status", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void retryTransaction_WhenMaxRetriesExceeded_ShouldThrowException() {
        // Given
        transaction.setStatus(Transaction.TransactionStatus.FAILED);
        transaction.setRetryCount(3);
        when(transactionRepository.findById(testTransactionId)).thenReturn(Optional.of(transaction));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> transactionService.retryTransaction(testTransactionId));

        assertEquals("Maximum retry count exceeded for transaction: " + testTransactionId, exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void findById_WhenTransactionExists_ShouldReturnTransaction() {
        // Given
        when(transactionRepository.findById(testTransactionId)).thenReturn(Optional.of(transaction));

        // When
        Transaction result = transactionService.findById(testTransactionId);

        // Then
        assertNotNull(result);
        assertEquals(transaction, result);
        verify(transactionRepository).findById(testTransactionId);
    }

    @Test
    void findById_WhenTransactionNotFound_ShouldThrowException() {
        // Given
        when(transactionRepository.findById(testTransactionId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> transactionService.findById(testTransactionId));

        assertEquals("Transaction not found with ID: " + testTransactionId, exception.getMessage());
    }

    @Test
    void findByCycleId_ShouldReturnPagedTransactions() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> page = new PageImpl<>(Arrays.asList(transaction));
        when(transactionRepository.findByCycleId(testCycleId, pageable)).thenReturn(page);

        // When
        Page<Transaction> result = transactionService.findByCycleId(testCycleId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(transaction, result.getContent().get(0));
        verify(transactionRepository).findByCycleId(testCycleId, pageable);
    }

    @Test
    void findByUserId_ShouldReturnPagedTransactions() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> page = new PageImpl<>(Arrays.asList(transaction));
        when(transactionRepository.findByUserId(testUserId, pageable)).thenReturn(page);

        // When
        Page<Transaction> result = transactionService.findByUserId(testUserId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(transaction, result.getContent().get(0));
        verify(transactionRepository).findByUserId(testUserId, pageable);
    }

    @Test
    void findByStatus_ShouldReturnTransactionList() {
        // Given
        List<Transaction> transactions = Arrays.asList(transaction);
        when(transactionRepository.findByStatus(Transaction.TransactionStatus.PENDING)).thenReturn(transactions);

        // When
        List<Transaction> result = transactionService.findByStatus(Transaction.TransactionStatus.PENDING);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(transaction, result.get(0));
        verify(transactionRepository).findByStatus(Transaction.TransactionStatus.PENDING);
    }

    @Test
    void getTransactionStatistics_ShouldReturnStatistics() {
        // Given
        List<Object[]> statsData = Arrays.asList(
            new Object[]{Transaction.TransactionStatus.COMPLETED, 5L, new BigDecimal("5000.00")},
            new Object[]{Transaction.TransactionStatus.FAILED, 2L, new BigDecimal("2000.00")},
            new Object[]{Transaction.TransactionStatus.PENDING, 1L, new BigDecimal("1000.00")}
        );
        when(transactionRepository.getTransactionStatisticsByCycle(testCycleId)).thenReturn(statsData);

        // When
        TransactionService.TransactionStatistics result = transactionService.getTransactionStatistics(testCycleId);

        // Then
        assertNotNull(result);
        assertEquals(8L, result.getTotalTransactions());
        assertEquals(5L, result.getCompletedTransactions());
        assertEquals(2L, result.getFailedTransactions());
        assertEquals(1L, result.getPendingTransactions());
        assertEquals(new BigDecimal("8000.00"), result.getTotalAmount());
        assertEquals(new BigDecimal("5000.00"), result.getCompletedAmount());
    }

    // @Test
    // void processAutomaticRetries_ShouldRetryEligibleTransactions() throws Exception {
    //     // Given
    //     Transaction retryableTransaction = new Transaction();
    //     retryableTransaction.setTransactionId(UUID.randomUUID());
    //     retryableTransaction.setStatus(Transaction.TransactionStatus.FAILED);
    //     retryableTransaction.setRetryCount(1);
    //     retryableTransaction.setUpdatedAt(LocalDateTime.now().minusHours(1));
    //     retryableTransaction.setUserId(testUserId);
    //     retryableTransaction.setPayerId(testPayerId);
    //     retryableTransaction.setCycleId(testCycleId);

    //     when(transactionRepository.findRetryableTransactions(3)).thenReturn(Arrays.asList(retryableTransaction));
    //     when(transactionRepository.findById(retryableTransaction.getTransactionId())).thenReturn(Optional.of(retryableTransaction));
    //     when(transactionRepository.save(any(Transaction.class))).thenReturn(retryableTransaction);
    //     when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
    //     when(payerDetailsRepository.findById(testPayerId)).thenReturn(Optional.of(payerDetails));
    //     when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
    //     when(paystackService.initializeTransaction(any(PaystackTransactionRequest.class))).thenReturn(paystackTransactionResponse);

    //     // When
    //     transactionService.processAutomaticRetries();

    //     // Then
    //     verify(transactionRepository).findRetryableTransactions(3);
    //     verify(transactionRepository, atLeast(2)).save(any(Transaction.class));
    // }

    @Test
    void handleStalePendingTransactions_ShouldVerifyStaleTransactions() throws Exception {
        // Given
        Transaction staleTransaction = new Transaction();
        staleTransaction.setTransactionId(UUID.randomUUID());
        staleTransaction.setStatus(Transaction.TransactionStatus.PENDING);
        staleTransaction.setPaystackReference("STALE_TXN_123");

        PaystackTransactionVerificationResponse.PaystackTransactionVerificationData verificationData = 
            new PaystackTransactionVerificationResponse.PaystackTransactionVerificationData();
        verificationData.setId(12345L);
        verificationData.setStatus("success");
        verificationData.setGatewayResponse("Successful");

        PaystackTransactionVerificationResponse verificationResponse = new PaystackTransactionVerificationResponse();
        verificationResponse.setStatus(true);
        verificationResponse.setData(verificationData);

        when(transactionRepository.findStalePendingTransactions(any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(staleTransaction));
        when(transactionRepository.findByPaystackReference("STALE_TXN_123"))
            .thenReturn(Optional.of(staleTransaction));
        when(paystackService.verifyTransaction("STALE_TXN_123")).thenReturn(verificationResponse);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(staleTransaction);
        when(groupCycleRepository.findById(any())).thenReturn(Optional.of(groupCycle));
        when(groupCycleRepository.save(any(GroupCycle.class))).thenReturn(groupCycle);
        when(transactionRepository.getTotalAmountByCycleAndStatus(any(), any())).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.countByCycleIdAndStatus(any(), any())).thenReturn(0L);

        // When
        transactionService.handleStalePendingTransactions();

        // Then
        verify(transactionRepository).findStalePendingTransactions(any(LocalDateTime.class));
        verify(paystackService).verifyTransaction("STALE_TXN_123");
    }

    // @Test
    // void handleStalePendingTransactions_WhenVerificationFails_ShouldMarkAsFailed() {
    //     // Given
    //     Transaction staleTransaction = new Transaction();
    //     staleTransaction.setTransactionId(UUID.randomUUID());
    //     staleTransaction.setStatus(Transaction.TransactionStatus.PENDING);
    //     staleTransaction.setPaystackReference("STALE_TXN_123");

    //     when(transactionRepository.findStalePendingTransactions(any(LocalDateTime.class)))
    //         .thenReturn(Arrays.asList(staleTransaction));
    //     when(transactionRepository.findByPaystackReference("STALE_TXN_123"))
    //         .thenReturn(Optional.of(staleTransaction));
    //     when(paystackService.verifyTransaction("STALE_TXN_123"))
    //         .thenThrow(new RuntimeException("Verification failed"));
    //     when(transactionRepository.save(any(Transaction.class))).thenReturn(staleTransaction);

    //     // When
    //     transactionService.handleStalePendingTransactions();

    //     // Then
    //     verify(transactionRepository).findStalePendingTransactions(any(LocalDateTime.class));
    //     verify(transactionRepository).save(any(Transaction.class));
        
    //     ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
    //     verify(transactionRepository).save(transactionCaptor.capture());
    //     Transaction savedTransaction = transactionCaptor.getValue();
    //     assertEquals(Transaction.TransactionStatus.FAILED, savedTransaction.getStatus());
    //     assertEquals("Transaction verification timeout", savedTransaction.getFailureReason());
    // }

    @Test
    void validatePayerDetails_WhenNotActive_ShouldThrowException() {
        // Given
        payerDetails.setIsActive(false);
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(payerDetailsRepository.findById(testPayerId)).thenReturn(Optional.of(payerDetails));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> transactionService.createTransaction(createTransactionDto));

        assertEquals("Payer details are not active", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void validatePayerDetails_WhenWrongUser_ShouldThrowException() {
        // Given
        payerDetails.setUserId(UUID.randomUUID());
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(payerDetailsRepository.findById(testPayerId)).thenReturn(Optional.of(payerDetails));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> transactionService.createTransaction(createTransactionDto));

        assertEquals("Payer details do not belong to the specified user", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transactionStatistics_FromRepositoryResult_ShouldCalculateCorrectly() {
        // Given
        List<Object[]> statsData = Arrays.asList(
            new Object[]{Transaction.TransactionStatus.COMPLETED, 3L, new BigDecimal("3000.00")},
            new Object[]{Transaction.TransactionStatus.CANCELLED, 1L, new BigDecimal("1000.00")},
            new Object[]{Transaction.TransactionStatus.PROCESSING, 2L, new BigDecimal("2000.00")}
        );

        // When
        TransactionService.TransactionStatistics stats = TransactionService.TransactionStatistics.fromRepositoryResult(statsData);

        // Then
        assertEquals(6L, stats.getTotalTransactions());
        assertEquals(3L, stats.getCompletedTransactions());
        assertEquals(1L, stats.getFailedTransactions()); // CANCELLED counts as failed
        assertEquals(2L, stats.getPendingTransactions()); // PROCESSING counts as pending
        assertEquals(new BigDecimal("6000.00"), stats.getTotalAmount());
        assertEquals(new BigDecimal("3000.00"), stats.getCompletedAmount());
    }

    @Test
    void transactionStatistics_EmptyResult_ShouldReturnZeros() {
        // Given
        List<Object[]> emptyStats = Collections.emptyList();

        // When
        TransactionService.TransactionStatistics stats = TransactionService.TransactionStatistics.fromRepositoryResult(emptyStats);

        // Then
        assertEquals(0L, stats.getTotalTransactions());
        assertEquals(0L, stats.getCompletedTransactions());
        assertEquals(0L, stats.getFailedTransactions());
        assertEquals(0L, stats.getPendingTransactions());
        assertEquals(BigDecimal.ZERO, stats.getTotalAmount());
        assertEquals(BigDecimal.ZERO, stats.getCompletedAmount());
    }

    @Test
    void createTransaction_WhenPayerDetailsNotFound_ShouldThrowException() {
        // Given
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(payerDetailsRepository.findById(testPayerId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> transactionService.createTransaction(createTransactionDto));

        assertEquals("Payer details not found with ID: " + testPayerId, exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void chargeStoredCard_WhenException_ShouldSetFailedStatus() throws Exception {
        // Given
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
        when(payerDetailsRepository.findById(testPayerId)).thenReturn(Optional.of(payerDetails));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(transactionRepository.findByCycleIdAndUserIdAndStatus(testCycleId, testUserId, Transaction.TransactionStatus.COMPLETED))
            .thenReturn(Collections.emptyList());
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(paystackService.chargeTransaction(any(PaystackChargeRequest.class)))
            .thenThrow(new RuntimeException("Network error"));

        // When
        Transaction result = transactionService.chargeStoredCard(createTransactionDto);

        // Then
        verify(transactionRepository, atLeast(2)).save(any(Transaction.class));
        
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, atLeast(2)).save(transactionCaptor.capture());
        List<Transaction> savedTransactions = transactionCaptor.getAllValues();
        Transaction finalTransaction = savedTransactions.get(savedTransactions.size() - 1);
        assertEquals(Transaction.TransactionStatus.FAILED, finalTransaction.getStatus());
        assertTrue(finalTransaction.getFailureReason().contains("Exception during charge"));
        assertEquals("error", finalTransaction.getGatewayStatus());
    }

    @Test
    void verifyTransaction_WhenVerificationFails_ShouldSetFailedStatus() throws Exception {
        // Given
        String reference = "TXN_test123";
        PaystackTransactionVerificationResponse verificationResponse = new PaystackTransactionVerificationResponse();
        verificationResponse.setStatus(false);
        verificationResponse.setMessage("Transaction not found");

        when(transactionRepository.findByPaystackReference(reference)).thenReturn(Optional.of(transaction));
        when(paystackService.verifyTransaction(reference)).thenReturn(verificationResponse);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // When
        Transaction result = transactionService.verifyTransaction(reference);

        // Then
        verify(transactionRepository).save(any(Transaction.class));
        
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction savedTransaction = transactionCaptor.getValue();
        assertEquals(Transaction.TransactionStatus.FAILED, savedTransaction.getStatus());
        assertEquals("Transaction not found", savedTransaction.getFailureReason());
    }

    @Test
    void verifyTransaction_WhenException_ShouldSetFailedStatus() throws Exception {
        // Given
        String reference = "TXN_test123";
        when(transactionRepository.findByPaystackReference(reference)).thenReturn(Optional.of(transaction));
        when(paystackService.verifyTransaction(reference)).thenThrow(new RuntimeException("Network error"));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // When
        Transaction result = transactionService.verifyTransaction(reference);

        // Then
        verify(transactionRepository).save(any(Transaction.class));
        
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction savedTransaction = transactionCaptor.getValue();
        assertEquals(Transaction.TransactionStatus.FAILED, savedTransaction.getStatus());
        assertTrue(savedTransaction.getFailureReason().contains("Verification failed"));
    }

    @Test
    void updateCycleTotals_WhenCollectionComplete_ShouldUpdateCycleStatus() {
        // Given
        groupCycle.setExpectedTotal(new BigDecimal("3000.00"));
        
        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(groupCycle));
        when(transactionRepository.getTotalAmountByCycleAndStatus(testCycleId, Transaction.TransactionStatus.COMPLETED))
            .thenReturn(new BigDecimal("3000.00"));
        when(transactionRepository.countByCycleIdAndStatus(eq(testCycleId), any(Transaction.TransactionStatus.class)))
            .thenReturn(5L);
        when(groupCycleRepository.save(any(GroupCycle.class))).thenReturn(groupCycle);

        // Create a completed transaction to trigger cycle update
        ProcessTransactionDto processDto = new ProcessTransactionDto();
        processDto.setStatus(Transaction.TransactionStatus.COMPLETED);
        processDto.setPaystackTransId("PST_123");
        processDto.setGatewayStatus("success");

        when(transactionRepository.findById(testTransactionId)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // When
        transactionService.processTransaction(testTransactionId, processDto);

        // Then
        verify(groupCycleRepository).save(any(GroupCycle.class));
        
        ArgumentCaptor<GroupCycle> cycleCaptor = ArgumentCaptor.forClass(GroupCycle.class);
        verify(groupCycleRepository).save(cycleCaptor.capture());
        GroupCycle savedCycle = cycleCaptor.getValue();
        assertEquals("collection_complete", savedCycle.getStatus());
        assertEquals(new BigDecimal("3000.00"), savedCycle.getCurrentTotal());
    }
}