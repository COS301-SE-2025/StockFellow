package com.stockfellow.transactionservice.repository;

import com.stockfellow.transactionservice.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, UUID> {
    
    /**
     * Find transfers by cycle ID
     */
    List<Transfer> findByCycleId(UUID cycleId);
    
    /**
     * Find transfers by user ID
     */
    Page<Transfer> findByUserId(UUID userId, Pageable pageable);
    
    /**
     * Find transfers by payout detail ID
     */
    List<Transfer> findByPayoutDetailId(UUID payoutDetailId);
    
    /*
     * Check if user has already recieved a payout this cycle
     */
    List<Transfer> findByCycleIdAndUserIdAndStatus(UUID cycleId, UUID userId, Transfer.TransferStatus status);
    /**
     * Find transfers by status
     */
    List<Transfer> findByStatus(Transfer.TransferStatus status);
    
    /**
     * Find transfers by Paystack transfer code
     */
    Optional<Transfer> findByPaystackTransferCode(String paystackTransferCode);
    
    /**
     * Find transfers by Paystack recipient code
     */
    List<Transfer> findByPaystackRecipientCode(String paystackRecipientCode);
    
    /**
     * Find failed transfers that can be retried
     */
    @Query("SELECT tr FROM Transfer tr WHERE tr.status = 'FAILED' AND tr.retryCount < :maxRetries")
    List<Transfer> findRetryableTransfers(@Param("maxRetries") Integer maxRetries);
    
    /**
     * Find transfers created within date range
     */
    List<Transfer> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find pending transfers older than specified time
     */
    @Query("SELECT tr FROM Transfer tr WHERE tr.status = 'PENDING' AND tr.createdAt < :cutoffTime")
    List<Transfer> findStalePendingTransfers(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Calculate total transfer amount by status
     */
    @Query("SELECT COALESCE(SUM(tr.amount), 0) FROM Transfer tr WHERE tr.status = :status")
    BigDecimal getTotalAmountByStatus(@Param("status") Transfer.TransferStatus status);
    
    /**
     * Find transfers by currency
     */
    List<Transfer> findByCurrency(String currency);
    
    /**
     * Get transfer statistics for a user
     */
    @Query("SELECT tr.status, COUNT(tr), COALESCE(SUM(tr.amount), 0) FROM Transfer tr WHERE tr.userId = :userId GROUP BY tr.status")
    List<Object[]> getTransferStatisticsByUser(@Param("userId") UUID userId);
}
