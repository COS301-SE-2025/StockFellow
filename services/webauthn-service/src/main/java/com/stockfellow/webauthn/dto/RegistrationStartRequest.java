package com.stockfellow.webauthn.dto;

import javax.validation.constraints.NotBlank;

//request to start WebAuthn registration process

public class RegistrationStartRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Username is required")
    private String username;

    private String authenticatorName;

    public RegistrationStartRequest() {
    }

    public RegistrationStartRequest(String userId, String username, String authenticatorName) {
        this.userId = userId;
        this.username = username;
        this.authenticatorName = authenticatorName;
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

    public String getAuthenticatorName() {
        return authenticatorName;
    }

    public void setAuthenticatorName(String authenticatorName) {
        this.authenticatorName = authenticatorName;
    }
}
