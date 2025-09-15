package com.stockfellow.adminservice.aspect;

import com.stockfellow.adminservice.model.AuditLog;
import com.stockfellow.adminservice.service.AuditLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class AuditLogAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditLogAspect.class);
    
    @Autowired
    private AuditLogService auditLogService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Around("@within(org.springframework.web.bind.annotation.RestController) && " +
            "execution(* com.stockfellow..*(..))")
    public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        String responseStatus = "500";
        
        try {
            result = joinPoint.proceed();
            
            // Extract status code from ResponseEntity
            if (result instanceof ResponseEntity) {
                ResponseEntity<?> responseEntity = (ResponseEntity<?>) result;
                responseStatus = String.valueOf(responseEntity.getStatusCode().value());
            } else {
                responseStatus = "200";
            }
            
            return result;
            
        } catch (Exception e) {
            responseStatus = "500";
            throw e;
        } finally {
            try {
                createAuditLog(joinPoint, responseStatus, startTime);
            } catch (Exception e) {
                logger.error("Failed to create audit log: {}", e.getMessage(), e);
            }
        }
    }
    
    private void createAuditLog(ProceedingJoinPoint joinPoint, String responseStatus, long startTime) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return;
            
            HttpServletRequest request = attributes.getRequest();
            
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(request.getHeader("X-User-Id"));
            auditLog.setEndpoint(request.getRequestURI());
            auditLog.setHttpMethod(request.getMethod());
            auditLog.setResponseStatus(responseStatus);
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
            auditLog.setSessionId(request.getHeader("X-Session-Id"));
            
            // Capture request headers (excluding sensitive ones)
            Map<String, String> headers = new HashMap<>();
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    if (!isSensitiveHeader(headerName)) {
                        headers.put(headerName, request.getHeader(headerName));
                    }
                }
            }
            auditLog.setHeaders(headers);
            
            // Capture request payload for non-GET requests (limit size)
            if (!"GET".equals(request.getMethod())) {
                try {
                    Object[] args = joinPoint.getArgs();
                    if (args.length > 0) {
                        String payload = objectMapper.writeValueAsString(args[0]);
                        if (payload.length() > 1000) {
                            payload = payload.substring(0, 1000) + "...";
                        }
                        auditLog.setRequestPayload(payload);
                    }
                } catch (Exception e) {
                    auditLog.setRequestPayload("Error serializing payload");
                }
            }
            
            // Add performance metrics
            long executionTime = System.currentTimeMillis() - startTime;
            Map<String, Object> metadata = auditLog.getHeaders() != null ? 
                new HashMap<>(auditLog.getHeaders()) : new HashMap<>();
            metadata.put("executionTimeMs", executionTime);
            
            auditLogService.createAuditLog(auditLog);
            
        } catch (Exception e) {
            logger.error("Error creating audit log: {}", e.getMessage(), e);
        }
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    private boolean isSensitiveHeader(String headerName) {
        String lowerName = headerName.toLowerCase();
        return lowerName.contains("authorization") || 
               lowerName.contains("cookie") || 
               lowerName.contains("password") ||
               lowerName.contains("token");
    }
}
