package com.stockfellow.transactionservice.repository;

import com.stockfellow.transactionservice.model.Mandate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MandateRepository extends JpaRepository<Mandate, UUID> {
    
    List<Mandate> findAll();
    /**
     * Check if a mandate exists for a specific user and group
     */
    boolean existsByPayerUserIdAndGroupId(UUID payerUserId, UUID groupId);
    
    /**
     * Find mandates by payer user ID
     */
    List<Mandate> findByPayerUserId(UUID payerUserId);
    
    /**
     * Find mandates by group ID
     */
    List<Mandate> findByGroupId(UUID groupId);
    
    /**
     * Find mandate by payer user ID and group ID
     */
    Optional<Mandate> findByPayerUserIdAndGroupId(UUID payerUserId, UUID groupId);
    
    /**
     * Find mandates by status
     */
    List<Mandate> findByStatus(String status);
    
    /**
     * Find active mandates for a specific user
     */
    @Query("SELECT m FROM Mandate m WHERE m.payerUserId = :payerUserId AND m.status = 'ACTIVE'")
    List<Mandate> findActiveMandatesByPayerUserId(@Param("payerUserId") UUID payerUserId);
    
    /**
     * Find active mandates for a specific group
     */
    @Query("SELECT m FROM Mandate m WHERE m.groupId = :groupId AND m.status = 'ACTIVE'")
    List<Mandate> findActiveMandatesByGroupId(@Param("groupId") UUID groupId);
}