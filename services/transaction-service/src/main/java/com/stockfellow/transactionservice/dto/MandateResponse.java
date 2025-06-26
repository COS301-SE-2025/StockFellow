package com.stockfellow.transactionservice.dto;

import com.stockfellow.transactionservice.model.Mandate;

import java.time.LocalDateTime;
import java.util.UUID;

public class MandateResponse {
    
    private UUID mandateId;
    private UUID payerUserId;
    private UUID groupId;
    private UUID paymentMethodId;
    private String status;
    private LocalDateTime signedDate;
    private String documentReference;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public MandateResponse() {}

    public MandateResponse(UUID mandateId, UUID payerUserId, UUID groupId, UUID paymentMethodId, 
                          String status, LocalDateTime signedDate, String documentReference, 
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.mandateId = mandateId;
        this.payerUserId = payerUserId;
        this.groupId = groupId;
        this.paymentMethodId = paymentMethodId;
        this.status = status;
        this.signedDate = signedDate;
        this.documentReference = documentReference;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public UUID getMandateId() { return mandateId; }
    public UUID getPayerUserId() { return payerUserId; }
    public UUID getGroupId() { return groupId; }
    public UUID getPaymentMethodId() { return paymentMethodId; }
    public String getStatus() { return status; }
    public LocalDateTime getSignedDate() { return signedDate; }
    public String getDocumentReference() { return documentReference; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setMandateId(UUID mandateId) { this.mandateId = mandateId; }
    public void setPayerUserId(UUID payerUserId) { this.payerUserId = payerUserId; }
    public void setGroupId(UUID groupId) { this.groupId = groupId; }
    public void setPaymentMethodId(UUID paymentMethodId) { this.paymentMethodId = paymentMethodId; }
    public void setStatus(String status) { this.status = status; }
    public void setSignedDate(LocalDateTime signedDate) { this.signedDate = signedDate; }
    public void setDocumentReference(String documentReference) { this.documentReference = documentReference; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public static MandateResponse from(Mandate mandate) {
        return new MandateResponse(
                mandate.getMandateId(),
                mandate.getPayerUserId(),
                mandate.getGroupId(),
                mandate.getPaymentMethodId(),
                mandate.getStatus(),
                mandate.getSignedDate(),
                mandate.getDocumentReference(),
                mandate.getCreatedAt(),
                mandate.getUpdatedAt()
        );
    }
}