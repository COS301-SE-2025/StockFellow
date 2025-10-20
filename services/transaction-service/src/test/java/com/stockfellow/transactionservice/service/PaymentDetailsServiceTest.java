package com.stockfellow.transactionservice.service;

import com.stockfellow.transactionservice.dto.CreatePayerDetailsDto;
import com.stockfellow.transactionservice.dto.CreatePayoutDetailsDto;
import com.stockfellow.transactionservice.dto.InitializeCardAuthDto;
import com.stockfellow.transactionservice.integration.PaystackService;
import com.stockfellow.transactionservice.integration.dto.*;
import com.stockfellow.transactionservice.integration.dto.PaystackTransactionResponse.PaystackTransactionData;
import com.stockfellow.transactionservice.integration.dto.PaystackTransferRecipientResponse.PaystackTransferRecipientData;
import com.stockfellow.transactionservice.model.*;
import com.stockfellow.transactionservice.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PayoutDetailsRepository payoutDetailsRepository;

    @Mock
    private PayerDetailsRepository payerDetailsRepository;

    @Mock
    private PaystackService paystackService;

    @InjectMocks
    private PaymentDetailsService paymentDetailsService;

    private UUID testUserId;
    private UUID testPayerId;
    private UUID testPayoutId;
    private User user;
    private PayerDetails payerDetails;
    private PayoutDetails payoutDetails;
    private CreatePayerDetailsDto createPayerDetailsDto;
    private CreatePayoutDetailsDto createPayoutDetailsDto;
    private InitializeCardAuthDto initializeCardAuthDto;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testPayerId = UUID.randomUUID();
        testPayoutId = UUID.randomUUID();

        // Set configuration values
        ReflectionTestUtils.setField(paymentDetailsService, "callbackBaseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(paymentDetailsService, "paystackKey", "sk_test_secretkey");

        // Create test entities
        user = new User();
        user.setUserId(testUserId);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");

        payerDetails = new PayerDetails();
        payerDetails.setPayerId(testPayerId);
        payerDetails.setUserId(testUserId);
        payerDetails.setType("card");
        payerDetails.setEmail("test@example.com");
        payerDetails.setAuthCode("AUTH_test123");
        payerDetails.setCardType("visa");
        payerDetails.setLast4("1234");
        payerDetails.setExpMonth("12");
        payerDetails.setExpYear("2025");
        payerDetails.setBin("123456");
        payerDetails.setBank("Test Bank");
        payerDetails.setSignature("SIG_test123");
        payerDetails.setIsActive(true);
        payerDetails.setIsAuthenticated(true);

        payoutDetails = new PayoutDetails();
        payoutDetails.setPayoutId(testPayoutId);
        payoutDetails.setUserId(testUserId);
        payoutDetails.setType("bank_account");
        payoutDetails.setRecipientName("John Doe");
        payoutDetails.setAccountNumber("1234567890");
        payoutDetails.setBankCode("632005");
        payoutDetails.setBankName("Test Bank");
        payoutDetails.setRecipientCode("RCP_test123");
        payoutDetails.setIsVerified(true);

        // Create test DTOs
        createPayerDetailsDto = new CreatePayerDetailsDto();
        createPayerDetailsDto.setUserId(testUserId);
        createPayerDetailsDto.setType("card");
        createPayerDetailsDto.setEmail("test@example.com");
        createPayerDetailsDto.setAuthCode("AUTH_test123");
        createPayerDetailsDto.setCardType("visa");
        createPayerDetailsDto.setLast4("1234");
        createPayerDetailsDto.setExpMonth("12");
        createPayerDetailsDto.setExpYear("2025");
        createPayerDetailsDto.setBin("123456");
        createPayerDetailsDto.setBank("Test Bank");
        createPayerDetailsDto.setSignature("SIG_test123");

        createPayoutDetailsDto = new CreatePayoutDetailsDto();
        createPayoutDetailsDto.setUserId(testUserId);
        createPayoutDetailsDto.setType("bank_account");
        createPayoutDetailsDto.setRecipientName("John Doe");
        createPayoutDetailsDto.setAccountNumber("1234567890");
        createPayoutDetailsDto.setBankCode("632005");
        createPayoutDetailsDto.setBankName("Test Bank");

        initializeCardAuthDto = new InitializeCardAuthDto();
        initializeCardAuthDto.setUserId(testUserId);
        initializeCardAuthDto.setType("card");
        initializeCardAuthDto.setEmail("test@example.com");
    }

    @Test
    void processPaystackCallback_WhenSuccessful_ShouldReturnSuccessResponse() throws Exception {
        // Given
        String reference = "TXN_test123";
        PaystackTransactionVerificationResponse.PaystackTransactionVerificationData verificationData = 
            new PaystackTransactionVerificationResponse.PaystackTransactionVerificationData();
        verificationData.setStatus("success");

        PaystackTransactionVerificationResponse verificationResponse = new PaystackTransactionVerificationResponse();
        verificationResponse.setStatus(true);
        verificationResponse.setData(verificationData);

        when(paystackService.verifyTransaction(reference)).thenReturn(verificationResponse);

        // When
        Map<String, Object> result = paymentDetailsService.processPaystackCallback(reference);

        // Then
        assertTrue((Boolean) result.get("success"));
        assertEquals("success", result.get("status"));
        assertEquals("Payment completed successfully! We're processing your card details...", result.get("message"));
        assertEquals(reference, result.get("reference"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("card_processing", data.get("next_step"));

        verify(paystackService).verifyTransaction(reference);
    }

    @Test
    void processPaystackCallback_WhenFailed_ShouldReturnFailureResponse() throws Exception {
        // Given
        String reference = "TXN_failed123";
        PaystackTransactionVerificationResponse.PaystackTransactionVerificationData verificationData = 
            new PaystackTransactionVerificationResponse.PaystackTransactionVerificationData();
        verificationData.setStatus("failed");

        PaystackTransactionVerificationResponse verificationResponse = new PaystackTransactionVerificationResponse();
        verificationResponse.setStatus(true);
        verificationResponse.setData(verificationData);

        when(paystackService.verifyTransaction(reference)).thenReturn(verificationResponse);

        // When
        Map<String, Object> result = paymentDetailsService.processPaystackCallback(reference);

        // Then
        assertFalse((Boolean) result.get("success"));
        assertEquals("failed", result.get("status"));
        assertEquals("Payment failed. Please try again.", result.get("message"));
        assertEquals(reference, result.get("reference"));
    }

    @Test
    void processPaystackCallback_WhenException_ShouldReturnErrorResponse() throws Exception {
        // Given
        String reference = "TXN_error123";
        when(paystackService.verifyTransaction(reference)).thenThrow(new RuntimeException("Network error"));

        // When
        Map<String, Object> result = paymentDetailsService.processPaystackCallback(reference);

        // Then
        assertFalse((Boolean) result.get("success"));
        assertEquals("error", result.get("status"));
        assertEquals("An error occurred. Please contact support.", result.get("message"));
        assertEquals(reference, result.get("reference"));
        assertEquals("Network error", result.get("error"));
    }

    @Test
    void processPaystackWebhook_WhenChargeSuccess_ShouldProcessAuthorization() throws Exception {
        // Given
        String payload = """
            {
                "event": "charge.success",
                "data": {
                    "reference": "TXN_webhook123",
                    "status": "success"
                }
            }
            """;

        PaystackTransactionVerificationResponse.PaystackTransactionVerificationData verificationData = 
            new PaystackTransactionVerificationResponse.PaystackTransactionVerificationData();
        verificationData.setStatus("success");
        verificationData.setReference("TXN_webhook123");

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("user_id", testUserId.toString());
        metadata.put("payment_method_type", "card");
        verificationData.setMetadata(metadata);

        PaystackAuthorization authorization = new PaystackAuthorization();
        authorization.setAuthorizationCode("AUTH_webhook123");
        authorization.setCardType("visa");
        authorization.setLast4("4321");
        authorization.setExpMonth("12");
        authorization.setExpYear("2026");
        authorization.setBin("654321");
        authorization.setBank("Webhook Bank");
        authorization.setSignature("SIG_webhook123");
        verificationData.setAuthorization(authorization);

        PaystackTransactionVerificationResponse verificationResponse = new PaystackTransactionVerificationResponse();
        verificationResponse.setStatus(true);
        verificationResponse.setData(verificationData);

        when(paystackService.verifyTransaction("TXN_webhook123")).thenReturn(verificationResponse);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(payerDetailsRepository.existsByUserIdAndSignature(testUserId, "SIG_webhook123")).thenReturn(false);
        when(payerDetailsRepository.findByAuthCode("AUTH_webhook123")).thenReturn(Optional.empty());
        when(payerDetailsRepository.countByUserIdAndIsActiveTrue(testUserId)).thenReturn(0L);
        when(payerDetailsRepository.save(any(PayerDetails.class))).thenReturn(payerDetails);
        when(payerDetailsRepository.findFirstByUserIdAndIsAuthenticatedFalse(testUserId)).thenReturn(Optional.empty());

        // When
        paymentDetailsService.processPaystackWebhook(payload);

        // Then
        verify(paystackService).verifyTransaction("TXN_webhook123");
        verify(payerDetailsRepository).save(any(PayerDetails.class));

        ArgumentCaptor<PayerDetails> payerCaptor = ArgumentCaptor.forClass(PayerDetails.class);
        verify(payerDetailsRepository).save(payerCaptor.capture());
        PayerDetails savedPayer = payerCaptor.getValue();
        assertEquals(testUserId, savedPayer.getUserId());
        assertEquals("AUTH_webhook123", savedPayer.getAuthCode());
        assertEquals("visa", savedPayer.getCardType());
        assertEquals("4321", savedPayer.getLast4());
    }

    @Test
    void processPaystackWebhook_WhenUnsupportedEvent_ShouldIgnore() {
        // Given
        String payload = """
            {
                "event": "invoice.create",
                "data": {
                    "reference": "INV_123"
                }
            }
            """;

        // When
        paymentDetailsService.processPaystackWebhook(payload);

        // Then
        verify(paystackService, never()).verifyTransaction(any());
        verify(payerDetailsRepository, never()).save(any());
    }

    @Test
    void processPaystackWebhook_WhenInvalidPayload_ShouldThrowException() {
        // Given
        String invalidPayload = "invalid json";

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> paymentDetailsService.processPaystackWebhook(invalidPayload));

        assertEquals("Webhook processing failed", exception.getMessage());
    }

    // @Test
    // void verifyWebhookSignature_WhenValid_ShouldReturnTrue() {
    //     // Given
    //     String payload = "test payload";
    //     String validSignature = "f52ca28cb388d7c8eb65beaae23fb7c3c79e7c6989b4b8b70b3b34e0df6dc9892a43d8e67b1a2a30102a6a7c5c0ec22d9b1a8c234e72a11a0d66f0aec6b2b69";

    //     // When
    //     boolean result = paymentDetailsService.verifyWebhookSignature(payload, validSignature);

    //     // Then
    //     assertTrue(result);
    // }

    @Test
    void verifyWebhookSignature_WhenInvalid_ShouldReturnFalse() {
        // Given
        String payload = "test payload";
        String invalidSignature = "invalid_signature";

        // When
        boolean result = paymentDetailsService.verifyWebhookSignature(payload, invalidSignature);

        // Then
        assertFalse(result);
    }

    @Test
    void addPayerDetails_WhenValid_ShouldCreatePayerDetails() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(payerDetailsRepository.existsByUserIdAndSignature(testUserId, "SIG_test123")).thenReturn(false);
        when(payerDetailsRepository.findByAuthCode("AUTH_test123")).thenReturn(Optional.empty());
        when(payerDetailsRepository.countByUserIdAndIsActiveTrue(testUserId)).thenReturn(0L);
        when(payerDetailsRepository.save(any(PayerDetails.class))).thenReturn(payerDetails);

        // When
        PayerDetails result = paymentDetailsService.addPayerDetails(createPayerDetailsDto);

        // Then
        assertNotNull(result);
        verify(payerDetailsRepository).save(any(PayerDetails.class));

        ArgumentCaptor<PayerDetails> payerCaptor = ArgumentCaptor.forClass(PayerDetails.class);
        verify(payerDetailsRepository).save(payerCaptor.capture());
        PayerDetails savedPayer = payerCaptor.getValue();
        assertEquals(testUserId, savedPayer.getUserId());
        assertEquals("card", savedPayer.getType());
        assertEquals("test@example.com", savedPayer.getEmail());
        assertEquals("AUTH_test123", savedPayer.getAuthCode());
        assertTrue(savedPayer.getIsActive()); // Should be active since no other active cards
    }

    @Test
    void addPayerDetails_WhenUserNotFound_ShouldThrowException() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> paymentDetailsService.addPayerDetails(createPayerDetailsDto));

        assertEquals("User not found with ID: " + testUserId, exception.getMessage());
        verify(payerDetailsRepository, never()).save(any());
    }

    @Test
    void addPayerDetails_WhenDuplicateSignature_ShouldThrowException() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(payerDetailsRepository.existsByUserIdAndSignature(testUserId, "SIG_test123")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> paymentDetailsService.addPayerDetails(createPayerDetailsDto));

        assertEquals("This card is already saved to your account", exception.getMessage());
        verify(payerDetailsRepository, never()).save(any());
    }

    @Test
    void addPayerDetails_WhenDuplicateAuthCode_ShouldThrowException() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(payerDetailsRepository.existsByUserIdAndSignature(testUserId, "SIG_test123")).thenReturn(false);
        when(payerDetailsRepository.findByAuthCode("AUTH_test123")).thenReturn(Optional.of(payerDetails));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> paymentDetailsService.addPayerDetails(createPayerDetailsDto));

        assertEquals("Payment authorization already exists", exception.getMessage());
        verify(payerDetailsRepository, never()).save(any());
    }

    @Test
    void addPayerDetails_WhenHasActiveCards_ShouldSetInactive() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(payerDetailsRepository.existsByUserIdAndSignature(testUserId, "SIG_test123")).thenReturn(false);
        when(payerDetailsRepository.findByAuthCode("AUTH_test123")).thenReturn(Optional.empty());
        when(payerDetailsRepository.countByUserIdAndIsActiveTrue(testUserId)).thenReturn(1L); // Has active cards
        when(payerDetailsRepository.save(any(PayerDetails.class))).thenReturn(payerDetails);

        // When
        PayerDetails result = paymentDetailsService.addPayerDetails(createPayerDetailsDto);

        // Then
        ArgumentCaptor<PayerDetails> payerCaptor = ArgumentCaptor.forClass(PayerDetails.class);
        verify(payerDetailsRepository).save(payerCaptor.capture());
        PayerDetails savedPayer = payerCaptor.getValue();
        assertFalse(savedPayer.getIsActive()); // Should be inactive since user has other active cards
    }

    @Test
    void findPayerDetailsByUserId_ShouldReturnList() {
        // Given
        List<PayerDetails> expectedList = Arrays.asList(payerDetails);
        when(payerDetailsRepository.findByUserId(testUserId)).thenReturn(expectedList);

        // When
        List<PayerDetails> result = paymentDetailsService.findPayerDetailsByUserId(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(payerDetails, result.get(0));
        verify(payerDetailsRepository).findByUserId(testUserId);
    }

    @Test
    void deactivateCard_WhenExists_ShouldDeactivate() {
        // Given
        when(payerDetailsRepository.findById(testPayerId)).thenReturn(Optional.of(payerDetails));
        when(payerDetailsRepository.save(any(PayerDetails.class))).thenReturn(payerDetails);

        // When
        PayerDetails result = paymentDetailsService.deactivateCard(testPayerId);

        // Then
        assertNotNull(result);
        verify(payerDetailsRepository).save(any(PayerDetails.class));

        ArgumentCaptor<PayerDetails> payerCaptor = ArgumentCaptor.forClass(PayerDetails.class);
        verify(payerDetailsRepository).save(payerCaptor.capture());
        PayerDetails savedPayer = payerCaptor.getValue();
        assertFalse(savedPayer.getIsActive());
    }

    @Test
    void deactivateCard_WhenNotFound_ShouldThrowException() {
        // Given
        when(payerDetailsRepository.findById(testPayerId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> paymentDetailsService.deactivateCard(testPayerId));

        assertEquals("Payer details not found with ID: " + testPayerId, exception.getMessage());
        verify(payerDetailsRepository, never()).save(any());
    }

    @Test
    void initializeCardAuth_WhenSuccessful_ShouldReturnAuthUrl() throws Exception {
        // Given
        PaystackTransactionData responseData = new PaystackTransactionData();
        responseData.setAuthorizationUrl("https://checkout.paystack.com/auth123");
        responseData.setAccessCode("access123");
        responseData.setReference("AUTH_ref123");

        PaystackTransactionResponse response = new PaystackTransactionResponse();
        response.setStatus(true);
        response.setData(responseData);

        when(paystackService.initializeTransaction(any(PaystackTransactionRequest.class))).thenReturn(response);
        when(payerDetailsRepository.save(any(PayerDetails.class))).thenReturn(payerDetails);

        // When
        Map<String, Object> result = paymentDetailsService.initializeCardAuth(initializeCardAuthDto);

        // Then
        assertTrue((Boolean) result.get("status"));
        assertEquals("Authorization initialized successfully", result.get("message"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("https://checkout.paystack.com/auth123", data.get("authorization_url"));
        assertEquals("access123", data.get("access_code"));
        assertEquals("AUTH_ref123", data.get("reference"));

        verify(paystackService).initializeTransaction(any(PaystackTransactionRequest.class));
        verify(payerDetailsRepository).save(any(PayerDetails.class));

        // Verify pending auth record creation
        ArgumentCaptor<PayerDetails> payerCaptor = ArgumentCaptor.forClass(PayerDetails.class);
        verify(payerDetailsRepository).save(payerCaptor.capture());
        PayerDetails pendingAuth = payerCaptor.getValue();
        assertEquals(testUserId, pendingAuth.getUserId());
        assertFalse(pendingAuth.getIsAuthenticated());
        assertFalse(pendingAuth.getIsActive());
    }

    @Test
    void initializeCardAuth_WhenPaystackFails_ShouldReturnError() throws Exception {
        // Given
        PaystackTransactionResponse response = new PaystackTransactionResponse();
        response.setStatus(false);
        response.setMessage("Initialization failed");

        when(paystackService.initializeTransaction(any(PaystackTransactionRequest.class))).thenReturn(response);

        // When
        Map<String, Object> result = paymentDetailsService.initializeCardAuth(initializeCardAuthDto);

        // Then
        assertFalse((Boolean) result.get("status"));
        assertTrue(result.get("message").toString().contains("Paystack initialization failed"));
        verify(payerDetailsRepository, never()).save(any());
    }

    @Test
    void addPayoutDetails_WhenValid_ShouldCreatePayoutDetails() throws Exception {
        // Given
        PaystackTransferRecipientData recipientData = new PaystackTransferRecipientData();
        recipientData.setRecipientCode("RCP_created123");

        PaystackTransferRecipientResponse recipientResponse = new PaystackTransferRecipientResponse();
        recipientResponse.setStatus(true);
        recipientResponse.setData(recipientData);

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(payoutDetailsRepository.existsByUserIdAndIsVerifiedTrue(testUserId)).thenReturn(false);
        when(paystackService.createTransferRecipient(any(PaystackTransferRecipientRequest.class))).thenReturn(recipientResponse);
        when(payoutDetailsRepository.save(any(PayoutDetails.class))).thenReturn(payoutDetails);

        // When
        PayoutDetails result = paymentDetailsService.addPayoutDetails(createPayoutDetailsDto);

        // Then
        assertNotNull(result);
        verify(paystackService).createTransferRecipient(any(PaystackTransferRecipientRequest.class));
        verify(payoutDetailsRepository).save(any(PayoutDetails.class));

        ArgumentCaptor<PaystackTransferRecipientRequest> requestCaptor = ArgumentCaptor.forClass(PaystackTransferRecipientRequest.class);
        verify(paystackService).createTransferRecipient(requestCaptor.capture());
        PaystackTransferRecipientRequest capturedRequest = requestCaptor.getValue();
        assertEquals("basa", capturedRequest.getType());
        assertEquals("1234567890", capturedRequest.getAccountNumber());
        assertEquals("632005", capturedRequest.getBankCode());
        assertEquals("ZAR", capturedRequest.getCurrency());
        assertEquals("John Doe", capturedRequest.getName());

        ArgumentCaptor<PayoutDetails> payoutCaptor = ArgumentCaptor.forClass(PayoutDetails.class);
        verify(payoutDetailsRepository).save(payoutCaptor.capture());
        PayoutDetails savedPayout = payoutCaptor.getValue();
        assertEquals(testUserId, savedPayout.getUserId());
        assertEquals("RCP_created123", savedPayout.getRecipientCode());
        assertTrue(savedPayout.getIsVerified());
    }

    @Test
    void addPayoutDetails_WhenUserNotFound_ShouldThrowException() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> paymentDetailsService.addPayoutDetails(createPayoutDetailsDto));

        assertEquals("User not found with ID: " + testUserId, exception.getMessage());
        verify(paystackService, never()).createTransferRecipient(any());
        verify(payoutDetailsRepository, never()).save(any());
    }

    @Test
    void addPayoutDetails_WhenDuplicateExists_ShouldThrowException() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(payoutDetailsRepository.existsByUserIdAndIsVerifiedTrue(testUserId)).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> paymentDetailsService.addPayoutDetails(createPayoutDetailsDto));

        assertEquals("User with ID: " + testUserId + " already has payout details assigned", exception.getMessage());
        verify(paystackService, never()).createTransferRecipient(any());
        verify(payoutDetailsRepository, never()).save(any());
    }

    @Test
    void addPayoutDetails_WhenPaystackFails_ShouldThrowException() throws Exception {
        // Given
        PaystackTransferRecipientResponse recipientResponse = new PaystackTransferRecipientResponse();
        recipientResponse.setStatus(false);
        recipientResponse.setMessage("Invalid account number");

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(payoutDetailsRepository.existsByUserIdAndIsVerifiedTrue(testUserId)).thenReturn(false);
        when(paystackService.createTransferRecipient(any(PaystackTransferRecipientRequest.class))).thenReturn(recipientResponse);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> paymentDetailsService.addPayoutDetails(createPayoutDetailsDto));

        assertTrue(exception.getMessage().contains("Paystack recipient creation failed"));
        verify(payoutDetailsRepository, never()).save(any());
    }

    @Test
    void addPayoutDetails_WhenPaystackThrowsException_ShouldThrowException() throws Exception {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(payoutDetailsRepository.existsByUserIdAndIsVerifiedTrue(testUserId)).thenReturn(false);
        when(paystackService.createTransferRecipient(any(PaystackTransferRecipientRequest.class)))
            .thenThrow(new RuntimeException("Network error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> paymentDetailsService.addPayoutDetails(createPayoutDetailsDto));

        assertTrue(exception.getMessage().contains("Failed to create transfer recipient"));
        verify(payoutDetailsRepository, never()).save(any());
    }

    @Test
    void findPayoutDetailsByUserId_ShouldReturnList() {
        // Given
        List<PayoutDetails> expectedList = Arrays.asList(payoutDetails);
        when(payoutDetailsRepository.findByUserId(testUserId)).thenReturn(expectedList);

        // When
        List<PayoutDetails> result = paymentDetailsService.findPayoutDetailsByUserId(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(payoutDetails, result.get(0));
        verify(payoutDetailsRepository).findByUserId(testUserId);
    }

    @Test
    void validateNoDuplicatePayerDetails_WhenNoDuplicates_ShouldPass() {
        // Given
        when(payerDetailsRepository.existsByUserIdAndSignature(testUserId, "SIG_test123")).thenReturn(false);
        when(payerDetailsRepository.findByAuthCode("AUTH_test123")).thenReturn(Optional.empty());

        // When & Then
        assertDoesNotThrow(() -> paymentDetailsService.validateNoDuplicatePayerDetails(createPayerDetailsDto));
    }

    @Test
    void validateNoDuplicatePayoutDetails_WhenNoDuplicates_ShouldPass() {
        // Given
        when(payoutDetailsRepository.existsByUserIdAndIsVerifiedTrue(testUserId)).thenReturn(false);

        // When & Then
        assertDoesNotThrow(() -> paymentDetailsService.validateNoDuplicatePayoutDetails(testUserId));
    }
}