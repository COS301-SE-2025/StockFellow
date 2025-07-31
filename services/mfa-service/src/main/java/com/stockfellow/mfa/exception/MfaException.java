package com.stockfellow.mfa.exception;

public class MfaException extends RuntimeException {
    public MfaException(String message) {
        super(message);
    }

    public MfaException(String message, Throwable cause) {
        super(message, cause);
    }
}