package com.stockfellow.transactionservice.dto;

import com.stockfellow.transactionservice.model.Mandate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Mandate DTO Tests")
class MandateDtoTest {

    @Nested
    @DisplayName("CreateMandateRequest Tests")
    class CreateMandateRequestTest {

        private CreateMandateRequest request;
        private UUID testPayerUserId;
        private UUID testGroupId;
        private UUID testPaymentMethodId;

        @BeforeEach
        void setUp() {
            testPayerUserId = UUID.randomUUID();
            testGroupId = UUID.randomUUID();
            testPaymentMethodId = UUID.randomUUID();

            request = new CreateMandateRequest();
        }

        @Test
        @DisplayName("Should create empty request with default constructor")
        void defaultConstructor_ShouldCreateEmptyRequest() {
            // When
            CreateMandateRequest emptyRequest = new CreateMandateRequest();

            // Then
            assertNotNull(emptyRequest);
            assertNull(emptyRequest.getPayerUserId());
            assertNull(emptyRequest.getGroupId());
            assertNull(emptyRequest.getPaymentMethodId());
            assertNull(emptyRequest.getDocumentReference());
            assertNull(emptyRequest.getIpAddress());
        }

        @Test
        @DisplayName("Should create request with parameterized constructor")
        void parameterizedConstructor_ShouldCreateRequestWithAllFields() {
            // Given
            String documentReference = "DOC123";
            String ipAddress = "192.168.1.1";

            // When
            CreateMandateRequest request = new CreateMandateRequest(
                testPayerUserId, testGroupId, testPaymentMethodId, documentReference, ipAddress);

            // Then
            assertNotNull(request);
            assertEquals(testPayerUserId, request.getPayerUserId());
            assertEquals(testGroupId, request.getGroupId());
            assertEquals(testPaymentMethodId, request.getPaymentMethodId());
            assertEquals(documentReference, request.getDocumentReference());
            assertEquals(ipAddress, request.getIpAddress());
        }

        @Test
        @DisplayName("Should set and get payer user ID")
        void setAndGetPayerUserId_ShouldWorkCorrectly() {
            // When
            request.setPayerUserId(testPayerUserId);

            // Then
            assertEquals(testPayerUserId, request.getPayerUserId());
        }

        @Test
        @DisplayName("Should set and get group ID")
        void setAndGetGroupId_ShouldWorkCorrectly() {
            // When
            request.setGroupId(testGroupId);

            // Then
            assertEquals(testGroupId, request.getGroupId());
        }

        @Test
        @DisplayName("Should set and get payment method ID")
        void setAndGetPaymentMethodId_ShouldWorkCorrectly() {
            // When
            request.setPaymentMethodId(testPaymentMethodId);

            // Then
            assertEquals(testPaymentMethodId, request.getPaymentMethodId());
        }

        @Test
        @DisplayName("Should set and get document reference")
        void setAndGetDocumentReference_ShouldWorkCorrectly() {
            // Given
            String documentReference = "DOC456";

            // When
            request.setDocumentReference(documentReference);

            // Then
            assertEquals(documentReference, request.getDocumentReference());
        }

        @Test
        @DisplayName("Should set and get IP address")
        void setAndGetIpAddress_ShouldWorkCorrectly() {
            // Given
            String ipAddress = "10.0.0.1";

            // When
            request.setIpAddress(ipAddress);

            // Then
            assertEquals(ipAddress, request.getIpAddress());
        }

        @Test
        @DisplayName("Should handle null values gracefully")
        void settersWithNullValues_ShouldHandleGracefully() {
            // When
            request.setPayerUserId(null);
            request.setGroupId(null);
            request.setPaymentMethodId(null);
            request.setDocumentReference(null);
            request.setIpAddress(null);

            // Then
            assertNull(request.getPayerUserId());
            assertNull(request.getGroupId());
            assertNull(request.getPaymentMethodId());
            assertNull(request.getDocumentReference());
            assertNull(request.getIpAddress());
        }
    }

    @Nested
    @DisplayName("MandateResponse Tests")
    class MandateResponseTest {

        private MandateResponse response;
        private UUID testMandateId;
        private UUID testPayerUserId;
        private UUID testGroupId;
        private UUID testPaymentMethodId;
        private LocalDateTime testDateTime;

        @BeforeEach
        void setUp() {
            testMandateId = UUID.randomUUID();
            testPayerUserId = UUID.randomUUID();
            testGroupId = UUID.randomUUID();
            testPaymentMethodId = UUID.randomUUID();
            testDateTime = LocalDateTime.now();

            response = new MandateResponse();
        }

        @Test
        @DisplayName("Should create empty response with default constructor")
        void defaultConstructor_ShouldCreateEmptyResponse() {
            // When
            MandateResponse emptyResponse = new MandateResponse();

            // Then
            assertNotNull(emptyResponse);
            assertNull(emptyResponse.getMandateId());
            assertNull(emptyResponse.getPayerUserId());
            assertNull(emptyResponse.getGroupId());
            assertNull(emptyResponse.getPaymentMethodId());
            assertNull(emptyResponse.getStatus());
            assertNull(emptyResponse.getSignedDate());
            assertNull(emptyResponse.getDocumentReference());
            assertNull(emptyResponse.getIpAddress());
            assertNull(emptyResponse.getCreatedAt());
            assertNull(emptyResponse.getUpdatedAt());
        }

        @Test
        @DisplayName("Should create response with parameterized constructor")
        void parameterizedConstructor_ShouldCreateResponseWithAllFields() {
            // Given
            String status = "ACTIVE";
            String documentReference = "DOC123";
            String ipAddress = "192.168.1.1";
            LocalDateTime signedDate = testDateTime.minusDays(1);
            LocalDateTime createdAt = testDateTime.minusDays(2);
            LocalDateTime updatedAt = testDateTime;

            // When
            MandateResponse response = new MandateResponse(
                testMandateId, testPayerUserId, testGroupId, testPaymentMethodId,
                status, signedDate, documentReference, ipAddress, createdAt, updatedAt);

            // Then
            assertNotNull(response);
            assertEquals(testMandateId, response.getMandateId());
            assertEquals(testPayerUserId, response.getPayerUserId());
            assertEquals(testGroupId, response.getGroupId());
            assertEquals(testPaymentMethodId, response.getPaymentMethodId());
            assertEquals(status, response.getStatus());
            assertEquals(signedDate, response.getSignedDate());
            assertEquals(documentReference, response.getDocumentReference());
            assertEquals(ipAddress, response.getIpAddress());
            assertEquals(createdAt, response.getCreatedAt());
            assertEquals(updatedAt, response.getUpdatedAt());
        }

        @Test
        @DisplayName("Should create response from mandate entity")
        void fromMandate_ShouldCreateResponseFromEntity() {
            // Given
            Mandate mandate = new Mandate();
            mandate.setMandateId(testMandateId);
            mandate.setPayerUserId(testPayerUserId);
            mandate.setGroupId(testGroupId);
            mandate.setPaymentMethodId(testPaymentMethodId);
            mandate.setStatus("ACTIVE");
            mandate.setSignedDate(testDateTime);
            mandate.setDocumentReference("DOC123");
            mandate.setIpAddress("192.168.1.1");
            mandate.setCreatedAt(testDateTime.minusDays(1));
            mandate.setUpdatedAt(testDateTime);

            // When
            MandateResponse response = MandateResponse.from(mandate);

            // Then
            assertNotNull(response);
            assertEquals(mandate.getMandateId(), response.getMandateId());
            assertEquals(mandate.getPayerUserId(), response.getPayerUserId());
            assertEquals(mandate.getGroupId(), response.getGroupId());
            assertEquals(mandate.getPaymentMethodId(), response.getPaymentMethodId());
            assertEquals(mandate.getStatus(), response.getStatus());
            assertEquals(mandate.getSignedDate(), response.getSignedDate());
            assertEquals(mandate.getDocumentReference(), response.getDocumentReference());
            assertEquals(mandate.getIpAddress(), response.getIpAddress());
            assertEquals(mandate.getCreatedAt(), response.getCreatedAt());
            assertEquals(mandate.getUpdatedAt(), response.getUpdatedAt());
        }

        @Test
        @DisplayName("Should set and get all fields correctly")
        void settersAndGetters_ShouldWorkCorrectly() {
            // Given
            String status = "INACTIVE";
            String documentReference = "DOC456";
            String ipAddress = "10.0.0.1";
            LocalDateTime signedDate = testDateTime.minusDays(5);
            LocalDateTime createdAt = testDateTime.minusDays(10);
            LocalDateTime updatedAt = testDateTime.minusDays(1);

            // When
            response.setMandateId(testMandateId);
            response.setPayerUserId(testPayerUserId);
            response.setGroupId(testGroupId);
            response.setPaymentMethodId(testPaymentMethodId);
            response.setStatus(status);
            response.setSignedDate(signedDate);
            response.setDocumentReference(documentReference);
            response.setIpAddress(ipAddress);
            response.setCreatedAt(createdAt);
            response.setUpdatedAt(updatedAt);

            // Then
            assertEquals(testMandateId, response.getMandateId());
            assertEquals(testPayerUserId, response.getPayerUserId());
            assertEquals(testGroupId, response.getGroupId());
            assertEquals(testPaymentMethodId, response.getPaymentMethodId());
            assertEquals(status, response.getStatus());
            assertEquals(signedDate, response.getSignedDate());
            assertEquals(documentReference, response.getDocumentReference());
            assertEquals(ipAddress, response.getIpAddress());
            assertEquals(createdAt, response.getCreatedAt());
            assertEquals(updatedAt, response.getUpdatedAt());
        }

        @Test
        @DisplayName("Should handle null mandate in from method")
        void fromMandate_WithNullMandate_ShouldHandleGracefully() {
            // When & Then - This should ideally be handled by null checks in the from method
            // For now, we expect NullPointerException if null mandate is passed
            assertThrows(NullPointerException.class, () -> MandateResponse.from(null));
        }

        @Test
        @DisplayName("Should handle mandate with null fields")
        void fromMandate_WithNullFields_ShouldHandleGracefully() {
            // Given
            Mandate mandate = new Mandate();
            // All fields are null by default

            // When
            MandateResponse response = MandateResponse.from(mandate);

            // Then
            assertNotNull(response);
            assertNull(response.getMandateId());
            assertNull(response.getPayerUserId());
            assertNull(response.getGroupId());
            assertNull(response.getPaymentMethodId());
            assertNull(response.getStatus());
            assertNull(response.getSignedDate());
            assertNull(response.getDocumentReference());
            assertNull(response.getIpAddress());
            assertNull(response.getCreatedAt());
            assertNull(response.getUpdatedAt());
        }
    }
}