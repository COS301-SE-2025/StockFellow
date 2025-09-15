package com.stockfellow.adminservice.repository;

import com.stockfellow.adminservice.model.AdminRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminRequestRepository extends JpaRepository<AdminRequest, String> {
    
    // Find pending requests
    Page<AdminRequest> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    
    // Find requests by type
    Page<AdminRequest> findByRequestTypeAndStatusOrderByCreatedAtDesc(
        String requestType, 
        String status, 
        Pageable pageable
    );
    
    // Find requests by user
    List<AdminRequest> findByUserIdOrderByCreatedAtDesc(String userId);
    
    // Find requests processed by admin
    List<AdminRequest> findByAdminUserIdOrderByProcessedAtDesc(String adminUserId);
    
    // Complex filtering for pending requests
    @Query("""
        SELECT ar FROM AdminRequest ar 
        WHERE ar.status = 'PENDING'
        AND (:requestType IS NULL OR ar.requestType = :requestType)
        ORDER BY ar.createdAt ASC
        """)
    Page<AdminRequest> findPendingRequestsWithFilters(
        @Param("requestType") String requestType,
        Pageable pageable
    );
    
    // Analytics queries
    @Query("SELECT COUNT(ar) FROM AdminRequest ar WHERE ar.status = :status")
    long countByStatus(@Param("status") String status);
    
    @Query("""
        SELECT ar.requestType, COUNT(ar) 
        FROM AdminRequest ar 
        WHERE ar.createdAt >= :startDate 
        GROUP BY ar.requestType
        """)
    List<Object[]> getRequestTypeDistribution(@Param("startDate") LocalDateTime startDate);
    
    // Find requests awaiting action for too long
    @Query("""
        SELECT ar FROM AdminRequest ar 
        WHERE ar.status = 'PENDING' 
        AND ar.createdAt <= :cutoffDate
        ORDER BY ar.createdAt ASC
        """)
    List<AdminRequest> findStaleRequests(@Param("cutoffDate") LocalDateTime cutoffDate);
}