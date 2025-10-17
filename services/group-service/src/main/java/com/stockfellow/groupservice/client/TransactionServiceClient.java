package com.stockfellow.groupservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
public class TransactionServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceClient.class);
    
    private final WebClient webClient;

    public TransactionServiceClient(
            @Value("${transaction.service.url:http://localhost:8082}") String transactionServiceUrl,
            WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(transactionServiceUrl)
                .build();
    }

    public void createRotation(CreateRotationRequest request) {
        try {
            webClient.post()
                    .uri("/api/rotations")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(RotationResponse.class)
                    .block(); // Blocking call
            
            logger.info("Successfully created rotation for group {}", request.getGroupId());
        } catch (Exception e) {
            logger.error("Failed to create rotation for group {}: {}", 
                        request.getGroupId(), e.getMessage());
        }
    }

    // DTOs for the transaction service
    public static class CreateRotationRequest {
        private String groupId;
        private BigDecimal amount;
        private UUID[] memberIds;
        private LocalDate collectionDate;
        private LocalDate payoutDate;
        private String frequency;

        // Constructor
        public CreateRotationRequest(String groupId, BigDecimal amount, UUID[] memberIds,
                                    LocalDate collectionDate, LocalDate payoutDate, String frequency) {
            this.groupId = groupId;
            this.amount = amount;
            this.memberIds = memberIds;
            this.collectionDate = collectionDate;
            this.payoutDate = payoutDate;
            this.frequency = frequency;
        }

        // Getters and setters
        public String getGroupId() { return groupId; }
        public void setGroupId(String groupId) { this.groupId = groupId; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public UUID[] getMemberIds() { return memberIds; }
        public void setMemberIds(UUID[] memberIds) { this.memberIds = memberIds; }
        
        public LocalDate getCollectionDate() { return collectionDate; }
        public void setCollectionDate(LocalDate collectionDate) { this.collectionDate = collectionDate; }
        
        public LocalDate getPayoutDate() { return payoutDate; }
        public void setPayoutDate(LocalDate payoutDate) { this.payoutDate = payoutDate; }

        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }

    }

    public static class RotationResponse {
        private UUID id;
        private String groupId;
        private String status;

        // Getters and setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        
        public String getGroupId() { return groupId; }
        public void setGroupId(String groupId) { this.groupId = groupId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}