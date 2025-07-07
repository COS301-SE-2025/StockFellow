package com.stockfellow.transactionservice.dto;

import com.stockfellow.transactionservice.model.BankDetails;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response object containing bank details information")
public class BankDetailResponse {

    @Schema(description = "Unique identifier for the bank details", 
            example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "User's unique identifier", 
            example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;

    @Schema(description = "Name of the bank", 
            example = "Standard Bank")
    private String bank;

    @Schema(description = "Last 4 digits of the card number for display purposes", 
            example = "1111")
    private String last4Digits;

    @Schema(description = "Name on the card", 
            example = "John Doe")
    private String cardHolder;

    @Schema(description = "Card expiry month (1-12)", 
            example = "12")
    private Integer expiryMonth;

    @Schema(description = "Card expiry year (YYYY)", 
            example = "2025")
    private Integer expiryYear;

    @Schema(description = "Type of card", 
            example = "VISA")
    private String cardType;

    @Schema(description = "Whether this is the active/primary payment method", 
            example = "true")
    private Boolean isActive;

    @Schema(description = "When the bank details were created", 
            example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "When the bank details were last updated", 
            example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Whether the card is expired based on current date", 
            example = "false")
    private Boolean isExpired;

    @Schema(description = "Formatted expiry date for display", 
            example = "12/25")
    private String expiryDisplay;

    // Default constructor
    public BankDetailResponse() {}

    // Constructor with all fields
    public BankDetailResponse(UUID id, UUID userId, String bank, String last4Digits, 
                              String cardHolder, Integer expiryMonth, Integer expiryYear, 
                              String cardType, Boolean isActive, LocalDateTime createdAt, 
                              LocalDateTime updatedAt, Boolean isExpired, String expiryDisplay) {
        this.id = id;
        this.userId = userId;
        this.bank = bank;
        this.last4Digits = last4Digits;
        this.cardHolder = cardHolder;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.cardType = cardType;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isExpired = isExpired;
        this.expiryDisplay = expiryDisplay;
    }

    // Static factory method to create from BankDetails entity
    public static BankDetailResponse from(BankDetails bankDetails) {
        if (bankDetails == null) {
            return null;
        }

        // Extract last 4 digits from card number
        String last4 = extractLast4Digits(bankDetails.getCardNumber());
        
        // Format expiry date for display
        String expiryDisplay = String.format("%02d/%02d", 
                bankDetails.getExpiryMonth(), 
                bankDetails.getExpiryYear() % 100);
        
        // Check if card is expired
        boolean isExpired = isCardExpired(bankDetails.getExpiryMonth(), bankDetails.getExpiryYear());

        return new BankDetailResponse(
                bankDetails.getId(),
                bankDetails.getUserId(),
                bankDetails.getBank(),
                last4,
                bankDetails.getCardHolder(),
                bankDetails.getExpiryMonth(),
                bankDetails.getExpiryYear(),
                bankDetails.getCardType(),
                bankDetails.getIsActive(),
                bankDetails.getCreatedAt(),
                bankDetails.getUpdatedAt(),
                isExpired,
                expiryDisplay
        );
    }

    // Helper method to extract last 4 digits safely
    private static String extractLast4Digits(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return cardNumber.substring(cardNumber.length() - 4);
    }

    // Helper method to check if card is expired
    private static boolean isCardExpired(Integer expiryMonth, Integer expiryYear) {
        if (expiryMonth == null || expiryYear == null) {
            return true;
        }
        
        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        
        return expiryYear < currentYear || 
               (expiryYear.equals(currentYear) && expiryMonth < currentMonth);
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getBank() { return bank; }
    public void setBank(String bank) { this.bank = bank; }

    public String getLast4Digits() { return last4Digits; }
    public void setLast4Digits(String last4Digits) { this.last4Digits = last4Digits; }

    public String getCardHolder() { return cardHolder; }
    public void setCardHolder(String cardHolder) { this.cardHolder = cardHolder; }

    public Integer getExpiryMonth() { return expiryMonth; }
    public void setExpiryMonth(Integer expiryMonth) { this.expiryMonth = expiryMonth; }

    public Integer getExpiryYear() { return expiryYear; }
    public void setExpiryYear(Integer expiryYear) { this.expiryYear = expiryYear; }

    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getIsExpired() { return isExpired; }
    public void setIsExpired(Boolean isExpired) { this.isExpired = isExpired; }

    public String getExpiryDisplay() { return expiryDisplay; }
    public void setExpiryDisplay(String expiryDisplay) { this.expiryDisplay = expiryDisplay; }

    @Override
    public String toString() {
        return "BankDetailResponse{" +
                "id=" + id +
                ", userId=" + userId +
                ", bank='" + bank + '\'' +
                ", last4Digits='" + last4Digits + '\'' +
                ", cardHolder='" + cardHolder + '\'' +
                ", expiryMonth=" + expiryMonth +
                ", expiryYear=" + expiryYear +
                ", cardType='" + cardType + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", isExpired=" + isExpired +
                ", expiryDisplay='" + expiryDisplay + '\'' +
                '}';
    }
}