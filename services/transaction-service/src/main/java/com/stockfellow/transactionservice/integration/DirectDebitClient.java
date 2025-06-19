package com.stockfellow.transactionservice.integration;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class DirectDebitClient {
    private static final Logger logger = LoggerFactory.getLogger(DirectDebitClient.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${directdebit.api.base-url}")
    private String baseUrl;
    @Value("${directdebit.api.client-id}")
    private String clientId;
    @Value("${directdebit.api.client-secret}")
    private String clientSecret;
    @Value("${directdebit.api.mandate-endpoint}")
    private String mandateEndpoint;
    @Value("${directdebit.api.debit-order-endpoint}")
    private String debitOrderEndpoint;
    @Value("${directdebit.api.payout-endpoint}")
    private String payoutEndpoint;

    private String getAccessToken() {
        // Mock OAuth2 token retrieval (replace with actual implementation)
        return "mock_access_token";
    }

    public String createMandate(String userId, String bankAccount) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        MandateRequest request = new MandateRequest();
        request.setUserId(userId);
        request.setBankAccount(bankAccount);
        request.setMandateId(UUID.randomUUID().toString());

        HttpEntity<MandateRequest> entity = new HttpEntity<>(request, headers);
        try {
            ResponseEntity<MandateResponse> response = restTemplate.exchange(
                baseUrl + mandateEndpoint, HttpMethod.POST, entity, MandateResponse.class);
            logger.info("Mandate created: {}", response.getBody().getMandateId());
            return response.getBody().getMandateId();
        } catch (Exception e) {
            logger.error("Failed to create mandate: {}", e.getMessage());
            throw new RuntimeException("Mandate creation failed", e);
        }
    }

    public String processDebitOrder(String mandateId, Double amount, String groupId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        DebitOrderRequest request = new DebitOrderRequest();
        request.setMandateId(mandateId);
        request.setAmount(amount);
        request.setTransactionId(UUID.randomUUID().toString());
        request.setReference("GROUP_" + groupId);

        HttpEntity<DebitOrderRequest> entity = new HttpEntity<>(request, headers);
        try {
            ResponseEntity<DebitOrderResponse> response = restTemplate.exchange(
                baseUrl + debitOrderEndpoint, HttpMethod.POST, entity, DebitOrderResponse.class);
            logger.info("Debit order processed: {}", response.getBody().getTransactionId());
            return response.getBody().getTransactionId();
        } catch (Exception e) {
            logger.error("Failed to process debit order: {}", e.getMessage());
            throw new RuntimeException("Debit order processing failed", e);
        }
    }

    public String processPayout(String userId, String bankAccount, Double amount, String groupId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        PayoutRequest request = new PayoutRequest();
        request.setUserId(userId);
        request.setBankAccount(bankAccount);
        request.setAmount(amount);
        request.setTransactionId(UUID.randomUUID().toString());
        request.setReference("PAYOUT_" + groupId);

        HttpEntity<PayoutRequest> entity = new HttpEntity<>(request, headers);
        try {
            ResponseEntity<PayoutResponse> response = restTemplate.exchange(
                baseUrl + payoutEndpoint, HttpMethod.POST, entity, PayoutResponse.class);
            logger.info("Payout processed: {}", response.getBody().getTransactionId());
            return response.getBody().getTransactionId();
        } catch (Exception e) {
            logger.error("Failed to process payout: {}", e.getMessage());
            throw new RuntimeException("Payout processing failed", e);
        }
    }

    @Data
    private static class MandateRequest {
        private String mandateId;
        private String userId;
        private String bankAccount;
    }

    @Data
    private static class MandateResponse {
        private String mandateId;
        private String status;
    }

    @Data
    private static class DebitOrderRequest {
        private String mandateId;
        private Double amount;
        private String transactionId;
        private String reference;
    }

    @Data
    private static class DebitOrderResponse {
        private String transactionId;
        private String status;
    }

    @Data
    private static class PayoutRequest {
        private String userId;
        private String bankAccount;
        private Double amount;
        private String transactionId;
        private String reference;
    }

    @Data
    private static class PayoutResponse {
        private String transactionId;
        private String status;
    }
}