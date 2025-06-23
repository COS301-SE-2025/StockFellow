package com.stockfellow.transactionservice.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @Column(name = "transaction_id")
    private String transactionId;
    @Column(name = "cycle_id")
    private String cycleId;
    @Column(name = "mandate_id")
    private String mandateId;
    @Column(name = "payer_user_id")
    private String payerId;
    @Column(name = "recipient_user_id")
    private String recipientId;
    @Column(name = "group_id")
    private String groupId;
    @Column(name = "payer_payment_method_id")
    private String payerMethodId;
    @Column(name = "recipient_payment_method_id")
    private String recipientMethodId;
    private BigDecimal amount;
    private String status;
    @Column(name = "external_ref")
    private String externalRef;
    @Column(name = "retry_count")
    private Integer retryCount;
    @Column(name = "fail_message")
    private String failMessage;        
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
