// package com.stockfellow.transactionservice.repository;

// import com.stockfellow.transactionservice.model.TransactionEvent;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository;

// import java.util.List;
// import java.util.UUID;

// @Repository
// public interface EventRepository extends JpaRepository<TransactionEvent, UUID> { // CHANGED: String to UUID

//     // Find events by transaction
//     List<TransactionEvent> findByTransactionIdOrderByCreatedAtDesc(UUID transactionId);

//     // Find events by type
//     List<TransactionEvent> findByEventTypeOrderByCreatedAtDesc(String eventType);

//     // Find events by status transition
//     List<TransactionEvent> findByPreviousStatusAndNewStatusOrderByCreatedAtDesc(
//             String previousStatus, String newStatus);

// }
