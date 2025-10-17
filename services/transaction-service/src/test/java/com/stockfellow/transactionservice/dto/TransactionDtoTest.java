package com.stockfellow.transactionservice.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.Validation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreateGroupCycleDto Tests")
class CreateGroupCycleDtoTest {

    private Validator validator;
    private CreateGroupCycleDto dto;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        dto = createValidDto();
    }

    private CreateGroupCycleDto createValidDto() {
        CreateGroupCycleDto dto = new CreateGroupCycleDto();
        dto.setGroupId(UUID.randomUUID());
        dto.setCyclePeriod("Monthly");
        dto.setRecipientUserId(UUID.randomUUID());
        dto.setContributionAmount(new BigDecimal("100.00"));
        dto.setExpectedTotal(new BigDecimal("1000.00"));
        dto.setCollectionStartDate(LocalDate.now().plusDays(1));
        dto.setCollectionEndDate(LocalDate.now().plusDays(30));
        dto.setPayoutDate(LocalDate.now().plusDays(35));
        return dto;
    }

    @Nested
    @DisplayName("Valid DTO Tests")
    class ValidDtoTests {

        @Test
        @DisplayName("Should pass validation with all valid fields")
        void shouldPassValidationWithValidFields() {
            Set<ConstraintViolation<CreateGroupCycleDto>> violations = validator.validate(dto);
            assertTrue(violations.isEmpty(), "Valid DTO should have no validation errors");
        }

        @Test
        @DisplayName("Should pass validation without optional payoutDate")
        void shouldPassValidationWithoutPayoutDate() {
            dto.setPayoutDate(null);
            
            Set<ConstraintViolation<CreateGroupCycleDto>> violations = validator.validate(dto);
            assertTrue(violations.isEmpty(), "DTO should be valid without optional payoutDate");
        }
    }

    @Nested
    @DisplayName("GroupId Validation Tests")
    class GroupIdValidationTests {

        @Test
        @DisplayName("Should fail validation when groupId is null")
        void shouldFailValidationWhenGroupIdIsNull() {
            dto.setGroupId(null);
            
            Set<ConstraintViolation<CreateGroupCycleDto>> violations = validator.validate(dto);
            assertEquals(1, violations.size());
            assertEquals("Group ID is required", violations.iterator().next().getMessage());
        }
    }

    @Nested
    @DisplayName("CyclePeriod Validation Tests")
    class CyclePeriodValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"", "   "})
        @DisplayName("Should fail validation when cyclePeriod is null, empty, or blank")
        void shouldFailValidationWhenCyclePeriodIsNullEmptyOrBlank(String cyclePeriod) {
            dto.setCyclePeriod(cyclePeriod);
            
            Set<ConstraintViolation<CreateGroupCycleDto>> violations = validator.validate(dto);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Cycle period is required")));
        }

        @Test
        @DisplayName("Should fail validation when cyclePeriod exceeds 50 characters")
        void shouldFailValidationWhenCyclePeriodTooLong() {
            String longCyclePeriod = "a".repeat(51);
            dto.setCyclePeriod(longCyclePeriod);
            
            Set<ConstraintViolation<CreateGroupCycleDto>> violations = validator.validate(dto);
            assertEquals(1, violations.size());
            assertEquals("Cycle period must not exceed 50 characters", 
                violations.iterator().next().getMessage());
        }

        @Test
        @DisplayName("Should pass validation when cyclePeriod is exactly 50 characters")
        void shouldPassValidationWhenCyclePeriodIsExactly50Characters() {
            String cyclePeriod = "a".repeat(50);
            dto.setCyclePeriod(cyclePeriod);
            
            Set<ConstraintViolation<CreateGroupCycleDto>> violations = validator.validate(dto);
            assertTrue(violations.isEmpty());
        }
    }

    @Nested
    @DisplayName("RecipientUserId Validation Tests")
    class RecipientUserIdValidationTests {

        @Test
        @DisplayName("Should fail validation when recipientUserId is null")
        void shouldFailValidationWhenRecipientUserIdIsNull() {
            dto.setRecipientUserId(null);
            
            Set<ConstraintViolation<CreateGroupCycleDto>> violations = validator.validate(dto);
            assertEquals(1, violations.size());
            assertEquals("Recipient user ID is required", violations.iterator().next().getMessage());
        }
    }

    @Nested
    @DisplayName("ContributionAmount Validation Tests")
    class ContributionAmountValidationTests {

        @Test
        @DisplayName("Should fail validation when contributionAmount is null")
        void shouldFailValidationWhenContributionAmountIsNull() {
            dto.setContributionAmount(null);
            
            Set<ConstraintViolation<CreateGroupCycleDto>> violations = validator.validate(dto);
            assertEquals(1, violations.size());
            assertEquals("Contribution amount is required", violations.iterator().next().getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "0.00", "-1", "-100.50"})
        @DisplayName("Should fail validation when contributionAmount is zero or negative")
        void shouldFailValidationWhenContributionAmountIsZeroOrNegative(String amount) {
            dto.setContributionAmount(new BigDecimal(amount));
            
            Set<ConstraintViolation<CreateGroupCycleDto>> violations = validator.validate(dto);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Contribution amount must be greater than 0")));
        }

        @Test
        @DisplayName("Should pass validation when contributionAmount is minimum valid value")
        void shouldPassValidationWhenContributionAmountIsMinimumValid() {
            dto.setContributionAmount(new BigDecimal("0.01"));
            
            Set<ConstraintViolation<CreateGroupCycleDto>> violations = validator.validate(dto);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when contributionAmount has too many decimal places")
        void shouldFailValidationWhenContributionAmountHasTooManyDecimals() {
            dto.setContributionAmount(new BigDecimal("100.123"));
            
            Set<ConstraintViolation<CreateGroupCycleDto>> violations = validator.validate(dto);
            assertEquals(1, violations.size());
            assertEquals("Invalid contribution amount format", violations.iterator().next().getMessage());
        }

        @Test
        @DisplayName("Should fail validation when contributionAmount has too many integer digits")
        void shouldFailValidationWhenContributionAmountHasTooManyIntegers() {
            // Create a number with 18 integer digits (max is 17)
            String largeNumber = "1".repeat(18) + ".00";
            dto.setContributionAmount(new BigDecimal(largeNumber));
            
            Set<ConstraintViolation<CreateGroupCycleDto>> violations = validator.validate(dto);
            assertEquals(1, violations.size());
            assertEquals("Invalid contribution amount format", violations.iterator().next().getMessage());
        }
    }

    @Nested
    @DisplayName("ExpectedTotal Validation Tests")
    class ExpectedTotalValidationTests {

        @Test
        @DisplayName("Should fail validation when expectedTotal is null")
        void shouldFailValidationWhenExpectedTotalIsNull() {
            dto.setExpectedTotal(null);
            
            Set<ConstraintViolation<CreateGroupCycleDto>> violations = validator.validate(dto);
            assertEquals(1, violations.size());
            assertEquals("Expected total is required", violations.iterator().next().getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "0.00", "-1", "-1000.00"})
        @DisplayName("Should fail validation when expectedTotal is zero or negative")
        void shouldFailValidationWhenExpectedTotalIsZeroOrNegative(String amount) {
            dto.setExpectedTotal(new BigDecimal(amount));
            
            Set<ConstraintViolation<CreateGroupCycleDto>> violations = validator.validate(dto);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Expected total must be greater than 0")));
        }
    }

    @Nested
    @DisplayName("Date Validation Tests")
    class DateValidationTests {

        @Test
        @DisplayName("Should fail validation when collectionStartDate is null")
        void shouldFailValidationWhenCollectionStartDateIsNull() {
            dto.setCollectionStartDate(null);
            
            Set<ConstraintViolation<CreateGroupCycleDto>> violations = validator.validate(dto);
            assertEquals(1, violations.size());
            assertEquals("Collection start date is required", violations.iterator().next().getMessage());
        }

        @Test
        @DisplayName("Should fail validation when collectionEndDate is null")
        void shouldFailValidationWhenCollectionEndDateIsNull() {
            dto.setCollectionEndDate(null);
            
            Set<ConstraintViolation<CreateGroupCycleDto>> violations = validator.validate(dto);
            assertEquals(1, violations.size());
            assertEquals("Collection end date is required", violations.iterator().next().getMessage());
        }

        @Test
        @DisplayName("Should fail validation when collectionStartDate is in the past")
        void shouldFailValidationWhenCollectionStartDateIsInPast() {
            dto.setCollectionStartDate(LocalDate.now().minusDays(1));
            
            Set<ConstraintViolation<CreateGroupCycleDto>> violations = validator.validate(dto);
            assertEquals(1, violations.size());
            assertEquals("Collection start date must be in the future", 
                violations.iterator().next().getMessage());
        }

        @Test
        @DisplayName("Should fail validation when collectionEndDate is in the past")
        void shouldFailValidationWhenCollectionEndDateIsInPast() {
            dto.setCollectionEndDate(LocalDate.now().minusDays(1));
            
            Set<ConstraintViolation<CreateGroupCycleDto>> violations = validator.validate(dto);
            assertEquals(1, violations.size());
            assertEquals("Collection end date must be in the future", 
                violations.iterator().next().getMessage());
        }

        @Test
        @DisplayName("Should fail validation when collectionStartDate is today")
        void shouldFailValidationWhenCollectionStartDateIsToday() {
            dto.setCollectionStartDate(LocalDate.now());
            
            Set<ConstraintViolation<CreateGroupCycleDto>> violations = validator.validate(dto);
            assertEquals(1, violations.size());
            assertEquals("Collection start date must be in the future", 
                violations.iterator().next().getMessage());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should correctly set and get groupId")
        void shouldCorrectlySetAndGetGroupId() {
            UUID testUuid = UUID.randomUUID();
            dto.setGroupId(testUuid);
            assertEquals(testUuid, dto.getGroupId());
        }

        @Test
        @DisplayName("Should correctly set and get cyclePeriod")
        void shouldCorrectlySetAndGetCyclePeriod() {
            String testPeriod = "Weekly";
            dto.setCyclePeriod(testPeriod);
            assertEquals(testPeriod, dto.getCyclePeriod());
        }

        @Test
        @DisplayName("Should correctly set and get recipientUserId")
        void shouldCorrectlySetAndGetRecipientUserId() {
            UUID testUuid = UUID.randomUUID();
            dto.setRecipientUserId(testUuid);
            assertEquals(testUuid, dto.getRecipientUserId());
        }

        @Test
        @DisplayName("Should correctly set and get contributionAmount")
        void shouldCorrectlySetAndGetContributionAmount() {
            BigDecimal testAmount = new BigDecimal("250.75");
            dto.setContributionAmount(testAmount);
            assertEquals(testAmount, dto.getContributionAmount());
        }

        @Test
        @DisplayName("Should correctly set and get expectedTotal")
        void shouldCorrectlySetAndGetExpectedTotal() {
            BigDecimal testTotal = new BigDecimal("2500.00");
            dto.setExpectedTotal(testTotal);
            assertEquals(testTotal, dto.getExpectedTotal());
        }

        @Test
        @DisplayName("Should correctly set and get collectionStartDate")
        void shouldCorrectlySetAndGetCollectionStartDate() {
            LocalDate testDate = LocalDate.of(2025, 12, 1);
            dto.setCollectionStartDate(testDate);
            assertEquals(testDate, dto.getCollectionStartDate());
        }

        @Test
        @DisplayName("Should correctly set and get collectionEndDate")
        void shouldCorrectlySetAndGetCollectionEndDate() {
            LocalDate testDate = LocalDate.of(2025, 12, 31);
            dto.setCollectionEndDate(testDate);
            assertEquals(testDate, dto.getCollectionEndDate());
        }

        @Test
        @DisplayName("Should correctly set and get payoutDate")
        void shouldCorrectlySetAndGetPayoutDate() {
            LocalDate testDate = LocalDate.of(2026, 1, 5);
            dto.setPayoutDate(testDate);
            assertEquals(testDate, dto.getPayoutDate());
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create DTO with default constructor")
        void shouldCreateDtoWithDefaultConstructor() {
            CreateGroupCycleDto newDto = new CreateGroupCycleDto();
            assertNotNull(newDto);
            assertNull(newDto.getGroupId());
            assertNull(newDto.getCyclePeriod());
            assertNull(newDto.getRecipientUserId());
            assertNull(newDto.getContributionAmount());
            assertNull(newDto.getExpectedTotal());
            assertNull(newDto.getCollectionStartDate());
            assertNull(newDto.getCollectionEndDate());
            assertNull(newDto.getPayoutDate());
        }
    }

    @Nested
    @DisplayName("Multiple Validation Errors Tests")
    class MultipleValidationErrorsTests {

        @Test
        @DisplayName("Should return multiple validation errors when multiple fields are invalid")
        void shouldReturnMultipleValidationErrorsWhenMultipleFieldsInvalid() {
            CreateGroupCycleDto invalidDto = new CreateGroupCycleDto();
            // Leave all required fields null
            
            Set<ConstraintViolation<CreateGroupCycleDto>> violations = validator.validate(invalidDto);
            
            // Should have violations for all required fields
            assertTrue(violations.size() >= 5, "Should have at least 5 validation errors");
            
            // Check that specific error messages are present
            Set<String> errorMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(java.util.stream.Collectors.toSet());
            
            assertTrue(errorMessages.contains("Group ID is required"));
            assertTrue(errorMessages.contains("Cycle period is required"));
            assertTrue(errorMessages.contains("Recipient user ID is required"));
            assertTrue(errorMessages.contains("Contribution amount is required"));
            assertTrue(errorMessages.contains("Expected total is required"));
            assertTrue(errorMessages.contains("Collection start date is required"));
            assertTrue(errorMessages.contains("Collection end date is required"));
        }
    }
}