package com.stockfellow.transactionservice.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class CreateMandateRequest {
    
    @NotNull(message = "Payer user ID is required")
    private UUID payerUserId;
    
    @NotNull(message = "Group ID is required")
    private UUID groupId;
    
    @NotNull(message = "Payment method ID is required")
    private UUID paymentMethodId;
    
    private String documentReference;
    
    @NotNull(message = "IP address is required")
    private String ipAddress;

    // Constructors
    public CreateMandateRequest() {}

    public CreateMandateRequest(UUID payerUserId, UUID groupId, UUID paymentMethodId, String documentReference, String ipAddress) {
        this.payerUserId = payerUserId;
        this.groupId = groupId;
        this.paymentMethodId = paymentMethodId;
        this.documentReference = documentReference;
        this.ipAddress = ipAddress;
    }

    // Getters
    public UUID getPayerUserId() { return payerUserId; }
    public UUID getGroupId() { return groupId; }
    public UUID getPaymentMethodId() { return paymentMethodId; }
    public String getDocumentReference() { return documentReference; }
    public String getIpAddress() { return ipAddress; }

    // Setters
    public void setPayerUserId(UUID payerUserId) { this.payerUserId = payerUserId; }
    public void setGroupId(UUID groupId) { this.groupId = groupId; }
    public void setPaymentMethodId(UUID paymentMethodId) { this.paymentMethodId = paymentMethodId; }
    public void setDocumentReference(String documentReference) { this.documentReference = documentReference; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}