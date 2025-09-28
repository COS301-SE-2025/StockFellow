package com.stockfellow.adminservice.repository;

import com.stockfellow.adminservice.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    
    // Find logs by user ID
    Page<AuditLog> findByUserIdOrderByTimestampDesc(String userId, Pageable pageable);
    
    // Find logs by endpoint
    Page<AuditLog> findByEndpointContainingIgnoreCaseOrderByTimestampDesc(String endpoint, Pageable pageable);
    
    // Find flagged logs for review
    Page<AuditLog> findByFlaggedForReviewTrueOrderByTimestampDesc(Pageable pageable);
    
    // Find high-risk logs (FIXED: Added Pageable parameter)
    Page<AuditLog> findByRiskScoreGreaterThanEqualOrderByRiskScoreDesc(Integer riskThreshold, Pageable pageable);
    
    // Find logs within date range
    Page<AuditLog> findByTimestampBetweenOrderByTimestampDesc(
        LocalDateTime startDate, 
        LocalDateTime endDate, 
        Pageable pageable
    );
    
    // Find user activity within date range (FIXED: This method exists)
    List<AuditLog> findByUserIdAndTimestampBetweenOrderByTimestampDesc(
        String userId, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
    
    // FIXED: Simplified complex filtering query - avoid null dates by using dynamic query logic
    @Query("""
        SELECT al FROM AuditLog al 
        WHERE (:userId IS NULL OR :userId = '' OR al.userId = :userId)
        AND (:endpoint IS NULL OR :endpoint = '' OR UPPER(al.endpoint) LIKE UPPER(CONCAT('%', :endpoint, '%')))
        AND (:flaggedOnly = FALSE OR al.flaggedForReview = TRUE)
        ORDER BY al.timestamp DESC
        """)
    Page<AuditLog> findAuditLogsWithFilters(
        @Param("userId") String userId,
        @Param("endpoint") String endpoint,
        @Param("flaggedOnly") Boolean flaggedOnly,
        Pageable pageable
    );
    
    // Additional method with date range filtering
    @Query("""
        SELECT al FROM AuditLog al 
        WHERE (:userId IS NULL OR :userId = '' OR al.userId = :userId)
        AND (:endpoint IS NULL OR :endpoint = '' OR UPPER(al.endpoint) LIKE UPPER(CONCAT('%', :endpoint, '%')))
        AND al.timestamp >= :startDate
        AND al.timestamp <= :endDate
        AND (:flaggedOnly = FALSE OR al.flaggedForReview = TRUE)
        ORDER BY al.timestamp DESC
        """)
    Page<AuditLog> findAuditLogsWithDateFilters(
        @Param("userId") String userId,
        @Param("endpoint") String endpoint,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("flaggedOnly") Boolean flaggedOnly,
        Pageable pageable
    );
    
    // Analytics queries
    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.timestamp >= :startDate")
    long countLogsAfter(@Param("startDate") LocalDateTime startDate);
    
    @Query("""
        SELECT al.endpoint, COUNT(al) as count 
        FROM AuditLog al 
        WHERE al.timestamp >= :startDate 
        GROUP BY al.endpoint 
        ORDER BY count DESC
        """)
    List<Object[]> getTopEndpointsByUsage(@Param("startDate") LocalDateTime startDate);
    
    @Query("""
        SELECT AVG(al.riskScore) 
        FROM AuditLog al 
        WHERE al.timestamp >= :startDate AND al.riskScore > 0
        """)
    Double getAverageRiskScore(@Param("startDate") LocalDateTime startDate);

    // Find suspicious activity (high risk score)
    @Query("SELECT a FROM AuditLog a WHERE a.riskScore >= :minRiskScore ORDER BY a.timestamp DESC")
    List<AuditLog> findSuspiciousActivity(@Param("minRiskScore") Integer minRiskScore);
    
    // Count flagged logs
    long countByFlaggedForReviewTrue();
    
    // Recent high-risk activities
    @Query("SELECT a FROM AuditLog a WHERE a.riskScore >= 70 AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentHighRiskActivity(@Param("since") LocalDateTime since);
    
    // User activity count in time period
    @Query("SELECT COUNT(DISTINCT a.userId) FROM AuditLog a WHERE a.timestamp >= :since")
    long countActiveUsersInPeriod(@Param("since") LocalDateTime since);
    
    // ADDED: Count user activities in time period (for frequency calculation)
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.userId = :userId AND a.timestamp BETWEEN :startDate AND :endDate")
    long countByUserIdAndTimestampBetween(@Param("userId") String userId, 
                                         @Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
}