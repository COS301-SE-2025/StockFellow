package com.stockfellow.gateway.model;

public class TokenValidationResult {
    private boolean success;
    private boolean expired;
    private String message;
    private TokenInfo tokenInfo;
    
    // Default constructor
    public TokenValidationResult() {}
    
    // All-args constructor
    public TokenValidationResult(boolean success, boolean expired, String message, TokenInfo tokenInfo) {
        this.success = success;
        this.expired = expired;
        this.message = message;
        this.tokenInfo = tokenInfo;
    }
    
    // Getters and setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public boolean isExpired() {
        return expired;
    }
    
    public void setExpired(boolean expired) {
        this.expired = expired;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public TokenInfo getTokenInfo() {
        return tokenInfo;
    }
    
    public void setTokenInfo(TokenInfo tokenInfo) {
        this.tokenInfo = tokenInfo;
    }
    
    // Static factory methods
    public static TokenValidationResult success() {
        return new TokenValidationResult(true, false, null, null);
    }
    
    public static TokenValidationResult success(TokenInfo tokenInfo) {
        return new TokenValidationResult(true, false, null, tokenInfo);
    }
    
    public static TokenValidationResult expired(String message) {
        return new TokenValidationResult(false, true, message, null);
    }
    
    public static TokenValidationResult unauthorized(String message) {
        return new TokenValidationResult(false, false, message, null);
    }
    
    // toString method (optional, for debugging)
    @Override
    public String toString() {
        return "TokenValidationResult{" +
                "success=" + success +
                ", expired=" + expired +
                ", message='" + message + '\'' +
                ", tokenInfo=" + tokenInfo +
                '}';
    }
    
    // equals and hashCode methods (optional, but good practice)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        TokenValidationResult that = (TokenValidationResult) o;
        
        if (success != that.success) return false;
        if (expired != that.expired) return false;
        if (message != null ? !message.equals(that.message) : that.message != null) return false;
        return tokenInfo != null ? tokenInfo.equals(that.tokenInfo) : that.tokenInfo == null;
    }
    
    @Override
    public int hashCode() {
        int result = (success ? 1 : 0);
        result = 31 * result + (expired ? 1 : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (tokenInfo != null ? tokenInfo.hashCode() : 0);
        return result;
    }
}