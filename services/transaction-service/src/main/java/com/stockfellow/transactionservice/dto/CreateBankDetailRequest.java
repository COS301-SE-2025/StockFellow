package com.stockfellow.transactionservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Schema(description = "Request object for creating new bank details")
public class CreateBankDetailRequest {

    @NotNull(message = "User ID is required")
    @Schema(description = "User's unique identifier", 
            example = "123e4567-e89b-12d3-a456-426614174000",
            required = true)
    private UUID userId;

    @NotBlank(message = "Bank name is required")
    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    @Schema(description = "Name of the bank", 
            example = "Standard Bank",
            maxLength = 100,
            required = true)
    private String bank;

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^[0-9]{13,19}$", message = "Card number must be 13-19 digits")
    @Schema(description = "Credit/Debit card number (will be encrypted)", 
            example = "4111111111111111",
            pattern = "^[0-9]{13,19}$",
            required = true)
    private String cardNumber;

    @NotBlank(message = "Card holder name is required")
    @Size(max = 100, message = "Card holder name must not exceed 100 characters")
    @Schema(description = "Name on the card", 
            example = "John Doe",
            maxLength = 100,
            required = true)
    private String cardHolder;

    @NotNull(message = "Expiry month is required")
    @Min(value = 1, message = "Expiry month must be between 1 and 12")
    @Max(value = 12, message = "Expiry month must be between 1 and 12")
    @Schema(description = "Card expiry month (1-12)", 
            example = "12",
            minimum = "1",
            maximum = "12",
            required = true)
    private Integer expiryMonth;

    @NotNull(message = "Expiry year is required")
    @Min(value = 2024, message = "Expiry year must be current year or later")
    @Schema(description = "Card expiry year (YYYY)", 
            example = "2025",
            minimum = "2024",
            required = true)
    private Integer expiryYear;

    @NotBlank(message = "Card type is required")
    @Pattern(regexp = "^(VISA|MASTERCARD|AMERICAN_EXPRESS|DINERS_CLUB|DISCOVER)$", 
             message = "Card type must be one of: VISA, MASTERCARD, AMERICAN_EXPRESS, DINERS_CLUB, DISCOVER")
    @Schema(description = "Type of card", 
            example = "VISA",
            allowableValues = {"VISA", "MASTERCARD", "AMERICAN_EXPRESS", "DINERS_CLUB", "DISCOVER"},
            required = true)
    private String cardType;

    @Schema(description = "Whether to set this as the active/primary payment method", 
            example = "false",
            defaultValue = "false")
    private Boolean setAsActive = false;

    // Default constructor
    public CreateBankDetailRequest() {}

    // Constructor with all fields
    public CreateBankDetailRequest(UUID userId, String bank, String cardNumber, String cardHolder,
                                   Integer expiryMonth, Integer expiryYear, String cardType, Boolean setAsActive) {
        this.userId = userId;
        this.bank = bank;
        this.cardNumber = cardNumber;
        this.cardHolder = cardHolder;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.cardType = cardType;
        this.setAsActive = setAsActive;
    }

    // Getters and Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getBank() { return bank; }
    public void setBank(String bank) { this.bank = bank; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getCardHolder() { return cardHolder; }
    public void setCardHolder(String cardHolder) { this.cardHolder = cardHolder; }

    public Integer getExpiryMonth() { return expiryMonth; }
    public void setExpiryMonth(Integer expiryMonth) { this.expiryMonth = expiryMonth; }

    public Integer getExpiryYear() { return expiryYear; }
    public void setExpiryYear(Integer expiryYear) { this.expiryYear = expiryYear; }

    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }

    public Boolean getSetAsActive() { return setAsActive; }
    public void setSetAsActive(Boolean setAsActive) { this.setAsActive = setAsActive; }

    @Override
    public String toString() {
        return "CreateBankDetailRequest{" +
                "userId=" + userId +
                ", bank='" + bank + '\'' +
                ", cardNumber='***'" + // Mask card number in logs
                ", cardHolder='" + cardHolder + '\'' +
                ", expiryMonth=" + expiryMonth +
                ", expiryYear=" + expiryYear +
                ", cardType='" + cardType + '\'' +
                ", setAsActive=" + setAsActive +
                '}';
    }
}