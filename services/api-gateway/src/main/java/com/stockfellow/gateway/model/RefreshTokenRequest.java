package com.stockfellow.gateway.model;

import java.util.Map;

public class RefreshTokenRequest {
    private String refreshToken;
    private Boolean retryOriginalRequest;
    private String originalRequestUrl;
    private String originalRequestMethod;
    private Map<String, String> originalRequestHeaders;
    private String originalRequestBody;

    public RefreshTokenRequest() {}
        
    // Constructor with all fields
    public RefreshTokenRequest(String refreshToken, Boolean retryOriginalRequest, 
                                String originalRequestUrl, String originalRequestMethod,
                                Map<String, String> originalRequestHeaders, String originalRequestBody) {
        this.refreshToken = refreshToken;
        this.retryOriginalRequest = retryOriginalRequest;
        this.originalRequestUrl = originalRequestUrl;
        this.originalRequestMethod = originalRequestMethod;
        this.originalRequestHeaders = originalRequestHeaders;
        this.originalRequestBody = originalRequestBody;
    }
    
    // Getters and setters
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public Boolean getRetryOriginalRequest() {
        return retryOriginalRequest;
    }
    
    public void setRetryOriginalRequest(Boolean retryOriginalRequest) {
        this.retryOriginalRequest = retryOriginalRequest;
    }
    
    public String getOriginalRequestUrl() {
        return originalRequestUrl;
    }
    
    public void setOriginalRequestUrl(String originalRequestUrl) {
        this.originalRequestUrl = originalRequestUrl;
    }
    
    public String getOriginalRequestMethod() {
        return originalRequestMethod;
    }
    
    public void setOriginalRequestMethod(String originalRequestMethod) {
        this.originalRequestMethod = originalRequestMethod;
    }
    
    public Map<String, String> getOriginalRequestHeaders() {
        return originalRequestHeaders;
    }
    
    public void setOriginalRequestHeaders(Map<String, String> originalRequestHeaders) {
        this.originalRequestHeaders = originalRequestHeaders;
    }
    
    public String getOriginalRequestBody() {
        return originalRequestBody;
    }
    
    public void setOriginalRequestBody(String originalRequestBody) {
        this.originalRequestBody = originalRequestBody;
    }
}