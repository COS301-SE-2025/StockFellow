package com.stockfellow.mfa.exception;

import com.stockfellow.mfa.dto.MfaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MfaException.class)
    public ResponseEntity<MfaResponse> handleMfaException(MfaException e) {
        logger.error("MFA Exception: ", e);
        return ResponseEntity.badRequest()
                .body(new MfaResponse(false, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MfaResponse> handleGenericException(Exception e) {
        logger.error("Unexpected error: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MfaResponse(false, "An unexpected error occurred"));
    }
}
