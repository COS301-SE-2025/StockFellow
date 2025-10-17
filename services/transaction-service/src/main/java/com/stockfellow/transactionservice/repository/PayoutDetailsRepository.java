
package com.stockfellow.transactionservice.repository;

import com.stockfellow.transactionservice.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayoutDetailsRepository extends JpaRepository<PayoutDetails, UUID> {
    
    /**
     * Find payout details by user ID
     */
    List<PayoutDetails> findByUserId(UUID userId);
    
    /**
     * Find default payout method for user
     */
    Optional<PayoutDetails> findByUserIdAndIsDefaultTrue(UUID userId);
    
    /**
     * Find payout details by type
     */
    List<PayoutDetails> findByType(String type);
    
    /**
     * Find payout details by user and type
     */
    List<PayoutDetails> findByUserIdAndType(UUID userId, String type);
    
    /**
     * Find verified payout details
     */
    List<PayoutDetails> findByIsVerifiedTrue();
    
    /**
     * Find payout details by recipient code
     */
    Optional<PayoutDetails> findByRecipientCode(String recipientCode);
    
    /**
     * Find payout details by account number
     */
    List<PayoutDetails> findByAccountNumber(String accountNumber);
    
    /**
     * Find payout details by bank code
     */
    List<PayoutDetails> findByBankCode(String bankCode);
    
    /**
     * Find payout details by phone number
     */
    List<PayoutDetails> findByPhoneNumber(String phoneNumber);
    
    /**
     * Find payout details by provider
     */
    List<PayoutDetails> findByProvider(String provider);
    
    /**
     * Check if user has verified payout method
     */
    boolean existsByUserIdAndIsVerifiedTrue(UUID userId);
    
    /**
     * Update default status for user (set all to false)
     */
    @Modifying
    @Query("UPDATE PayoutDetails pd SET pd.isDefault = false WHERE pd.userId = :userId")
    void clearDefaultForUser(@Param("userId") UUID userId);
    
    /**
     * Count verified payout methods by user
     */
    long countByUserIdAndIsVerifiedTrue(UUID userId);
    
    /**
     * Find payout details by recipient name (case insensitive)
     */
    List<PayoutDetails> findByRecipientNameContainingIgnoreCase(String recipientName);
}
