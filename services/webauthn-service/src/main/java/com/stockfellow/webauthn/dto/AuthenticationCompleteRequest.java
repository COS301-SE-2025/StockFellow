package com.stockfellow.webauthn.dto;

import javax.validation.constraints.NotBlank;

public class AuthenticationCompleteRequest {

    @NotBlank(message = "Credential ID is required")
    private String credentialId;

    @NotBlank(message = "Credential type is required")
    private String credentialType;

    @NotBlank(message = "Response is required")
    private String response;

    @NotBlank(message = "Client data JSON is required")
    private String clientDataJSON;

    @NotBlank(message = "Authenticator data is required")
    private String authenticatorData;

    @NotBlank(message = "Signature is required")
    private String signature;

    private String userHandle; // Optional

    public AuthenticationCompleteRequest() {
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    public String getCredentialType() {
        return credentialType;
    }

    public void setCredentialType(String credentialType) {
        this.credentialType = credentialType;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getClientDataJSON() {
        return clientDataJSON;
    }

    public void setClientDataJSON(String clientDataJSON) {
        this.clientDataJSON = clientDataJSON;
    }

    public String getAuthenticatorData() {
        return authenticatorData;
    }

    public void setAuthenticatorData(String authenticatorData) {
        this.authenticatorData = authenticatorData;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getUserHandle() {
        return userHandle;
    }

    public void setUserHandle(String userHandle) {
        this.userHandle = userHandle;
    }
}
