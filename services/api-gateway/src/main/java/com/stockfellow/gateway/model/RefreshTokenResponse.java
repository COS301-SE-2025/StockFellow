package com.stockfellow.gateway.model;

public class RefreshTokenResponse {
    private boolean success;
    private String accessToken;
    private String refreshToken;
    private Integer expiresIn;
    private String tokenType;
    private String error;
    
    // Default constructor
    public RefreshTokenResponse() {}
    
    // All-args constructor
    public RefreshTokenResponse(boolean success, String accessToken, String refreshToken, 
                               Integer expiresIn, String tokenType, String error) {
        this.success = success;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.tokenType = tokenType;
        this.error = error;
    }
    
    // Builder pattern implementation
    public static RefreshTokenResponseBuilder builder() {
        return new RefreshTokenResponseBuilder();
    }
    
    public static class RefreshTokenResponseBuilder {
        private boolean success;
        private String accessToken;
        private String refreshToken;
        private Integer expiresIn;
        private String tokenType;
        private String error;
        
        public RefreshTokenResponseBuilder success(boolean success) {
            this.success = success;
            return this;
        }
        
        public RefreshTokenResponseBuilder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }
        
        public RefreshTokenResponseBuilder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }
        
        public RefreshTokenResponseBuilder expiresIn(Integer expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }
        
        public RefreshTokenResponseBuilder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }
        
        public RefreshTokenResponseBuilder error(String error) {
            this.error = error;
            return this;
        }
        
        public RefreshTokenResponse build() {
            return new RefreshTokenResponse(success, accessToken, refreshToken, 
                                          expiresIn, tokenType, error);
        }
    }
    
    // Getters and setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public Integer getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    // Static factory method
    public static RefreshTokenResponse failure(String error) {
        return RefreshTokenResponse.builder()
            .success(false)
            .error(error)
            .build();
    }
    
    @Override
    public String toString() {
        return "RefreshTokenResponse{" +
                "success=" + success +
                ", accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", expiresIn=" + expiresIn +
                ", tokenType='" + tokenType + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
    
    // equals and hashCode methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        RefreshTokenResponse that = (RefreshTokenResponse) o;
        
        if (success != that.success) return false;
        if (accessToken != null ? !accessToken.equals(that.accessToken) : that.accessToken != null) return false;
        if (refreshToken != null ? !refreshToken.equals(that.refreshToken) : that.refreshToken != null) return false;
        if (expiresIn != null ? !expiresIn.equals(that.expiresIn) : that.expiresIn != null) return false;
        if (tokenType != null ? !tokenType.equals(that.tokenType) : that.tokenType != null) return false;
        return error != null ? error.equals(that.error) : that.error == null;
    }
    
    @Override
    public int hashCode() {
        int result = (success ? 1 : 0);
        result = 31 * result + (accessToken != null ? accessToken.hashCode() : 0);
        result = 31 * result + (refreshToken != null ? refreshToken.hashCode() : 0);
        result = 31 * result + (expiresIn != null ? expiresIn.hashCode() : 0);
        result = 31 * result + (tokenType != null ? tokenType.hashCode() : 0);
        result = 31 * result + (error != null ? error.hashCode() : 0);
        return result;
    }
}