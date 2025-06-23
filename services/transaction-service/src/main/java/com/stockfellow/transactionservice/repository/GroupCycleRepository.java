package com.stockfellow.transactionservice.repository;

import com.stockfellow.transactionservice.model.GroupCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface GroupCycleRepository extends JpaRepository<GroupCycle, String> {
    List<GroupCycle> findByNextRunAndStatus(LocalDate collectionDate, String status);
}
