package com.stockfellow.userservice.unit;

import com.stockfellow.userservice.dto.UserSyncRequest;
import com.stockfellow.userservice.model.User;
import com.stockfellow.userservice.repository.UserRepository;
import com.stockfellow.userservice.service.UserService;
import com.stockfellow.userservice.service.SouthAfricanIdValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserSyncRequest syncRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUserId("test-user-id");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        syncRequest = new UserSyncRequest();
        syncRequest.setKeycloakId("test-user-id");
        syncRequest.setUsername("testuser");
        syncRequest.setEmail("test@example.com");
        syncRequest.setFirstName("Test");
        syncRequest.setLastName("User");
        syncRequest.setEmailVerified(true);
    }

    @Test
    void createUser_ShouldSaveNewUser() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User createdUser = userService.createUser(testUser);

        assertNotNull(createdUser);
        assertEquals("test-user-id", createdUser.getUserId());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void createOrUpdateUser_ShouldCreateNewUserWhenNotExists() {
        when(userRepository.findByUserId("test-user-id")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.createOrUpdateUser(syncRequest);

        assertNotNull(result);
        assertEquals("test-user-id", result.getUserId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createOrUpdateUser_ShouldUpdateExistingUser() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUserId("test-user-id");
        existingUser.setUsername("oldusername");

        when(userRepository.findByUserId("test-user-id")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.createOrUpdateUser(syncRequest);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void getUserByUserId_ShouldReturnUserWhenExists() {
        when(userRepository.findByUserId("test-user-id")).thenReturn(Optional.of(testUser));

        User foundUser = userService.getUserByUserId("test-user-id");

        assertNotNull(foundUser);
        assertEquals("test-user-id", foundUser.getUserId());
    }

    @Test
    void getUserByUserId_ShouldReturnNullWhenNotExists() {
        when(userRepository.findByUserId("nonexistent-id")).thenReturn(Optional.empty());

        User foundUser = userService.getUserByUserId("nonexistent-id");

        assertNull(foundUser);
    }

    @Test
    void updateIdVerificationStatus_ShouldUpdateUserDetails() {
        when(userRepository.findByUserId("test-user-id")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        SouthAfricanIdValidationService.SouthAfricanIdInfo idInfo = new SouthAfricanIdValidationService.SouthAfricanIdInfo();
        idInfo.setDateOfBirth("1990-01-01");
        idInfo.setGender("Male");
        idInfo.setCitizenship("South African Citizen");

        User updatedUser = userService.updateIdVerificationStatus(
                "test-user-id", "1234567890123", "doc-123", idInfo);

        assertNotNull(updatedUser);
        assertTrue(updatedUser.isIdVerified());
        assertEquals("1234567890123", updatedUser.getIdNumber());
        assertEquals("1990-01-01", updatedUser.getDateOfBirth());
    }

    @Test
    void updateUserAffordabilityTier_ShouldUpdateTierAndConfidence() {
        User user = new User();
        user.setUserId("test-user-id");

        when(userRepository.findByUserId("test-user-id")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.updateUserAffordabilityTier("test-user-id", 3, 0.85);

        assertEquals(3, user.getAffordabilityTier());
        assertEquals(0.85, user.getAffordabilityConfidence());
        assertNotNull(user.getAffordabilityAnalyzedAt());
    }
}