package com.stockfellow.transactionservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payer_details")
public class PayerDetails {
    
    @Id
    @Column(name = "payer_id")
    private UUID payerId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "type", nullable = false)
    private String type;
    
    @Column(name = "auth_code")
    private String authCode;
    
    @Column(name = "card_type")
    private String cardType;
    
    @Column(name = "last4")
    private String last4;
    
    @Column(name = "exp_month")
    private String expMonth;
    
    @Column(name = "exp_year")
    private String expYear;
    
    @Column(name = "bin")
    private String bin;
    
    @Column(name = "bank")
    private String bank;
    
    @Column(name = "email", nullable = false)
    private String email;
    
    @Column(name = "is_authenticated", nullable = false)
    private Boolean isAuthenticated = false;
    
    @Column(name = "signature")
    private String signature;

    @Column(name = "paystack_reference")
    private String paystackReference;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public PayerDetails() {}

    public PayerDetails(UUID userId, String type, String email) {
        this.userId = userId;
        this.type = type;
        this.email = email;
    }

    // Getters
    public UUID getPayerId() { return payerId; }
    public UUID getUserId() { return userId; }
    public String getType() { return type; }
    public String getAuthCode() { return authCode; }
    public String getCardType() { return cardType; }
    public String getLast4() { return last4; }
    public String getExpMonth() { return expMonth; }
    public String getExpYear() { return expYear; }
    public String getBin() { return bin; }
    public String getBank() { return bank; }
    public String getEmail() { return email; }
    public Boolean getIsAuthenticated() { return isAuthenticated; }
    public String getSignature() { return signature; }
    public Boolean getIsActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getPaystackReference() { return paystackReference; }

    // Setters
    public void setPayerId(UUID payerId) { this.payerId = payerId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setType(String type) { this.type = type; }
    // public void setType(String type) {
    //     try {
    //         this.type = PaymentMethodType.valueOf(type.toUpperCase());
    //     } catch (IllegalArgumentException e) {
    //         throw new IllegalArgumentException("Invalid payment method type: " + type);
    //     }
    // }
    public void setAuthCode(String authCode) { this.authCode = authCode; }
    public void setCardType(String cardType) { this.cardType = cardType; }
    public void setLast4(String last4) { this.last4 = last4; }
    public void setExpMonth(String expMonth) { this.expMonth = expMonth; }
    public void setExpYear(String expYear) { this.expYear = expYear; }
    public void setBin(String bin) { this.bin = bin; }
    public void setBank(String bank) { this.bank = bank; }
    public void setEmail(String email) { this.email = email; }
    public void setIsAuthenticated(Boolean isAuthenticated) { this.isAuthenticated = isAuthenticated; }
    public void setSignature(String signature) { this.signature = signature; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setPaystackReference(String paystackReference) { this.paystackReference = paystackReference; }


    @PrePersist
    protected void onCreate() {
        if (payerId == null) {
            payerId = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isAuthenticated == null) {
            isAuthenticated = false;
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum PaymentMethodType {
        card,
        bank_account,
        mobile_money
    }
}