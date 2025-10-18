package com.stockfellow.transactionservice.service;

import com.stockfellow.transactionservice.dto.UpdateUserStatusDto;
import com.stockfellow.transactionservice.dto.SyncUserDto;
import com.stockfellow.transactionservice.model.User;
import com.stockfellow.transactionservice.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UserService userService;

    private UUID testUserId;
    private SyncUserDto syncUserDto;
    private User existingUser;
    private UpdateUserStatusDto updateStatusDto;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        
        syncUserDto = new SyncUserDto();
        syncUserDto.setUserId(testUserId);
        syncUserDto.setEmail("test@example.com");
        syncUserDto.setFirstName("John");
        syncUserDto.setLastName("Doe");
        syncUserDto.setPhone("+1234567890");
        syncUserDto.setStatus(User.UserStatus.ACTIVE);

        existingUser = new User();
        existingUser.setUserId(testUserId);
        existingUser.setEmail("old@example.com");
        existingUser.setFirstName("Jane");
        existingUser.setLastName("Smith");
        existingUser.setPhone("+0987654321");
        existingUser.setStatus(User.UserStatus.ACTIVE);

        updateStatusDto = new UpdateUserStatusDto();
        updateStatusDto.setStatus(User.UserStatus.INACTIVE);
    }

    @Test
    void syncUser_WhenUserExists_ShouldUpdateExistingUser() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        User result = userService.syncUser(syncUserDto);

        // Then
        assertNotNull(result);
        assertEquals(syncUserDto.getEmail(), result.getEmail());
        assertEquals(syncUserDto.getFirstName(), result.getFirstName());
        assertEquals(syncUserDto.getLastName(), result.getLastName());
        assertEquals(syncUserDto.getPhone(), result.getPhone());
        assertEquals(syncUserDto.getStatus(), result.getStatus());

        verify(userRepository).findById(testUserId);
        verify(userRepository).save(existingUser);
    }

    @Test
    void syncUser_WhenUserDoesNotExist_ShouldCreateNewUser() {
        // Given
        User newUser = new User();
        newUser.setUserId(testUserId);
        newUser.setEmail(syncUserDto.getEmail());
        newUser.setFirstName(syncUserDto.getFirstName());
        newUser.setLastName(syncUserDto.getLastName());
        newUser.setPhone(syncUserDto.getPhone());
        newUser.setStatus(syncUserDto.getStatus());

        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // When
        User result = userService.syncUser(syncUserDto);

        // Then
        assertNotNull(result);
        assertEquals(testUserId, result.getUserId());
        assertEquals(syncUserDto.getEmail(), result.getEmail());
        assertEquals(syncUserDto.getFirstName(), result.getFirstName());
        assertEquals(syncUserDto.getLastName(), result.getLastName());
        assertEquals(syncUserDto.getPhone(), result.getPhone());
        assertEquals(syncUserDto.getStatus(), result.getStatus());

        verify(userRepository).findById(testUserId);
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertEquals(testUserId, capturedUser.getUserId());
        assertEquals(syncUserDto.getEmail(), capturedUser.getEmail());
    }

    @Test
    void syncUser_WhenUserDoesNotExistAndStatusIsNull_ShouldCreateUserWithActiveStatus() {
        // Given
        syncUserDto.setStatus(null);
        User newUser = new User();
        newUser.setUserId(testUserId);
        newUser.setStatus(User.UserStatus.ACTIVE);

        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // When
        User result = userService.syncUser(syncUserDto);

        // Then
        assertNotNull(result);
        assertEquals(User.UserStatus.ACTIVE, result.getStatus());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertEquals(User.UserStatus.ACTIVE, capturedUser.getStatus());
    }

    @Test
    void syncUser_WhenUserExistsAndStatusIsNull_ShouldNotUpdateStatus() {
        // Given
        syncUserDto.setStatus(null);
        User.UserStatus originalStatus = existingUser.getStatus();
        
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        User result = userService.syncUser(syncUserDto);

        // Then
        assertEquals(originalStatus, result.getStatus());
        verify(userRepository).save(existingUser);
    }

    @Test
    void findById_WhenUserExists_ShouldReturnUser() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(existingUser));

        // When
        User result = userService.findById(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(existingUser, result);
        verify(userRepository).findById(testUserId);
    }

    @Test
    void findById_WhenUserDoesNotExist_ShouldThrowRuntimeException() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.findById(testUserId));
        
        assertEquals("User not found with ID: " + testUserId, exception.getMessage());
        verify(userRepository).findById(testUserId);
    }

    @Test
    void updateUserStatus_WhenUserExists_ShouldUpdateStatus() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        User result = userService.updateUserStatus(testUserId, updateStatusDto);

        // Then
        assertNotNull(result);
        assertEquals(updateStatusDto.getStatus(), result.getStatus());
        verify(userRepository).findById(testUserId);
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUserStatus_WhenUserDoesNotExist_ShouldThrowRuntimeException() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.updateUserStatus(testUserId, updateStatusDto));
        
        assertEquals("User not found with ID: " + testUserId, exception.getMessage());
        verify(userRepository).findById(testUserId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void syncUsers_WhenProvidedMultipleUsers_ShouldSyncAllUsers() {
        // Given
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        
        SyncUserDto dto1 = new SyncUserDto();
        dto1.setUserId(userId1);
        dto1.setEmail("user1@example.com");
        dto1.setFirstName("User");
        dto1.setLastName("One");
        
        SyncUserDto dto2 = new SyncUserDto();
        dto2.setUserId(userId2);
        dto2.setEmail("user2@example.com");
        dto2.setFirstName("User");
        dto2.setLastName("Two");

        List<SyncUserDto> syncDtos = Arrays.asList(dto1, dto2);

        User user1 = new User();
        user1.setUserId(userId1);
        user1.setEmail("user1@example.com");

        User user2 = new User();
        user2.setUserId(userId2);
        user2.setEmail("user2@example.com");

        when(userRepository.findById(userId1)).thenReturn(Optional.empty());
        when(userRepository.findById(userId2)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user1).thenReturn(user2);

        // When
        List<User> result = userService.syncUsers(syncDtos);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository, times(2)).findById(any());
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void syncUsers_WhenEmptyList_ShouldReturnEmptyList() {
        // Given
        List<SyncUserDto> emptyList = Arrays.asList();

        // When
        List<User> result = userService.syncUsers(emptyList);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    // @Test
    // void fetchUsers_WhenSuccessful_ShouldReturnUserList() {
    //     // Given
    //     String groupId = UUID.randomUUID().toString();
    //     List<User> expectedUsers = Arrays.asList(existingUser);
    //     ResponseEntity<List<User>> responseEntity = new ResponseEntity<>(expectedUsers, HttpStatus.OK);

    //     when(restTemplate.exchange(
    //         eq("http://api.paystack.co/transfer/" + groupId),
    //         eq(HttpMethod.GET),
    //         any(HttpEntity.class),
    //         any(ParameterizedTypeReference.class)
    //     )).thenReturn(responseEntity);

    //     // When
    //     List<User> result = userService.fetchUsers(groupId);

    //     // Then
    //     assertNotNull(result);
    //     assertEquals(expectedUsers, result);
    //     verify(restTemplate).exchange(
    //         eq("http://api.paystack.co/transfer/" + groupId),
    //         eq(HttpMethod.GET),
    //         any(HttpEntity.class),
    //         any(ParameterizedTypeReference.class)
    //     );
    // }

    // @Test
    // void fetchUsers_WhenRestTemplateThrowsException_ShouldThrowResponseStatusException() {
    //     // Given
    //     String groupId = UUID.randomUUID().toString();
    //     when(restTemplate.exchange(
    //         anyString(),
    //         eq(HttpMethod.GET),
    //         any(HttpEntity.class),
    //         any(ParameterizedTypeReference.class)
    //     )).thenThrow(new RuntimeException("Connection failed"));

    //     // When & Then
    //     ResponseStatusException exception = assertThrows(ResponseStatusException.class,
    //         () -> userService.fetchUsers(groupId));
        
    //     assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    //     assertEquals("Failed to fetch users", exception.getReason());
    // }

    // @Test
    // void fetchUsers_WhenResponseBodyIsNull_ShouldReturnNull() {
    //     // Given
    //     String groupId = UUID.randomUUID().toString();
    //     ResponseEntity<List<User>> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

    //     when(restTemplate.exchange(
    //         eq("http://api.paystack.co/transfer/" + groupId),
    //         eq(HttpMethod.GET),
    //         any(HttpEntity.class),
    //         any(ParameterizedTypeReference.class)
    //     )).thenReturn(responseEntity);

    //     // When
    //     List<User> result = userService.fetchUsers(groupId);

    //     // Then
    //     assertNull(result);
    // }
}