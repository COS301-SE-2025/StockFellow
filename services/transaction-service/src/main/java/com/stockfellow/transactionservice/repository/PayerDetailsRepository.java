
package com.stockfellow.transactionservice.repository;

import com.stockfellow.transactionservice.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface PayerDetailsRepository extends JpaRepository<PayerDetails, UUID> {
    
    /**
     * Find payer details by user ID
     */
    List<PayerDetails> findByUserId(UUID userId);
    
    /**
     * Find active payer details by user ID
     */
    List<PayerDetails> findByUserIdAndIsActiveTrue(UUID userId);
    
    /**
     * Find payer details by email
     */
    List<PayerDetails> findByEmail(String email);
    
    /**
     * Find payer details by type
     */
    List<PayerDetails> findByType(PayerDetails.PaymentMethodType type);
    
    /**
     * Find payer details by user and type
     */
    List<PayerDetails> findByUserIdAndType(UUID userId, PayerDetails.PaymentMethodType type);
    
    /**
     * Find authenticated payer details
     */
    List<PayerDetails> findByIsAuthenticatedTrue();
    
    /**
     * Find payer details by auth code
     */
    Optional<PayerDetails> findByAuthCode(String authCode);
    
    /**
     * Find payer details by last 4 digits and user
     */
    List<PayerDetails> findByUserIdAndLast4(UUID userId, String last4);
    
    /**
     * Find payer details by bank
     */
    List<PayerDetails> findByBank(String bank);
    
    /**
     * Check if user has authenticated payment method
     */
    boolean existsByUserIdAndIsAuthenticatedTrue(UUID userId);

    /**
     * Check for duplicat payer details by card signature
     */
    boolean existsByUserIdAndSignature(UUID userId, String signature);
    
    /**
     * Count active payment methods by user
     */
    long countByUserIdAndIsActiveTrue(UUID userId);
}