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

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
    
    // Find logs by user ID
    Page<AuditLog> findByUserIdOrderByTimestampDesc(String userId, Pageable pageable);
    
    // Find logs by endpoint
    Page<AuditLog> findByEndpointContainingIgnoreCaseOrderByTimestampDesc(String endpoint, Pageable pageable);
    
    // Find flagged logs for review
    Page<AuditLog> findByFlaggedForReviewTrueOrderByTimestampDesc(Pageable pageable);
    
    // Find high-risk logs
    Page<AuditLog> findByRiskScoreGreaterThanEqualOrderByRiskScoreDesc(Integer riskThreshold, Pageable pageable);
    
    // Find logs within date range
    Page<AuditLog> findByTimestampBetweenOrderByTimestampDesc(
        LocalDateTime startDate, 
        LocalDateTime endDate, 
        Pageable pageable
    );
    
    // Find user activity within date range
    List<AuditLog> findByUserIdAndTimestampBetweenOrderByTimestampDesc(
        String userId, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
    
    // Complex filtering query
    @Query("""
        SELECT al FROM AuditLog al 
        WHERE (:userId IS NULL OR al.userId = :userId)
        AND (:endpoint IS NULL OR al.endpoint LIKE %:endpoint%)
        AND (:startDate IS NULL OR al.timestamp >= :startDate)
        AND (:endDate IS NULL OR al.timestamp <= :endDate)
        AND (:flaggedOnly = false OR al.flaggedForReview = true)
        ORDER BY al.timestamp DESC
        """)
    Page<AuditLog> findAuditLogsWithFilters(
        @Param("userId") String userId,
        @Param("endpoint") String endpoint,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("flaggedOnly") boolean flaggedOnly,
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
}