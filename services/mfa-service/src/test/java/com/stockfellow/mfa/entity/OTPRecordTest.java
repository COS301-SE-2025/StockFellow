package com.stockfellow.mfa.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OTPRecord Tests")
class OTPRecordTest {

    @Test
    @DisplayName("Should create OTPRecord with correct values")
    void shouldCreateOTPRecordWithCorrectValues() {
  
        String otpCode = "123456";
        String userId = "user123";
        int expiryMinutes = 5;

 
        OTPRecord record = new OTPRecord(otpCode, userId, expiryMinutes);

 
        assertEquals(otpCode, record.getOtpCode());
        assertEquals(userId, record.getUserId());
        assertFalse(record.isVerified());
        assertNotNull(record.getExpiryTime());
        assertTrue(record.getExpiryTime().isAfter(LocalDateTime.now()));
    }

    @Test
    @DisplayName("Should set correct expiry time")
    void shouldSetCorrectExpiryTime() {

        String otpCode = "123456";
        String userId = "user123";
        int expiryMinutes = 10;
        LocalDateTime beforeCreation = LocalDateTime.now();


        OTPRecord record = new OTPRecord(otpCode, userId, expiryMinutes);


        LocalDateTime afterCreation = LocalDateTime.now().plusMinutes(expiryMinutes);
        assertTrue(record.getExpiryTime().isAfter(beforeCreation.plusMinutes(expiryMinutes - 1)));
        assertTrue(record.getExpiryTime().isBefore(afterCreation.plusMinutes(1)));
    }

    @Test
    @DisplayName("Should not be expired when just created")
    void shouldNotBeExpiredWhenJustCreated() {
   
        OTPRecord record = new OTPRecord("123456", "user123", 5);


        assertFalse(record.isExpired());
    }

    @Test
    @DisplayName("Should be expired when expiry time is in the past")
    void shouldBeExpiredWhenExpiryTimeIsInThePast() {
  
        OTPRecord record = new OTPRecord("123456", "user123", 5);
        LocalDateTime pastTime = LocalDateTime.now().minusMinutes(1);
        record.setExpiryTime(pastTime);


        assertTrue(record.isExpired());
    }

    @Test
    @DisplayName("Should get and set OTP code")
    void shouldGetAndSetOTPCode() {

        OTPRecord record = new OTPRecord("123456", "user123", 5);
        String newOtpCode = "654321";

        record.setOtpCode(newOtpCode);


        assertEquals(newOtpCode, record.getOtpCode());
    }

    @Test
    @DisplayName("Should get and set user ID")
    void shouldGetAndSetUserId() {
        
        OTPRecord record = new OTPRecord("123456", "user123", 5);
        String newUserId = "user456";

     
        record.setUserId(newUserId);

  
        assertEquals(newUserId, record.getUserId());
    }

    @Test
    @DisplayName("Should get and set verified status")
    void shouldGetAndSetVerifiedStatus() {
      
        OTPRecord record = new OTPRecord("123456", "user123", 5);

   
        record.setVerified(true);

     
        assertTrue(record.isVerified());

     
        record.setVerified(false);

      
        assertFalse(record.isVerified());
    }

    @Test
    @DisplayName("Should handle null OTP code")
    void shouldHandleNullOTPCode() {
     
        OTPRecord record = new OTPRecord(null, "user123", 5);

   
        assertNull(record.getOtpCode());
    }

    @Test
    @DisplayName("Should handle null user ID")
    void shouldHandleNullUserId() {
  
        OTPRecord record = new OTPRecord("123456", null, 5);

   
        assertNull(record.getUserId());
    }

    @Test
    @DisplayName("Should handle negative expiry minutes")
    void shouldHandleNegativeExpiryMinutes() {

        OTPRecord record = new OTPRecord("123456", "user123", -5);

    
        assertTrue(record.isExpired()); 
    }

    @Test
    @DisplayName("Should handle empty strings")
    void shouldHandleEmptyStrings() {
       
        OTPRecord record = new OTPRecord("", "", 5);

        
        assertEquals("", record.getOtpCode());
        assertEquals("", record.getUserId());
        assertFalse(record.isExpired());
    }
}
