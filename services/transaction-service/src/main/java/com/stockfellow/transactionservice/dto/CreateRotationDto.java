package com.stockfellow.transactionservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

public class CreateRotationDto {
    @NotNull(message = "Group ID is required")
    private String groupId;
    
    @NotNull(message = "Contribution amount is required")
    @DecimalMin(value = "0.01", message = "Contribution amount must be greater than 0")
    @Digits(integer = 17, fraction = 2, message = "Invalid contribution amount format")
    private BigDecimal amount;

    @NotNull(message = "Group member ids are required")
    private UUID[] memberIds;
    
    @NotNull(message = "Collection start date is required")
    @Future(message = "Collection start date must be in the future")
    private LocalDate collectionDate;
    
    @NotNull(message = "Payout date is required")
    @Future(message = "Payout date must be in the future")
    private LocalDate payoutDate;

    @NotNull(message = "Contribution Frequency cannot be null")
    private String frequency;
    

    // Constructors
    public CreateRotationDto() {}

    // Getters and Setters
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public LocalDate getCollectionDate() { return collectionDate; }
    public void setCollectionDate(LocalDate collectionDate) { this.collectionDate = collectionDate; }
    
    public LocalDate getPayoutDate() { return payoutDate; }
    public void setPayoutDate(LocalDate payoutDate) { this.payoutDate = payoutDate; }
    
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public UUID[] getMemberIds() { return memberIds; }
    public void setMemberIds(UUID[] memberIds) { this.memberIds = memberIds; }    
}
