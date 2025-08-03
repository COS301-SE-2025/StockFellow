package com.stockfellow.transactionservice.dto;

import com.stockfellow.transactionservice.model.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class PayoutDetailsResponseDto {
    
    private UUID payoutId;
    private UUID userId;
    private PayoutDetails.PayoutType type;
    private String recipientName;
    private String accountNumber;
    private String bankCode;
    private String bankName;
    private String phoneNumber;
    private String provider;
    private String recipientCode;
    private Boolean isDefault;
    private Boolean isVerified;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // Static factory method
    public static PayoutDetailsResponseDto fromEntity(PayoutDetails payoutDetails) {
        PayoutDetailsResponseDto dto = new PayoutDetailsResponseDto();
        dto.payoutId = payoutDetails.getPayoutId();
        dto.userId = payoutDetails.getUserId();
        dto.type = payoutDetails.getType();
        dto.recipientName = payoutDetails.getRecipientName();
        dto.accountNumber = payoutDetails.getAccountNumber();
        dto.bankCode = payoutDetails.getBankCode();
        dto.bankName = payoutDetails.getBankName();
        dto.phoneNumber = payoutDetails.getPhoneNumber();
        dto.provider = payoutDetails.getProvider();
        dto.recipientCode = payoutDetails.getRecipientCode();
        dto.isDefault = payoutDetails.getIsDefault();
        dto.isVerified = payoutDetails.getIsVerified();
        dto.createdAt = payoutDetails.getCreatedAt();
        dto.updatedAt = payoutDetails.getUpdatedAt();
        return dto;
    }

    // Getters and Setters
    public UUID getPayoutId() { return payoutId; }
    public void setPayoutId(UUID payoutId) { this.payoutId = payoutId; }
    
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public PayoutDetails.PayoutType getType() { return type; }
    public void setType(PayoutDetails.PayoutType type) { this.type = type; }
    
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    
    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }
    
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    
    public String getRecipientCode() { return recipientCode; }
    public void setRecipientCode(String recipientCode) { this.recipientCode = recipientCode; }
    
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
    
    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}