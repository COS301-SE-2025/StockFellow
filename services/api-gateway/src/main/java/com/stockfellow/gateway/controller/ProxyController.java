package com.stockfellow.gateway.controller;

import com.stockfellow.gateway.model.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;

@RestController
public class ProxyController {
    
    @Autowired
    private List<Route> routes;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @RequestMapping(value = "/api/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
    public ResponseEntity<String> proxyRequest(
            @RequestBody(required = false) String body,
            HttpMethod method,
            HttpServletRequest request,
            @RequestHeader MultiValueMap<String, String> headers) {
        
        String requestPath = request.getRequestURI();
        
        Route matchingRoute = findMatchingRoute(requestPath);
        if (matchingRoute == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            String targetUrl = matchingRoute.getProxy().getTarget() + requestPath;
            
            HttpHeaders proxyHeaders = new HttpHeaders();
            headers.forEach((key, values) -> {
                if (!key.equalsIgnoreCase("host")) {
                    proxyHeaders.addAll(key, values);
                }
            });
            
            // CORS headers
            proxyHeaders.add("Access-Control-Allow-Origin", "*");
            proxyHeaders.add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            proxyHeaders.add("Access-Control-Allow-Headers", "Content-Type, Authorization");
            
            // Create request entity
            HttpEntity<String> entity = new HttpEntity<>(body, proxyHeaders);
            
            // Make the proxy request
            ResponseEntity<String> response = restTemplate.exchange(
                URI.create(targetUrl),
                method,
                entity,
                String.class
            );
            
            // Return response with CORS headers
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.putAll(response.getHeaders());
            responseHeaders.add("Access-Control-Allow-Origin", "*");
            responseHeaders.add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            responseHeaders.add("Access-Control-Allow-Headers", "Content-Type, Authorization");
            
            return ResponseEntity.status(response.getStatusCode())
                .headers(responseHeaders)
                .body(response.getBody());
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Proxy error: " + e.getMessage());
        }
    }
    
    private Route findMatchingRoute(String requestPath) {
        // Find the most specific matching route (longest match)
        Route bestMatch = null;
        int longestMatch = 0;
        
        for (Route route : routes) {
            String routePattern = route.getUrl();
            // Remove wildcard for matching
            String basePattern = routePattern.replace("*", "");
            
            if (requestPath.startsWith(basePattern) && basePattern.length() > longestMatch) {
                bestMatch = route;
                longestMatch = basePattern.length();
            }
        }
        
        return bestMatch;
    }
}