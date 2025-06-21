package com.stockfellow.userservice.service;

import com.stockfellow.userservice.dto.RegisterUserRequest;
import com.stockfellow.userservice.exception.InvalidInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class RegisterUserService {
    private static final Logger logger = LoggerFactory.getLogger(RegisterUserService.class);

    @Autowired
    private EventStoreService eventStoreService;

    @Autowired
    private ReadModelService readModelService;

    public Map<String, Object> execute(String userId, RegisterUserRequest request) {
        if (request.getSaId() == null || request.getSaId().length() != 13 || !request.getSaId().matches("\\d+")) {
            throw new InvalidInputException("Invalid SA ID");
        }
        if (request.getMobileNumber() == null || !request.getMobileNumber().matches("\\+?\\d{10,15}")) {
            throw new InvalidInputException("Invalid mobile number");
        }
        if (request.getName() == null || request.getEmail() == null) {
            throw new InvalidInputException("Missing required fields");
        }

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("userId", userId);
        eventData.put("username", request.getName());
        eventData.put("email", request.getEmail());
        eventData.put("firstName", request.getName().split(" ")[0]);
        eventData.put("lastName", request.getName().split(" ").length > 1 ? request.getName().split(" ")[1] : "");
        eventData.put("emailVerified", false);
        eventData.put("contactNumber", request.getMobileNumber());
        eventData.put("SAId", request.getSaId());
        eventData.put("createdAt", new Date());
        eventData.put("updatedAt", new Date());

        Map<String, Object> event = Map.of("_id", eventStoreService.appendEvent("UserRegistered", eventData).getId());
        readModelService.rebuildState(userId);
        logger.info("User {} registered successfully", userId);

        return event;
    }
}