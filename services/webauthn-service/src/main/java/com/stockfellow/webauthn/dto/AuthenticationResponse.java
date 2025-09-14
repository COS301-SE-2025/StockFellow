package com.stockfellow.webauthn.dto;

// Response after successful authentication containing JWT token
public class AuthenticationResponse {

    private String token;
    private String userId;
    private String username;
    private String tokenType = "Bearer";
    private long expiresIn;

    public AuthenticationResponse() {
    }

    public AuthenticationResponse(String token, String userId, String username, long expiresIn) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
