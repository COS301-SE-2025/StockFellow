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
import java.time.DayOfWeek;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    // Cache for tracking request frequencies (userId -> timestamp list)
    private final Map<String, List<LocalDateTime>> requestCache = new ConcurrentHashMap<>();
    
    // Cache for tracking IP addresses per user
    private final Map<String, Set<String>> userIpCache = new ConcurrentHashMap<>();

    public void createAuditLog(AuditLog auditLog) {
        try {
            if (auditLog.getTimestamp() == null) {
                auditLog.setTimestamp(LocalDateTime.now());
            }
            
            if (auditLog.getEndpoint() == null || auditLog.getHttpMethod() == null) {
                logger.warn("Skipping audit log creation - missing required fields");
                return;
            }
            
            // Calculate comprehensive risk score
            Integer riskScore = calculateRiskScore(auditLog);
            auditLog.setRiskScore(riskScore);
            
            // Auto-flag high-risk activities
            if (riskScore >= 70) {
                auditLog.setFlaggedForReview(true);
                logger.warn("High-risk activity detected: userId={}, endpoint={}, riskScore={}, ip={}", 
                           auditLog.getUserId(), auditLog.getEndpoint(), riskScore, auditLog.getIpAddress());
            }
            
            auditLogRepository.save(auditLog);
            logger.debug("Audit log created: endpoint={}, riskScore={}", 
                        auditLog.getEndpoint(), riskScore);
            
        } catch (Exception e) {
            logger.error("Failed to create audit log: {}", e.getMessage());
        }
    }

    public Page<AuditLog> getAuditLogs(String userId, String endpoint, LocalDateTime startDate, 
                                       LocalDateTime endDate, boolean flaggedOnly, Pageable pageable) {
        
        if (startDate != null && endDate != null) {
            logger.debug("Using date filtering: {} to {}", startDate, endDate);
            return auditLogRepository.findAuditLogsWithDateFilters(
                userId, endpoint, startDate, endDate, flaggedOnly, pageable
            );
        }
        
        logger.debug("Using basic filtering without date constraints");
        return auditLogRepository.findAuditLogsWithFilters(
            userId, endpoint, flaggedOnly, pageable
        );
    }

    public List<AuditLog> getSuspiciousActivity() {
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

        // 1. Time-based analysis (10 points)
        if (isOffHoursActivity(auditLog.getTimestamp())) {
            score += 10;
            riskFactors.add("OFF_HOURS_ACTIVITY");
        }

        // 2. Weekend activity check (5 points)
        if (isWeekendActivity(auditLog.getTimestamp())) {
            score += 5;
            riskFactors.add("WEEKEND_ACTIVITY");
        }

        // 3. Endpoint sensitivity (20 points)
        if (isSuspiciousEndpoint(auditLog.getEndpoint())) {
            score += 20;
            riskFactors.add("SENSITIVE_ENDPOINT");
        }

        // 4. User agent analysis (15 points)
        if (isUnusualUserAgent(auditLog.getUserAgent())) {
            score += 15;
            riskFactors.add("UNUSUAL_USER_AGENT");
        }

        // 5. Bot detection (20 points)
        if (isBotActivity(auditLog.getUserAgent())) {
            score += 20;
            riskFactors.add("BOT_DETECTED");
        }

        // 6. High frequency detection (25 points)
        if (isHighFrequencyUser(auditLog.getUserId(), auditLog.getTimestamp())) {
            score += 25;
            riskFactors.add("HIGH_FREQUENCY_ACCESS");
        }

        // 7. Multiple IP addresses (15 points)
        if (hasMultipleIPs(auditLog.getUserId(), auditLog.getIpAddress())) {
            score += 15;
            riskFactors.add("MULTIPLE_IP_ADDRESSES");
        }

        // 8. Suspicious IP patterns (10 points)
        if (isSuspiciousIP(auditLog.getIpAddress())) {
            score += 10;
            riskFactors.add("SUSPICIOUS_IP");
        }

        // 9. Failed request patterns (10 points)
        if (isFailedRequest(auditLog.getResponseStatus())) {
            score += 10;
            riskFactors.add("FAILED_REQUEST");
        }

        // 10. Anonymous user accessing sensitive endpoints (15 points)
        if (isAnonymousAccessToSensitiveEndpoint(auditLog.getUserId(), auditLog.getEndpoint())) {
            score += 15;
            riskFactors.add("ANONYMOUS_SENSITIVE_ACCESS");
        }

        if (!riskFactors.isEmpty()) {
            auditLog.setRiskFactors(String.join(", ", riskFactors));
        }
        
        // Cap the score at 100
        return Math.min(score, 100);
    }

    private boolean isOffHoursActivity(LocalDateTime timestamp) {
        int hour = timestamp.getHour();
        return hour < 6 || hour > 22;
    }

    private boolean isWeekendActivity(LocalDateTime timestamp) {
        DayOfWeek day = timestamp.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    private boolean isSuspiciousEndpoint(String endpoint) {
        if (endpoint == null) return false;
        
        String[] sensitiveEndpoints = {
            "/admin/", 
            "/users/stats", 
            "/affordability/", 
            "/verifyID",
            "/api/admin/",
            "/settings",
            "/delete",
            "/export",
            "/download"
        };
        
        return Arrays.stream(sensitiveEndpoints)
                .anyMatch(sensitive -> endpoint.toLowerCase().contains(sensitive.toLowerCase()));
    }

    private boolean isUnusualUserAgent(String userAgent) {
        if (userAgent == null || userAgent.trim().isEmpty()) return true;
        
        String[] normalAgents = {"Mozilla", "Chrome", "Safari", "Firefox", "Edge", "Opera"};
        return Arrays.stream(normalAgents)
                .noneMatch(userAgent::contains);
    }

    private boolean isBotActivity(String userAgent) {
        if (userAgent == null) return false;
        
        String lowerAgent = userAgent.toLowerCase();
        String[] botIndicators = {
            "bot", "crawler", "spider", "scraper", "curl", "wget", 
            "python", "java", "axios", "postman", "insomnia"
        };
        
        return Arrays.stream(botIndicators)
                .anyMatch(lowerAgent::contains);
    }

    private boolean isHighFrequencyUser(String userId, LocalDateTime currentTime) {
        if (userId == null || "anonymous".equals(userId)) return false;
        
        // Clean up old entries (older than 1 hour)
        LocalDateTime oneHourAgo = currentTime.minusHours(1);
        requestCache.entrySet().removeIf(entry -> 
            entry.getValue().stream().allMatch(time -> time.isBefore(oneHourAgo))
        );
        
        // Add current request
        requestCache.computeIfAbsent(userId, k -> new ArrayList<>()).add(currentTime);
        
        // Remove old timestamps for this user
        List<LocalDateTime> userRequests = requestCache.get(userId);
        userRequests.removeIf(time -> time.isBefore(oneHourAgo));
        
        // Check if user has made more than 50 requests in the last hour
        return userRequests.size() > 50;
    }

    private boolean hasMultipleIPs(String userId, String currentIp) {
        if (userId == null || "anonymous".equals(userId) || currentIp == null) {
            return false;
        }
        
        userIpCache.computeIfAbsent(userId, k -> new HashSet<>()).add(currentIp);
        
        Set<String> userIps = userIpCache.get(userId);
        
        // Flag if user has accessed from more than 3 different IPs in recent history
        return userIps.size() > 3;
    }

    private boolean isSuspiciousIP(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) return false;
        
        // Check for common VPN/Proxy IP ranges (simplified)
        // In production, use a proper IP reputation service
        String[] suspiciousPatterns = {
            "10.0.",      // Private network
            "172.16.",    // Private network
            "192.168.",   // Private network
            "0.0.0.0",    // Invalid
            "127.0.0.1"   // Localhost
        };
        
        for (String pattern : suspiciousPatterns) {
            if (ipAddress.startsWith(pattern)) {
                return true;
            }
        }
        
        return false;
    }

    private boolean isFailedRequest(String responseStatus) {
        if (responseStatus == null) return false;
        
        try {
            int status = Integer.parseInt(responseStatus);
            return status >= 400; // 4xx and 5xx errors
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isAnonymousAccessToSensitiveEndpoint(String userId, String endpoint) {
        boolean isAnonymous = userId == null || "anonymous".equals(userId) || userId.trim().isEmpty();
        boolean isSensitive = isSuspiciousEndpoint(endpoint);
        return isAnonymous && isSensitive;
    }

    public void flagForInvestigation(String logIdString, String reason) {
        try {
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
    
    public void flagForInvestigation(UUID logId, String reason) {
        logger.info("Flagging log {} for investigation: {}", logId, reason);
        
        AuditLog log = auditLogRepository.findById(logId)
            .orElseThrow(() -> new IllegalArgumentException("Audit log not found: " + logId));
        
        log.setFlaggedForReview(true);
        String currentRiskFactors = log.getRiskFactors() != null ? log.getRiskFactors() : "";
        log.setRiskFactors(currentRiskFactors + " | Investigation: " + reason);
        
        auditLogRepository.save(log);
    }

    /**
     * Get fraud analytics summary
     */
    public Map<String, Object> getFraudAnalytics() {
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        LocalDateTime last7Days = LocalDateTime.now().minusDays(7);
        
        Map<String, Object> analytics = new HashMap<>();
        
        // Total high-risk activities
        List<AuditLog> highRiskLogs = auditLogRepository.findRecentHighRiskActivity(last7Days);
        analytics.put("totalHighRiskActivities", highRiskLogs.size());
        
        // Unique users with suspicious activity
        long uniqueSuspiciousUsers = highRiskLogs.stream()
            .map(AuditLog::getUserId)
            .filter(userId -> userId != null && !"anonymous".equals(userId))
            .distinct()
            .count();
        analytics.put("uniqueSuspiciousUsers", uniqueSuspiciousUsers);
        
        // Flagged logs count
        long flaggedCount = auditLogRepository.countByFlaggedForReviewTrue();
        analytics.put("flaggedLogsCount", flaggedCount);
        
        // Top risk factors
        Map<String, Long> riskFactorCounts = highRiskLogs.stream()
            .filter(log -> log.getRiskFactors() != null)
            .flatMap(log -> Arrays.stream(log.getRiskFactors().split(",")))
            .map(String::trim)
            .collect(Collectors.groupingBy(factor -> factor, Collectors.counting()));
        
        analytics.put("topRiskFactors", riskFactorCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        
        return analytics;
    }
}