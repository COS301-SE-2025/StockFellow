package com.stockfellow.adminservice.service;

import com.stockfellow.adminservice.model.DailyMetrics;
import com.stockfellow.adminservice.repository.DailyMetricsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class AnalyticsService {

    @Autowired
    private DailyMetricsRepository metricsRepository;

    @Autowired
    private RestTemplate restTemplate;

    public Map<String, Object> getDashboardData(String timeRange) {
        LocalDate startDate = calculateStartDate(timeRange);
        LocalDate today = LocalDate.now();

        Map<String, Object> dashboard = new HashMap<>();
        
        // Get basic metrics
        dashboard.put("userMetrics", getUserMetrics(startDate));
        dashboard.put("groupMetrics", getGroupMetrics(startDate));
        dashboard.put("transactionMetrics", getTransactionMetrics(startDate));
        dashboard.put("growthTrends", getGrowthTrends(startDate));
        dashboard.put("timeRange", timeRange);
        dashboard.put("generatedAt", new Date());

        return dashboard;
    }

    public Map<String, Object> getUserMetrics(LocalDate startDate) {
        Map<String, Object> userMetrics = new HashMap<>();

        try {
            // Get user statistics from user service
            ResponseEntity<Map> userStatsResponse = restTemplate.getForEntity(
                "http://user-service:4020/api/users/stats", 
                Map.class
            );
            
            if (userStatsResponse.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> userStats = userStatsResponse.getBody();
                userMetrics.putAll(userStats);
            }

            // Get growth data from daily metrics
            Long newUsers = metricsRepository.getTotalNewUsersAfter(startDate);
            Double avgActiveUsers = metricsRepository.getAverageActiveUsersAfter(startDate);

            userMetrics.put("newUsersInPeriod", newUsers != null ? newUsers : 0L);
            userMetrics.put("averageActiveUsers", avgActiveUsers != null ? avgActiveUsers.intValue() : 0);

            // Calculate growth rate
            List<DailyMetrics> growthData = metricsRepository.getMetricsForGrowthCalculation(startDate);
            userMetrics.put("growthRate", calculateGrowthRate(growthData, "users"));

        } catch (Exception e) {
            userMetrics.put("error", "Failed to fetch user metrics");
            userMetrics.put("totalUsers", 0);
            userMetrics.put("verifiedUsers", 0);
        }

        return userMetrics;
    }

    public Map<String, Object> getGroupMetrics(LocalDate startDate) {
        Map<String, Object> groupMetrics = new HashMap<>();

        try {
            // Get group statistics from group service
            ResponseEntity<Map> groupStatsResponse = restTemplate.getForEntity(
                "http://group-service:4040/api/groups/admin/analytics", 
                Map.class
            );

            if (groupStatsResponse.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> groupStats = groupStatsResponse.getBody();
                groupMetrics.putAll(groupStats);
            }

            // Get growth data from daily metrics
            Long newGroups = metricsRepository.getTotalNewGroupsAfter(startDate);
            groupMetrics.put("newGroupsInPeriod", newGroups != null ? newGroups : 0L);

        } catch (Exception e) {
            groupMetrics.put("error", "Failed to fetch group metrics");
            groupMetrics.put("totalGroups", 0);
            groupMetrics.put("activeGroups", 0);
        }

        return groupMetrics;
    }

    public Map<String, Object> getTransactionMetrics(LocalDate startDate) {
        Map<String, Object> transactionMetrics = new HashMap<>();

        try {
            // Get transaction statistics from transaction service
            ResponseEntity<Map> transactionStatsResponse = restTemplate.getForEntity(
                "http://transaction-service:4080/api/transactions/admin/analytics", 
                Map.class
            );

            if (transactionStatsResponse.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> transactionStats = transactionStatsResponse.getBody();
                transactionMetrics.putAll(transactionStats);
            }

            // Get volume data from daily metrics
            BigDecimal totalVolume = metricsRepository.getTotalTransactionVolumeAfter(startDate);
            transactionMetrics.put("volumeInPeriod", totalVolume != null ? totalVolume : BigDecimal.ZERO);

        } catch (Exception e) {
            transactionMetrics.put("error", "Failed to fetch transaction metrics");
            transactionMetrics.put("totalTransactions", 0);
            transactionMetrics.put("successfulTransactions", 0);
        }

        return transactionMetrics;
    }

    public Map<String, Object> getRevenueAnalytics(String period) {
        LocalDate startDate = calculateStartDate(period);
        List<DailyMetrics> metrics = metricsRepository.findByDateBetweenOrderByDateDesc(
            startDate, LocalDate.now()
        );

        Map<String, Object> revenue = new HashMap<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        
        for (DailyMetrics metric : metrics) {
            // Assuming 2% fee on transaction volume
            BigDecimal dailyRevenue = metric.getTransactionVolume().multiply(new BigDecimal("0.02"));
            totalRevenue = totalRevenue.add(dailyRevenue);
        }

        revenue.put("totalRevenue", totalRevenue);
        revenue.put("period", period);
        revenue.put("projectedMonthly", totalRevenue.multiply(new BigDecimal("30")).divide(new BigDecimal(metrics.size())));
        revenue.put("dailyBreakdown", formatDailyRevenue(metrics));

        return revenue;
    }

    public List<Map<String, Object>> getGrowthTrends(LocalDate startDate) {
        List<DailyMetrics> metrics = metricsRepository.findByDateBetweenOrderByDateDesc(
            startDate, LocalDate.now()
        );

        List<Map<String, Object>> trends = new ArrayList<>();
        for (DailyMetrics metric : metrics) {
            Map<String, Object> dailyTrend = new HashMap<>();
            dailyTrend.put("date", metric.getDate());
            dailyTrend.put("newUsers", metric.getNewUsers());
            dailyTrend.put("activeUsers", metric.getActiveUsers());
            dailyTrend.put("newGroups", metric.getNewGroups());
            dailyTrend.put("transactions", metric.getTotalTransactions());
            dailyTrend.put("volume", metric.getTransactionVolume());
            trends.add(dailyTrend);
        }

        return trends;
    }

    private LocalDate calculateStartDate(String timeRange) {
        LocalDate today = LocalDate.now();
        switch (timeRange != null ? timeRange.toLowerCase() : "7d") {
            case "24h": return today.minusDays(1);
            case "7d": return today.minusDays(7);
            case "30d": return today.minusDays(30);
            case "90d": return today.minusDays(90);
            case "1y": return today.minusYears(1);
            default: return today.minusDays(7);
        }
    }

    private double calculateGrowthRate(List<DailyMetrics> data, String metric) {
        if (data.size() < 2) return 0.0;

        DailyMetrics first = data.get(0);
        DailyMetrics last = data.get(data.size() - 1);

        long initialValue = metric.equals("users") ? first.getNewUsers() : first.getNewGroups();
        long finalValue = metric.equals("users") ? last.getNewUsers() : last.getNewGroups();

        if (initialValue == 0) return 0.0;
        return ((double) (finalValue - initialValue) / initialValue) * 100;
    }

    private List<Map<String, Object>> formatDailyRevenue(List<DailyMetrics> metrics) {
        List<Map<String, Object>> dailyRevenue = new ArrayList<>();
        for (DailyMetrics metric : metrics) {
            Map<String, Object> daily = new HashMap<>();
            daily.put("date", metric.getDate());
            daily.put("revenue", metric.getTransactionVolume().multiply(new BigDecimal("0.02")));
            dailyRevenue.add(daily);
        }
        return dailyRevenue;
    }
}