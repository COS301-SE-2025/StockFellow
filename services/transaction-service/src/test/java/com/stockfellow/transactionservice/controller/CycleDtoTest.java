// package com.stockfellow.transactionservice.dto;

// import com.stockfellow.transactionservice.model.Transaction;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;

// import java.math.BigDecimal;
// import java.time.LocalDateTime;
// import java.util.UUID;

// import static org.junit.jupiter.api.Assertions.*;

// @DisplayName("Transaction DTO Tests")
// class TransactionDtoTest {

//     @Nested
//     @DisplayName("ProcessTransactionRequest Tests")
//     class ProcessTransactionRequestTest {

//         private ProcessTransactionRequest request;
//         private UUID testCycleId;
//         private UUID testPayerUserId;

//         @BeforeEach
//         void setUp() {
//             testCycleId = UUID.randomUUID();
//             testPayerUserId = UUID.randomUUID();
//             request = new ProcessTransactionRequest();
//         }

//         @Test
//         @DisplayName("Should create empty request with default constructor")
//         void defaultConstructor_ShouldCreateEmptyRequest() {
//             // When
//             ProcessTransactionRequest emptyRequest = new ProcessTransactionRequest();

//             // Then
//             assertNotNull(emptyRequest);
//             assertNull(emptyRequest.getCycleId());
//             assertNull(emptyRequest.getSpecificPayerUserId());
//             assertEquals(3, emptyRequest.getMaxRetries()); // Default value
//             assertNull(emptyRequest.getProcessingReason());
//         }

//         @Test
//         @DisplayName("Should create request with parameterized constructor")
//         void parameterizedConstructor_ShouldCreateRequestWithAllFields() {
//             // Given
//             Integer maxRetries = 5;
//             String processingReason = "Manual retry request";

//             // When
//             ProcessTransactionRequest request = new ProcessTransactionRequest(
//                 testCycleId, testPayerUserId, maxRetries, processingReason);

//             // Then
//             assertNotNull(request);
//             assertEquals(testCycleId, request.getCycleId());
//             assertEquals(testPayerUserId, request.getSpecificPayerUserId());
//             assertEquals(maxRetries, request.getMaxRetries());
//             assertEquals(processingReason, request.getProcessingReason());
//         }

//         @Test
//         @DisplayName("Should set default max retries when null in constructor")
//         void parameterizedConstructor_WithNullMaxRetries_ShouldSetDefault() {
//             // When
//             ProcessTransactionRequest request = new ProcessTransactionRequest(
//                 testCycleId, testPayerUserId, null, "Test reason");

//             // Then
//             assertEquals(3, request.getMaxRetries()); // Default value
//         }

//         @Test
//         @DisplayName("Should set and get all fields correctly")
//         void settersAndGetters_ShouldWorkCorrectly() {
//             // Given
//             Integer maxRetries = 7;
//             String processingReason = "Automated retry";

//             // When
//             request.setCycleId(testCycleId);
//             request.setSpecificPayerUserId(testPayerUserId);
//             request.setMaxRetries(maxRetries);
//             request.setProcessingReason(processingReason);

//             // Then
//             assertEquals(testCycleId, request.getCycleId());
//             assertEquals(testPayerUserId, request.getSpecificPayerUserId());
//             assertEquals(maxRetries, request.getMaxRetries());
//             assertEquals(processingReason, request.getProcessingReason());
//         }

//         @Test
//         @DisplayName("Should set default max retries when null in setter")
//         void setMaxRetries_WithNull_ShouldSetDefault() {
//             // When
//             request.setMaxRetries(null);

//             // Then
//             assertEquals(3, request.getMaxRetries());
//         }

//         @Test
//         @DisplayName("Should handle null values gracefully")
//         void settersWithNullValues_ShouldHandleGracefully() {
//             // When
//             request.setCycleId(null);
//             request.setSpecificPayerUserId(null);
//             request.setProcessingReason(null);

//             // Then
//             assertNull(request.getCycleId());
//             assertNull(request.getSpecificPayerUserId());
//             assertNull(request.getProcessingReason());
//         }
//     }

//     @Nested
//     @DisplayName("TransactionResponse Tests")
//     class TransactionResponseTest {

//         private TransactionResponse response;
//         private UUID testTransactionId;
//         private UUID testCycleId;
//         private UUID testMandateId;
//         private UUID testPayerUserId;
//         private UUID testRecipientUserId;
//         private UUID testGroupId;
//         private UUID testPayerPaymentMethodId;
//         private UUID testRecipientPaymentMethodId;
//         private LocalDateTime testDateTime;

//         @BeforeEach
//         void setUp() {
//             testTransactionId = UUID.randomUUID();
//             testCycleId = UUID.randomUUID();
//             testMandateId = UUID.randomUUID();
//             testPayerUserId = UUID.randomUUID();
//             testRecipientUserId = UUID.randomUUID();
//             testGroupId = UUID.randomUUID();
//             testPayerPaymentMethodId = UUID.randomUUID();
//             testRecipientPaymentMethodId = UUID.randomUUID();
//             testDateTime = LocalDateTime.now();

//             response = new TransactionResponse();
//         }

//         @Test
//         @DisplayName("Should create empty response with default constructor")
//         void defaultConstructor_ShouldCreateEmptyResponse() {
//             // When
//             TransactionResponse emptyResponse = new TransactionResponse();

//             // Then
//             assertNotNull(emptyResponse);
//             assertNull(emptyResponse.getTransactionId());
//             assertNull(emptyResponse.getCycleId());
//             assertNull(emptyResponse.getMandateId());
//             assertNull(emptyResponse.getPayerUserId());
//             assertNull(emptyResponse.getRecipientUserId());
//             assertNull(emptyResponse.getGroupId());
//             assertNull(emptyResponse.getPayerPaymentMethodId());
//             assertNull(emptyResponse.getRecipientPaymentMethodId());
//             assertNull(emptyResponse.getAmount());
//             assertNull(emptyResponse.getStatus());
//             assertNull(emptyResponse.getExternalReference());
//             assertNull(emptyResponse.getRetryCount());
//             assertNull(emptyResponse.getFailMessage());
//             assertNull(emptyResponse.getCreatedAt());
//             assertNull(emptyResponse.getCompletedAt());
//             assertNull(emptyResponse.getStatusDescription());
//             assertNull(emptyResponse.getPayerName());
//             assertNull(emptyResponse.getRecipientName());
//         }

//         @Test
//         @DisplayName("Should create response with parameterized constructor")
//         void parameterizedConstructor_ShouldCreateResponseWithAllFields() {
//             // Given
//             BigDecimal amount = new BigDecimal("150.75");
//             String status = "COMPLETED";
//             String externalReference = "EXT123456";
//             Integer retryCount = 2;
//             String failMessage = "Network timeout";
//             LocalDateTime createdAt = testDateTime.minusHours(2);
//             LocalDateTime completedAt = testDateTime;
//             String statusDescription = "Transaction completed successfully";
//             String payerName = "John Doe";
//             String recipientName = "Jane Smith";

//             // When
//             TransactionResponse response = new TransactionResponse(
//                 testTransactionId, testCycleId, testMandateId, testPayerUserId,
//                 testRecipientUserId, testGroupId, testPayerPaymentMethodId,
//                 testRecipientPaymentMethodId, amount, status, externalReference,
//                 retryCount, failMessage, createdAt, completedAt, statusDescription,
//                 payerName, recipientName);

//             // Then
//             assertNotNull(response);
//             assertEquals(testTransactionId, response.getTransactionId());
//             assertEquals(testCycleId, response.getCycleId());
//             assertEquals(testMandateId, response.getMandateId());
//             assertEquals(testPayerUserId, response.getPayerUserId());
//             assertEquals(testRecipientUserId, response.getRecipientUserId());
//             assertEquals(testGroupId, response.getGroupId());
//             assertEquals(testPayerPaymentMethodId, response.getPayerPaymentMethodId());
//             assertEquals(testRecipientPaymentMethodId, response.getRecipientPaymentMethodId());
//             assertEquals(amount, response.getAmount());
//             assertEquals(status, response.getStatus());
//             assertEquals(externalReference, response.getExternalReference());
//             assertEquals(retryCount, response.getRetryCount());
//             assertEquals(failMessage, response.getFailMessage());
//             assertEquals(createdAt, response.getCreatedAt());
//             assertEquals(completedAt, response.getCompletedAt());
//             assertEquals(statusDescription, response.getStatusDescription());
//             assertEquals(payerName, response.getPayerName());
//             assertEquals(recipientName, response.getRecipientName());
//         }

//         @Test
//         @DisplayName("Should create response from Transaction entity")
//         void fromTransaction_ShouldCreateResponseFromEntity() {
//             // Given
//             Transaction transaction = new Transaction();
//             transaction.setTransactionId(testTransactionId);
//             transaction.setCycleId(testCycleId);
//             transaction.setMandateId(testMandateId);
//             transaction.setPayerUserId(testPayerUserId);
//             transaction.setRecipientUserId(testRecipientUserId);
//             transaction.setGroupId(testGroupId);
//             transaction.setPayerPaymentMethodId(testPayerPaymentMethodId);
//             transaction.setRecipientPaymentMethodId(testRecipientPaymentMethodId);
//             transaction.setAmount(new BigDecimal("200.00"));
//             transaction.setStatus("PENDING");
//             transaction.setExternalReference("EXT789");
//             transaction.setRetryCount(1);
//             transaction.setFailMessage("Initial failure");
//             transaction.setCreatedAt(testDateTime.minusMinutes(30));
//             transaction.setCompletedAt(testDateTime);

//             // When
//             TransactionResponse response = TransactionResponse.fromTransaction(transaction);

//             // Then
//             assertNotNull(response);
//             assertEquals(transaction.getTransactionId(), response.getTransactionId());
//             assertEquals(transaction.getCycleId(), response.getCycleId());
//             assertEquals(transaction.getMandateId(), response.getMandateId());
//             assertEquals(transaction.getPayerUserId(), response.getPayerUserId());
//             assertEquals(transaction.getRecipientUserId(), response.getRecipientUserId());
//             assertEquals(transaction.getGroupId(), response.getGroupId());
//             assertEquals(transaction.getPayerPaymentMethodId(), response.getPayerPaymentMethodId());
//             assertEquals(transaction.getRecipientPaymentMethodId(), response.getRecipientPaymentMethodId());
//             assertEquals(transaction.getAmount(), response.getAmount());
//             assertEquals(transaction.getStatus(), response.getStatus());
//             assertEquals(transaction.getExternalReference(), response.getExternalReference());
//             assertEquals(transaction.getRetryCount(), response.getRetryCount());
//             assertEquals(transaction.getFailMessage(), response.getFailMessage());
//             assertEquals(transaction.getCreatedAt(), response.getCreatedAt());
//             assertEquals(transaction.getCompletedAt(), response.getCompletedAt());
//             assertEquals("Transaction is pending", response.getStatusDescription());
//             assertNull(response.getPayerName()); // Not set in factory method
//             assertNull(response.getRecipientName()); // Not set in factory method
//         }

//         @Test
//         @DisplayName("Should set and get all fields correctly")
//         void settersAndGetters_ShouldWorkCorrectly() {
//             // Given
//             BigDecimal amount = new BigDecimal("99.99");
//             String status = "FAILED";
//             String externalReference = "EXT999";
//             Integer retryCount = 3;
//             String failMessage = "Payment declined";
//             LocalDateTime createdAt = testDateTime.minusHours(1);
//             LocalDateTime completedAt = testDateTime.minusMinutes(5);
//             String statusDescription = "Transaction failed";
//             String payerName = "Alice Johnson";
//             String recipientName = "Bob Wilson";

//             // When
//             response.setTransactionId(testTransactionId);
//             response.setCycleId(testCycleId);
//             response.setMandateId(testMandateId);
//             response.setPayerUserId(testPayerUserId);
//             response.setRecipientUserId(testRecipientUserId);
//             response.setGroupId(testGroupId);
//             response.setPayerPaymentMethodId(testPayerPaymentMethodId);
//             response.setRecipientPaymentMethodId(testRecipientPaymentMethodId);
//             response.setAmount(amount);
//             response.setStatus(status);
//             response.setExternalReference(externalReference);
//             response.setRetryCount(retryCount);
//             response.setFailMessage(failMessage);
//             response.setCreatedAt(createdAt);
//             response.setCompletedAt(completedAt);
//             response.setStatusDescription(statusDescription);
//             response.setPayerName(payerName);
//             response.setRecipientName(recipientName);

//             // Then
//             assertEquals(testTransactionId, response.getTransactionId());
//             assertEquals(testCycleId, response.getCycleId());
//             assertEquals(testMandateId, response.getMandateId());
//             assertEquals(testPayerUserId, response.getPayerUserId());
//             assertEquals(testRecipientUserId, response.getRecipientUserId());
//             assertEquals(testGroupId, response.getGroupId());
//             assertEquals(testPayerPaymentMethodId, response.getPayerPaymentMethodId());
//             assertEquals(testRecipientPaymentMethodId, response.getRecipientPaymentMethodId());
//             assertEquals(amount, response.getAmount());
//             assertEquals(status, response.getStatus());
//             assertEquals(externalReference, response.getExternalReference());
//             assertEquals(retryCount, response.getRetryCount());
//             assertEquals(failMessage, response.getFailMessage());
//             assertEquals(createdAt, response.getCreatedAt());
//             assertEquals(completedAt, response.getCompletedAt());
//             assertEquals(statusDescription, response.getStatusDescription());
//             assertEquals(payerName, response.getPayerName());
//             assertEquals(recipientName, response.getRecipientName());
//         }

//         @Test
//         @DisplayName("Should generate correct status descriptions")
//         void fromTransaction_ShouldGenerateCorrectStatusDescriptions() {
//             // Test PENDING status
//             Transaction pendingTx = new Transaction();
//             pendingTx.setStatus("PENDING");
//             TransactionResponse pendingResponse = TransactionResponse.fromTransaction(pendingTx);
//             assertEquals("Transaction is pending", pendingResponse.getStatusDescription());

//             // Test PROCESSING status
//             Transaction processingTx = new Transaction();
//             processingTx.setStatus("PROCESSING");
//             TransactionResponse processingResponse = TransactionResponse.fromTransaction(processingTx);
//             assertEquals("Transaction is being processed", processingResponse.getStatusDescription());

//             // Test COMPLETED status
//             Transaction completedTx = new Transaction();
//             completedTx.setStatus("COMPLETED");
//             TransactionResponse completedResponse = TransactionResponse.fromTransaction(completedTx);
//             assertEquals("Transaction completed successfully", completedResponse.getStatusDescription());

//             // Test FAILED status
//             Transaction failedTx = new Transaction();
//             failedTx.setStatus("FAILED");
//             TransactionResponse failedResponse = TransactionResponse.fromTransaction(failedTx);
//             assertEquals("Transaction failed", failedResponse.getStatusDescription());

//             // Test unknown status
//             Transaction unknownTx = new Transaction();
//             unknownTx.setStatus("UNKNOWN_STATUS");
//             TransactionResponse unknownResponse = TransactionResponse.fromTransaction(unknownTx);
//             assertEquals("Unknown status", unknownResponse.getStatusDescription());
//         }

//         @Test
//         @DisplayName("Should handle null Transaction in fromTransaction method")
//         void fromTransaction_WithNullTransaction_ShouldHandleGracefully() {
//             // When & Then
//             assertThrows(NullPointerException.class, () -> TransactionResponse.fromTransaction(null));
//         }

//         @Test
//         @DisplayName("Should handle Transaction with null fields")
//         void fromTransaction_WithNullFields_ShouldHandleGracefully() {
//             // Given
//             Transaction transaction = new Transaction();
//             // All fields are null by default

//             // When
//             TransactionResponse response = TransactionResponse.fromTransaction(transaction);

//             // Then
//             assertNotNull(response);
//             assertNull(response.getTransactionId());
//             assertNull(response.getCycleId());
//             assertNull(response.getMandateId());
//             assertNull(response.getPayerUserId());
//             assertNull(response.getRecipientUserId());
//             assertNull(response.getGroupId());
//             assertNull(response.getPayerPaymentMethodId());
//             assertNull(response.getRecipientPaymentMethodId());
//             assertNull(response.getAmount());
//             assertNull(response.getStatus());
//             assertNull(response.getExternalReference());
//             assertEquals(0, response.getRetryCount()); // Assuming primitive int in entity
//             assertNull(response.getFailMessage());
//             assertNull(response.getCreatedAt());
//             assertNull(response.getCompletedAt());
//             assertEquals("Unknown status", response.getStatusDescription()); // Due to null status
//             assertNull(response.getPayerName());
//             assertNull(response.getRecipientName());
//         }

//         @Test
//         @DisplayName("Should handle BigDecimal precision correctly")
//         void amount_ShouldHandlePrecisionCorrectly() {
//             // Given
//             BigDecimal preciseAmount = new BigDecimal("123.456789123456");

//             // When
//             response.setAmount(preciseAmount);

//             // Then
//             assertEquals(preciseAmount, response.getAmount());
//         }
//     }
// }