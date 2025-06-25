package com.stockfellow.transactionservice.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @Column(name = "transaction_id")
    private String transactionId;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "group_id")
    private String groupId;
    private String type;
    private Double amount;
    private String status;
    @Column(name = "external_ref")
    private String externalRef;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
