// package com.stockfellow.transactionservice.model;

// import lombok.Data;
// import javax.persistence.*;
// import java.time.LocalDateTime;
// import java.math.BigDecimal;

// @Data
// @Entity
// @Table(name = "transactions")
// public class Transaction {
// @Id
// @Column(name = "transaction_id")
// private String transactionId;
// @Column(name = "cycle_id")
// private String cycleId;
// @Column(name = "mandate_id")
// private String mandateId;
// @Column(name = "payer_user_id")
// private String payerId;
// @Column(name = "recipient_user_id")
// private String recipientId;
// @Column(name = "group_id")
// private String groupId;
// @Column(name = "payer_payment_method_id")
// private String payerMethodId;
// @Column(name = "recipient_payment_method_id")
// private String recipientMethodId;
// private BigDecimal amount;
// private String status;
// @Column(name = "external_ref")
// private String externalRef;
// @Column(name = "retry_count")
// private Integer retryCount;
// @Column(name = "fail_message")
// private String failMessage;
// @Column(name = "created_at")
// private LocalDateTime createdAt;
// @Column(name = "completed_at")
// private LocalDateTime completedAt;
// }

package com.stockfellow.transactionservice.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @Column(name = "transaction_id")
    private UUID transactionId;

    @Column(name = "cycle_id", nullable = false)
    private UUID cycleId;

    @Column(name = "mandate_id", nullable = false)
    private UUID mandateId;

    @Column(name = "payer_user_id", nullable = false)
    private UUID payerUserId;

    @Column(name = "recipient_user_id", nullable = false)
    private UUID recipientUserId;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "payer_payment_method_id", nullable = false)
    private UUID payerPaymentMethodId;

    @Column(name = "recipient_payment_method_id", nullable = false)
    private UUID recipientPaymentMethodId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "external_reference")
    private String externalReference;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "fail_message", columnDefinition = "TEXT")
    private String failMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        if (transactionId == null) {
            transactionId = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
    }
}