package com.stockfellow.gateway.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenInfo {
    private boolean valid;
    private boolean expired;
    private String userId;
    private String username;
    private Set<String> roles;
    private long expiresIn; // seconds until expiry
    private Map<String, Object> claims;
    
    // Default constructor
    public TokenInfo() {}
    
    // All-args constructor
    public TokenInfo(boolean valid, boolean expired, String userId, String username, 
                     Set<String> roles, long expiresIn, Map<String, Object> claims) {
        this.valid = valid;
        this.expired = expired;
        this.userId = userId;
        this.username = username;
        this.roles = roles;
        this.expiresIn = expiresIn;
        this.claims = claims;
    }
    
    // Builder pattern implementation
    public static TokenInfoBuilder builder() {
        return new TokenInfoBuilder();
    }
    
    public static class TokenInfoBuilder {
        private boolean valid;
        private boolean expired;
        private String userId;
        private String username;
        private Set<String> roles;
        private long expiresIn;
        private Map<String, Object> claims;
        
        public TokenInfoBuilder valid(boolean valid) {
            this.valid = valid;
            return this;
        }
        
        public TokenInfoBuilder expired(boolean expired) {
            this.expired = expired;
            return this;
        }
        
        public TokenInfoBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public TokenInfoBuilder username(String username) {
            this.username = username;
            return this;
        }
        
        public TokenInfoBuilder roles(Set<String> roles) {
            this.roles = roles;
            return this;
        }
        
        public TokenInfoBuilder expiresIn(long expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }
        
        public TokenInfoBuilder claims(Map<String, Object> claims) {
            this.claims = claims;
            return this;
        }
        
        public TokenInfo build() {
            return new TokenInfo(valid, expired, userId, username, roles, expiresIn, claims);
        }
    }
    
    // Getters and setters
    public boolean isValid() {
        return valid;
    }
    
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    public boolean isExpired() {
        return expired;
    }
    
    public void setExpired(boolean expired) {
        this.expired = expired;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public Set<String> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
    
    public long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    public Map<String, Object> getClaims() {
        return claims;
    }
    
    public void setClaims(Map<String, Object> claims) {
        this.claims = claims;
    }
    
    // Static factory methods
    public static TokenInfo invalid() {
        return new TokenInfo(false, false, null, null, null, 0, null);
    }
    
    public static TokenInfo expired() {
        return new TokenInfo(false, true, null, null, null, 0, null);
    }
    
    // toString method (optional, for debugging)
    @Override
    public String toString() {
        return "TokenInfo{" +
                "valid=" + valid +
                ", expired=" + expired +
                ", userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", roles=" + roles +
                ", expiresIn=" + expiresIn +
                ", claims=" + claims +
                '}';
    }
    
    // equals and hashCode methods (optional, but good practice)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        TokenInfo tokenInfo = (TokenInfo) o;
        
        if (valid != tokenInfo.valid) return false;
        if (expired != tokenInfo.expired) return false;
        if (expiresIn != tokenInfo.expiresIn) return false;
        if (userId != null ? !userId.equals(tokenInfo.userId) : tokenInfo.userId != null) return false;
        if (username != null ? !username.equals(tokenInfo.username) : tokenInfo.username != null) return false;
        if (roles != null ? !roles.equals(tokenInfo.roles) : tokenInfo.roles != null) return false;
        return claims != null ? claims.equals(tokenInfo.claims) : tokenInfo.claims == null;
    }
    
    @Override
    public int hashCode() {
        int result = (valid ? 1 : 0);
        result = 31 * result + (expired ? 1 : 0);
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (roles != null ? roles.hashCode() : 0);
        result = 31 * result + (int) (expiresIn ^ (expiresIn >>> 32));
        result = 31 * result + (claims != null ? claims.hashCode() : 0);
        return result;
    }
}