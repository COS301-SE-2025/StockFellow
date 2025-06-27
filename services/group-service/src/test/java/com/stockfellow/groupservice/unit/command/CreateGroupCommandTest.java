package com.stockfellow.groupservice.unit.command;

import com.stockfellow.groupservice.command.CreateGroupCommand;
import com.stockfellow.groupservice.model.Event;
import com.stockfellow.groupservice.model.Group;
import com.stockfellow.groupservice.service.EventStoreService;
import com.stockfellow.groupservice.service.ReadModelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateGroupCommandTest {

    @Mock
    private EventStoreService eventStoreService;

    @Mock
    private ReadModelService readModelService;

    private CreateGroupCommand createGroupCommand;

    @BeforeEach
    void setUp() {
        createGroupCommand = new CreateGroupCommand(eventStoreService, readModelService);
    }

    @Test
    @DisplayName("Should create group successfully with valid inputs and no additional members")
    void testExecute_ValidInputs_NoAdditionalMembers() {
        // Arrange
        String groupId = "group-123";
        String adminId = "admin-456";
        String name = "Test Group";
        Double minContribution = 100.0;
        Integer maxMembers = 10;
        String description = "Test description";
        String profileImage = "image-url";
        String visibility = "Public";
        String contributionFrequency = "Monthly";
        Date contributionDate = new Date();
        String payoutFrequency = "Monthly";
        Date payoutDate = new Date();
        List<String> members = null;

        Event mockEvent = new Event();
        mockEvent.setId("event-789");
        when(eventStoreService.appendEvent(eq("GroupCreated"), any(Map.class))).thenReturn(mockEvent);

        // Act
        String result = createGroupCommand.execute(groupId, adminId, name, minContribution,
                maxMembers, description, profileImage, visibility, contributionFrequency,
                contributionDate, payoutFrequency, payoutDate, members);

        // Assert
        assertEquals("event-789", result);
        
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(eventStoreService).appendEvent(eq("GroupCreated"), dataCaptor.capture());
        verify(readModelService).rebuildState(groupId);

        Map<String, Object> capturedData = dataCaptor.getValue();
        assertEquals(groupId, capturedData.get("groupId"));
        assertEquals(name, capturedData.get("name"));
        assertEquals(adminId, capturedData.get("adminId"));
        assertEquals(minContribution, capturedData.get("minContribution"));
        assertEquals(minContribution, capturedData.get("balance")); // Balance should equal admin's contribution
        assertEquals(maxMembers, capturedData.get("maxMembers"));
        assertEquals(description, capturedData.get("description"));
        assertEquals(profileImage, capturedData.get("profileImage"));
        assertEquals(visibility, capturedData.get("visibility"));
        assertEquals(contributionFrequency, capturedData.get("contributionFrequency"));
        assertEquals(payoutFrequency, capturedData.get("payoutFrequency"));
        assertNotNull(capturedData.get("createdAt"));

        // Verify members array contains only admin with founder role
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> membersList = (List<Map<String, Object>>) capturedData.get("members");
        assertEquals(1, membersList.size());
        Map<String, Object> adminMember = membersList.get(0);
        assertEquals(adminId, adminMember.get("userId"));
        assertEquals("founder", adminMember.get("role"));
        assertEquals(minContribution, adminMember.get("contribution"));
    }

    @Test
    @DisplayName("Should create group successfully with additional members")
    void testExecute_ValidInputs_WithAdditionalMembers() {
        // Arrange
        String groupId = "group-123";
        String adminId = "admin-456";
        String name = "Test Group";
        Double minContribution = 100.0;
        Integer maxMembers = 10;
        String description = "Test description";
        String profileImage = "image-url";
        String visibility = "Private";
        String contributionFrequency = "Weekly";
        Date contributionDate = new Date();
        String payoutFrequency = "Bi-weekly";
        Date payoutDate = new Date();
        List<String> members = Arrays.asList(adminId, "member-1", "member-2");

        Event mockEvent = new Event();
        mockEvent.setId("event-789");
        when(eventStoreService.appendEvent(eq("GroupCreated"), any(Map.class))).thenReturn(mockEvent);

        // Act
        String result = createGroupCommand.execute(groupId, adminId, name, minContribution,
                maxMembers, description, profileImage, visibility, contributionFrequency,
                contributionDate, payoutFrequency, payoutDate, members);

        // Assert
        assertEquals("event-789", result);

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(eventStoreService).appendEvent(eq("GroupCreated"), dataCaptor.capture());

        Map<String, Object> capturedData = dataCaptor.getValue();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> membersList = (List<Map<String, Object>>) capturedData.get("members");
        
        assertEquals(3, membersList.size());
        
        // Verify admin member
        Map<String, Object> adminMember = membersList.stream()
                .filter(m -> adminId.equals(m.get("userId")))
                .findFirst()
                .orElse(null);
        assertNotNull(adminMember);
        assertEquals("founder", adminMember.get("role"));
        assertEquals(minContribution, adminMember.get("contribution"));

        // Verify regular members
        long regularMembersCount = membersList.stream()
                .filter(m -> "member".equals(m.get("role")))
                .count();
        assertEquals(2, regularMembersCount);
        
        // Verify regular members have zero contribution
        membersList.stream()
                .filter(m -> "member".equals(m.get("role")))
                .forEach(m -> assertEquals(0.0, m.get("contribution")));
    }

    @Test
    @DisplayName("Should throw exception when group name is null")
    void testExecute_NullName_ThrowsException() {
        // Arrange
        String groupId = "group-123";
        String adminId = "admin-456";
        String name = null;
        Double minContribution = 100.0;
        Integer maxMembers = 10;
        String description = "Test description";
        String profileImage = "image-url";
        String visibility = "Public";
        String contributionFrequency = "Monthly";
        Date contributionDate = new Date();
        String payoutFrequency = "Monthly";
        Date payoutDate = new Date();
        List<String> members = null;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            createGroupCommand.execute(groupId, adminId, name, minContribution,
                    maxMembers, description, profileImage, visibility, contributionFrequency,
                    contributionDate, payoutFrequency, payoutDate, members);
        });

        assertEquals("Group name cannot be empty", exception.getMessage());
        verify(eventStoreService, never()).appendEvent(any(), any());
        verify(readModelService, never()).rebuildState(any());
    }

    @Test
    @DisplayName("Should throw exception when group name is empty")
    void testExecute_EmptyName_ThrowsException() {
        // Arrange
        String name = "   "; // Whitespace only

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            createGroupCommand.execute("group-123", "admin-456", name, 100.0,
                    10, "desc", "image", "Public", "Monthly",
                    new Date(), "Monthly", new Date(), null);
        });

        assertEquals("Group name cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when contribution frequency is invalid")
    void testExecute_InvalidContributionFrequency_ThrowsException() {
        // Arrange
        String contributionFrequency = "Daily"; // Invalid frequency

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            createGroupCommand.execute("group-123", "admin-456", "Test Group", 100.0,
                    10, "desc", "image", "Public", contributionFrequency,
                    new Date(), "Monthly", new Date(), null);
        });

        assertEquals("Invalid contribution frequency. Must be: Monthly, Bi-weekly, or Weekly", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when payout frequency is invalid")
    void testExecute_InvalidPayoutFrequency_ThrowsException() {
        // Arrange
        String payoutFrequency = "Yearly"; // Invalid frequency

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            createGroupCommand.execute("group-123", "admin-456", "Test Group", 100.0,
                    10, "desc", "image", "Public", "Monthly",
                    new Date(), payoutFrequency, new Date(), null);
        });

        assertEquals("Invalid payout frequency. Must be: Monthly, Bi-weekly, or Weekly", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when visibility is invalid")
    void testExecute_InvalidVisibility_ThrowsException() {
        // Arrange
        String visibility = "Protected"; // Invalid visibility

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            createGroupCommand.execute("group-123", "admin-456", "Test Group", 100.0,
                    10, "desc", "image", visibility, "Monthly",
                    new Date(), "Monthly", new Date(), null);
        });

        assertEquals("Invalid visibility. Must be: Private or Public", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when minimum contribution is null")
    void testExecute_NullMinContribution_ThrowsException() {
        // Arrange
        Double minContribution = null;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            createGroupCommand.execute("group-123", "admin-456", "Test Group", minContribution,
                    10, "desc", "image", "Public", "Monthly",
                    new Date(), "Monthly", new Date(), null);
        });

        assertEquals("Minimum contribution must be greater than 0", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when minimum contribution is zero or negative")
    void testExecute_ZeroOrNegativeMinContribution_ThrowsException() {
        // Test zero contribution
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () -> {
            createGroupCommand.execute("group-123", "admin-456", "Test Group", 0.0,
                    10, "desc", "image", "Public", "Monthly",
                    new Date(), "Monthly", new Date(), null);
        });
        assertEquals("Minimum contribution must be greater than 0", exception1.getMessage());

        // Test negative contribution
        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> {
            createGroupCommand.execute("group-123", "admin-456", "Test Group", -50.0,
                    10, "desc", "image", "Public", "Monthly",
                    new Date(), "Monthly", new Date(), null);
        });
        assertEquals("Minimum contribution must be greater than 0", exception2.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when max members is null or non-positive")
    void testExecute_InvalidMaxMembers_ThrowsException() {
        // Test null max members
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () -> {
            createGroupCommand.execute("group-123", "admin-456", "Test Group", 100.0,
                    null, "desc", "image", "Public", "Monthly",
                    new Date(), "Monthly", new Date(), null);
        });
        assertEquals("Maximum members must be greater than 0", exception1.getMessage());

        // Test zero max members
        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> {
            createGroupCommand.execute("group-123", "admin-456", "Test Group", 100.0,
                    0, "desc", "image", "Public", "Monthly",
                    new Date(), "Monthly", new Date(), null);
        });
        assertEquals("Maximum members must be greater than 0", exception2.getMessage());

        // Test negative max members
        IllegalArgumentException exception3 = assertThrows(IllegalArgumentException.class, () -> {
            createGroupCommand.execute("group-123", "admin-456", "Test Group", 100.0,
                    -5, "desc", "image", "Public", "Monthly",
                    new Date(), "Monthly", new Date(), null);
        });
        assertEquals("Maximum members must be greater than 0", exception3.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when initial members exceed max members")
    void testExecute_TooManyInitialMembers_ThrowsException() {
        // Arrange
        Integer maxMembers = 3;
        List<String> members = Arrays.asList("admin-456", "member-1", "member-2", "member-3"); // 4 members total

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            createGroupCommand.execute("group-123", "admin-456", "Test Group", 100.0,
                    maxMembers, "desc", "image", "Public", "Monthly",
                    new Date(), "Monthly", new Date(), members);
        });

        assertEquals("Number of initial members cannot exceed maximum members (admin takes one slot)", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle null dates gracefully")
    void testExecute_NullDates_Success() {
        // Arrange
        Event mockEvent = new Event();
        mockEvent.setId("event-789");
        when(eventStoreService.appendEvent(eq("GroupCreated"), any(Map.class))).thenReturn(mockEvent);

        // Act
        String result = createGroupCommand.execute("group-123", "admin-456", "Test Group", 100.0,
                10, "desc", "image", "Public", "Monthly",
                null, "Monthly", null, null);

        // Assert
        assertEquals("event-789", result);
        
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(eventStoreService).appendEvent(eq("GroupCreated"), dataCaptor.capture());

        Map<String, Object> capturedData = dataCaptor.getValue();
        assertNull(capturedData.get("contributionDate"));
        assertNull(capturedData.get("payoutDate"));
    }

    @Test
    @DisplayName("Should handle empty member list")
    void testExecute_EmptyMemberList_Success() {
        // Arrange
        List<String> members = new ArrayList<>(); // Empty list
        Event mockEvent = new Event();
        mockEvent.setId("event-789");
        when(eventStoreService.appendEvent(eq("GroupCreated"), any(Map.class))).thenReturn(mockEvent);

        // Act
        String result = createGroupCommand.execute("group-123", "admin-456", "Test Group", 100.0,
                10, "desc", "image", "Public", "Monthly",
                new Date(), "Monthly", new Date(), members);

        // Assert
        assertEquals("event-789", result);
        
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(eventStoreService).appendEvent(eq("GroupCreated"), dataCaptor.capture());

        Map<String, Object> capturedData = dataCaptor.getValue();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> membersList = (List<Map<String, Object>>) capturedData.get("members");
        assertEquals(1, membersList.size()); // Only admin should be added
    }

    @Test
    @DisplayName("Should verify all valid frequency combinations work")
    void testExecute_AllValidFrequencyCombinations_Success() {
        // Arrange
        String[] validFrequencies = {"Monthly", "Bi-weekly", "Weekly"};
        Event mockEvent = new Event();
        mockEvent.setId("event-789");
        when(eventStoreService.appendEvent(eq("GroupCreated"), any(Map.class))).thenReturn(mockEvent);

        // Act & Assert
        for (String contributionFreq : validFrequencies) {
            for (String payoutFreq : validFrequencies) {
                assertDoesNotThrow(() -> {
                    createGroupCommand.execute("group-123", "admin-456", "Test Group", 100.0,
                            10, "desc", "image", "Public", contributionFreq,
                            new Date(), payoutFreq, new Date(), null);
                });
            }
        }

        // Verify the method was called for each combination
        verify(eventStoreService, times(9)).appendEvent(eq("GroupCreated"), any(Map.class));
    }

    @Test
    @DisplayName("Should verify all valid visibility options work")
    void testExecute_AllValidVisibilityOptions_Success() {
        // Arrange
        String[] validVisibilities = {"Private", "Public"};
        Event mockEvent = new Event();
        mockEvent.setId("event-789");
        when(eventStoreService.appendEvent(eq("GroupCreated"), any(Map.class))).thenReturn(mockEvent);

        // Act & Assert
        for (String visibility : validVisibilities) {
            assertDoesNotThrow(() -> {
                createGroupCommand.execute("group-123", "admin-456", "Test Group", 100.0,
                        10, "desc", "image", visibility, "Monthly",
                        new Date(), "Monthly", new Date(), null);
            });
        }

        verify(eventStoreService, times(2)).appendEvent(eq("GroupCreated"), any(Map.class));
    }
}
