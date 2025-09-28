package com.stockfellow.webauthn.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

// entity maps -> keycloak credential table
// stores webauthn credentials alongside other keycloak credentials

@Entity
@Table(name = "credential")
public class KeycloakWebAuthnCredential {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "type", nullable = false)
    private String type = "webauthn";

    @Column(name = "created_date")
    private Long createdDate;

    @Column(name = "user_label")
    private String userLabel;

    @Column(name = "secret_data", columnDefinition = "TEXT")
    private String secretData;

    @Column(name = "credential_data", columnDefinition = "TEXT")
    private String credentialData;

    @Column(name = "priority")
    private Integer priority = 10;

    public KeycloakWebAuthnCredential() {
        this.createdDate = System.currentTimeMillis();
    }

    public KeycloakWebAuthnCredential(String userId, String userLabel, String secretData, String credentialData) {
        this();
        this.userId = userId;
        this.userLabel = userLabel;
        this.secretData = secretData;
        this.credentialData = credentialData;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
    }

    public String getUserLabel() {
        return userLabel;
    }

    public void setUserLabel(String userLabel) {
        this.userLabel = userLabel;
    }

    public String getSecretData() {
        return secretData;
    }

    public void setSecretData(String secretData) {
        this.secretData = secretData;
    }

    public String getCredentialData() {
        return credentialData;
    }

    public void setCredentialData(String credentialData) {
        this.credentialData = credentialData;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
