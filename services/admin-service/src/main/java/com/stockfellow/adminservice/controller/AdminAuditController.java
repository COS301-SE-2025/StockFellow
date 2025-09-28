package com.stockfellow.adminservice.controller;

import com.stockfellow.adminservice.model.AuditLog;
import com.stockfellow.adminservice.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/audit")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class AdminAuditController {

    private static final Logger logger = LoggerFactory.getLogger(AdminAuditController.class);

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/logs")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String endpoint,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "false") boolean flaggedOnly,
            Pageable pageable) {
        
        try {
            logger.info("Fetching audit logs with filters - userId: {}, endpoint: {}, flaggedOnly: {}", 
                       userId, endpoint, flaggedOnly);
            
            Page<AuditLog> auditLogs = auditLogService.getAuditLogs(
                userId, endpoint, startDate, endDate, flaggedOnly, pageable
            );
            
            logger.info("Retrieved {} audit logs", auditLogs.getTotalElements());
            return ResponseEntity.ok(auditLogs);
            
        } catch (Exception e) {
            logger.error("Error fetching audit logs: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Page.empty());
        }
    }

    @GetMapping("/fraud/suspicious")
    public ResponseEntity<?> getSuspiciousActivity() {
        try {
            logger.info("Fetching suspicious activity logs");
            List<AuditLog> suspiciousLogs = auditLogService.getSuspiciousActivity();
            
            return ResponseEntity.ok(Map.of(
                "suspiciousActivities", suspiciousLogs,
                "count", suspiciousLogs.size(),
                "generatedAt", LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            logger.error("Error fetching suspicious activities: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal server error",
                "message", "Failed to fetch suspicious activities"
            ));
        }
    }

    @PostMapping("/fraud/investigate")
    public ResponseEntity<?> markForInvestigation(@RequestBody InvestigationRequest request) {
        try {
            if (request.getLogId() == null || request.getReason() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid request",
                    "message", "Log ID and reason are required"
                ));
            }

            logger.info("Marking log {} for investigation with reason: {}", 
                       request.getLogId(), request.getReason());
            
            // Convert String to UUID
            UUID logId = UUID.fromString(request.getLogId());
            auditLogService.markForInvestigation(logId, request.getReason());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Log marked for investigation successfully",
                "logId", request.getLogId()
            ));
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format: {}", request.getLogId());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Invalid log ID format",
                "message", "Log ID must be a valid UUID"
            ));
        } catch (Exception e) {
            logger.error("Error marking log for investigation: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal server error",
                "message", "Failed to mark log for investigation"
            ));
        }
    }

    @GetMapping("/user/{userId}/activity")
    public ResponseEntity<?> getUserActivity(@PathVariable String userId) {
        try {
            logger.info("Fetching activity for user: {}", userId);
            List<AuditLog> userActivity = auditLogService.getUserActivity(userId);
            
            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "activities", userActivity,
                "count", userActivity.size(),
                "period", "Last 30 days"
            ));
            
        } catch (Exception e) {
            logger.error("Error fetching user activity for {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal server error",
                "message", "Failed to fetch user activity"
            ));
        }
    }

    // DTO for investigation request
    public static class InvestigationRequest {
        private String logId;
        private String reason;

        // Getters and setters
        public String getLogId() { return logId; }
        public void setLogId(String logId) { this.logId = logId; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}