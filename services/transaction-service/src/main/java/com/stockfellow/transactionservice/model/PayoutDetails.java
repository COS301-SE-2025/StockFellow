package com.stockfellow.transactionservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payout_details")
public class PayoutDetails {
    
    @Id
    @Column(name = "payout_id")
    private UUID payoutId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "type", nullable = false)
    private String type;
    
    @Column(name = "recp_name", nullable = false)
    private String recipientName;
    
    @Column(name = "acc_number")
    private String accountNumber;
    
    @Column(name = "bank_code")
    private String bankCode;
    
    @Column(name = "bank_name")
    private String bankName;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "provider")
    private String provider;
    
    @Column(name = "recipient_code")
    private String recipientCode;
    
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
    
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public PayoutDetails() {}

    public PayoutDetails(UUID userId, String type, String recipientName) {
        this.userId = userId;
        this.type = type;
        this.recipientName = recipientName;
    }

    // Getters
    public UUID getPayoutId() { return payoutId; }
    public UUID getUserId() { return userId; }
    public String getType() { return type; }
    public String getRecipientName() { return recipientName; }
    public String getAccountNumber() { return accountNumber; }
    public String getBankCode() { return bankCode; }
    public String getBankName() { return bankName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getProvider() { return provider; }
    public String getRecipientCode() { return recipientCode; }
    public Boolean getIsDefault() { return isDefault; }
    public Boolean getIsVerified() { return isVerified; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setPayoutId(UUID payoutId) { this.payoutId = payoutId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setType(String type) { this.type = type; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setProvider(String provider) { this.provider = provider; }
    public void setRecipientCode(String recipientCode) { this.recipientCode = recipientCode; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    protected void onCreate() {
        if (payoutId == null) {
            payoutId = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isDefault == null) {
            isDefault = false;
        }
        if (isVerified == null) {
            isVerified = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Payout Type Enum
    // public enum PayoutType {
    //     BANK_ACCOUNT,
    //     MOBILE_MONEY,
    //     DIGITAL_WALLET,
    //     CRYPTO_WALLET
    // }
}