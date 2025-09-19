package com.stockfellow.adminservice.controller;

import com.stockfellow.adminservice.service.AnalyticsService;
import com.stockfellow.adminservice.service.AuditLogService;
import com.stockfellow.adminservice.service.RequestReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class AdminDashboardController {

    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardController.class);

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private RequestReviewService requestReviewService;

    @GetMapping("/dashboard/summary")
    public ResponseEntity<?> getAdminDashboard() {
        try {
            logger.info("Fetching consolidated admin dashboard summary");
            
            Map<String, Object> dashboard = new HashMap<>();
            
            // Get analytics data
            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
            dashboard.put("userMetrics", analyticsService.getUserMetrics(thirtyDaysAgo));
            dashboard.put("groupMetrics", analyticsService.getGroupMetrics(thirtyDaysAgo));
            dashboard.put("transactionMetrics", analyticsService.getTransactionMetrics(thirtyDaysAgo));
            
            // Get recent activity and alerts
            dashboard.put("recentSuspiciousActivity", auditLogService.getSuspiciousActivity());
            dashboard.put("pendingRequestsCount", requestReviewService.getPendingRequests(null, null).getTotalElements());
            dashboard.put("staleRequests", requestReviewService.getStaleRequests());
            
            // Revenue analytics
            dashboard.put("revenueData", analyticsService.getRevenueAnalytics("30d"));
            
            dashboard.put("generatedAt", java.time.LocalDateTime.now());
            dashboard.put("dataFreshness", "Real-time");
            
            return ResponseEntity.ok(dashboard);
            
        } catch (Exception e) {
            logger.error("Error fetching admin dashboard summary: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal server error",
                "message", "Failed to fetch dashboard summary"
            ));
        }
    }

    @GetMapping
    public ResponseEntity<?> getServiceInfo() {
        return ResponseEntity.ok(Map.of(
            "service", "Admin Service",
            "version", "1.0.0",
            "description", "Administrative functions for StockFellow platform",
            "endpoints", Map.of(
                "dashboard", "/api/admin/dashboard/summary",
                "analytics", "/api/admin/analytics/*",
                "audit", "/api/admin/audit/*",
                "requests", "/api/admin/requests/*"
            )
        ));
    }
}
