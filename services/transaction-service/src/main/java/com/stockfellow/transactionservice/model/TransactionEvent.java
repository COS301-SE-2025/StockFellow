package com.stockfellow.transactionservice.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transaction_events")
public class TransactionEvent {
    @Id
    @Column(name = "event_id")
    private String eventId;
    @Column(name = "transaction_id")
    private String transactionId;
    @Column(name = "event_type")
    private String eventType;
    @Column(name = "previous_status")
    private String prevStatus;
    @Column(name = "new_status")
    private String newStatus;
    @Column(name = "triggered_by")
    private String triggeredBy;
    @Column(name = "metadata")
    private String metaData;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
