package com.stockfellow.transactionservice.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "group_cycles")
public class GroupCycle {
    @Id
    @Column(name = "cycle_id")
    private String cycleId;
    @Column(name = "group_id")
    private String groupId;
    @Column(name = "cycle_month")
    private String cycleMonth;
    @Column(name = "recipient_user_id")
    private String recipientId;
    @Column(name = "recipient_payment_method_id")
    private String recipientMethodId;
    @Column(name = "contribution_amount")
    private BigDecimal amount;
    @Column(name = "collection_date")
    private LocalDate collectionDate;
    private String status;
    @Column(name = "total_expected_amount")
    private BigDecimal totalExpected;
    @Column(name = "total_collected_amount")
    private BigDecimal totalCollected;
    @Column(name = "successful_payments")
    private Integer successfulPayments;
    @Column(name = "failed_payments")
    private Integer failedPayments;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}