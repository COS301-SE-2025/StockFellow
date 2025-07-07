// // package com.stockfellow.transactionservice.model;

// // import lombok.Data;
// // import javax.persistence.*;
// // import java.time.LocalDateTime;

// // @Data
// // @Entity
// // @Table(name = "transaction_events")
// // public class TransactionEvent {
// //     @Id
// //     @Column(name = "event_id")
// //     private String eventId;
// //     @Column(name = "transaction_id")
// //     private String transactionId;
// //     @Column(name = "event_type")
// //     private String eventType;
// //     @Column(name = "previous_status")
// //     private String prevStatus;
// //     @Column(name = "new_status")
// //     private String newStatus;
// //     @Column(name = "triggered_by")
// //     private String triggeredBy;
// //     @Column(name = "metadata")
// //     private String metaData;
// //     @Column(name = "created_at")
// //     private LocalDateTime createdAt;
// // }

// package com.stockfellow.transactionservice.model;

// import lombok.Data;
// import jakarta.persistence.*;
// import org.hibernate.annotations.JdbcTypeCode;
// import org.hibernate.type.SqlTypes;
// import java.time.LocalDateTime;
// import java.util.Map;
// import java.util.UUID;

// @Data
// @Entity
// @Table(name = "transaction_events")
// public class TransactionEvent {
//     @Id
//     @Column(name = "event_id")
//     private UUID eventId;

//     @Column(name = "transaction_id", nullable = false)
//     private UUID transactionId;

//     @Column(name = "event_type", nullable = false, length = 30)
//     private String eventType;

//     @Column(name = "previous_status", length = 20)
//     private String previousStatus;

//     @Column(name = "new_status", nullable = false, length = 20)
//     private String newStatus;

//     @Column(name = "triggered_by", length = 50)
//     private String triggeredBy;

//     @JdbcTypeCode(SqlTypes.JSON)
//     @Column(name = "metadata", columnDefinition = "jsonb")
//     private Map<String, Object> metadata;

//     @Column(name = "created_at", nullable = false)
//     private LocalDateTime createdAt;

//     @PrePersist
//     protected void onCreate() {
//         if (eventId == null) {
//             eventId = UUID.randomUUID();
//         }
//         createdAt = LocalDateTime.now();
//     }
// }