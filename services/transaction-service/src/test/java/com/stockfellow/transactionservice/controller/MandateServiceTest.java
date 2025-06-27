package com.stockfellow.transactionservice.service;

import com.stockfellow.transactionservice.dto.CreateMandateRequest;
import com.stockfellow.transactionservice.dto.MandateResponse;
import com.stockfellow.transactionservice.model.Mandate;
import com.stockfellow.transactionservice.repository.MandateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MandateService Tests")
class MandateServiceTest {

    @Mock
    private MandateRepository mandateRepository;

    @InjectMocks
    private MandateService mandateService;

    private UUID testUserId;
    private UUID testGroupId;
    private UUID testPaymentMethodId;
    private UUID testMandateId;
    private CreateMandateRequest validRequest;
    private Mandate testMandate;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testGroupId = UUID.randomUUID();
        testPaymentMethodId = UUID.randomUUID();
        testMandateId = UUID.randomUUID();

        validRequest = new CreateMandateRequest();
        validRequest.setPayerUserId(testUserId);
        validRequest.setGroupId(testGroupId);
        validRequest.setPaymentMethodId(testPaymentMethodId);
        validRequest.setDocumentReference("DOC123");
        validRequest.setIpAddress("192.168.1.1");

        testMandate = new Mandate();
        testMandate.setMandateId(testMandateId);
        testMandate.setPayerUserId(testUserId);
        testMandate.setGroupId(testGroupId);
        testMandate.setPaymentMethodId(testPaymentMethodId);
        testMandate.setStatus("ACTIVE");
        testMandate.setSignedDate(LocalDateTime.now());
        testMandate.setDocumentReference("DOC123");
        testMandate.setIpAddress("192.168.1.1");
    }

    @Test
    @DisplayName("Should create mandate successfully when valid request is provided")
    void createMandate_WithValidRequest_ShouldReturnCreatedMandate() {
        // Given
        when(mandateRepository.existsByPayerUserIdAndGroupId(testUserId, testGroupId)).thenReturn(false);
        when(mandateRepository.save(any(Mandate.class))).thenReturn(testMandate);

        // When
        Mandate result = mandateService.createMandate(validRequest);

        // Then
        assertNotNull(result);
        assertEquals(testUserId, result.getPayerUserId());
        assertEquals(testGroupId, result.getGroupId());
        assertEquals(testPaymentMethodId, result.getPaymentMethodId());
        assertEquals("ACTIVE", result.getStatus());
        assertEquals("DOC123", result.getDocumentReference());
        assertEquals("192.168.1.1", result.getIpAddress());
        assertNotNull(result.getSignedDate());

        verify(mandateRepository).existsByPayerUserIdAndGroupId(testUserId, testGroupId);
        verify(mandateRepository).save(any(Mandate.class));
    }

    @Test
    @DisplayName("Should throw exception when mandate already exists for user and group")
    void createMandate_WhenMandateAlreadyExists_ShouldThrowException() {
        // Given
        when(mandateRepository.existsByPayerUserIdAndGroupId(testUserId, testGroupId)).thenReturn(true);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> mandateService.createMandate(validRequest));
        
        assertEquals("Mandate already exists for user " + testUserId + " in group " + testGroupId, 
            exception.getMessage());
        
        verify(mandateRepository).existsByPayerUserIdAndGroupId(testUserId, testGroupId);
        verify(mandateRepository, never()).save(any(Mandate.class));
    }

    @Test
    @DisplayName("Should throw exception when payer user ID is null")
    void createMandate_WithNullPayerUserId_ShouldThrowException() {
        // Given
        validRequest.setPayerUserId(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> mandateService.createMandate(validRequest));
        
        assertEquals("Payer user ID cannot be null", exception.getMessage());
        verify(mandateRepository, never()).save(any(Mandate.class));
    }

    @Test
    @DisplayName("Should throw exception when group ID is null")
    void createMandate_WithNullGroupId_ShouldThrowException() {
        // Given
        validRequest.setGroupId(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> mandateService.createMandate(validRequest));
        
        assertEquals("Group ID cannot be null", exception.getMessage());
        verify(mandateRepository, never()).save(any(Mandate.class));
    }

    @Test
    @DisplayName("Should throw exception when payment method ID is null")
    void createMandate_WithNullPaymentMethodId_ShouldThrowException() {
        // Given
        validRequest.setPaymentMethodId(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> mandateService.createMandate(validRequest));
        
        assertEquals("Payment method ID cannot be null", exception.getMessage());
        verify(mandateRepository, never()).save(any(Mandate.class));
    }

    @Test
    @DisplayName("Should throw exception when IP address is null")
    void createMandate_WithNullIpAddress_ShouldThrowException() {
        // Given
        validRequest.setIpAddress(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> mandateService.createMandate(validRequest));
        
        assertEquals("IP address cannot be null or empty", exception.getMessage());
        verify(mandateRepository, never()).save(any(Mandate.class));
    }

    @Test
    @DisplayName("Should throw exception when IP address is empty")
    void createMandate_WithEmptyIpAddress_ShouldThrowException() {
        // Given
        validRequest.setIpAddress("   ");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> mandateService.createMandate(validRequest));
        
        assertEquals("IP address cannot be null or empty", exception.getMessage());
        verify(mandateRepository, never()).save(any(Mandate.class));
    }

    @Test
    @DisplayName("Should return all mandates successfully")
    void getAllMandates_ShouldReturnAllMandates() {
        // Given
        List<Mandate> mandates = Arrays.asList(testMandate);
        when(mandateRepository.findAll()).thenReturn(mandates);

        // When
        List<MandateResponse> result = mandateService.getAllMandates();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mandateRepository).findAll();
    }

    @Test
    @DisplayName("Should return mandate by ID when found")
    void getMandateById_WithValidId_ShouldReturnMandate() {
        // Given
        when(mandateRepository.findById(testMandateId)).thenReturn(Optional.of(testMandate));

        // When
        Mandate result = mandateService.getMandateById(testMandateId);

        // Then
        assertNotNull(result);
        assertEquals(testMandateId, result.getMandateId());
        verify(mandateRepository).findById(testMandateId);
    }

    @Test
    @DisplayName("Should throw exception when mandate not found by ID")
    void getMandateById_WithInvalidId_ShouldThrowException() {
        // Given
        when(mandateRepository.findById(testMandateId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> mandateService.getMandateById(testMandateId));
        
        assertEquals("Mandate not found with ID: " + testMandateId, exception.getMessage());
        verify(mandateRepository).findById(testMandateId);
    }

    @Test
    @DisplayName("Should deactivate mandate successfully")
    void deactivateMandate_WithValidId_ShouldDeactivateMandate() {
        // Given
        when(mandateRepository.findById(testMandateId)).thenReturn(Optional.of(testMandate));
        when(mandateRepository.save(any(Mandate.class))).thenReturn(testMandate);

        // When
        mandateService.deactivateMandate(testMandateId);

        // Then
        verify(mandateRepository).findById(testMandateId);
        verify(mandateRepository).save(argThat(mandate -> "INACTIVE".equals(mandate.getStatus())));
    }

    @Test
    @DisplayName("Should return mandates by group ID")
    void getMandatesByGroup_WithValidGroupId_ShouldReturnMandates() {
        // Given
        List<Mandate> mandates = Arrays.asList(testMandate);
        when(mandateRepository.findByGroupId(testGroupId)).thenReturn(mandates);

        // When
        List<MandateResponse> result = mandateService.getMandatesByGroup(testGroupId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mandateRepository).findByGroupId(testGroupId);
    }

    @Test
    @DisplayName("Should return mandates by status")
    void getMandatesByStatus_WithValidStatus_ShouldReturnMandates() {
        // Given
        String status = "ACTIVE";
        List<Mandate> mandates = Arrays.asList(testMandate);
        when(mandateRepository.findByStatus(status)).thenReturn(mandates);

        // When
        List<MandateResponse> result = mandateService.getMandatesByStatus(status);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mandateRepository).findByStatus(status);
    }

    @Test
    @DisplayName("Should return active mandates by group ID")
    void getActiveMandatesByGroup_WithValidGroupId_ShouldReturnActiveMandates() {
        // Given
        List<Mandate> mandates = Arrays.asList(testMandate);
        when(mandateRepository.findActiveMandatesByGroupId(testGroupId)).thenReturn(mandates);

        // When
        List<MandateResponse> result = mandateService.getActiveMandatesByGroup(testGroupId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mandateRepository).findActiveMandatesByGroupId(testGroupId);
    }
}