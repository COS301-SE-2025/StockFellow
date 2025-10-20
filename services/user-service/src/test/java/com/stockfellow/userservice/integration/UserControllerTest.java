package com.stockfellow.userservice.integration;


import com.stockfellow.userservice.controller.UserController;
import com.stockfellow.userservice.dto.AffordabilityTierResult;
import com.stockfellow.userservice.dto.BankStatementUploadRequest;
import com.stockfellow.userservice.model.User;
import com.stockfellow.userservice.service.UserService;
import com.stockfellow.userservice.service.AffordabilityTierService;
import com.stockfellow.userservice.service.AlfrescoService;
import com.stockfellow.userservice.service.SouthAfricanIdValidationService;
import com.stockfellow.userservice.service.PdfIdExtractionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.*;
import com.stockfellow.userservice.model.BankTransaction;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = UserController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
    })

public class UserControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockBean
//     private UserService userService;

//     @MockBean
//     private AffordabilityTierService affordabilityTierService;

//     @MockBean
//     private SouthAfricanIdValidationService idValidationService;

//     @MockBean
//     private PdfIdExtractionService pdfExtractionService;

//     @MockBean
//     private AlfrescoService alfrescoService;

//     private User testUser;

//     @BeforeEach
//     void setUp() {
//         testUser = new User();
//         testUser.setId(1L);
//         testUser.setUserId("test-user-id");
//         testUser.setUsername("testuser");
//         testUser.setEmail("test@example.com");
//         testUser.setFirstName("Test");
//         testUser.setLastName("User");
//         testUser.setCreatedAt(LocalDateTime.now());
//         testUser.setUpdatedAt(LocalDateTime.now());
//         Mockito.reset(userService);
//         Mockito.reset(affordabilityTierService);
//         Mockito.reset(idValidationService);
//         Mockito.reset(pdfExtractionService);
//         Mockito.reset(alfrescoService);
//     }

//     @Test
//     void getServiceInfo_ShouldReturnServiceDetails() throws Exception {
//         mockMvc.perform(get("/api/users"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.service").value("User Service"))
//                 .andExpect(jsonPath("$.version").exists())
//                 .andExpect(jsonPath("$.database").exists())
//                 .andExpect(jsonPath("$.endpoints").isArray());
//     }

//     @Test
//     void registerUser_ShouldCreateNewUser() throws Exception {
//         Mockito.when(userService.getUserByUserId("test-user-id")).thenReturn(null);
//         Mockito.when(userService.getUserByEmail("test@example.com")).thenReturn(null);
//         Mockito.when(userService.createUser(Mockito.any(User.class))).thenReturn(testUser);

//         String requestBody = "{\"userId\":\"test-user-id\",\"username\":\"testuser\",\"email\":\"test@example.com\",\"firstName\":\"Test\",\"lastName\":\"User\"}";

//         mockMvc.perform(post("/api/users/register")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(requestBody))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.success").value(true))
//                 .andExpect(jsonPath("$.user.userId").value("test-user-id"))
//                 .andExpect(jsonPath("$.user.email").value("test@example.com"));
//     }

//     @Test
//     void registerUser_ShouldReturnConflictWhenUserExists() throws Exception {
//         Mockito.when(userService.getUserByUserId("test-user-id")).thenReturn(testUser);

//         String requestBody = "{\"userId\":\"test-user-id\",\"username\":\"testuser\",\"email\":\"test@example.com\"}";

//         mockMvc.perform(post("/api/users/register")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(requestBody))
//                 .andExpect(status().isConflict())
//                 .andExpect(jsonPath("$.error").value("User already exists"));
//     }

//     @Test
//     void getProfile_ShouldReturnUserProfile() throws Exception {
//         Mockito.when(userService.getUserByUserId("test-user-id")).thenReturn(testUser);

//         mockMvc.perform(get("/api/users/profile")
//                         .header("X-User-Id", "test-user-id")
//                         .header("X-User-Name", "testuser"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.user.userId").value("test-user-id"))
//                 .andExpect(jsonPath("$.affordability.tier").exists());
//     }

//     @Test
//    void verifyID_ShouldReturnSuccessForValidId() throws Exception {
//         // Mock the PDF extraction to return a valid ID number
//         Mockito.when(pdfExtractionService.extractIdNumberFromPdf(Mockito.any()))
//                .thenReturn("8001015009087");
        
//         // Mock the ID validation to return true
//         Mockito.when(idValidationService.validateSouthAfricanId("8001015009087"))
//                .thenReturn(true);
        
//         // Mock the user service calls
//         Mockito.when(userService.getUserByUserId("test-user-id")).thenReturn(testUser);
//         Mockito.when(userService.getUserByIdNumber("8001015009087")).thenReturn(null);
        
//         // Mock the ID info extraction
//         SouthAfricanIdValidationService.SouthAfricanIdInfo idInfo = new SouthAfricanIdValidationService.SouthAfricanIdInfo();
//         idInfo.setDateOfBirth("1980-01-01");
//         idInfo.setGender("Male");
//         idInfo.setCitizenship("South African Citizen");
//         Mockito.when(idValidationService.extractIdInfo("8001015009087"))
//                .thenReturn(idInfo);
        
//         // Mock the user update
//         Mockito.when(userService.updateIdVerificationStatus(
//             Mockito.eq("test-user-id"),
//             Mockito.eq("8001015009087"),
//             Mockito.anyString(),
//             Mockito.any(SouthAfricanIdValidationService.SouthAfricanIdInfo.class)
//         )).thenReturn(testUser);
        
//         MockMultipartFile file = new MockMultipartFile(
//             "file", 
//             "test.pdf", 
//             "application/pdf", 
//             "test content".getBytes()
//         );

//         mockMvc.perform(MockMvcRequestBuilders.multipart("/api/users/verifyID")
//                 .file(file)
//                 .param("userId", "test-user-id"))
//             .andExpect(status().isOk())
//             .andExpect(jsonPath("$.success").value(true))
//             .andExpect(jsonPath("$.extractedInfo.dateOfBirth").exists());
//     }

    
//     @Test
//     void analyzeAffordability_ShouldReturnTierResult() throws Exception {
//         BankStatementUploadRequest request = new BankStatementUploadRequest();
//         List<BankTransaction> transactions = Collections.nCopies(50, new BankTransaction());
//         request.setTransactions(transactions);

//         AffordabilityTierResult result = new AffordabilityTierResult();
//         result.setTier(3);
//         result.setConfidence(0.85);
//         result.setTierName("Balanced Savers");

//         Mockito.when(affordabilityTierService.analyzeBankStatements(Mockito.anyString(), Mockito.anyList()))
//                 .thenReturn(result);
//         Mockito.when(userService.getUserByUserId("test-user-id")).thenReturn(testUser);
//         Mockito.doNothing().when(userService).updateUserAffordabilityTier(
//             Mockito.anyString(), 
//             Mockito.anyInt(), 
//             Mockito.anyDouble()
//         );

//         String requestBody = "{\"transactions\": [{\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}, {\"amount\": 100.0, \"description\": \"Test\"}]}";
//         mockMvc.perform(post("/api/users/affordability/analyze")
//                         .header("X-User-Id", "test-user-id")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(requestBody))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.result.tier").value(3))
//                 .andExpect(jsonPath("$.result.confidence").exists());
//     }

//     @Test
//     void getUserById_ShouldReturnUserForAdmin() throws Exception {
//         Mockito.when(userService.getUserByUserId("test-user-id")).thenReturn(testUser);

//         mockMvc.perform(get("/api/users/test-user-id")
//                         .header("X-User-Id", "admin-user")
//                         .header("X-User-Roles", "admin"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.userId").value("test-user-id"));
//     }

//     @Test
//     void getUserById_ShouldDenyAccessForNonAdmin() throws Exception {
//         mockMvc.perform(get("/api/users/test-user-id")
//                         .header("X-User-Id", "other-user")
//                         .header("X-User-Roles", "user"))
//                 .andExpect(status().isForbidden());
//     }
}
