package com.stockfellow.demoservice.orchestrator;

import com.stockfellow.demoservice.dto.DemoRequest;
import com.stockfellow.demoservice.dto.RegisterRequest;
import com.stockfellow.demoservice.dto.RegistrationResponse;
import com.stockfellow.demoservice.orchestrator.UserRegistrationService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

@Service
public class DemoOrchestrator {
    @Autowired
    private UserRegistrationService userRegistrationService;
    private static final Logger logger = LoggerFactory.getLogger(DemoOrchestrator.class);
    
    private final RestTemplate restTemplate;
    private String baseUrl = "http://10.0.2.2:3000/api";
    
    //Constructor
    public DemoOrchestrator(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }
    
    public String startDemo(DemoRequest session){
        if (!session.getScenarioType().equals("Group Cycles")) {
            return "Currently only support: Group Cycles";
        }
       
        executeDemo(session);

        return "";
    }
    
    private void executeDemo(DemoRequest session) {
        try {
            // Step 1: Create users via gateway registration
            updateStatus(session, "Registering 5 demo users via gateway...");
            List<RegistrationResponse> registrationResponses = userRegistrationService.registerUsers();
            
            // Check if all registrations were successful
            List<RegistrationResponse> successfulRegistrations = registrationResponses.stream()
                .filter(response -> response.getError() == null)
                .collect(Collectors.toList());
            
            if (successfulRegistrations.isEmpty()) {
                throw new RuntimeException("Failed to register any users");
            }
            
            logger.info("Successfully registered {} out of {} users", 
                successfulRegistrations.size(), registrationResponses.size());
            
            // Continue with rest of demo...
            updateStatus(session, "Registering payment details...");

            
        } catch (Exception e) {
            session.setStatus("FAILED");
            session.setMessage(e.getMessage());
            updateStatus(session, "Demo failed: " + e.getMessage());
        }
    }
    
    public void updateStatus(DemoRequest session, String status){
        
    }

    public void cleanup(String sessionId){

    }
}