package com.stockfellow.adminservice.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_user_timestamp", columnList = "user_id, timestamp"),
    @Index(name = "idx_endpoint", columnList = "endpoint"),
    @Index(name = "idx_flagged", columnList = "flagged_for_review"),
    @Index(name = "idx_risk_score", columnList = "risk_score")
})
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "log_id", columnDefinition = "UUID")
    private UUID logId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "endpoint", nullable = false, length = 500)
    private String endpoint;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    @Column(name = "request_payload", columnDefinition = "TEXT")
    private String requestPayload;

    @Column(name = "response_status", length = 10)
    private String responseStatus;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now(ZoneOffset.ofHours(2));

    @Column(name = "session_id")
    private String sessionId;

    // Store headers as JSON string - this fixes the JSONB issue
    @Column(name = "headers", columnDefinition = "TEXT")
    private String headers;

    @Column(name = "risk_score")
    private Integer riskScore = 0;

    @Column(name = "risk_factors", columnDefinition = "TEXT")
    private String riskFactors;

    @Column(name = "geolocation", length = 100)
    private String geolocation;

    @Column(name = "flagged_for_review")
    private Boolean flaggedForReview = false;

    // Constructors
    public AuditLog() {}

    public AuditLog(String userId, String endpoint, String httpMethod) {
        this.userId = userId;
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters - Fixed UUID handling
    public UUID getLogId() { 
        return logId; 
    }
    
    public void setLogId(UUID logId) { 
        this.logId = logId; 
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

    public String getRequestPayload() { return requestPayload; }
    public void setRequestPayload(String requestPayload) { this.requestPayload = requestPayload; }

    public String getResponseStatus() { return responseStatus; }
    public void setResponseStatus(String responseStatus) { this.responseStatus = responseStatus; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getHeaders() { return headers; }
    public void setHeaders(String headers) { this.headers = headers; }

    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }

    public String getRiskFactors() { return riskFactors; }
    public void setRiskFactors(String riskFactors) { this.riskFactors = riskFactors; }

    public String getGeolocation() { return geolocation; }
    public void setGeolocation(String geolocation) { this.geolocation = geolocation; }

    public Boolean getFlaggedForReview() { return flaggedForReview; }
    public void setFlaggedForReview(Boolean flaggedForReview) { this.flaggedForReview = flaggedForReview; }
}