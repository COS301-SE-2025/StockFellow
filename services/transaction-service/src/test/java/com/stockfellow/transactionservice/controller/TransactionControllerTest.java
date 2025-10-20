package com.stockfellow.transactionservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockfellow.transactionservice.dto.CreateTransactionDto;
import com.stockfellow.transactionservice.model.Transaction;
import com.stockfellow.transactionservice.model.Transaction.TransactionStatus;
import com.stockfellow.transactionservice.service.TransactionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    private UUID userId;
    private UUID cycleId;
    private UUID transactionId;
    private Transaction sampleTransaction;
    private CreateTransactionDto createDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        cycleId = UUID.randomUUID();
        transactionId = UUID.randomUUID();

        createDto = new CreateTransactionDto();
        createDto.setUserId(userId);
        createDto.setCycleId(cycleId);
        createDto.setAmount(new BigDecimal("100.00"));

        sampleTransaction = new Transaction();
        sampleTransaction.setTransactionId(transactionId);
        sampleTransaction.setUserId(userId);
        sampleTransaction.setCycleId(cycleId);
        sampleTransaction.setAmount(new BigDecimal("100.00"));
        sampleTransaction.setStatus(TransactionStatus.PENDING);
    }

    // @Test
    // void createTransaction_ValidRequest_ReturnsCreated() throws Exception {
    //     when(transactionService.createTransaction(any(CreateTransactionDto.class)))
    //         .thenReturn(sampleTransaction);

    //     mockMvc.perform(post("/api/transactions")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(createDto)))
    //             .andExpect(status().isCreated())
    //             .andExpect(jsonPath("$.transactionId").exists())
    //             .andExpect(jsonPath("$.userId").value(userId.toString()));
    // }

    // @Test
    // void chargeTransaction_ValidRequest_ReturnsCreated() throws Exception {
    //     when(transactionService.chargeStoredCard(any(CreateTransactionDto.class)))
    //         .thenReturn(sampleTransaction);

    //     mockMvc.perform(post("/api/transactions/charge-card")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(createDto)))
    //             .andExpect(status().isCreated())
    //             .andExpect(jsonPath("$.transactionId").exists());
    // }

    @Test
    void getTransaction_ExistingTransaction_ReturnsOk() throws Exception {
        when(transactionService.findById(transactionId))
            .thenReturn(sampleTransaction);

        mockMvc.perform(get("/api/transactions/{transactionId}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(transactionId.toString()));
    }

    @Test
    void getTransactionsByCycle_ReturnsOk() throws Exception {
        Page<Transaction> page = new PageImpl<>(Arrays.asList(sampleTransaction));
        when(transactionService.findByCycleId(any(UUID.class), any(Pageable.class)))
            .thenReturn(page);

        mockMvc.perform(get("/api/transactions/cycle/{cycleId}", cycleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].cycleId").value(cycleId.toString()));
    }

    @Test
    void getTransactionsByUser_WithValidHeader_ReturnsOk() throws Exception {
        Page<Transaction> page = new PageImpl<>(Arrays.asList(sampleTransaction));
        when(transactionService.findByUserId(any(UUID.class), any(Pageable.class)))
            .thenReturn(page);

        mockMvc.perform(get("/api/transactions/user")
                .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userId").value(userId.toString()));
    }

    @Test
    void getTransactionsByUser_WithMissingHeader_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/transactions/user"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTransactionsByUser_WithInvalidHeader_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/transactions/user")
                .header("X-User-Id", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void retryTransaction_ExistingTransaction_ReturnsOk() throws Exception {
        when(transactionService.retryTransaction(transactionId))
            .thenReturn(sampleTransaction);

        mockMvc.perform(post("/api/transactions/{transactionId}/retry", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(transactionId.toString()));
    }
}