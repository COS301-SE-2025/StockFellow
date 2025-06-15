package com.stockfellow.transactionservice.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "mandates")
public class Mandate {
    @Id
    @Column(name = "mandate_id")
    private String mandateId;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "bank_account")
    private String bankAccount;
    private String status;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
