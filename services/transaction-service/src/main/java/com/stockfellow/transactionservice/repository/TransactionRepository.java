package com.stockfellow.transactionservice.repository;

import com.stockfellow.transactionservice.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
    /**
     * Find transactions by cycle ID
     */
    Page<Transaction> findByCycleId(UUID cycleId, Pageable pageable);
    
    /**
     * Find transactions by user ID
     */
    Page<Transaction> findByUserId(UUID userId, Pageable pageable);
    
    /**
     * Find transactions by payer ID
     */
    List<Transaction> findByPayerId(UUID payerId);
    
    /**
     * Find transactions by status
     */
    List<Transaction> findByStatus(Transaction.TransactionStatus status);
    
    /**
     * Find transactions by cycle and status
     */
    List<Transaction> findByCycleIdAndStatus(UUID cycleId, Transaction.TransactionStatus status);
    
    /**
     * Find transactions by user and status
     */
    List<Transaction> findByUserIdAndStatus(UUID userId, Transaction.TransactionStatus status);
    
    // Check if user already has a successful transaction for this cycle
    List<Transaction> findByCycleIdAndUserIdAndStatus(UUID cycleId, UUID userId, Transaction.TransactionStatus status);

    /**
     * Find transactions by Paystack reference
     */
    Optional<Transaction> findByPaystackReference(String paystackReference);
    
    /**
     * Find transactions by Paystack transaction ID
     */
    Optional<Transaction> findByPaystackTransId(String paystackTransId);
    
    /**
     * Find failed transactions that can be retried
     */
    @Query("SELECT t FROM Transaction t WHERE t.status = 'FAILED' AND t.retryCount < :maxRetries")
    List<Transaction> findRetryableTransactions(@Param("maxRetries") Integer maxRetries);
    
    /**
     * Find transactions created within date range
     */
    List<Transaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Calculate total amount by cycle and status
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.cycleId = :cycleId AND t.status = :status")
    BigDecimal getTotalAmountByCycleAndStatus(@Param("cycleId") UUID cycleId, @Param("status") Transaction.TransactionStatus status);
    
    /**
     * Count transactions by cycle and status
     */
    long countByCycleIdAndStatus(UUID cycleId, Transaction.TransactionStatus status);
    
    /**
     * Find pending transactions older than specified time
     */
    @Query("SELECT t FROM Transaction t WHERE t.status = 'PENDING' AND t.createdAt < :cutoffTime")
    List<Transaction> findStalePendingTransactions(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Get transaction statistics for a cycle
     */
    @Query("SELECT t.status, COUNT(t), COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.cycleId = :cycleId GROUP BY t.status")
    List<Object[]> getTransactionStatisticsByCycle(@Param("cycleId") UUID cycleId);
}