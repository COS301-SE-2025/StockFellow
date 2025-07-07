package com.stockfellow.transactionservice.repository;

import com.stockfellow.transactionservice.model.BankDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankDetailRepository extends JpaRepository<BankDetails, UUID> {
    // Find all bank details for a specific user
    List<BankDetails> findByUserId(UUID userId);
    
    // Find all active bank details for a specific user
    List<BankDetails> findByUserIdAndIsActiveTrue(UUID userId);
    
    // Find a specific bank detail by user and id
    Optional<BankDetails> findByIdAndUserId(UUID id, UUID userId);
    
    // Find active bank detail by user and id
    Optional<BankDetails> findByIdAndUserIdAndIsActiveTrue(UUID id, UUID userId);
    
    // Check if user has any active bank details
    boolean existsByUserIdAndIsActiveTrue(UUID userId);
    
    // Count active bank details for a user
    long countByUserIdAndIsActiveTrue(UUID userId);
    
    // Find by card type for a specific user
    List<BankDetails> findByUserIdAndCardTypeAndIsActiveTrue(UUID userId, String cardType);
    
    // Find by bank name for a specific user
    List<BankDetails> findByUserIdAndBankAndIsActiveTrue(UUID userId, String bank);
    
    // Custom query to find bank details by partial card number (last 4 digits)
    @Query("SELECT bd FROM BankDetails bd WHERE bd.userId = :userId AND bd.cardNumber LIKE %:lastFourDigits AND bd.isActive = true")
    List<BankDetails> findByUserIdAndLastFourDigits(@Param("userId") UUID userId, @Param("lastFourDigits") String lastFourDigits);
    
    // Custom query to find expiring cards (useful for notifications)
    @Query("SELECT bd FROM BankDetails bd WHERE bd.userId = :userId AND bd.expiryYear = :year AND bd.expiryMonth <= :month AND bd.isActive = true")
    List<BankDetails> findExpiringCards(@Param("userId") UUID userId, @Param("year") Integer year, @Param("month") Integer month);
    
    // Soft delete - mark as inactive instead of actual deletion
    @Query("UPDATE BankDetails bd SET bd.isActive = false WHERE bd.id = :id AND bd.userId = :userId")
    int deactivateBankDetail(@Param("id") UUID id, @Param("userId") UUID userId);
    
    // Find default/primary bank detail (you might add a 'isPrimary' field later)
    Optional<BankDetails> findFirstByUserIdAndIsActiveTrueOrderByCreatedAtAsc(UUID userId);

    //Activate
    @Query("UPDATE BankDetails bd SET bd.isActive = true WHERE bd.id = :id AND bd.userId = :userId")
    int activateBankDetail(@Param("id") UUID id, @Param("userId") UUID userId);
}