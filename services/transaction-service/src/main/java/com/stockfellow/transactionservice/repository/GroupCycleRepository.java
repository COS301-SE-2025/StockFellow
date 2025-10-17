
package com.stockfellow.transactionservice.repository;

import com.stockfellow.transactionservice.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Optional;


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
    List<GroupCycle> findByStatus(String status);
    
    /**
     * Find cycles by group and status
     */
    List<GroupCycle> findByGroupIdAndStatus(UUID groupId, String status);

    /*
     * 
     */
    Optional<GroupCycle> findFirstByStatusAndCollectionStartDateGreaterThanEqualOrderByCollectionStartDateAsc(String status, LocalDate collectionDate);

    /*
     * 
     */
    Optional<GroupCycle> findFirstByGroupIdOrderByCollectionStartDateAsc(UUID groupId);

    /*
     * Find current group cycles to charge transactions
     */
    List<GroupCycle> findByStatusAndCollectionStartDateLessThanEqual(String status, LocalDate date);
    /*
     * 
     */
    Optional<GroupCycle> findFirstByGroupIdAndStatusOrderByCollectionStartDateAsc(UUID groupId, String status);
    
    /*
     * 
     */
    List<GroupCycle> findByGroupIdOrderByCollectionStartDateDesc(UUID groupId);
    
    /**
     * Find active cycles (ACTIVE, COLLECTING, COLLECTION_COMPLETE)
     */
    @Query("SELECT gc FROM GroupCycle gc WHERE gc.status IN ('active', 'collecting', 'collection_complete')")
    List<GroupCycle> findActiveCycles();
    
    /**
     * Find cycles ending within specified date range
     */
    List<GroupCycle> findByCollectionEndDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find cycles due for payout
     */
    @Query("SELECT gc FROM GroupCycle gc WHERE gc.status = 'collection_complete' AND gc.payoutDate <= :date")
    List<GroupCycle> findCyclesDueForPayout(@Param("date") LocalDate date);
    
    /**
     * Find cycles by period
     */
    List<GroupCycle> findByCyclePeriod(String cyclePeriod);

    /*
     * Find cycles by group id for a specific period(week, month, etc)
     */
    Optional<GroupCycle> findByGroupIdAndCyclePeriod(UUID groupId, String cyclePeriod);
    
    /**
     * Get cycle statistics by group
     */
    @Query("SELECT gc.status, COUNT(gc) FROM GroupCycle gc WHERE gc.groupId = :groupId GROUP BY gc.status")
    List<Object[]> getCycleStatisticsByGroup(@Param("groupId") UUID groupId);
    
    /**
     * Find cycles where current total is less than expected total
     */
    @Query("SELECT gc FROM GroupCycle gc WHERE gc.currentTotal < gc.expectedTotal AND gc.status IN ('active', 'collecting')")
    List<GroupCycle> findUnderFundedCycles();
}