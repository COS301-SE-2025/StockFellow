package com.stockfellow.gateway.controller;

import com.stockfellow.gateway.config.RouteConfig;
import com.stockfellow.gateway.model.Route;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Base64;

@RestController
@RequestMapping("/api")
public class ProxyController {

    private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);

    private final RestTemplate restTemplate;
    private final List<Route> routes;

    public ProxyController(RestTemplate restTemplate, RouteConfig routeConfig){
        this.restTemplate = restTemplate;
        this.routes = routeConfig.routes();
    }
    
    @GetMapping("/**")
    public ResponseEntity<?> handleGetRequest(HttpServletRequest request) {
        return proxyRequest(request, HttpMethod.GET, null);
    }
    
    // Handle all POST requests
    @PostMapping("/**")
    public ResponseEntity<?> handlePostRequest(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest(request, HttpMethod.POST, body);
    }
    
    // Handle all PUT requests
    @PutMapping("/**")
    public ResponseEntity<?> handlePutRequest(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest(request, HttpMethod.PUT, body);
    }
    
    // Handle all DELETE requests
    @DeleteMapping("/**")
    public ResponseEntity<?> handleDeleteRequest(HttpServletRequest request) {
        return proxyRequest(request, HttpMethod.DELETE, null);
    }
    
    // Handle all PATCH requests
    @PatchMapping("/**")
    public ResponseEntity<?> handlePatchRequest(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest(request, HttpMethod.PATCH, body);
    }
    
    private ResponseEntity<?> proxyRequest(HttpServletRequest request, HttpMethod method, Object body) {
        try {
            String requestPath = request.getRequestURI();
            logger.debug("Proxying {} request to: {}", method, requestPath);
            
            // Find matching route
            Optional<Route> matchingRoute = findMatchingRoute(requestPath);
            
            if (matchingRoute.isEmpty()) {
                logger.warn("No route found for path: {}", requestPath);
                return ResponseEntity.notFound().build();
            }
            
            Route route = matchingRoute.get();
            
            // Build target URL
            String targetUrl = buildTargetUrl(route, request);
            
            // Build headers (including user context from AuthFilter)
            HttpHeaders headers = buildProxyHeaders(request);
            
            // Create HTTP entity
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);
            
            // Make the proxied request
            logger.debug("Forwarding to: {} {}", method, targetUrl);
            ResponseEntity<String> response = restTemplate.exchange(
                targetUrl,
                method,
                entity,
                String.class
            );
            
            // Return response with appropriate headers
            return ResponseEntity.status(response.getStatusCode())
                .headers(filterResponseHeaders(response.getHeaders()))
                .body(response.getBody());
                
        } catch (Exception e) {
            logger.error("Proxy request failed for {} {}", method, request.getRequestURI(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Gateway error\",\"message\":\"Request forwarding failed\"}");
        }
    }
    
    private Optional<Route> findMatchingRoute(String requestPath) {
        return routes.stream()
            .filter(route -> {
                String routeUrl = route.getUrl();
                
                // Exact match
                if (requestPath.equals(routeUrl)) {
                    return true;
                }
                
                // Path pattern matching
                if (routeUrl.endsWith("/**")) {
                    String basePath = routeUrl.substring(0, routeUrl.length() - 3);
                    return requestPath.startsWith(basePath);
                }
                
                if (routeUrl.endsWith("/*")) {
                    String basePath = routeUrl.substring(0, routeUrl.length() - 2);
                    return requestPath.startsWith(basePath) && 
                           requestPath.substring(basePath.length()).indexOf('/') == -1;
                }
                
                return false;
            })
            .findFirst();
    }
    
    private String buildTargetUrl(Route route, HttpServletRequest request) {
        String targetBase = route.getProxy().getTarget();
        String requestPath = request.getRequestURI();

        String pathSuffix = requestPath;

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(targetBase + pathSuffix);
    
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isEmpty()) {
            builder.query(queryString);
        }
        
        return builder.build().toUriString();
    }
    
    private HttpHeaders buildProxyHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();

        String token = extractTokenFromRequest(request);
        if (token != null) {
            String sub = extractClaimFromToken(token, "sub");
            String username = extractClaimFromToken(token, "preferred_username");
            
            // Set user context headers
            if (sub != null) {
                headers.set("X-User-Id", sub);
            }
            if (username != null) {
                headers.set("X-User-Name", username);
            }
        }
        
        // Copy headers from original request, excluding some
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            
            // Skip headers that shouldn't be forwarded
            if (shouldForwardHeader(headerName)) {
                headers.add(headerName, headerValue);
            }
        }
        
        // Add/override specific headers
        headers.set("X-Forwarded-For", getClientIp(request));
        headers.set("X-Forwarded-Proto", request.getScheme());
        headers.set("X-Forwarded-Host", request.getHeader("Host"));
        headers.set("X-Gateway-Request-Id", generateRequestId());
        
        // User context headers are already added by AuthFilter if authenticated
        // X-User-Id, X-Username, X-User-Roles
        
        return headers;
    }
    
    private boolean shouldForwardHeader(String headerName) {
        String lowerHeaderName = headerName.toLowerCase();
        
        // Headers that should NOT be forwarded
        return !lowerHeaderName.equals("host") &&
               !lowerHeaderName.equals("content-length") &&
               !lowerHeaderName.equals("connection") &&
               !lowerHeaderName.equals("upgrade") &&
               !lowerHeaderName.equals("proxy-connection") &&
               !lowerHeaderName.equals("proxy-authenticate") &&
               !lowerHeaderName.equals("proxy-authorization") &&
               !lowerHeaderName.equals("te") &&
               !lowerHeaderName.equals("trailers") &&
               !lowerHeaderName.equals("transfer-encoding");
    }
    
    private HttpHeaders filterResponseHeaders(HttpHeaders responseHeaders) {
        HttpHeaders filteredHeaders = new HttpHeaders();
        
        responseHeaders.forEach((key, values) -> {
            String lowerKey = key.toLowerCase();
            
            // Headers that should NOT be returned to client
            if (!lowerKey.equals("transfer-encoding") &&
                !lowerKey.equals("connection") &&
                !lowerKey.equals("upgrade") &&
                !lowerKey.equals("proxy-authenticate")) {
                
                filteredHeaders.put(key, values);
            }
        });
        
        // Add gateway-specific headers
        filteredHeaders.set("X-Gateway", "stockfellow-gateway");
        filteredHeaders.set("X-Response-Time", String.valueOf(System.currentTimeMillis()));
        
        return filteredHeaders;
    }
    
    private String getClientIp(HttpServletRequest request) {
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
    
    private String generateRequestId() {
        return "req-" + System.currentTimeMillis() + "-" + 
               Integer.toHexString((int) (Math.random() * 0x10000));
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private String extractClaimFromToken(String token, String claimName) {
        try {
            // Split the JWT token into its three parts
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                logger.warn("Invalid JWT token format");
                return null;
            }
            
            // Decode the payload (second part)
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            
            // Parse JSON to extract the claim
            ObjectMapper mapper = new ObjectMapper();
            JsonNode claims = mapper.readTree(payload);
            
            JsonNode claimNode = claims.get(claimName);
            return claimNode != null ? claimNode.asText() : null;
            
        } catch (Exception e) {
            logger.error("Error extracting claim '{}' from JWT token", claimName, e);
            return null;
        }
    }
    
    // Health check endpoint that doesn't require authentication
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok()
            .body("{\"status\":\"UP\",\"service\":\"api-gateway\",\"timestamp\":\"" + 
                  java.time.Instant.now() + "\"}");
    }
    
    // Route information endpoint (useful for debugging)
    @GetMapping("/routes")
    public ResponseEntity<?> getRoutes(HttpServletRequest request) {
        // Only show routes if user has admin role
        String userRoles = request.getHeader("X-User-Roles");
        if (userRoles == null || !userRoles.contains("admin")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("{\"error\":\"Insufficient permissions\"}");
        }
        
        return ResponseEntity.ok(routes.stream()
            .map(route -> Map.of(
                "url", route.getUrl(),
                "target", route.getProxy().getTarget(),
                "auth", route.isAuth(),
                "rateLimit", route.getRateLimit() != null ? 
                    Map.of("max", route.getRateLimit().getMax(), 
                           "windowMs", route.getRateLimit().getWindowMs()) : null
            ))
            .collect(Collectors.toList()));
    }
}