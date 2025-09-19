package com.stockfellow.adminservice.controller;

import com.stockfellow.adminservice.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class AdminAnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(AdminAnalyticsController.class);

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardData(@RequestParam(required = false, defaultValue = "7d") String timeRange) {
        try {
            logger.info("Fetching dashboard data for timeRange: {}", timeRange);
            Map<String, Object> dashboardData = analyticsService.getDashboardData(timeRange);
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            logger.error("Error fetching dashboard data: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal server error",
                "message", "Failed to fetch dashboard data"
            ));
        }
    }

    @GetMapping("/users/stats")
    public ResponseEntity<?> getUserStats() {
        try {
            logger.info("Fetching user statistics");
            Map<String, Object> userStats = analyticsService.getUserMetrics(java.time.LocalDate.now().minusDays(30));
            return ResponseEntity.ok(userStats);
        } catch (Exception e) {
            logger.error("Error fetching user stats: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal server error",
                "message", "Failed to fetch user statistics"
            ));
        }
    }

    @GetMapping("/groups/stats")
    public ResponseEntity<?> getGroupStats() {
        try {
            logger.info("Fetching group statistics");
            Map<String, Object> groupStats = analyticsService.getGroupMetrics(java.time.LocalDate.now().minusDays(30));
            return ResponseEntity.ok(groupStats);
        } catch (Exception e) {
            logger.error("Error fetching group stats: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal server error",
                "message", "Failed to fetch group statistics"
            ));
        }
    }

    @GetMapping("/transactions/stats")
    public ResponseEntity<?> getTransactionStats() {
        try {
            logger.info("Fetching transaction statistics");
            Map<String, Object> transactionStats = analyticsService.getTransactionMetrics(java.time.LocalDate.now().minusDays(30));
            return ResponseEntity.ok(transactionStats);
        } catch (Exception e) {
            logger.error("Error fetching transaction stats: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal server error",
                "message", "Failed to fetch transaction statistics"
            ));
        }
    }

    @GetMapping("/revenue")
    public ResponseEntity<?> getRevenueAnalytics(@RequestParam(defaultValue = "30d") String period) {
        try {
            logger.info("Fetching revenue analytics for period: {}", period);
            Map<String, Object> revenueData = analyticsService.getRevenueAnalytics(period);
            return ResponseEntity.ok(revenueData);
        } catch (Exception e) {
            logger.error("Error fetching revenue analytics: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal server error",
                "message", "Failed to fetch revenue analytics"
            ));
        }
    }
}