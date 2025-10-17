package com.stockfellow.transactionservice.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/**
 * Response DTO for Paystack transaction verification
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaystackTransactionVerificationResponse {
    
    @JsonProperty("status")
    private Boolean status;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("data")
    private PaystackTransactionVerificationData data;

    // Constructors
    public PaystackTransactionVerificationResponse() {}

    // Getters and Setters
    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public PaystackTransactionVerificationData getData() { return data; }
    public void setData(PaystackTransactionVerificationData data) { this.data = data; }

    // Inner class for verification data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaystackTransactionVerificationData {
        
        @JsonProperty("id")
        private Long id;
        
        @JsonProperty("domain")
        private String domain;
        
        @JsonProperty("status")
        private String status; // "success", "failed", "abandoned"
        
        @JsonProperty("reference")
        private String reference;
        
        @JsonProperty("amount")
        private Integer amount;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("gateway_response")
        private String gatewayResponse;
        
        @JsonProperty("paid_at")
        private String paidAt;
        
        @JsonProperty("created_at")
        private String createdAt;
        
        @JsonProperty("channel")
        private String channel;
        
        @JsonProperty("currency")
        private String currency;
        
        @JsonProperty("ip_address")
        private String ipAddress;
        
        @JsonProperty("metadata")
        private Map<String, Object> metadata;
        
        @JsonProperty("customer")
        private PaystackCustomer customer;
        
        @JsonProperty("authorization")
        private PaystackAuthorization authorization;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getDomain() { return domain; }
        public void setDomain(String domain) { this.domain = domain; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }
        
        public Integer getAmount() { return amount; }
        public void setAmount(Integer amount) { this.amount = amount; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getGatewayResponse() { return gatewayResponse; }
        public void setGatewayResponse(String gatewayResponse) { this.gatewayResponse = gatewayResponse; }
        
        public String getPaidAt() { return paidAt; }
        public void setPaidAt(String paidAt) { this.paidAt = paidAt; }
        
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        
        public String getChannel() { return channel; }
        public void setChannel(String channel) { this.channel = channel; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
        
        public PaystackCustomer getCustomer() { return customer; }
        public void setCustomer(PaystackCustomer customer) { this.customer = customer; }
        
        public PaystackAuthorization getAuthorization() { return authorization; }
        public void setAuthorization(PaystackAuthorization authorization) { this.authorization = authorization; }
    }
}
