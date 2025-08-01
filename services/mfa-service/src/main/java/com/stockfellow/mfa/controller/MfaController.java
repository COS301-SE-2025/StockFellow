package com.stockfellow.mfa.controller;

import com.stockfellow.mfa.dto.MfaRequest;
import com.stockfellow.mfa.dto.MfaVerifyRequest;
import com.stockfellow.mfa.dto.MfaResponse;
import com.stockfellow.mfa.service.InMemoryOTPService;
import com.stockfellow.mfa.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/mfa")
@CrossOrigin(origins = "*")
@Validated
public class MfaController {

    private static final Logger logger = LoggerFactory.getLogger(MfaController.class);

    private final InMemoryOTPService otpService;
    private final EmailService emailService;

    public MfaController(InMemoryOTPService otpService, EmailService emailService) {
        this.otpService = otpService;
        this.emailService = emailService;
    }

    // Used by Gateway for login
    // sends otp to user email after login attempt
    @PostMapping("/send-otp")
    public ResponseEntity<MfaResponse> sendOTP(@Valid @RequestBody MfaRequest request) {
        try {
            logger.info("Generating OTP for user: {}", request.getEmail());

            // Generate OTP
            String otpCode = otpService.generateOTP(request.getEmail(), request.getUserId());

            // Send OTP via email
            emailService.sendOTP(request.getEmail(), otpCode, request.getUserId());

            return ResponseEntity.ok(new MfaResponse(true, "OTP sent successfully"));

        } catch (Exception e) {
            logger.error("Failed to send OTP for user: {}", request.getEmail(), e);
            return ResponseEntity.internalServerError()
                    .body(new MfaResponse(false, "Failed to send OTP"));
        }
    }

    // verifies the otp sent to user - checks if the OTP is valid and not expired
    @PostMapping("/verify-otp")
    public ResponseEntity<MfaResponse> verifyOTP(@Valid @RequestBody MfaVerifyRequest request) {
        try {
            logger.info("Verifying OTP for user: {}", request.getEmail());

            boolean isValid = otpService.verifyOTP(request.getEmail(), request.getOtpCode());

            if (isValid) {
                // Generate session token or mark user as MFA verified
                String sessionToken = generateSessionToken(request.getEmail());
                return ResponseEntity.ok(new MfaResponse(true, "OTP verified successfully", sessionToken));
            } else {
                return ResponseEntity.badRequest()
                        .body(new MfaResponse(false, "Invalid or expired OTP"));
            }

        } catch (Exception e) {
            logger.error("Failed to verify OTP for user: {}", request.getEmail(), e);
            return ResponseEntity.internalServerError()
                    .body(new MfaResponse(false, "OTP verification failed"));
        }
    }

    // Endpoint to check if a valid OTP exists for the user
    @GetMapping("/status/{email}")
    public ResponseEntity<MfaResponse> getOTPStatus(@PathVariable String email) {
        boolean hasValidOTP = otpService.hasValidOTP(email);
        return ResponseEntity.ok(new MfaResponse(hasValidOTP,
                hasValidOTP ? "Valid OTP exists" : "No valid OTP found"));
    }

    // Endpoint to invalidate the OTP for the user: eg they have too many failed
    // login attempts
    @DeleteMapping("/invalidate/{email}")
    public ResponseEntity<MfaResponse> invalidateOTP(@PathVariable String email) {
        otpService.invalidateOTP(email);
        return ResponseEntity.ok(new MfaResponse(true, "OTP invalidated"));
    }

    private String generateSessionToken(String email) {
        // Generate a simple session token
        return "session_" + email + "_" + System.currentTimeMillis();
    }
}
