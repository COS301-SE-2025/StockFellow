package com.stockfellow.transactionservice.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "member_balances")
public class MemberBalance {
    @Id
    @Column(name = "balance_id")
    private String balanceId;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "group_id")
    private String groupId;
    @Column(name = "total_contributed")
    private BigDecimal totalContributed;
    @Column(name = "total_received")
    private BigDecimal totalReceived;
    @Column(name = "last_contribution_date")
    private LocalDate lastContribution;
    @Column(name = "last_receipt_date")
    private LocalDate lastReceipt;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
