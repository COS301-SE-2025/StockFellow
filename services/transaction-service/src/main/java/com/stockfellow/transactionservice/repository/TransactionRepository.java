package com.stockfellow.transactionservice.repository;

import com.stockfellow.transactionservice.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    // Find transactions by cycle id
    List<Transaction> findByCycleId(UUID cycleId);

    // Find transactions by cycle
    List<Transaction> findByCycleIdOrderByCreatedAtDesc(UUID cycleId);

    // Find transactions by status
    List<Transaction> findByStatus(String status);

    // Find by cycle Id and status
    List<Transaction> findByCycleIdAndStatus(UUID cycleId, String status);

    // Find transactions by payer
    List<Transaction> findByPayerUserIdOrderByCreatedAtDesc(UUID payerUserId);

    // Find transactions by recipient
    List<Transaction> findByRecipientUserIdOrderByCreatedAtDesc(UUID recipientUserId);

    // Find transactions by group
    List<Transaction> findByGroupIdOrderByCreatedAtDesc(UUID groupId);
}
