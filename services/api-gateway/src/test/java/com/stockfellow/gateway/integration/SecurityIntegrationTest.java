// package com.stockfellow.gateway.integration;

// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.security.test.context.support.WithMockUser;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.web.servlet.MockMvc;

// import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @SpringBootTest
// @AutoConfigureWebMvc
// @ActiveProfiles("test")
// class SecurityIntegrationTest {
    
//     @Autowired
//     private MockMvc mockMvc;
    
//     @Test
//     void shouldAllowAccessToPublicEndpoints() throws Exception {
//         // Test that public endpoints don't require authentication
        
//         mockMvc.perform(get("/api/users/profile"))
//                 .andExpect(status().isOk()); // or appropriate response based on your setup
        
//         mockMvc.perform(get("/api/group/list"))
//                 .andExpect(status().isOk());
        
//         mockMvc.perform(get("/actuator/health"))
//                 .andExpect(status().isOk());
//     }
    
//     @Test
//     void shouldAllowAuthEndpoints() throws Exception {
//         // Test that authentication endpoints are accessible
        
//         mockMvc.perform(get("/login"))
//                 .andExpect(status().is3xxRedirection()); // Should redirect to Keycloak
        
//         mockMvc.perform(get("/register"))
//                 .andExpect(status().is3xxRedirection()); // Should redirect to Keycloak
        
//         mockMvc.perform(get("/logout"))
//                 .andExpect(status().is3xxRedirection()); // Should redirect to Keycloak
//     }
    
//     @Test
//     void shouldHandleDirectLoginRequests() throws Exception {
//         // Test direct login endpoint
        
//         String loginRequest = "{\"username\":\"testuser\",\"password\":\"testpass\"}";
        
//         mockMvc.perform(post("/auth/login")
//                 .contentType("application/json")
//                 .content(loginRequest))
//                 .andExpect(status().isUnauthorized()); // Expected without valid Keycloak setup
//     }
    
//     @Test
//     void shouldHandleCORSPreflightRequests() throws Exception {
//         // Test CORS preflight requests
        
//         mockMvc.perform(options("/api/users/profile")
//                 .header("Origin", "http://localhost:3001")
//                 .header("Access-Control-Request-Method", "GET")
//                 .header("Access-Control-Request-Headers", "authorization,content-type"))
//                 .andExpect(status().isOk())
//                 .andExpect(header().string("Access-Control-Allow-Origin", "*"))
//                 .andExpect(header().string("Access-Control-Allow-Methods", 
//                     org.hamcrest.Matchers.containsString("GET")));
//     }
    
//     @Test
//     void shouldHandleCSRFForApiRequests() throws Exception {
//         // Test that CSRF is disabled for API endpoints (as configured)
        
//         String jsonData = "{\"name\":\"Test User\"}";
        
//         mockMvc.perform(post("/api/users")
//                 .contentType("application/json")
//                 .content(jsonData))
//                 .andExpect(status().isOk()); // Should not require CSRF token
//     }
// }