package com.stockfellow.transactionservice.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {

    private UUID transactionId;
    private UUID cycleId;
    private UUID mandateId;
    private UUID payerUserId;
    private UUID recipientUserId;
    private UUID groupId;
    private UUID payerPaymentMethodId;
    private UUID recipientPaymentMethodId;
    private BigDecimal amount;
    private String status;
    private String externalReference;
    private Integer retryCount;
    private String failMessage;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    // user-friendly fields
    private String statusDescription;
    private String payerName;
    private String recipientName;

    public static TransactionResponse fromTransaction(
            com.stockfellow.transactionservice.model.Transaction transaction) {
        return TransactionResponse.builder()
                .transactionId(transaction.getTransactionId())
                .cycleId(transaction.getCycleId())
                .mandateId(transaction.getMandateId())
                .payerUserId(transaction.getPayerUserId())
                .recipientUserId(transaction.getRecipientUserId())
                .groupId(transaction.getGroupId())
                .payerPaymentMethodId(transaction.getPayerPaymentMethodId())
                .recipientPaymentMethodId(transaction.getRecipientPaymentMethodId())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .externalReference(transaction.getExternalReference())
                .retryCount(transaction.getRetryCount())
                .failMessage(transaction.getFailMessage())
                .createdAt(transaction.getCreatedAt())
                .completedAt(transaction.getCompletedAt())
                .statusDescription(getStatusDescription(transaction.getStatus()))
                .build();
    }

    private static String getStatusDescription(String status) {
        return switch (status) {
            case "PENDING" -> "Transaction is pending";
            case "PROCESSING" -> "Transaction is being processed";
            case "COMPLETED" -> "Transaction completed successfully";
            case "FAILED" -> "Transaction failed";
            default -> "Unknown status";
        };
    }
}
