package com.stockfellow.webauthn.dto;

import javax.validation.constraints.NotBlank;

// Request to complete WebAuthn registration

public class RegistrationCompleteRequest {

    @NotBlank(message = "Credential ID is required")
    private String credentialId;

    @NotBlank(message = "Credential type is required")
    private String credentialType;

    @NotBlank(message = "Client data JSON is required")
    private String clientDataJSON;

    @NotBlank(message = "Attestation object is required")
    private String attestationObject;

    private String authenticatorName;

    public RegistrationCompleteRequest() {
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

    public String getClientDataJSON() {
        return clientDataJSON;
    }

    public void setClientDataJSON(String clientDataJSON) {
        this.clientDataJSON = clientDataJSON;
    }

    public String getAttestationObject() {
        return attestationObject;
    }

    public void setAttestationObject(String attestationObject) {
        this.attestationObject = attestationObject;
    }

    public String getAuthenticatorName() {
        return authenticatorName;
    }

    public void setAuthenticatorName(String authenticatorName) {
        this.authenticatorName = authenticatorName;
    }
}
