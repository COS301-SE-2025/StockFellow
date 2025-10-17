package com.stockfellow.webauthn.exception;

public class WebAuthnException extends RuntimeException {

    public WebAuthnException(String message) {
        super(message);
    }

    public WebAuthnException(String message, Throwable cause) {
        super(message, cause);
    }
}
