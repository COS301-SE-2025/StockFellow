// package com.stockfellow.mfa.service;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.ArgumentCaptor;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.mail.SimpleMailMessage;
// import org.springframework.mail.javamail.JavaMailSender;
// import org.springframework.test.util.ReflectionTestUtils;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// @DisplayName("EmailService Tests")
// class EmailServiceTest {

//     @Mock
//     private JavaMailSender mailSender;

//     private EmailService emailService;

//     @BeforeEach
//     void setUp() {
//         emailService = new EmailService(mailSender);
//         ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@stockfellow.com");
//         ReflectionTestUtils.setField(emailService, "mailUsername", "test@gmail.com");
//     }

//     @Test
//     @DisplayName("Should send OTP email successfully")
//     void shouldSendOTPEmailSuccessfully() {
        
//         String toEmail = "user@example.com";
//         String otpCode = "123456";
//         String userName = "John Doe";

      
//         emailService.sendOTP(toEmail, otpCode, userName);

   
//         verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
//     }

//     @Test
//     @DisplayName("Should send OTP email with correct content")
//     void shouldSendOTPEmailWithCorrectContent() {
      
//         String toEmail = "user@example.com";
//         String otpCode = "123456";
//         String userName = "John Doe";
//         ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

  
//         emailService.sendOTP(toEmail, otpCode, userName);


//         verify(mailSender).send(messageCaptor.capture());
//         SimpleMailMessage sentMessage = messageCaptor.getValue();

//         assertEquals("noreply@stockfellow.com", sentMessage.getFrom());
//         assertEquals(toEmail, sentMessage.getTo()[0]);
//         assertEquals("StockFellow - Your Verification Code", sentMessage.getSubject());
//         assertTrue(sentMessage.getText().contains(otpCode));
//         assertTrue(sentMessage.getText().contains(userName));
//     }

//     @Test
//     @DisplayName("Should handle null userName")
//     void shouldHandleNullUserName() {
   
//         String toEmail = "user@example.com";
//         String otpCode = "123456";
//         String userName = null;

   
//         assertDoesNotThrow(() -> emailService.sendOTP(toEmail, otpCode, userName));
//         verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
//     }

//     @Test
//     @DisplayName("Should throw RuntimeException when mail sending fails")
//     void shouldThrowRuntimeExceptionWhenMailSendingFails() {
    
//         String toEmail = "user@example.com";
//         String otpCode = "123456";
//         String userName = "John Doe";

//         doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(SimpleMailMessage.class));

      
//         RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//             emailService.sendOTP(toEmail, otpCode, userName);
//         });

//         assertTrue(exception.getMessage().contains("Failed to send OTP email"));
//     }

//     @Test
//     @DisplayName("Should handle empty OTP code gracefully")
//     void shouldHandleEmptyOTPCodeGracefully() {

//         String toEmail = "user@example.com";
//         String otpCode = "";
//         String userName = "John Doe";

  
//         assertDoesNotThrow(() -> emailService.sendOTP(toEmail, otpCode, userName));
//         verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
//     }

//     @Test
//     @DisplayName("Should handle special characters in userName")
//     void shouldHandleSpecialCharactersInUserName() {

//         String toEmail = "user@example.com";
//         String otpCode = "123456";
//         String userName = "João O'Sullivan";


//         assertDoesNotThrow(() -> emailService.sendOTP(toEmail, otpCode, userName));
//         verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
//     }
// }