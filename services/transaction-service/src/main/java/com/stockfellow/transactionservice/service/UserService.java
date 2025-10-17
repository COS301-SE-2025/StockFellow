package com.stockfellow.transactionservice.service;

import com.stockfellow.transactionservice.dto.UpdateUserStatusDto;
import com.stockfellow.transactionservice.dto.SyncUserDto;
import com.stockfellow.transactionservice.repository.UserRepository;
// import com.stockfellow.transactionservice.model.ActivityLog;
import com.stockfellow.transactionservice.model.User;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;


import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    private final RestTemplate restTemplate;
    private static final String GROUP_SERVICE_BASE_URL = "http://api.paystack.co";


    public UserService(RestTemplate restTemplate, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
    }

    public User syncUser(SyncUserDto syncDto) {
        logger.info("Syncing user: {}", syncDto.getUserId());

        // Check if user already exists
        User user = userRepository.findById(syncDto.getUserId()).orElse(null);
        
        if (user != null) {
            // Update existing user
            logger.info("Updating existing user: {}", syncDto.getUserId());
            user.setEmail(syncDto.getEmail());
            user.setFirstName(syncDto.getFirstName());
            user.setLastName(syncDto.getLastName());
            user.setPhone(syncDto.getPhone());
            if (syncDto.getStatus() != null) {
                user.setStatus(syncDto.getStatus());
            }
        } else {
            // Create new user with the provided Keycloak ID
            logger.info("Creating new user with ID: {}", syncDto.getUserId());
            user = new User();
            user.setUserId(syncDto.getUserId()); // Use Keycloak ID
            user.setEmail(syncDto.getEmail());
            user.setFirstName(syncDto.getFirstName());
            user.setLastName(syncDto.getLastName());
            user.setPhone(syncDto.getPhone());
            user.setStatus(syncDto.getStatus() != null ? syncDto.getStatus() : User.UserStatus.active);
        }

        user = userRepository.save(user);

        // activityLogService.logActivity(
        //     syncDto.getUserId(), 
        //     ActivityLog.EntityType.USER,
        //     user.getUserId(),
        //     user == null ? "USER_CREATED" : "USER_UPDATED", 
        //     null, 
        //     null
        // );

        logger.info("User synced successfully with ID: {}", user.getUserId());
        return user;
    }

    public User findById(UUID userId){
        logger.info("Searching for user with UserId: {}", userId);
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    public User updateUserStatus(UUID userId, UpdateUserStatusDto status){
        logger.info("Updating user details with UserId: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        user.setStatus(status.getStatus());

        user = userRepository.save(user);
        return user;
    }

    public List<User> syncUsers(List<SyncUserDto> syncDtos) {
        logger.info("Syncing {} users", syncDtos.size());
        List<User> userList = new ArrayList<>();

        for(SyncUserDto userDto: syncDtos){
            User syncedUser = syncUser(userDto);
            userList.add(syncedUser);
        }

        return userList;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }


    public List<User> fetchUsers(String groupId) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<List<User>> response = restTemplate.exchange(
                GROUP_SERVICE_BASE_URL + "/transfer/" + groupId,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<User>>() {}
            );
            
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error fetching users: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch users");
        }
    }
}
