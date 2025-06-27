package com.stockfellow.transactionservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

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

    // Constructors
    public CreateCycleRequest() {}

    public CreateCycleRequest(UUID groupId, String cycleMonth, UUID recipientUserId, 
                             UUID recipientPaymentMethodId, BigDecimal contributionAmount, 
                             LocalDate collectionDate, BigDecimal totalExpectedAmount) {
        this.groupId = groupId;
        this.cycleMonth = cycleMonth;
        this.recipientUserId = recipientUserId;
        this.recipientPaymentMethodId = recipientPaymentMethodId;
        this.contributionAmount = contributionAmount;
        this.collectionDate = collectionDate;
        this.totalExpectedAmount = totalExpectedAmount;
    }

    // Getters
    public UUID getGroupId() { return groupId; }
    public String getCycleMonth() { return cycleMonth; }
    public UUID getRecipientUserId() { return recipientUserId; }
    public UUID getRecipientPaymentMethodId() { return recipientPaymentMethodId; }
    public BigDecimal getContributionAmount() { return contributionAmount; }
    public LocalDate getCollectionDate() { return collectionDate; }
    public BigDecimal getTotalExpectedAmount() { return totalExpectedAmount; }

    // Setters
    public void setGroupId(UUID groupId) { this.groupId = groupId; }
    public void setCycleMonth(String cycleMonth) { this.cycleMonth = cycleMonth; }
    public void setRecipientUserId(UUID recipientUserId) { this.recipientUserId = recipientUserId; }
    public void setRecipientPaymentMethodId(UUID recipientPaymentMethodId) { this.recipientPaymentMethodId = recipientPaymentMethodId; }
    public void setContributionAmount(BigDecimal contributionAmount) { this.contributionAmount = contributionAmount; }
    public void setCollectionDate(LocalDate collectionDate) { this.collectionDate = collectionDate; }
    public void setTotalExpectedAmount(BigDecimal totalExpectedAmount) { this.totalExpectedAmount = totalExpectedAmount; }
}