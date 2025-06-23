// package com.stockfellow.transactionservice.model;

// import lombok.Data;
// import javax.persistence.*;
// import java.time.LocalDateTime;
// import java.math.BigDecimal;

// @Data
// @Entity
// @Table(name = "group_cycles")
// public class GroupCycle {
//     @Id
//     @Column(name = "cycle_id")
//     private String cycleId;
//     @Column(name = "group_id")
//     private String groupId;
//     @Column(name = "cycle_month")
//     private String cycleMonth;
//     @Column(name = "recipient_user_id")
//     private String recipientId;
//     @Column(name = "recipient_payment_method_id")
//     private String recipientMethodId;
//     @Column(name = "contribution_amount")
//     private BigDecimal amount;
//     @Column(name = "collection_date")
//     private LocalDate collectionDate;
//     private String status;
//     @Column(name = "total_expected_amount")
//     private BigDecimal totalExpected;
//     @Column(name = "total_collected_amount")
//     private BigDecimal totalCollected;
//     @Column(name = "successful_payments")
//     private Integer successfulPayments;
//     @Column(name = "failed_payments")
//     private Integer failedPayments;
//     @Column(name = "created_at")
//     private LocalDateTime createdAt;
//     @Column(name = "updated_at")
//     private LocalDateTime updatedAt;
// }

package com.stockfellow.transactionservice.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Table(name = "group_cycles")
public class GroupCycle {
    @Id
    @Column(name = "cycle_id")
    private UUID cycleId; // String to UUID

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "cycle_month", nullable = false, length = 7)
    private String cycleMonth;

    @Column(name = "recipient_user_id", nullable = false)
    private UUID recipientUserId; // field name and type

    @Column(name = "recipient_payment_method_id", nullable = false)
    private UUID recipientPaymentMethodId;

    @Column(name = "contribution_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal contributionAmount;

    @Column(name = "collection_date", nullable = false)
    private LocalDate collectionDate;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "total_expected_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalExpectedAmount;

    @Column(name = "total_collected_amount", precision = 12, scale = 2)
    private BigDecimal totalCollectedAmount;

    @Column(name = "successful_payments")
    private Integer successfulPayments;

    @Column(name = "failed_payments")
    private Integer failedPayments;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (cycleId == null) {
            cycleId = UUID.randomUUID();
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