package com.stockfellow.demoservice.orchestrator;

import com.stockfellow.demoservice.dto.RegisterRequest;
import com.stockfellow.demoservice.dto.RegistrationResponse;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


@Service
public class UserRegistrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserRegistrationService.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    private String baseUrl = "http://10.0.2.2:3000/api";
    
    public List<RegistrationResponse> registerUsers() {
        String[] usernames = {
            "testuser1", "testuser2", "testuser3", "testuser4", "testuser5"
        };
        String[] firstNames = {
            "User1", "User2", "User3", "User4", "User5"
        };
        String[] lastNames = {
            "One", "Two", "Three", "Four", "Five"
        };
        String[] userEmails = {
            "testuser1@mail.com", "testuser2@mail.com", "testuser3@mail.com", 
            "testuser4@mail.com", "testuser5@mail.com"
        };
        String userPassword = "pass123!";
        String userPhone = "0829458879";
        String userId = "0302045129087";
        
        List<RegistrationResponse> responses = new ArrayList<>();
        
        for (int i = 0; i < usernames.length; i++) {
            try {
                RegisterRequest request = new RegisterRequest(
                    usernames[i],
                    firstNames[i],
                    lastNames[i],
                    userEmails[i],
                    userPassword,
                    userPhone + i, // Make phone numbers unique
                    userId + i     // Make ID numbers unique
                );
                
                HttpHeaders headers = createHeadersWithoutAuth();
                headers.setContentType(MediaType.APPLICATION_JSON);
                
                HttpEntity<RegisterRequest> entity = new HttpEntity<>(request, headers);
                ResponseEntity<RegistrationResponse> response = restTemplate.exchange(
                    baseUrl + "/auth/register",
                    HttpMethod.POST,
                    entity,
                    RegistrationResponse.class
                );
                
                RegistrationResponse registrationResponse = response.getBody();
                responses.add(registrationResponse);
                
                logger.info("Successfully registered user: {}", usernames[i]);
                
            } catch (HttpClientErrorException e) {
                // Handle 4xx errors (user already exists, validation errors)
                logger.warn("Failed to register user {}: {} - {}", 
                    usernames[i], e.getStatusCode(), e.getResponseBodyAsString());
                
                RegistrationResponse errorResponse = new RegistrationResponse();
                errorResponse.setError("Registration failed: " + e.getResponseBodyAsString());
                responses.add(errorResponse);
                
            } catch (Exception e) {
                // Handle other errors
                logger.error("Error registering user {}: ", usernames[i], e);
                
                RegistrationResponse errorResponse = new RegistrationResponse();
                errorResponse.setError("Registration failed: " + e.getMessage());
                responses.add(errorResponse);
            }
        }
        
        return responses;
    }
    
    private HttpHeaders createHeadersWithoutAuth() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }
}
