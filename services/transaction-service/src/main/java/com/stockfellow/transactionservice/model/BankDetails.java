package com.stockfellow.transactionservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "banking_details")
public class BankDetails {
    
    @Id
    @Column(name = "id")
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "bank", nullable = false)
    private String bank;
    
    @Column(name = "card_number", nullable = false)
    private String cardNumber;
    
    @Column(name = "card_holder_name", nullable = false)
    private String cardHolder;
    
    @Column(name = "expiry_month", nullable = false)
    private Integer expiryMonth;
    
    @Column(name = "expiry_year", nullable = false)
    private Integer expiryYear;
    
    @Column(name = "card_type", nullable = false)
    private String cardType;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Getters
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getBank() { return bank; }
    public String getCardNumber() { return cardNumber; }
    public String getCardHolder() { return cardHolder; }
    public Integer getExpiryMonth() { return expiryMonth; }
    public Integer getExpiryYear() { return expiryYear; }
    public String getCardType() { return cardType; }
    public Boolean getIsActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(UUID id) { this.id = id; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setBank(String bank) { this.bank = bank; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public void setCardHolder(String cardHolder) { this.cardHolder = cardHolder; }
    public void setExpiryMonth(Integer expiryMonth) { this.expiryMonth = expiryMonth; }
    public void setExpiryYear(Integer expiryYear) { this.expiryYear = expiryYear; }
    public void setCardType(String cardType) { this.cardType = cardType; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}