package com.stockfellow.adminservice.scheduled;

import com.stockfellow.adminservice.model.DailyMetrics;
import com.stockfellow.adminservice.repository.DailyMetricsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    private DailyMetricsRepository metricsRepository;

    @Autowired
    private RestTemplate restTemplate;

    // Run every hour to aggregate daily metrics
    @Scheduled(cron = "0 0 * * * *")
    public void aggregateDailyMetrics() {
        try {
            logger.info("Starting daily metrics aggregation");
            
            LocalDate today = LocalDate.now();
            Optional<DailyMetrics> existingMetrics = metricsRepository.findById(today);
            
            DailyMetrics metrics = existingMetrics.orElse(new DailyMetrics(today));
            
            // Update metrics from other services
            updateUserMetrics(metrics);
            updateGroupMetrics(metrics);
            updateTransactionMetrics(metrics);
            
            metricsRepository.save(metrics); // Fixed: changed from dailyMetricsRepository to metricsRepository
            logger.info("Daily metrics aggregated successfully for date: {}", today);
            
        } catch (Exception e) {
            logger.error("Error during daily metrics aggregation: {}", e.getMessage(), e);
        }
    }

    private void updateUserMetrics(DailyMetrics metrics) {
        try {
            Map<String, Object> userStats = restTemplate.getForObject(
                "http://user-service:4020/api/users/stats", Map.class);
                
            if (userStats != null) {
                metrics.setActiveUsers(getLongValue(userStats, "totalUsers"));
                // For new users, we'd need a separate endpoint that tracks daily registrations
                // This is a simplified version
                metrics.setNewUsers(getLongValue(userStats, "newUsersToday"));
            }
        } catch (Exception e) {
            logger.error("Error updating user metrics: {}", e.getMessage());
        }
    }

    private void updateGroupMetrics(DailyMetrics metrics) {
        try {
            Map<String, Object> groupStats = restTemplate.getForObject(
                "http://group-service:4040/api/groups/admin/analytics", Map.class);
                
            if (groupStats != null) {
                // This would need to be implemented in the group service
                metrics.setNewGroups(getLongValue(groupStats, "newGroupsToday"));
            }
        } catch (Exception e) {
            logger.error("Error updating group metrics: {}", e.getMessage());
        }
    }

    private void updateTransactionMetrics(DailyMetrics metrics) {
        try {
            Map<String, Object> transactionStats = restTemplate.getForObject(
                "http://transaction-service:4080/api/transactions/admin/analytics", Map.class);
                
            if (transactionStats != null) {
                metrics.setTotalTransactions(getLongValue(transactionStats, "totalTransactions"));
                metrics.setTransactionVolume(getBigDecimalValue(transactionStats, "totalVolume"));
            }
        } catch (Exception e) {
            logger.error("Error updating transaction metrics: {}", e.getMessage());
        }
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }

    private BigDecimal getBigDecimalValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return BigDecimal.ZERO;
    }
}