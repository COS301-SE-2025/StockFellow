package com.stockfellow.webauthn.dto;

import javax.validation.constraints.NotBlank;

// request to start webauthn  authenticatio process
public class AuthenticationStartRequest {

    @NotBlank(message = "Username is required")
    private String username;

    public AuthenticationStartRequest() {
    }

    public AuthenticationStartRequest(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
