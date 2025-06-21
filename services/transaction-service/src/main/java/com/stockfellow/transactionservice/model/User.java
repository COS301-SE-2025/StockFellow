package com.stockfellow.transactionservice.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "user_id")
    private String userId;
    private String email;
    private String phone;
    @Column(name = "financial_tier")
    private String financialTier;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
