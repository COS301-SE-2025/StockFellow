package com.stockfellow.adminservice.service;

import com.stockfellow.adminservice.model.AuditLog;
import com.stockfellow.adminservice.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public AuditLog createAuditLog(AuditLog auditLog) {
        // Calculate risk score before saving
        auditLog.setRiskScore(calculateRiskScore(auditLog));
        return auditLogRepository.save(auditLog);
    }

    public Page<AuditLog> getAuditLogs(String userId, String endpoint, LocalDateTime startDate, 
                                       LocalDateTime endDate, boolean flaggedOnly, Pageable pageable) {
        return auditLogRepository.findAuditLogsWithFilters(
            userId, endpoint, startDate, endDate, flaggedOnly, pageable
        );
    }

    public List<AuditLog> getSuspiciousActivity() {
        return auditLogRepository.findByRiskScoreGreaterThanEqualOrderByRiskScoreDesc(70, null)
                .getContent();
    }

    public List<AuditLog> getUserActivity(String userId) {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        return auditLogRepository.findByUserIdAndTimestampBetweenOrderByTimestampDesc(
            userId, oneMonthAgo, LocalDateTime.now()
        );
    }

    public void markForInvestigation(String logId, String reason) {
        Optional<AuditLog> logOpt = auditLogRepository.findById(logId);
        if (logOpt.isPresent()) {
            AuditLog log = logOpt.get();
            log.setFlaggedForReview(true);
            log.setRiskFactors(log.getRiskFactors() + "; MANUAL_REVIEW: " + reason);
            auditLogRepository.save(log);
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

        auditLog.setRiskFactors(String.join(", ", riskFactors));
        return score;
    }

    private boolean isOffHoursActivity(LocalDateTime timestamp) {
        int hour = timestamp.getHour();
        return hour < 6 || hour > 22;
    }

    private boolean isSuspiciousEndpoint(String endpoint) {
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
        long recentRequests = auditLogRepository.findByUserIdAndTimestampBetweenOrderByTimestampDesc(
            userId, oneHourAgo, LocalDateTime.now()
        ).size();
        
        return recentRequests > 50; // More than 50 requests per hour
    }
}