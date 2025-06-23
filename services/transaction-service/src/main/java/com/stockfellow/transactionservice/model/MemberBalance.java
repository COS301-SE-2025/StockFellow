// package com.stockfellow.transactionservice.model;

// import lombok.Data;
// import javax.persistence.*;
// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.math.BigDecimal;

// @Data
// @Entity
// @Table(name = "member_balances")
// public class MemberBalance {
//     @Id
//     @Column(name = "balance_id")
//     private String balanceId;
//     @Column(name = "user_id")
//     private String userId;
//     @Column(name = "group_id")
//     private String groupId;
//     @Column(name = "total_contributed")
//     private BigDecimal totalContributed;
//     @Column(name = "total_received")
//     private BigDecimal totalReceived;
//     @Column(name = "last_contribution_date")
//     private LocalDate lastContribution;
//     @Column(name = "last_receipt_date")
//     private LocalDate lastReceipt;
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
@Table(name = "member_balances")
public class MemberBalance {
    @Id
    @Column(name = "balance_id")
    private UUID balanceId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "total_contributed", precision = 12, scale = 2)
    private BigDecimal totalContributed = BigDecimal.ZERO;

    @Column(name = "total_received", precision = 12, scale = 2)
    private BigDecimal totalReceived = BigDecimal.ZERO;

    @Column(name = "last_contribution_date")
    private LocalDate lastContributionDate;

    @Column(name = "last_receipt_date")
    private LocalDate lastReceiptDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (balanceId == null) {
            balanceId = UUID.randomUUID();
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