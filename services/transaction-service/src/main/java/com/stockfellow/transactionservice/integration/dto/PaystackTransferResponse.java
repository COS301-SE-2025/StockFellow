package com.stockfellow.transactionservice.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response DTO for Paystack transfer initiation
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaystackTransferResponse {
    
    @JsonProperty("status")
    private Boolean status;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("data")
    private PaystackTransferData data;

    // Constructors
    public PaystackTransferResponse() {}

    // Getters and Setters
    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public PaystackTransferData getData() { return data; }
    public void setData(PaystackTransferData data) { this.data = data; }

    // Inner class for transfer data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaystackTransferData {
        
        @JsonProperty("integration")
        private Long integration;
        
        @JsonProperty("domain")
        private String domain;
        
        @JsonProperty("amount")
        private Integer amount;
        
        @JsonProperty("currency")
        private String currency;
        
        @JsonProperty("source")
        private String source;
        
        @JsonProperty("reason")
        private String reason;
        
        @JsonProperty("recipient")
        private Long recipient;
        
        @JsonProperty("status")
        private String status; // "pending", "success", "failed"
        
        @JsonProperty("transfer_code")
        private String transferCode;
        
        @JsonProperty("id")
        private Long id;
        
        @JsonProperty("createdAt")
        private String createdAt;
        
        @JsonProperty("updatedAt")
        private String updatedAt;

        // Getters and Setters
        public Long getIntegration() { return integration; }
        public void setIntegration(Long integration) { this.integration = integration; }
        
        public String getDomain() { return domain; }
        public void setDomain(String domain) { this.domain = domain; }
        
        public Integer getAmount() { return amount; }
        public void setAmount(Integer amount) { this.amount = amount; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public Long getRecipient() { return recipient; }
        public void setRecipient(Long recipient) { this.recipient = recipient; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getTransferCode() { return transferCode; }
        public void setTransferCode(String transferCode) { this.transferCode = transferCode; }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    }
}
