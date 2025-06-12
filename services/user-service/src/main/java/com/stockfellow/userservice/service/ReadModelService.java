package com.stockfellow.userservice.service;

import com.stockfellow.userservice.model.Event;
import com.stockfellow.userservice.model.User;
import com.stockfellow.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ReadModelService {
    @Autowired
    private EventStoreService eventStoreService;

    @Autowired
    private UserRepository userRepository;

    public void rebuildState(String userId) {
        List<Event> events = eventStoreService.getEvents(userId);
        User userData = null;

        for (Event event : events) {
            Map<String, Object> data = event.getData();
            if ("UserRegistered".equals(event.getEventType())) {
                userData = new User();
                userData.setUserId((String) data.get("userId"));
                userData.setUsername((String) data.getOrDefault("username", ""));
                userData.setEmail((String) data.get("email"));
                userData.setFirstName((String) data.getOrDefault("firstName", ""));
                userData.setLastName((String) data.getOrDefault("lastName", ""));
                userData.setEmailVerified((Boolean) data.getOrDefault("emailVerified", false));
                userData.setContactNumber((String) data.getOrDefault("contactNumber", ""));
                userData.setIdNumber((String) data.getOrDefault("idNumber", ""));
                userData.setCreatedAt((Date) data.getOrDefault("createdAt", new Date()));
                userData.setUpdatedAt((Date) data.getOrDefault("updatedAt", new Date()));
            } else if ("UserUpdated".equals(event.getEventType()) && userData != null) {
                userData.setUsername(data.containsKey("username") ? (String) data.get("username") : userData.getUsername());
                userData.setEmail(data.containsKey("email") ? (String) data.get("email") : userData.getEmail());
                userData.setFirstName(data.containsKey("firstName") ? (String) data.get("firstName") : userData.getFirstName());
                userData.setLastName(data.containsKey("lastName") ? (String) data.get("lastName") : userData.getLastName());
                userData.setEmailVerified(data.containsKey("emailVerified") ? (Boolean) data.get("emailVerified") : userData.isEmailVerified());
                userData.setContactNumber(data.containsKey("contactNumber") ? (String) data.get("contactNumber") : userData.getContactNumber());
                userData.setIdNumber(data.containsKey("idNumber") ? (String) data.get("idNumber") : userData.getIdNumber());
                userData.setUpdatedAt((Date) data.getOrDefault("updatedAt", event.getTimestamp()));
            }
        }

        if (userData != null) {
            userRepository.save(userData);
            System.out.println("Read model updated for user: " + userData.getUserId());
        }
    }

    public User getUser(String userId) {
        return userRepository.findByUserId(userId);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}