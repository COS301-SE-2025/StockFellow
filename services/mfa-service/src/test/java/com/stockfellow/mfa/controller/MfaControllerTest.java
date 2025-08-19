package com.stockfellow.mfa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockfellow.mfa.dto.MfaRequest;
import com.stockfellow.mfa.dto.MfaVerifyRequest;
import com.stockfellow.mfa.service.EmailService;
import com.stockfellow.mfa.service.InMemoryOTPService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MfaController.class)
@DisplayName("MfaController Tests")
class MfaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InMemoryOTPService otpService;

    @MockBean
    private EmailService emailService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should send OTP successfully")
    void shouldSendOTPSuccessfully() throws Exception {

        MfaRequest request = new MfaRequest("test@example.com", "user123");
        String generatedOTP = "123456";

        when(otpService.generateOTP(anyString(), anyString())).thenReturn(generatedOTP);
        doNothing().when(emailService).sendOTP(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/mfa/send-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OTP sent successfully"));

        verify(otpService).generateOTP("test@example.com", "user123");
        verify(emailService).sendOTP("test@example.com", generatedOTP, "user123");
    }

    @Test
    @DisplayName("Should handle OTP generation failure")
    void shouldHandleOTPGenerationFailure() throws Exception {

        MfaRequest request = new MfaRequest("test@example.com", "user123");

        when(otpService.generateOTP(anyString(), anyString())).thenThrow(new RuntimeException("OTP generation failed"));

        mockMvc.perform(post("/api/mfa/send-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to send OTP"));

        verify(otpService).generateOTP("test@example.com", "user123");
        verify(emailService, never()).sendOTP(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should handle email sending failure")
    void shouldHandleEmailSendingFailure() throws Exception {

        MfaRequest request = new MfaRequest("test@example.com", "user123");
        String generatedOTP = "123456";

        when(otpService.generateOTP(anyString(), anyString())).thenReturn(generatedOTP);
        doThrow(new RuntimeException("Email sending failed")).when(emailService).sendOTP(anyString(), anyString(),
                anyString());

        mockMvc.perform(post("/api/mfa/send-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to send OTP"));

        verify(otpService).generateOTP("test@example.com", "user123");
        verify(emailService).sendOTP("test@example.com", generatedOTP, "user123");
    }

    @Test
    @DisplayName("Should verify OTP successfully")
    void shouldVerifyOTPSuccessfully() throws Exception {

        MfaVerifyRequest request = new MfaVerifyRequest("test@example.com", "123456");

        when(otpService.verifyOTP(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/api/mfa/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OTP verified successfully"))
                .andExpect(jsonPath("$.sessionToken").exists())
                .andExpect(jsonPath("$.sessionToken")
                        .value(org.hamcrest.Matchers.startsWith("session_test@example.com_")));

        verify(otpService).verifyOTP("test@example.com", "123456");
    }

    @Test
    @DisplayName("Should reject invalid OTP")
    void shouldRejectInvalidOTP() throws Exception {

        MfaVerifyRequest request = new MfaVerifyRequest("test@example.com", "123456");

        when(otpService.verifyOTP(anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/api/mfa/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid or expired OTP"))
                .andExpect(jsonPath("$.sessionToken").doesNotExist());

        verify(otpService).verifyOTP("test@example.com", "123456");
    }

    @Test
    @DisplayName("Should handle OTP verification failure")
    void shouldHandleOTPVerificationFailure() throws Exception {

        MfaVerifyRequest request = new MfaVerifyRequest("test@example.com", "123456");

        when(otpService.verifyOTP(anyString(), anyString())).thenThrow(new RuntimeException("Verification failed"));

        mockMvc.perform(post("/api/mfa/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("OTP verification failed"));

        verify(otpService).verifyOTP("test@example.com", "123456");
    }

    @Test
    @DisplayName("Should return OTP status when valid OTP exists")
    void shouldReturnOTPStatusWhenValidOTPExists() throws Exception {

        String email = "test@example.com";
        when(otpService.hasValidOTP(email)).thenReturn(true);

        mockMvc.perform(get("/api/mfa/status/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Valid OTP exists"));

        verify(otpService).hasValidOTP(email);
    }

    @Test
    @DisplayName("Should return OTP status when no valid OTP exists")
    void shouldReturnOTPStatusWhenNoValidOTPExists() throws Exception {

        String email = "test@example.com";
        when(otpService.hasValidOTP(email)).thenReturn(false);

        mockMvc.perform(get("/api/mfa/status/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("No valid OTP found"));

        verify(otpService).hasValidOTP(email);
    }

    @Test
    @DisplayName("Should invalidate OTP successfully")
    void shouldInvalidateOTPSuccessfully() throws Exception {

        String email = "test@example.com";
        doNothing().when(otpService).invalidateOTP(email);

        mockMvc.perform(delete("/api/mfa/invalidate/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OTP invalidated"));

        verify(otpService).invalidateOTP(email);
    }

    @Test
    @DisplayName("Should handle missing request body for send OTP")
    void shouldHandleMissingRequestBodyForSendOTP() throws Exception {

        mockMvc.perform(post("/api/mfa/send-otp")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    @DisplayName("Should handle missing request body for verify OTP")
    void shouldHandleMissingRequestBodyForVerifyOTP() throws Exception {

        mockMvc.perform(post("/api/mfa/verify-otp")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    @DisplayName("Should handle invalid JSON for send OTP")
    void shouldHandleInvalidJSONForSendOTP() throws Exception {

        mockMvc.perform(post("/api/mfa/send-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    @DisplayName("Should handle invalid JSON for verify OTP")
    void shouldHandleInvalidJSONForVerifyOTP() throws Exception {

        mockMvc.perform(post("/api/mfa/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    @DisplayName("Should handle special characters in email path variable")
    void shouldHandleSpecialCharactersInEmailPathVariable() throws Exception {

        String email = "test+user@example.com";
        when(otpService.hasValidOTP(email)).thenReturn(true);

        mockMvc.perform(get("/api/mfa/status/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Valid OTP exists"));

        verify(otpService).hasValidOTP(email);
    }

    @Test
    @DisplayName("Should handle empty email in path variable for status")
    void shouldHandleEmptyEmailInPathVariableForStatus() throws Exception {

        mockMvc.perform(get("/api/mfa/status/"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    @DisplayName("Should handle empty email in path variable for invalidate")
    void shouldHandleEmptyEmailInPathVariableForInvalidate() throws Exception {

        mockMvc.perform(delete("/api/mfa/invalidate/"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
