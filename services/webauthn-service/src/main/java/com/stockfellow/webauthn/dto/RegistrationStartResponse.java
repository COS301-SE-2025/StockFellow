package com.stockfellow.webauthn.dto;

// Response containing WebAuthn registration options for the frontend

public class RegistrationStartResponse {

    private String challenge;
    private String rpId;
    private String rpName;
    private UserInfo user;
    private PubKeyCredParams[] pubKeyCredParams;
    private AuthenticatorSelection authenticatorSelection;
    private String attestation;
    private long timeout;

    public static class UserInfo {
        private String id;
        private String name;
        private String displayName;

        public UserInfo(String id, String name, String displayName) {
            this.id = id;
            this.name = name;
            this.displayName = displayName;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
    }

    public static class PubKeyCredParams {
        private String type;
        private int alg;

        public PubKeyCredParams(String type, int alg) {
            this.type = type;
            this.alg = alg;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getAlg() {
            return alg;
        }

        public void setAlg(int alg) {
            this.alg = alg;
        }
    }

    public static class AuthenticatorSelection {
        private String authenticatorAttachment;
        private String userVerification;
        private boolean requireResidentKey;

        public AuthenticatorSelection(String authenticatorAttachment, String userVerification,
                boolean requireResidentKey) {
            this.authenticatorAttachment = authenticatorAttachment;
            this.userVerification = userVerification;
            this.requireResidentKey = requireResidentKey;
        }

        public String getAuthenticatorAttachment() {
            return authenticatorAttachment;
        }

        public void setAuthenticatorAttachment(String authenticatorAttachment) {
            this.authenticatorAttachment = authenticatorAttachment;
        }

        public String getUserVerification() {
            return userVerification;
        }

        public void setUserVerification(String userVerification) {
            this.userVerification = userVerification;
        }

        public boolean isRequireResidentKey() {
            return requireResidentKey;
        }

        public void setRequireResidentKey(boolean requireResidentKey) {
            this.requireResidentKey = requireResidentKey;
        }
    }

    public RegistrationStartResponse() {
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public String getRpId() {
        return rpId;
    }

    public void setRpId(String rpId) {
        this.rpId = rpId;
    }

    public String getRpName() {
        return rpName;
    }

    public void setRpName(String rpName) {
        this.rpName = rpName;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public PubKeyCredParams[] getPubKeyCredParams() {
        return pubKeyCredParams;
    }

    public void setPubKeyCredParams(PubKeyCredParams[] pubKeyCredParams) {
        this.pubKeyCredParams = pubKeyCredParams;
    }

    public AuthenticatorSelection getAuthenticatorSelection() {
        return authenticatorSelection;
    }

    public void setAuthenticatorSelection(AuthenticatorSelection authenticatorSelection) {
        this.authenticatorSelection = authenticatorSelection;
    }

    public String getAttestation() {
        return attestation;
    }

    public void setAttestation(String attestation) {
        this.attestation = attestation;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
