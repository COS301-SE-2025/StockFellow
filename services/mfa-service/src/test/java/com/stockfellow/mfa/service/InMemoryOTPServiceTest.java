package com.stockfellow.mfa.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InMemoryOTPService Tests")
class InMemoryOTPServiceTest {

    private InMemoryOTPService otpService;

    @BeforeEach
    void setUp() {
        otpService = new InMemoryOTPService();
    }

    @Test
    @DisplayName("Should generate 6-digit OTP")
    void shouldGenerate6DigitOTP() {
       
        String email = "test@example.com";
        String userId = "user123";

      
        String otp = otpService.generateOTP(email, userId);

      
        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));
    }

    @Test
    @DisplayName("Should generate different OTPs for same user")
    void shouldGenerateDifferentOTPsForSameUser() {
        
        String email = "test@example.com";
        String userId = "user123";

      
        String otp1 = otpService.generateOTP(email, userId);
        String otp2 = otpService.generateOTP(email, userId);

     
        assertNotEquals(otp1, otp2);
    }

    @Test
    @DisplayName("Should verify valid OTP")
    void shouldVerifyValidOTP() {
     
        String email = "test@example.com";
        String userId = "user123";
        String otp = otpService.generateOTP(email, userId);

 
        boolean isValid = otpService.verifyOTP(email, otp);

     
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should reject invalid OTP")
    void shouldRejectInvalidOTP() {
   
        String email = "test@example.com";
        String userId = "user123";
        otpService.generateOTP(email, userId);


        boolean isValid = otpService.verifyOTP(email, "000000");


        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject OTP for non-existent email")
    void shouldRejectOTPForNonExistentEmail() {
   
        boolean isValid = otpService.verifyOTP("nonexistent@example.com", "123456");


        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should remove OTP after successful verification")
    void shouldRemoveOTPAfterSuccessfulVerification() {
    
        String email = "test@example.com";
        String userId = "user123";
        String otp = otpService.generateOTP(email, userId);


        otpService.verifyOTP(email, otp);

   
        assertFalse(otpService.hasValidOTP(email));
    }

    @Test
    @DisplayName("Should return true for valid OTP status")
    void shouldReturnTrueForValidOTPStatus() {
  
        String email = "test@example.com";
        String userId = "user123";
        otpService.generateOTP(email, userId);


        boolean hasValidOTP = otpService.hasValidOTP(email);


        assertTrue(hasValidOTP);
    }

    @Test
    @DisplayName("Should return false for non-existent OTP status")
    void shouldReturnFalseForNonExistentOTPStatus() {

        boolean hasValidOTP = otpService.hasValidOTP("nonexistent@example.com");

  
        assertFalse(hasValidOTP);
    }

    @Test
    @DisplayName("Should invalidate existing OTP")
    void shouldInvalidateExistingOTP() {

        String email = "test@example.com";
        String userId = "user123";
        otpService.generateOTP(email, userId);
        assertTrue(otpService.hasValidOTP(email));

        
        otpService.invalidateOTP(email);

    
        assertFalse(otpService.hasValidOTP(email));
    }

    @Test
    @DisplayName("Should return correct active OTP count")
    void shouldReturnCorrectActiveOTPCount() {
    
        assertEquals(0, otpService.getActiveOTPCount());

    
        otpService.generateOTP("user1@example.com", "user1");
        otpService.generateOTP("user2@example.com", "user2");

     
        assertEquals(2, otpService.getActiveOTPCount());
    }

    @Test
    @DisplayName("Should replace existing OTP for same email")
    void shouldReplaceExistingOTPForSameEmail() {
     
        String email = "test@example.com";
        String userId = "user123";

    
        String firstOTP = otpService.generateOTP(email, userId);
        String secondOTP = otpService.generateOTP(email, userId);

      
        assertFalse(otpService.verifyOTP(email, firstOTP));
        assertEquals(1, otpService.getActiveOTPCount()); // Should still be 1, not 2
    }
}