package com.stockfellow.adminservice.repository;

import com.stockfellow.adminservice.model.DailyMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyMetricsRepository extends JpaRepository<DailyMetrics, LocalDate> {
    
    // Find metrics within date range
    List<DailyMetrics> findByDateBetweenOrderByDateDesc(LocalDate startDate, LocalDate endDate);
    
    // Get latest metrics
    Optional<DailyMetrics> findTopByOrderByDateDesc();
    
    // Aggregation queries for dashboard
    @Query("""
        SELECT SUM(dm.newUsers) 
        FROM DailyMetrics dm 
        WHERE dm.date >= :startDate
        """)
    Long getTotalNewUsersAfter(@Param("startDate") LocalDate startDate);
    
    @Query("""
        SELECT SUM(dm.newGroups) 
        FROM DailyMetrics dm 
        WHERE dm.date >= :startDate
        """)
    Long getTotalNewGroupsAfter(@Param("startDate") LocalDate startDate);
    
    @Query("""
        SELECT SUM(dm.transactionVolume) 
        FROM DailyMetrics dm 
        WHERE dm.date >= :startDate
        """)
    BigDecimal getTotalTransactionVolumeAfter(@Param("startDate") LocalDate startDate);
    
    @Query("""
        SELECT AVG(dm.activeUsers) 
        FROM DailyMetrics dm 
        WHERE dm.date >= :startDate
        """)
    Double getAverageActiveUsersAfter(@Param("startDate") LocalDate startDate);
    
    // Growth rate calculation
    @Query("""
        SELECT dm FROM DailyMetrics dm 
        WHERE dm.date >= :startDate 
        ORDER BY dm.date ASC
        """)
    List<DailyMetrics> getMetricsForGrowthCalculation(@Param("startDate") LocalDate startDate);
}
