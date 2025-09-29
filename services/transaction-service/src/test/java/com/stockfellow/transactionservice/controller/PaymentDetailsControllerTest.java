package com.stockfellow.transactionservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockfellow.transactionservice.dto.*;
import com.stockfellow.transactionservice.model.*;
import com.stockfellow.transactionservice.service.PaymentDetailsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

@WebMvcTest(PaymentDetailsController.class)
class PaymentDetailsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentDetailsService paymentDetailsService;

    private UUID userId;
    private UUID payerId;
    private InitializeCardAuthDto initializeDto;
    private CreatePayoutDetailsDto payoutDto;
    private PayerDetails samplePayerDetails;
    private PayoutDetails samplePayoutDetails;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        payerId = UUID.randomUUID();

        // Setup initialize card auth DTO
        initializeDto = new InitializeCardAuthDto();
        initializeDto.setEmail("test@example.com");

        // Setup payout details DTO
        payoutDto = new CreatePayoutDetailsDto();
        payoutDto.setAccountNumber("1234567890");
        payoutDto.setBankCode("057");
        payoutDto.setBankName("Test Account");

        // Setup sample payer details
        samplePayerDetails = new PayerDetails();
        samplePayerDetails.setPayerId(payerId);
        samplePayerDetails.setUserId(userId);

        // Setup sample payout details
        samplePayoutDetails = new PayoutDetails();
        samplePayoutDetails.setPayoutId(UUID.randomUUID());
        samplePayoutDetails.setUserId(userId);
        samplePayoutDetails.setAccountNumber("1234567890");
    }

    @Test
    void handlePaystackCallback_Success_RedirectsWithSuccess() throws Exception {
        String reference = "test_reference";
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);

        when(paymentDetailsService.processPaystackCallback(reference))
            .thenReturn(result);

        mockMvc.perform(get("/api/transaction/payment-methods/payer/callback")
                .param("reference", reference))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", 
                    "stockfellow://cards/callback?reference=" + reference + "&status=success"));
    }

    // @Test
    // void initializeCardAuthorization_ValidRequest_ReturnsCreated() throws Exception {
    //     Map<String, String> response = new HashMap<>();
    //     response.put("authorizationUrl", "https://test.url");
        
    //     when(paymentDetailsService.initializeCardAuth(any()))
    //         .thenReturn(response);

    //     mockMvc.perform(post("/api/transaction/payment-methods/payer/initialize")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .header("X-User-Id", userId.toString())
    //             .content(objectMapper.writeValueAsString(initializeDto)))
    //             .andExpect(status().isCreated())
    //             .andExpect(jsonPath("$.authorizationUrl").exists());
    // }

    @Test
    void getPayerDetailsByUser_ValidRequest_ReturnsOk() throws Exception {
        when(paymentDetailsService.findPayerDetailsByUserId(userId))
            .thenReturn(Arrays.asList(samplePayerDetails));

        mockMvc.perform(get("/api/transaction/payment-methods/payer/user")
                .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].payerId").exists());
    }

    @Test
    void deactivateCard_ValidRequest_ReturnsOk() throws Exception {
        when(paymentDetailsService.deactivateCard(payerId))
            .thenReturn(samplePayerDetails);

        mockMvc.perform(put("/api/transaction/payment-methods/payer/{payerId}/deactivate", payerId)
                .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payerId").exists());
    }

    // @Test
    // void addPayoutDetails_ValidRequest_ReturnsCreated() throws Exception {
    //     when(paymentDetailsService.addPayoutDetails(any()))
    //         .thenReturn(samplePayoutDetails);

    //     mockMvc.perform(post("/api/transaction/payment-methods/payout")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .header("X-User-Id", userId.toString())
    //             .content(objectMapper.writeValueAsString(payoutDto)))
    //             .andExpect(status().isCreated())
    //             .andExpect(jsonPath("$.payoutId").exists());
    // }

    @Test
    void getPayoutDetailsByUser_ValidRequest_ReturnsOk() throws Exception {
        when(paymentDetailsService.findPayoutDetailsByUserId(userId))
            .thenReturn(Arrays.asList(samplePayoutDetails));

        mockMvc.perform(get("/api/transaction/payment-methods/payout/user")
                .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].payoutId").exists());
    }

    @Test
    void allEndpoints_MissingHeader_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/transaction/payment-methods/payer/user"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/transaction/payment-methods/payout/user"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void allEndpoints_InvalidHeader_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/transaction/payment-methods/payer/user")
                .header("X-User-Id", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/transaction/payment-methods/payout/user")
                .header("X-User-Id", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }
}