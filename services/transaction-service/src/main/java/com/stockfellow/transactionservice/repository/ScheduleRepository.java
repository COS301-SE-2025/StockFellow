package com.stockfellow.transactionservice.repository;

import com.stockfellow.transactionservice.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, String> {
    List<Schedule> findByNextRunAndStatus(LocalDate nextRun, String status);
}
