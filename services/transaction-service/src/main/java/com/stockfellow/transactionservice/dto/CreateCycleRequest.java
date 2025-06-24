package com.stockfellow.transactionservice.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateCycleRequest {

    @NotNull(message = "Group ID is required")
    private UUID groupId;

    @NotBlank(message = "Cycle month is required")
    private String cycleMonth;

    @NotNull(message = "Recipient user ID is required")
    private UUID recipientUserId;

    @NotNull(message = "Recipient payment method ID is required")
    private UUID recipientPaymentMethodId;

    @NotNull(message = "Contribution amount is required")
    @DecimalMin(value = "0.01", message = "Contribution amount must be greater than 0")
    private BigDecimal contributionAmount;

    @NotNull(message = "Collection date is required")
    private LocalDate collectionDate;

    @NotNull(message = "Total expected amount is required")
    @DecimalMin(value = "0.01", message = "Total expected amount must be greater than 0")
    private BigDecimal totalExpectedAmount;
}