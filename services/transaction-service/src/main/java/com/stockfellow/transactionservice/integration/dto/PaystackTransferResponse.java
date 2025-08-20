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
        
        @JsonProperty("id")
        private Long id;

        @JsonProperty("reference")
        private String reference; // Your reference for tracking
        
        @JsonProperty("transfer_code")
        private String transferCode; // Important for verification/tracking
        
        @JsonProperty("amount")
        private Integer amount; 
        
        @JsonProperty("status")
        private String status; // "pending", "success", "failed"
        
        @JsonProperty("recipient")
        private Long recipient;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getReference() { return reference; }
        public void setId(String reference) { this.reference = reference; }

        public String getTransferCode() { return transferCode; }
        public void setTransferCode(String transferCode) { this.transferCode = transferCode; }

        public Integer getAmount() { return amount; }
        public void setAmount(Integer amount) { this.amount = amount; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Long getRecipient() { return recipient; }
        public void setRecipient(Long recipient) { this.recipient = recipient; }        
    }
}
