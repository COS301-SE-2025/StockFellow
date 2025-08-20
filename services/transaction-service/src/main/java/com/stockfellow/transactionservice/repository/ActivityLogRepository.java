
package com.stockfellow.transactionservice.repository;

import com.stockfellow.transactionservice.model.ActivityLog;
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
public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
    
    /**
     * Find activity logs by user ID
     */
    Page<ActivityLog> findByUserId(UUID userId, Pageable pageable);
    
    /**
     * Find activity logs by cycle ID
     */
    Page<ActivityLog> findByCycleId(UUID cycleId, Pageable pageable);
    
    /**
     * Find activity logs by entity type and entity ID
     */
    Page<ActivityLog> findByEntityTypeAndEntityId(ActivityLog.EntityType entityType, UUID entityId, Pageable pageable);
    
    /**
     * Find activity logs by action
     */
    List<ActivityLog> findByAction(String action);
    
    /**
     * Find activity logs by entity type
     */
    Page<ActivityLog> findByEntityType(ActivityLog.EntityType entityType, Pageable pageable);
    
    /**
     * Find activity logs within date range
     */
    Page<ActivityLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Find activity logs by user and date range
     */
    Page<ActivityLog> findByUserIdAndCreatedAtBetween(UUID userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Find activity logs by IP address
     */
    List<ActivityLog> findByIpAddress(String ipAddress);
    
    /**
     * Count activity logs by user
     */
    long countByUserId(UUID userId);
    
    /**
     * Count activity logs by entity
     */
    long countByEntityTypeAndEntityId(ActivityLog.EntityType entityType, UUID entityId);
    
    /**
     * Find recent activity logs (last N days)
     */
    @Query("SELECT al FROM ActivityLog al WHERE al.createdAt >= :cutoffDate ORDER BY al.createdAt DESC")
    Page<ActivityLog> findRecentActivityLogs(@Param("cutoffDate") LocalDateTime cutoffDate, Pageable pageable);
    
    /**
     * Get activity statistics by action
     */
    @Query("SELECT al.action, COUNT(al) FROM ActivityLog al GROUP BY al.action ORDER BY COUNT(al) DESC")
    List<Object[]> getActivityStatistics();
    
    /**
     * Get activity statistics by entity type
     */
    @Query("SELECT al.entityType, COUNT(al) FROM ActivityLog al GROUP BY al.entityType ORDER BY COUNT(al) DESC")
    List<Object[]> getEntityTypeStatistics();
}