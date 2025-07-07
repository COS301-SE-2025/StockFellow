package com.stockfellow.transactionservice.repository;

import com.stockfellow.transactionservice.model.GroupCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupCycleRepository extends JpaRepository<GroupCycle, UUID> {

    // Find next upcoming cycle (>= date)
    Optional<GroupCycle> findFirstByStatusAndCollectionDateGreaterThanEqualOrderByCollectionDateAsc(String status,
            LocalDate date); // would probs use this one

    List<GroupCycle> findFirstByGroupIdOrderByCollectionDateAsc(UUID groupId);

    // Find cycles by group
    List<GroupCycle> findByGroupIdOrderByCollectionDateDesc(UUID groupId);

    // Find cycle by group and month
    Optional<GroupCycle> findByGroupIdAndCycleMonth(UUID groupId, String cycleMonth);

    // Find cycles by status
    List<GroupCycle> findByStatus(String status);

    List<GroupCycle> findByStatusAndCollectionDateLessThanEqual(String status, LocalDate collectionDate);

    // Finds the next upcoming cycle for a group by status, sorted by earliest
    // collection date
    Optional<GroupCycle> findFirstByGroupIdAndStatusOrderByCollectionDateAsc(UUID groupId, String status);

}
