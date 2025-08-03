
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
public interface GroupCycleRepository extends JpaRepository<GroupCycle, UUID> {
    
    /**
     * Find cycles by group ID
     */
    List<GroupCycle> findByGroupId(UUID groupId);
    
    /**
     * Find cycles by recipient user ID
     */
    List<GroupCycle> findByRecipientUserId(UUID recipientUserId);
    
    /**
     * Find cycles by status
     */
    List<GroupCycle> findByStatus(GroupCycle.CycleStatus status);
    
    /**
     * Find cycles by group and status
     */
    List<GroupCycle> findByGroupIdAndStatus(UUID groupId, GroupCycle.CycleStatus status);
    
    /**
     * Find active cycles (ACTIVE, COLLECTING, COLLECTION_COMPLETE)
     */
    @Query("SELECT gc FROM GroupCycle gc WHERE gc.status IN ('ACTIVE', 'COLLECTING', 'COLLECTION_COMPLETE')")
    List<GroupCycle> findActiveCycles();
    
    /**
     * Find cycles ending within specified date range
     */
    List<GroupCycle> findByCollectionEndDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find cycles due for payout
     */
    @Query("SELECT gc FROM GroupCycle gc WHERE gc.status = 'COLLECTION_COMPLETE' AND gc.payoutDate <= :date")
    List<GroupCycle> findCyclesDueForPayout(@Param("date") LocalDate date);
    
    /**
     * Find cycles by period
     */
    List<GroupCycle> findByCyclePeriod(String cyclePeriod);
    
    /**
     * Get cycle statistics by group
     */
    @Query("SELECT gc.status, COUNT(gc) FROM GroupCycle gc WHERE gc.groupId = :groupId GROUP BY gc.status")
    List<Object[]> getCycleStatisticsByGroup(@Param("groupId") UUID groupId);
    
    /**
     * Find cycles where current total is less than expected total
     */
    @Query("SELECT gc FROM GroupCycle gc WHERE gc.currentTotal < gc.expectedTotal AND gc.status IN ('ACTIVE', 'COLLECTING')")
    List<GroupCycle> findUnderFundedCycles();
}