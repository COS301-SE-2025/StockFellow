package com.stockfellow.transactionservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class CreateGroupCycleDto {
    @NotNull(message = "Group ID is required")
    private String groupId;

    @NotNull(message = "Rotation ID is required")
    private UUID rotationId;
    
    @NotBlank(message = "Cycle period is required")
    @Size(max = 50, message = "Cycle period must not exceed 50 characters")
    private String cyclePeriod;
    
    @NotNull(message = "Recipient user ID is required")
    private UUID recipientUserId;
    
    @NotNull(message = "Contribution amount is required")
    @DecimalMin(value = "0.01", message = "Contribution amount must be greater than 0")
    @Digits(integer = 17, fraction = 2, message = "Invalid contribution amount format")
    private BigDecimal contributionAmount;
    
    @NotNull(message = "Expected total is required")
    @DecimalMin(value = "0.01", message = "Expected total must be greater than 0")
    @Digits(integer = 17, fraction = 2, message = "Invalid expected total format")
    private BigDecimal expectedTotal;
    
    @NotNull(message = "Collection start date is required")
    @Future(message = "Collection start date must be in the future")
    private LocalDate collectionStartDate;
    
    @NotNull(message = "Collection end date is required")
    @Future(message = "Collection end date must be in the future")
    private LocalDate collectionEndDate;
    
    private LocalDate payoutDate;

    // Constructors
    public CreateGroupCycleDto() {}

    // Getters and Setters
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public UUID getRotationId() { return rotationId; }
    public void setRotationId(UUID rotationId) { this.rotationId = rotationId; }
    
    public String getCyclePeriod() { return cyclePeriod; }
    public void setCyclePeriod(String cyclePeriod) { this.cyclePeriod = cyclePeriod; }
    
    public UUID getRecipientUserId() { return recipientUserId; }
    public void setRecipientUserId(UUID recipientUserId) { this.recipientUserId = recipientUserId; }
    
    public BigDecimal getContributionAmount() { return contributionAmount; }
    public void setContributionAmount(BigDecimal contributionAmount) { this.contributionAmount = contributionAmount; }
    
    public BigDecimal getExpectedTotal() { return expectedTotal; }
    public void setExpectedTotal(BigDecimal expectedTotal) { this.expectedTotal = expectedTotal; }
    
    public LocalDate getCollectionStartDate() { return collectionStartDate; }
    public void setCollectionStartDate(LocalDate collectionStartDate) {
        this.collectionStartDate = collectionStartDate; 
    }
    
    public LocalDate getCollectionEndDate() { return collectionEndDate; }
    public void setCollectionEndDate(LocalDate collectionEndDate) { this.collectionEndDate = collectionEndDate; }
    
    public LocalDate getPayoutDate() { return payoutDate; }
    public void setPayoutDate(LocalDate payoutDate) { this.payoutDate = payoutDate; }
}