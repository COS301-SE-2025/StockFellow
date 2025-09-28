package com.stockfellow.adminservice.service;

import com.stockfellow.adminservice.model.AuditLog;
import com.stockfellow.adminservice.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void createAuditLog(AuditLog auditLog) {
        try {
            // Ensure timestamp is set
            if (auditLog.getTimestamp() == null) {
                auditLog.setTimestamp(LocalDateTime.now());
            }
            
            // Validate required fields
            if (auditLog.getEndpoint() == null || auditLog.getHttpMethod() == null) {
                logger.warn("Skipping audit log creation - missing required fields");
                return;
            }
            
            // Calculate risk score before saving
            Integer riskScore = calculateRiskScore(auditLog);
            auditLog.setRiskScore(riskScore);
            
            auditLogRepository.save(auditLog);
            logger.debug("Audit log created successfully for endpoint: {}", auditLog.getEndpoint());
            
        } catch (Exception e) {
            logger.error("Failed to create audit log: {}", e.getMessage());
            // Don't rethrow - we don't want audit logging failures to break main functionality
        }
    }

     // FIXED: Updated method to handle date parameters properly
    public Page<AuditLog> getAuditLogs(String userId, String endpoint, LocalDateTime startDate, 
                                       LocalDateTime endDate, boolean flaggedOnly, Pageable pageable) {
        
        // If both start and end dates are provided, use the date filtering method
        if (startDate != null && endDate != null) {
            logger.debug("Using date filtering query with startDate: {}, endDate: {}", startDate, endDate);
            return auditLogRepository.findAuditLogsWithDateFilters(
                userId, endpoint, startDate, endDate, flaggedOnly, pageable
            );
        }
        
        // Otherwise use the simpler method without date constraints
        logger.debug("Using basic filtering query without date constraints");
        return auditLogRepository.findAuditLogsWithFilters(
            userId, endpoint, flaggedOnly, pageable
        );
    }

    public List<AuditLog> getSuspiciousActivity() {
        // Use a pageable with a reasonable size for suspicious activities
        Pageable pageable = PageRequest.of(0, 100);
        return auditLogRepository.findByRiskScoreGreaterThanEqualOrderByRiskScoreDesc(70, pageable)
                .getContent();
    }

    public List<AuditLog> getUserActivity(String userId) {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        return auditLogRepository.findByUserIdAndTimestampBetweenOrderByTimestampDesc(
            userId, oneMonthAgo, LocalDateTime.now()
        );
    }

    public void markForInvestigation(UUID logId, String reason) {
        Optional<AuditLog> logOpt = auditLogRepository.findById(logId);
        if (logOpt.isPresent()) {
            AuditLog log = logOpt.get();
            log.setFlaggedForReview(true);
            String currentRiskFactors = log.getRiskFactors() != null ? log.getRiskFactors() : "";
            log.setRiskFactors(currentRiskFactors + "; MANUAL_REVIEW: " + reason);
            auditLogRepository.save(log);
            logger.info("Audit log {} marked for investigation: {}", logId, reason);
        }
    }

    private Integer calculateRiskScore(AuditLog auditLog) {
        int score = 0;
        List<String> riskFactors = new ArrayList<>();

        // Check for suspicious patterns
        if (isOffHoursActivity(auditLog.getTimestamp())) {
            score += 10;
            riskFactors.add("OFF_HOURS_ACTIVITY");
        }

        if (isSuspiciousEndpoint(auditLog.getEndpoint())) {
            score += 20;
            riskFactors.add("SENSITIVE_ENDPOINT");
        }

        if (isUnusualUserAgent(auditLog.getUserAgent())) {
            score += 15;
            riskFactors.add("UNUSUAL_USER_AGENT");
        }

        if (isHighFrequencyUser(auditLog.getUserId())) {
            score += 25;
            riskFactors.add("HIGH_FREQUENCY_ACCESS");
        }

        if (!riskFactors.isEmpty()) {
            auditLog.setRiskFactors(String.join(", ", riskFactors));
        }
        return score;
    }

    private boolean isOffHoursActivity(LocalDateTime timestamp) {
        int hour = timestamp.getHour();
        return hour < 6 || hour > 22;
    }

    private boolean isSuspiciousEndpoint(String endpoint) {
        if (endpoint == null) return false;
        
        String[] sensitiveEndpoints = {"/admin/", "/users/stats", "/affordability/", "/verifyID"};
        return Arrays.stream(sensitiveEndpoints)
                .anyMatch(endpoint::contains);
    }

    private boolean isUnusualUserAgent(String userAgent) {
        if (userAgent == null) return true;
        
        String[] normalAgents = {"Mozilla", "Chrome", "Safari", "Firefox"};
        return Arrays.stream(normalAgents)
                .noneMatch(userAgent::contains);
    }

    private boolean isHighFrequencyUser(String userId) {
        if (userId == null) return false;
        
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        // Use the existing repository method with a custom query
        List<AuditLog> recentLogs = auditLogRepository.findByUserIdAndTimestampBetweenOrderByTimestampDesc(
            userId, oneHourAgo, LocalDateTime.now()
        );
        
        return recentLogs.size() > 50; // More than 50 requests per hour
    }

    public void flagForInvestigation(String logIdString, String reason) {
        try {
            // Convert string to UUID properly
            UUID logId = UUID.fromString(logIdString);
            
            logger.info("Flagging log {} for investigation: {}", logId, reason);
            
            AuditLog log = auditLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("Audit log not found: " + logId));
            
            log.setFlaggedForReview(true);
            String currentRiskFactors = log.getRiskFactors() != null ? log.getRiskFactors() : "";
            log.setRiskFactors(currentRiskFactors + " | Investigation: " + reason);
            
            auditLogRepository.save(log);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format for log ID: {}", logIdString);
            throw new IllegalArgumentException("Invalid log ID format", e);
        } catch (Exception e) {
            logger.error("Error flagging log for investigation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to flag log for investigation", e);
        }
    }
    
    // Overloaded method that accepts UUID directly
    public void flagForInvestigation(UUID logId, String reason) {
        logger.info("Flagging log {} for investigation: {}", logId, reason);
        
        AuditLog log = auditLogRepository.findById(logId)
            .orElseThrow(() -> new IllegalArgumentException("Audit log not found: " + logId));
        
        log.setFlaggedForReview(true);
        String currentRiskFactors = log.getRiskFactors() != null ? log.getRiskFactors() : "";
        log.setRiskFactors(currentRiskFactors + " | Investigation: " + reason);
        
        auditLogRepository.save(log);
    }
}