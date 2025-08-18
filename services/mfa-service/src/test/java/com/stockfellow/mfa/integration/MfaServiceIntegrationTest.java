package com.stockfellow.mfa.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockfellow.mfa.config.TestConfig;
import com.stockfellow.mfa.dto.MfaRequest;
import com.stockfellow.mfa.dto.MfaVerifyRequest;
import com.stockfellow.mfa.service.InMemoryOTPService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@DisplayName("MFA Service Integration Tests")
class MfaServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InMemoryOTPService otpService;

    @Autowired
    private TestConfig.TestOtpCapture testOtpCapture;

    private static final String BASE_URL = "/api/mfa";
    private static final String TEST_EMAIL = "integration.test@stockfellow.com";
    private static final String TEST_USER_ID = "integration-user-123";

    @BeforeEach
    void setUp() {

        otpService.invalidateOTP(TEST_EMAIL);
        testOtpCapture.clearOtps();
    }

    @Test
    @DisplayName("Should complete full MFA workflow successfully")
    void shouldCompleteFullMfaWorkflowSuccessfully() throws Exception {
        // Send OTP
        MfaRequest sendRequest = new MfaRequest(TEST_EMAIL, TEST_USER_ID);

        mockMvc.perform(post(BASE_URL + "/send-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sendRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OTP sent successfully"));

        // OTP status shows if valid OTP existsa
        mockMvc.perform(get(BASE_URL + "/status/{email}", TEST_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Valid OTP exists"));

        // retrive otp from test capture in testconfig
        String generatedOtp = testOtpCapture.getLastOtpForEmail(TEST_EMAIL);
        assertNotNull(generatedOtp, "OTP should be captured from mock email service");
        assertEquals(6, generatedOtp.length(), "OTP should be 6 digits");

        // verify otp
        MfaVerifyRequest verifyRequest = new MfaVerifyRequest(TEST_EMAIL, generatedOtp);

        mockMvc.perform(post(BASE_URL + "/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OTP verified successfully"))
                .andExpect(jsonPath("$.sessionToken").exists())
                .andExpect(jsonPath("$.sessionToken").value(startsWith("session_" + TEST_EMAIL + "_")));

        // OTP status shows no valid OTP after successful verification
        mockMvc.perform(get(BASE_URL + "/status/{email}", TEST_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("No valid OTP found"));
    }

    @Test
    @DisplayName("Should handle multiple OTP requests for same email")
    void shouldHandleMultipleOtpRequestsForSameEmail() throws Exception {

        MfaRequest request = new MfaRequest(TEST_EMAIL, TEST_USER_ID);

        mockMvc.perform(post(BASE_URL + "/send-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        String firstOtp = testOtpCapture.getLastOtpForEmail(TEST_EMAIL);

        // Send second OTP -> should replace the first otp
        mockMvc.perform(post(BASE_URL + "/send-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        String secondOtp = testOtpCapture.getLastOtpForEmail(TEST_EMAIL);

        // Verify first OTP no longer valid
        MfaVerifyRequest firstVerifyRequest = new MfaVerifyRequest(TEST_EMAIL, firstOtp);
        mockMvc.perform(post(BASE_URL + "/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstVerifyRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid or expired OTP"));

        // Verify second OTP -> valid
        MfaVerifyRequest secondVerifyRequest = new MfaVerifyRequest(TEST_EMAIL, secondOtp);
        mockMvc.perform(post(BASE_URL + "/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondVerifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should handle OTP invalidation correctly")
    void shouldHandleOtpInvalidationCorrectly() throws Exception {

        MfaRequest request = new MfaRequest(TEST_EMAIL, TEST_USER_ID);

        mockMvc.perform(post(BASE_URL + "/send-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get(BASE_URL + "/status/{email}", TEST_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(delete(BASE_URL + "/invalidate/{email}", TEST_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OTP invalidated"));

        // Verify OTP no longer exists
        mockMvc.perform(get(BASE_URL + "/status/{email}", TEST_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("No valid OTP found"));
    }

    @Test
    @DisplayName("Should reject invalid OTP verification attempts")
    void shouldRejectInvalidOtpVerificationAttempts() throws Exception {

        MfaRequest request = new MfaRequest(TEST_EMAIL, TEST_USER_ID);

        mockMvc.perform(post(BASE_URL + "/send-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        MfaVerifyRequest wrongOtpRequest = new MfaVerifyRequest(TEST_EMAIL, "000000");

        mockMvc.perform(post(BASE_URL + "/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongOtpRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid or expired OTP"));

        mockMvc.perform(get(BASE_URL + "/status/{email}", TEST_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should handle verification for non-existent email")
    void shouldHandleVerificationForNonExistentEmail() throws Exception {
        String nonExistentEmail = "nonexistent@example.com";

        MfaVerifyRequest request = new MfaVerifyRequest(nonExistentEmail, "123456");

        mockMvc.perform(post(BASE_URL + "/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid or expired OTP"));
    }

    @Test
    @DisplayName("Should handle malformed requests gracefully")
    void shouldHandleMalformedRequestsGracefully() throws Exception {

        mockMvc.perform(post(BASE_URL + "/send-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));

        mockMvc.perform(post(BASE_URL + "/send-otp")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    @DisplayName("Should handle special characters in email paths")
    void shouldHandleSpecialCharactersInEmailPaths() throws Exception {
        String specialEmail = "test+special@example.com";

        MfaRequest request = new MfaRequest(specialEmail, TEST_USER_ID);

        mockMvc.perform(post(BASE_URL + "/send-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get(BASE_URL + "/status/{email}", specialEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        otpService.invalidateOTP(specialEmail);
    }

    @Test
    @DisplayName("Should validate service component interactions")
    void shouldValidateServiceComponentInteractions() throws Exception {

        assertEquals(0, otpService.getActiveOTPCount(), "Should start with no active OTPs");
        assertFalse(otpService.hasValidOTP(TEST_EMAIL), "Should not have valid OTP initially");

        MfaRequest request = new MfaRequest(TEST_EMAIL, TEST_USER_ID);

        mockMvc.perform(post(BASE_URL + "/send-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        assertEquals(1, otpService.getActiveOTPCount(), "Should have one active OTP");
        assertTrue(otpService.hasValidOTP(TEST_EMAIL), "Should have valid OTP for test email");

        String generatedOtp = testOtpCapture.getLastOtpForEmail(TEST_EMAIL);
        MfaVerifyRequest verifyRequest = new MfaVerifyRequest(TEST_EMAIL, generatedOtp);

        mockMvc.perform(post(BASE_URL + "/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk());

        assertEquals(0, otpService.getActiveOTPCount(), "Should have no active OTPs after verification");
        assertFalse(otpService.hasValidOTP(TEST_EMAIL), "Should not have valid OTP after verification");
    }

    @Test
    @DisplayName("Should handle concurrent OTP operations safely")
    void shouldHandleConcurrentOtpOperationsSafely() throws Exception {
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";

        MfaRequest request1 = new MfaRequest(email1, "user1");
        MfaRequest request2 = new MfaRequest(email2, "user2");

        mockMvc.perform(post(BASE_URL + "/send-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        mockMvc.perform(post(BASE_URL + "/send-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk());

        assertEquals(2, otpService.getActiveOTPCount(), "Should have two active OTPs");
        assertTrue(otpService.hasValidOTP(email1), "Should have valid OTP for user1");
        assertTrue(otpService.hasValidOTP(email2), "Should have valid OTP for user2");

        String otp1 = testOtpCapture.getLastOtpForEmail(email1);
        assertNotNull(otp1, "Should have captured OTP for email1");

        MfaVerifyRequest verifyRequest1 = new MfaVerifyRequest(email1, otp1);

        mockMvc.perform(post(BASE_URL + "/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyRequest1)))
                .andExpect(status().isOk());

        assertEquals(1, otpService.getActiveOTPCount(), "Should have one active OTP remaining");
        assertFalse(otpService.hasValidOTP(email1), "Should not have valid OTP for user1");
        assertTrue(otpService.hasValidOTP(email2), "Should still have valid OTP for user2");

        otpService.invalidateOTP(email2);
    }
}
