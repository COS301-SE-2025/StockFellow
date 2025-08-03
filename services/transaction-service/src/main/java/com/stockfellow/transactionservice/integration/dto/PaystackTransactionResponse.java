package com.stockfellow.transactionservice.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for Paystack transaction initialization
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaystackTransactionResponse {
    
    @JsonProperty("status")
    private Boolean status;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("data")
    private PaystackTransactionData data;

    // Constructors
    public PaystackTransactionResponse() {}

    // Getters and Setters
    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public PaystackTransactionData getData() { return data; }
    public void setData(PaystackTransactionData data) { this.data = data; }

    // Inner class for transaction data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaystackTransactionData {
        
        @JsonProperty("authorization_url")
        private String authorizationUrl;
        
        @JsonProperty("access_code")
        private String accessCode;
        
        @JsonProperty("reference")
        private String reference;

        // Getters and Setters
        public String getAuthorizationUrl() { return authorizationUrl; }
        public void setAuthorizationUrl(String authorizationUrl) { this.authorizationUrl = authorizationUrl; }
        
        public String getAccessCode() { return accessCode; }
        public void setAccessCode(String accessCode) { this.accessCode = accessCode; }
        
        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }
    }
}