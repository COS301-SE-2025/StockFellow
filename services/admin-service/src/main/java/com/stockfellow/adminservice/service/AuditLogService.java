package com.stockfellow.adminservice.service;

import com.stockfellow.adminservice.model.AuditLog;
import com.stockfellow.adminservice.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    public AuditLog createAuditLog(AuditLog auditLog) {
        try {
            // Calculate risk score before saving
            auditLog.setRiskScore(calculateRiskScore(auditLog));
            return auditLogRepository.save(auditLog);
        } catch (Exception e) {
            logger.error("Error creating audit log: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create audit log", e);
        }
    }

    public Page<AuditLog> getAuditLogs(String userId, String endpoint, LocalDateTime startDate, 
                                       LocalDateTime endDate, boolean flaggedOnly, Pageable pageable) {
        try {
            // Check if repository method exists, if not return empty page with proper structure
            if (auditLogRepository == null) {
                return createEmptyPage(pageable);
            }
            
            // Try to call the repository method, catch any exceptions
            return auditLogRepository.findAuditLogsWithFilters(
                userId, endpoint, startDate, endDate, flaggedOnly, pageable
            );
        } catch (Exception e) {
            logger.error("Error fetching audit logs: {}", e.getMessage(), e);
            // Return empty page instead of throwing exception
            return createEmptyPage(pageable);
        }
    }

    public List<AuditLog> getSuspiciousActivity() {
        try {
            if (auditLogRepository == null) {
                return new ArrayList<>();
            }
            
            // Try to find suspicious activities, return empty list if method doesn't exist
            Page<AuditLog> suspiciousPage = auditLogRepository.findByRiskScoreGreaterThanEqualOrderByRiskScoreDesc(70, null);
            return suspiciousPage != null ? suspiciousPage.getContent() : new ArrayList<>();
        } catch (Exception e) {
            logger.error("Error fetching suspicious activities: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<AuditLog> getUserActivity(String userId) {
        try {
            if (auditLogRepository == null) {
                return new ArrayList<>();
            }
            
            LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
            return auditLogRepository.findByUserIdAndTimestampBetweenOrderByTimestampDesc(
                userId, oneMonthAgo, LocalDateTime.now()
            );
        } catch (Exception e) {
            logger.error("Error fetching user activity for {}: {}", userId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public void markForInvestigation(String logId, String reason) {
        try {
            if (auditLogRepository == null) {
                throw new RuntimeException("Audit repository not available");
            }
            
            Optional<AuditLog> logOpt = auditLogRepository.findById(logId);
            if (logOpt.isPresent()) {
                AuditLog log = logOpt.get();
                log.setFlaggedForReview(true);
                log.setRiskFactors(log.getRiskFactors() + "; MANUAL_REVIEW: " + reason);
                auditLogRepository.save(log);
            } else {
                throw new RuntimeException("Audit log not found with ID: " + logId);
            }
        } catch (Exception e) {
            logger.error("Error marking log for investigation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to mark log for investigation: " + e.getMessage());
        }
    }

    private Page<AuditLog> createEmptyPage(Pageable pageable) {
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }

    private Integer calculateRiskScore(AuditLog auditLog) {
        try {
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
        } catch (Exception e) {
            logger.error("Error calculating risk score: {}", e.getMessage(), e);
            return 0;
        }
    }

    private boolean isOffHoursActivity(LocalDateTime timestamp) {
        try {
            if (timestamp == null) return false;
            int hour = timestamp.getHour();
            return hour < 6 || hour > 22;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isSuspiciousEndpoint(String endpoint) {
        try {
            if (endpoint == null) return false;
            String[] sensitiveEndpoints = {"/admin/", "/users/stats", "/affordability/", "/verifyID"};
            return Arrays.stream(sensitiveEndpoints)
                    .anyMatch(endpoint::contains);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isUnusualUserAgent(String userAgent) {
        try {
            if (userAgent == null) return true;
            
            String[] normalAgents = {"Mozilla", "Chrome", "Safari", "Firefox"};
            return Arrays.stream(normalAgents)
                    .noneMatch(userAgent::contains);
        } catch (Exception e) {
            return true;
        }
    }

    private boolean isHighFrequencyUser(String userId) {
        try {
            if (userId == null || auditLogRepository == null) return false;
            
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            long recentRequests = auditLogRepository.findByUserIdAndTimestampBetweenOrderByTimestampDesc(
                userId, oneHourAgo, LocalDateTime.now()
            ).size();
            
            return recentRequests > 50; // More than 50 requests per hour
        } catch (Exception e) {
            logger.error("Error checking high frequency user: {}", e.getMessage(), e);
            return false;
        }
    }
}