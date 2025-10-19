package com.stockfellow.adminservice.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockfellow.adminservice.model.AuditLog;
import com.stockfellow.adminservice.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;
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

    // Pointcut for all controller methods
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {}

    // Pointcut for all request mapping methods
    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public void requestMappingMethods() {}

    @AfterReturning(pointcut = "controllerMethods() && requestMappingMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request == null) {
                logger.debug("No HTTP request context available");
                return;
            }

            AuditLog auditLog = createAuditLog(joinPoint, request);
            
            // Set response status
            if (result instanceof ResponseEntity) {
                ResponseEntity<?> response = (ResponseEntity<?>) result;
                auditLog.setResponseStatus(String.valueOf(response.getStatusCodeValue()));
            } else {
                auditLog.setResponseStatus("200"); // Default success
            }

            auditLogService.createAuditLog(auditLog);
            
        } catch (Exception e) {
            logger.error("Error in audit logging aspect: {}", e.getMessage(), e);
        }
    }

    @AfterThrowing(pointcut = "controllerMethods() && requestMappingMethods()", throwing = "error")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable error) {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request == null) {
                logger.debug("No HTTP request context available");
                return;
            }

            AuditLog auditLog = createAuditLog(joinPoint, request);
            auditLog.setResponseStatus("500"); // Internal Server Error
            
            // Add error information to request payload
            String originalPayload = auditLog.getRequestPayload() != null ? 
                auditLog.getRequestPayload() : "{}";
            String enhancedPayload = originalPayload + " | ERROR: " + error.getMessage();
            auditLog.setRequestPayload(enhancedPayload);

            auditLogService.createAuditLog(auditLog);
            
        } catch (Exception e) {
            logger.error("Error in audit logging aspect (exception handling): {}", e.getMessage(), e);
        }
    }

    private AuditLog createAuditLog(JoinPoint joinPoint, HttpServletRequest request) {
        AuditLog auditLog = new AuditLog();
        
        // Set timestamp
        auditLog.setTimestamp(LocalDateTime.now());
        
        // Get user ID from security context
        String userId = extractUserId();
        auditLog.setUserId(userId);
        
        // Set endpoint and HTTP method
        auditLog.setEndpoint(request.getRequestURI());
        auditLog.setHttpMethod(request.getMethod());
        
        // Extract and set IP address with X-Forwarded-For support
        String ipAddress = extractIpAddress(request);
        auditLog.setIpAddress(ipAddress);
        
        // Set user agent
        String userAgent = request.getHeader("User-Agent");
        auditLog.setUserAgent(userAgent);
        
        // Set session ID
        if (request.getSession(false) != null) {
            auditLog.setSessionId(request.getSession(false).getId());
        }
        
        // Capture request headers as JSON
        try {
            Map<String, String> headers = extractHeaders(request);
            auditLog.setHeaders(objectMapper.writeValueAsString(headers));
        } catch (Exception e) {
            logger.warn("Failed to serialize headers: {}", e.getMessage());
        }
        
        // Capture request payload
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                // Filter out HttpServletRequest and other non-serializable objects
                Object[] serializableArgs = Arrays.stream(args)
                    .filter(arg -> arg != null && 
                           !(arg instanceof HttpServletRequest) &&
                           !(arg instanceof ServletRequestAttributes))
                    .toArray();
                    
                if (serializableArgs.length > 0) {
                    auditLog.setRequestPayload(objectMapper.writeValueAsString(serializableArgs));
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to serialize request payload: {}", e.getMessage());
            auditLog.setRequestPayload("{ \"error\": \"Payload serialization failed\" }");
        }
        
        // Set geolocation (basic implementation - enhance with GeoIP service)
        // auditLog.setGeolocation(determineGeolocation(ipAddress));
        
        return auditLog;
    }

    private String extractUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getPrincipal())) {
                return authentication.getName();
            }
        } catch (Exception e) {
            logger.debug("Failed to extract user ID: {}", e.getMessage());
        }
        return "anonymous";
    }

    private String extractIpAddress(HttpServletRequest request) {
        // Check for proxy headers first
        String[] headerNames = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };
        
        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple IPs, get the first one
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                
                // Validate IP format
                if (isValidIp(ip)) {
                    return ip;
                }
            }
        }
        
        // Fallback to remote address
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : "unknown";
    }

    private boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        
        // Basic IPv4 validation
        String ipv4Pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        if (ip.matches(ipv4Pattern)) return true;
        
        // Basic IPv6 validation (simplified)
        if (ip.contains(":")) return true;
        
        return false;
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            // Filter sensitive headers
            if (!isSensitiveHeader(headerName)) {
                headers.put(headerName, request.getHeader(headerName));
            }
        }
        
        return headers;
    }

    private boolean isSensitiveHeader(String headerName) {
        String lowerCaseName = headerName.toLowerCase();
        return lowerCaseName.contains("authorization") ||
               lowerCaseName.contains("cookie") ||
               lowerCaseName.contains("token") ||
               lowerCaseName.contains("password") ||
               lowerCaseName.contains("secret");
    }

    // private String determineGeolocation(String ipAddress) {
    //     // If GeoIP service is available, use it
    //     if (geoIpService != null) {
    //         try {
    //             return geoIpService.getLocation(ipAddress);
    //         } catch (Exception e) {
    //             logger.debug("GeoIP lookup failed, using fallback: {}", e.getMessage());
    //         }
    //     }
        
    //     // Fallback to basic implementation
    //     if (ipAddress == null || "unknown".equals(ipAddress)) {
    //         return "Unknown";
    //     }
        
    //     // Check for local/private IPs
    //     if (ipAddress.startsWith("127.") || 
    //         ipAddress.startsWith("10.") ||
    //         ipAddress.startsWith("192.168.") ||
    //         ipAddress.startsWith("172.")) {
    //         return "Local Network";
    //     }
        
    //     return "Unknown Location";
    // }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            logger.debug("Failed to get current request: {}", e.getMessage());
            return null;
        }
    }
}