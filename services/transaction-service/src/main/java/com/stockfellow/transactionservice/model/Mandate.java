// package com.stockfellow.transactionservice.model;

// import lombok.Data;
// import javax.persistence.*;
// import java.time.LocalDateTime;

// @Data
// @Entity
// @Table(name = "mandates")
// public class Mandate {
//     @Id
//     @Column(name = "mandate_id")
//     private String mandateId;
//     @Column(name = "payer_user_id")
//     private String payerId;
//     @Column(name = "group_id")
//     private String groupId;
//     @Column(name = "payment_method_id")
//     private String payMethodId;
//     private String status;
//     @Column(name = "signed_date")
//     private LocalDateTime signed;
//     @Column(name = "document_reference")
//     private String docRef;
//     @Column(name = "ip_address")
//     private String ipAddr;
//     @Column(name = "created_at")
//     private LocalDateTime createdAt;
//     @Column(name = "updated_at")
//     private LocalDateTime updatedAt;
// }

package com.stockfellow.transactionservice.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
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

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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