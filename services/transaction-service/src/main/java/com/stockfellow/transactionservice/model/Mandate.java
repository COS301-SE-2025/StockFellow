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
    @Column(name = "payer_user_id")
    private String payerId;
    @Column(name = "group_id")
    private String groupId;
    @Column(name = "payment_method_id")
    private String payMethodId;
    private String status;
    @Column(name = "signed_date")
    private LocalDateTime signed;
    @Column(name = "document_reference")
    private String docRef;
    @Column(name = "ip_address")
    private String ipAddr;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}