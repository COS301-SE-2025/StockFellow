package com.stockfellow.transactionservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mandates")
public class Mandate {
    @Id
    @Column(name = "mandate_id")
    private UUID mandateId;

    @Column(name = "payer_user_id", nullable = false)
    private UUID payerUserId;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "payment_method_id", nullable = false)
    private UUID paymentMethodId;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "signed_date", nullable = false)
    private LocalDateTime signedDate;

    @Column(name = "document_reference")
    private String documentReference;

    @Column(name = "ip_address", nullable = false, columnDefinition = "inet")
    private String ipAddress;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Getters
    public UUID getMandateId() { return mandateId; }
    public UUID getPayerUserId() { return payerUserId; }
    public UUID getGroupId() { return groupId; }
    public UUID getPaymentMethodId() { return paymentMethodId; }
    public String getStatus() { return status; }
    public LocalDateTime getSignedDate() { return signedDate; }
    public String getDocumentReference() { return documentReference; }
    public String getIpAddress() { return ipAddress; }
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
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    protected void onCreate() {
        if (mandateId == null) {
            mandateId = UUID.randomUUID();
        }
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}