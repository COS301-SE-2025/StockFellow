package com.stockfellow.userservice.service;

import com.stockfellow.userservice.dto.UserSyncRequest;
import com.stockfellow.userservice.model.User;
import com.stockfellow.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SouthAfricanIdValidationService idValidationService;
    
    /**
     * Create or update user from Keycloak sync request
     */
    public User createOrUpdateUser(UserSyncRequest request) {
        logger.info("Creating or updating user: {}", request.getKeycloakId());
        
        Optional<User> existingUser = userRepository.findByUserId(request.getKeycloakId());
        User user;
        
        if (existingUser.isPresent()) {
            user = existingUser.get();
            updateUserFromRequest(user, request);
            logger.info("Updated existing user: {}", request.getKeycloakId());
        } else {
            user = createUserFromRequest(request);
            logger.info("Created new user: {}", request.getKeycloakId());
        }
        
        return userRepository.save(user);
    }
    
    /**
     * Create new user from sync request
     */
    private User createUserFromRequest(UserSyncRequest request) {
        User user = new User();
        user.setUserId(request.getKeycloakId());
        user.setUsername(request.getUsername() != null ? request.getUsername() : "");
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName() != null ? request.getFirstName() : "");
        user.setLastName(request.getLastName() != null ? request.getLastName() : "");
        user.setEmailVerified(request.getEmailVerified() != null ? request.getEmailVerified() : false);
        user.setContactNumber(request.getPhoneNumber() != null ? request.getPhoneNumber() : "");
        user.setIdNumber(request.getIdNumber() != null ? request.getIdNumber() : "");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
    
    /**
     * Update existing user from sync request
     */
    private void updateUserFromRequest(User user, UserSyncRequest request) {
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getEmailVerified() != null) {
            user.setEmailVerified(request.getEmailVerified());
        }
        if (request.getPhoneNumber() != null) {
            user.setContactNumber(request.getPhoneNumber());
        }
        if (request.getIdNumber() != null) {
            user.setIdNumber(request.getIdNumber());
        }
        user.setUpdatedAt(LocalDateTime.now());
    }
    
    /**
     * Get user by Keycloak user ID
     */
    @Transactional(readOnly = true)
    public User getUserByUserId(String userId) {
        return userRepository.findByUserId(userId).orElse(null);
    }
    
    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
    
    /**
     * Get user by ID number
     */
    @Transactional(readOnly = true)
    public User getUserByIdNumber(String idNumber) {
        return userRepository.findByIdNumber(idNumber).orElse(null);
    }
    
    /**
     * Get all users
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Update ID verification status after successful verification
     */
    public User updateIdVerificationStatus(String userId, String idNumber, String alfrescoDocumentId, 
                                          SouthAfricanIdValidationService.SouthAfricanIdInfo idInfo) {
        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIdNumber(idNumber);
            user.setIdVerified(true);
            user.setAlfrescoDocumentId(alfrescoDocumentId);
            
            if (idInfo != null) {
                user.setDateOfBirth(idInfo.getDateOfBirth());
                user.setGender(idInfo.getGender());
                user.setCitizenship(idInfo.getCitizenship());
            }
            
            user.setUpdatedAt(LocalDateTime.now());
            User savedUser = userRepository.save(user);
            logger.info("Updated ID verification status for user: {}", userId);
            return savedUser;
        }
        return null;
    }
    
    /**
     * Check if user exists by Keycloak user ID
     */
    @Transactional(readOnly = true)
    public boolean userExists(String userId) {
        return userRepository.existsByUserId(userId);
    }
    
    /**
     * Check if email exists
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Check if ID number exists
     */
    @Transactional(readOnly = true)
    public boolean idNumberExists(String idNumber) {
        return userRepository.existsByIdNumber(idNumber);
    }
    
    /**
     * Search users by name
     */
    @Transactional(readOnly = true)
    public List<User> searchUsersByName(String name) {
        return userRepository.findByNameContaining(name);
    }
    
    /**
     * Get users by verification status
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByIdVerificationStatus(boolean verified) {
        return userRepository.findByIdVerified(verified);
    }
    
    /**
     * Get verified users
     */
    @Transactional(readOnly = true)
    public List<User> getVerifiedUsers() {
        return userRepository.findVerifiedUsers();
    }
    
    /**
     * Get users needing ID verification
     */
    @Transactional(readOnly = true)
    public List<User> getUsersNeedingIdVerification() {
        return userRepository.findUsersNeedingIdVerification();
    }
    
    /**
     * Get users with incomplete profiles
     */
    @Transactional(readOnly = true)
    public List<User> getUsersWithIncompleteProfiles() {
        return userRepository.findUsersWithIncompleteProfiles();
    }
    
    /**
     * Count users created after specific date
     */
    @Transactional(readOnly = true)
    public long countUsersCreatedAfter(LocalDateTime date) {
        return userRepository.countUsersCreatedAfter(date);
    }
    
    /**
     * Delete user (soft delete by setting active flag if needed)
     */
    public void deleteUser(String userId) {
        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isPresent()) {
            userRepository.delete(userOpt.get());
            logger.info("Deleted user: {}", userId);
        }
    }
}