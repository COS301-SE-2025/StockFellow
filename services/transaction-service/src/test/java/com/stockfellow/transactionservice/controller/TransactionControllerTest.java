// package com.stockfellow.transactionservice.controller;

// import com.stockfellow.transactionservice.model.Transaction;
// import com.stockfellow.transactionservice.repository.TransactionRepository;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.BeforeEach;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;

// import java.math.BigDecimal;
// import java.time.LocalDateTime;
// import java.util.Arrays;
// import java.util.List;
// import java.util.Optional;
// import java.util.UUID;

// import static org.mockito.Mockito.when;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest(TransactionController.class)
// class TransactionControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockBean
//     private TransactionRepository transactionRepository;

//     private Transaction testTransaction;
//     private UUID testTransactionId;
//     private UUID testCycleId;
//     private UUID testMandateId;
//     private UUID testPayerUserId;
//     private UUID testRecipientUserId;
//     private UUID testGroupId;
//     private UUID testPayerPaymentMethodId;
//     private UUID testRecipientPaymentMethodId;

//     @BeforeEach
//     void setUp() {
//         testTransactionId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-111111111111");
//         testCycleId = UUID.fromString("bbbbbbbb-cccc-dddd-eeee-222222222222");
//         testMandateId = UUID.fromString("cccccccc-dddd-eeee-ffff-333333333333");
//         testPayerUserId = UUID.fromString("dddddddd-eeee-ffff-aaaa-444444444444");
//         testRecipientUserId = UUID.fromString("eeeeeeee-ffff-aaaa-bbbb-555555555555");
//         testGroupId = UUID.fromString("ffffffff-aaaa-bbbb-cccc-666666666666");
//         testPayerPaymentMethodId = UUID.fromString("aaaabbbb-cccc-dddd-eeee-777777777777");
//         testRecipientPaymentMethodId = UUID.fromString("bbbbcccc-dddd-eeee-ffff-888888888888");

//         testTransaction = new Transaction();
//         testTransaction.setTransactionId(testTransactionId);
//         testTransaction.setCycleId(testCycleId);
//         testTransaction.setMandateId(testMandateId);
//         testTransaction.setPayerUserId(testPayerUserId);
//         testTransaction.setRecipientUserId(testRecipientUserId);
//         testTransaction.setGroupId(testGroupId);
//         testTransaction.setPayerPaymentMethodId(testPayerPaymentMethodId);
//         testTransaction.setRecipientPaymentMethodId(testRecipientPaymentMethodId);
//         testTransaction.setAmount(new BigDecimal("1000.00"));
//         testTransaction.setStatus("PENDING");
//         testTransaction.setExternalReference("EXT-REF-TEST-001");
//         testTransaction.setRetryCount(0);
//         testTransaction.setFailMessage(null);
//         testTransaction.setCreatedAt(LocalDateTime.of(2025, 6, 1, 10, 0, 0));
//         testTransaction.setCompletedAt(null);
//     }

//     @Test
//     void getAllTransactions_ShouldReturnListOfTransactions() throws Exception {
//         List<Transaction> transactions = Arrays.asList(testTransaction);
//         when(transactionRepository.findAll()).thenReturn(transactions);

//         mockMvc.perform(get("/api/transactions"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$").isArray())
//                 .andExpect(jsonPath("$.length()").value(1))
//                 .andExpect(jsonPath("$[0].transactionId").value(testTransactionId.toString()))
//                 .andExpect(jsonPath("$[0].cycleId").value(testCycleId.toString()))
//                 .andExpect(jsonPath("$[0].payerUserId").value(testPayerUserId.toString()))
//                 .andExpect(jsonPath("$[0].status").value("PENDING"))
//                 .andExpect(jsonPath("$[0].amount").value(1000.00))
//                 .andExpect(jsonPath("$[0].externalReference").value("EXT-REF-TEST-001"))
//                 .andExpect(jsonPath("$[0].retryCount").value(0));
//     }

//     @Test
//     void getAllTransactions_ShouldReturnEmptyList_WhenNoTransactions() throws Exception {
//         when(transactionRepository.findAll()).thenReturn(Arrays.asList());

//         mockMvc.perform(get("/api/transactions"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$").isArray())
//                 .andExpect(jsonPath("$.length()").value(0));
//     }

//     @Test
//     void getTransaction_ShouldReturnTransaction_WhenExists() throws Exception {
//         when(transactionRepository.findById(testTransactionId)).thenReturn(Optional.of(testTransaction));

//         mockMvc.perform(get("/api/transactions/{transactionId}", testTransactionId))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$.transactionId").value(testTransactionId.toString()))
//                 .andExpect(jsonPath("$.cycleId").value(testCycleId.toString()))
//                 .andExpect(jsonPath("$.mandateId").value(testMandateId.toString()))
//                 .andExpect(jsonPath("$.payerUserId").value(testPayerUserId.toString()))
//                 .andExpect(jsonPath("$.recipientUserId").value(testRecipientUserId.toString()))
//                 .andExpect(jsonPath("$.groupId").value(testGroupId.toString()))
//                 .andExpect(jsonPath("$.amount").value(1000.00))
//                 .andExpect(jsonPath("$.status").value("PENDING"))
//                 .andExpect(jsonPath("$.externalReference").value("EXT-REF-TEST-001"));
//     }

//     @Test
//     void getTransaction_ShouldReturn404_WhenNotExists() throws Exception {
//         UUID nonExistentId = UUID.randomUUID();
//         when(transactionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

//         mockMvc.perform(get("/api/transactions/{transactionId}", nonExistentId))
//                 .andExpect(status().isNotFound());
//     }

//     @Test
//     void getTransactionsByCycle_ShouldReturnTransactions_WhenCycleExists() throws Exception {
//         List<Transaction> transactions = Arrays.asList(testTransaction);
//         when(transactionRepository.findByCycleIdOrderByCreatedAtDesc(testCycleId)).thenReturn(transactions);

//         mockMvc.perform(get("/api/transactions/cycle/{cycleId}", testCycleId))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$").isArray())
//                 .andExpect(jsonPath("$.length()").value(1))
//                 .andExpect(jsonPath("$[0].cycleId").value(testCycleId.toString()))
//                 .andExpect(jsonPath("$[0].transactionId").value(testTransactionId.toString()));
//     }

//     @Test
//     void getTransactionsByCycle_ShouldReturnEmptyList_WhenCycleHasNoTransactions() throws Exception {
//         UUID emptyCycleId = UUID.randomUUID();
//         when(transactionRepository.findByCycleIdOrderByCreatedAtDesc(emptyCycleId)).thenReturn(Arrays.asList());

//         mockMvc.perform(get("/api/transactions/cycle/{cycleId}", emptyCycleId))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$").isArray())
//                 .andExpect(jsonPath("$.length()").value(0));
//     }

//     @Test
//     void getTransactionsByPayer_ShouldReturnTransactions_WhenPayerExists() throws Exception {
//         List<Transaction> transactions = Arrays.asList(testTransaction);
//         when(transactionRepository.findByPayerUserIdOrderByCreatedAtDesc(testPayerUserId)).thenReturn(transactions);

//         mockMvc.perform(get("/api/transactions/payer/{payerUserId}", testPayerUserId))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$").isArray())
//                 .andExpect(jsonPath("$.length()").value(1))
//                 .andExpect(jsonPath("$[0].payerUserId").value(testPayerUserId.toString()))
//                 .andExpect(jsonPath("$[0].transactionId").value(testTransactionId.toString()));
//     }

//     @Test
//     void getTransactionsByPayer_ShouldReturnEmptyList_WhenPayerHasNoTransactions() throws Exception {
//         UUID emptyPayerId = UUID.randomUUID();
//         when(transactionRepository.findByPayerUserIdOrderByCreatedAtDesc(emptyPayerId)).thenReturn(Arrays.asList());

//         mockMvc.perform(get("/api/transactions/payer/{payerUserId}", emptyPayerId))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$").isArray())
//                 .andExpect(jsonPath("$.length()").value(0));
//     }

//     @Test
//     void getTransactionsByStatus_ShouldReturnTransactions_WhenStatusExists() throws Exception {
//         List<Transaction> pendingTransactions = Arrays.asList(testTransaction);
//         when(transactionRepository.findByStatus("PENDING")).thenReturn(pendingTransactions);

//         mockMvc.perform(get("/api/transactions/status/{status}", "PENDING"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$").isArray())
//                 .andExpect(jsonPath("$.length()").value(1))
//                 .andExpect(jsonPath("$[0].status").value("PENDING"))
//                 .andExpect(jsonPath("$[0].transactionId").value(testTransactionId.toString()));
//     }

//     @Test
//     void getTransactionsByStatus_ShouldReturnEmptyList_WhenStatusHasNoTransactions() throws Exception {
//         when(transactionRepository.findByStatus("COMPLETED")).thenReturn(Arrays.asList());

//         mockMvc.perform(get("/api/transactions/status/{status}", "COMPLETED"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$").isArray())
//                 .andExpect(jsonPath("$.length()").value(0));
//     }

//     @Test
//     void getTransactionsByStatus_ShouldReturnTransactions_ForDifferentStatuses() throws Exception {
//         Transaction completedTransaction = new Transaction();
//         completedTransaction.setTransactionId(UUID.randomUUID());
//         completedTransaction.setStatus("COMPLETED");
//         completedTransaction.setAmount(new BigDecimal("500.00"));
//         completedTransaction.setCompletedAt(LocalDateTime.now());

//         List<Transaction> completedTransactions = Arrays.asList(completedTransaction);
//         when(transactionRepository.findByStatus("COMPLETED")).thenReturn(completedTransactions);

//         mockMvc.perform(get("/api/transactions/status/{status}", "COMPLETED"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$").isArray())
//                 .andExpect(jsonPath("$.length()").value(1))
//                 .andExpect(jsonPath("$[0].status").value("COMPLETED"));
//     }

//     @Test
//     void getTransaction_ShouldReturn400_WhenInvalidUUID() throws Exception {
//         mockMvc.perform(get("/api/transactions/{transactionId}", "invalid-uuid"))
//                 .andExpect(status().isBadRequest());
//     }

//     @Test
//     void getTransactionsByCycle_ShouldReturn400_WhenInvalidCycleUUID() throws Exception {
//         mockMvc.perform(get("/api/transactions/cycle/{cycleId}", "invalid-uuid"))
//                 .andExpect(status().isBadRequest());
//     }

//     @Test
//     void getTransactionsByPayer_ShouldReturn400_WhenInvalidPayerUUID() throws Exception {
//         mockMvc.perform(get("/api/transactions/payer/{payerUserId}", "invalid-uuid"))
//                 .andExpect(status().isBadRequest());
//     }
// }