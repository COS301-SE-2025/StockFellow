package com.stockfellow.webauthn.dto;

//Response containing WebAuthn authentication options for the frontend

public class AuthenticationStartResponse {

    private String challenge;
    private String rpId;
    private AllowCredentials[] allowCredentials;
    private String userVerification;
    private long timeout;

    public static class AllowCredentials {
        private String type;
        private String id;
        private String[] transports;

        public AllowCredentials(String type, String id, String[] transports) {
            this.type = type;
            this.id = id;
            this.transports = transports;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String[] getTransports() {
            return transports;
        }

        public void setTransports(String[] transports) {
            this.transports = transports;
        }
    }

    public AuthenticationStartResponse() {
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

    public AllowCredentials[] getAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(AllowCredentials[] allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public String getUserVerification() {
        return userVerification;
    }

    public void setUserVerification(String userVerification) {
        this.userVerification = userVerification;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
