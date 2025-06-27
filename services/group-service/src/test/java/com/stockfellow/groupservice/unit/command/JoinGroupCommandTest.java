package com.stockfellow.groupservice.unit.command;

import com.stockfellow.groupservice.command.JoinGroupCommand;
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
class JoinGroupCommandTest {

    @Mock
    private EventStoreService eventStoreService;

    @Mock
    private ReadModelService readModelService;

    private JoinGroupCommand joinGroupCommand;

    @BeforeEach
    void setUp() {
        joinGroupCommand = new JoinGroupCommand(eventStoreService, readModelService);
    }

    @Test
    @DisplayName("Should create join request successfully for public group")
    void testCreateJoinRequest_PublicGroup_Success() {
        // Arrange
        String groupId = "group-123";
        String userId1 = "user-456";
        String userId2 = "user-789";

        Group mockGroup = createMockGroup(groupId, "Public", 5, 10);
        when(readModelService.getGroup(groupId)).thenReturn(Optional.of(mockGroup));

        Event mockEvent1 = new Event();
        mockEvent1.setId("event-123");
        Event mockEvent2 = new Event();
        mockEvent2.setId("event-456");
        when(eventStoreService.appendEvent(eq("JoinRequestCreated"), any(Map.class)))
                .thenReturn(mockEvent1)
                .thenReturn(mockEvent2);

        // Act
        String result1 = joinGroupCommand.createJoinRequest(groupId, userId1);
        String result2 = joinGroupCommand.createJoinRequest(groupId, userId2);

        // Assert
        assertEquals("event-123", result1);
        assertEquals("event-456", result2);

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(eventStoreService, times(2)).appendEvent(eq("JoinRequestCreated"), dataCaptor.capture());

        List<Map<String, Object>> capturedData = dataCaptor.getAllValues();
        String requestId1 = (String) capturedData.get(0).get("requestId");
        String requestId2 = (String) capturedData.get(1).get("requestId");

        assertNotNull(requestId1);
        assertNotNull(requestId2);
        assertNotEquals(requestId1, requestId2); // Request IDs should be unique
    }

    @Test
    @DisplayName("Should handle edge case with exactly max members minus one")
    void testCreateJoinRequest_ExactlyOneSlotAvailable_Success() {
        // Arrange
        String groupId = "group-123";
        String userId = "user-456";

        Group mockGroup = createMockGroup(groupId, "Public", 9, 10); // 9 out of 10 members
        when(readModelService.getGroup(groupId)).thenReturn(Optional.of(mockGroup));

        Event mockEvent = new Event();
        mockEvent.setId("event-789");
        when(eventStoreService.appendEvent(eq("JoinRequestCreated"), any(Map.class))).thenReturn(mockEvent);

        // Act
        String result = joinGroupCommand.createJoinRequest(groupId, userId);

        // Assert
        assertEquals("event-789", result);
        verify(eventStoreService).appendEvent(eq("JoinRequestCreated"), any(Map.class));
        verify(readModelService).rebuildState(groupId);
    }

    @Test
    @DisplayName("Should validate request data contains all required fields")
    void testCreateJoinRequest_ValidateRequestData() {
        // Arrange
        String groupId = "group-123";
        String userId = "user-456";

        Group mockGroup = createMockGroup(groupId, "Public", 5, 10);
        when(readModelService.getGroup(groupId)).thenReturn(Optional.of(mockGroup));

        Event mockEvent = new Event();
        mockEvent.setId("event-789");
        when(eventStoreService.appendEvent(eq("JoinRequestCreated"), any(Map.class))).thenReturn(mockEvent);

        // Act
        joinGroupCommand.createJoinRequest(groupId, userId);

        // Assert
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(eventStoreService).appendEvent(eq("JoinRequestCreated"), dataCaptor.capture());

        Map<String, Object> capturedData = dataCaptor.getValue();
        assertEquals(groupId, capturedData.get("groupId"));
        assertEquals(userId, capturedData.get("userId"));
        assertEquals("waiting", capturedData.get("state"));
        
        String requestId = (String) capturedData.get("requestId");
        assertNotNull(requestId);
        assertEquals(12, requestId.length()); // UUID substring should be 12 characters
        
        Long requestedAt = (Long) capturedData.get("requestedAt");
        assertNotNull(requestedAt);
        assertTrue(requestedAt > 0);
        assertTrue(requestedAt <= System.currentTimeMillis());
    }

    /**
     * Helper method to create a mock Group object with the specified parameters
     */
    private Group createMockGroup(String groupId, String visibility, int currentMembers, int maxMembers) {
        Group group = new Group();
        group.setId(groupId);
        group.setVisibility(visibility);
        group.setMaxMembers(maxMembers);
        
        // Create members list
        List<Group.Member> members = new ArrayList<>();
        for (int i = 0; i < currentMembers; i++) {
            Group.Member member = new Group.Member("member-" + i, "member");
            members.add(member);
        }
        group.setMembers(members);
        
        // Initialize requests list
        group.setRequests(new ArrayList<>());
        
        return group;
    }
// }d"), any(Map.class))).thenReturn(mockEvent);

//         // Act
//         String result = joinGroupCommand.createJoinRequest(groupId, userId);

//         // Assert
//         assertEquals("event-789", result);
        
//         ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
//         verify(eventStoreService).appendEvent(eq("JoinRequestCreated"), dataCaptor.capture());
//         verify(readModelService).rebuildState(groupId);

//         Map<String, Object> capturedData = dataCaptor.getValue();
//         assertEquals(groupId, capturedData.get("groupId"));
//         assertEquals(userId, capturedData.get("userId"));
//         assertEquals("waiting", capturedData.get("state"));
//         assertNotNull(capturedData.get("requestId"));
//         assertNotNull(capturedData.get("requestedAt"));
//     }

    @Test
    @DisplayName("Should throw exception when group not found")
    void testCreateJoinRequest_GroupNotFound_ThrowsException() {
        // Arrange
        String groupId = "non-existent-group";
        String userId = "user-456";

        when(readModelService.getGroup(groupId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            joinGroupCommand.createJoinRequest(groupId, userId);
        });

        assertEquals("Group not found", exception.getMessage());
        verify(eventStoreService, never()).appendEvent(any(), any());
        verify(readModelService, never()).rebuildState(any());
    }

    @Test
    @DisplayName("Should throw exception when user is already a member")
    void testCreateJoinRequest_UserAlreadyMember_ThrowsException() {
        // Arrange
        String groupId = "group-123";
        String userId = "user-456";

        Group mockGroup = createMockGroup(groupId, "Public", 5, 10);
        // Add user as existing member
        Group.Member existingMember = new Group.Member(userId, "member");
        mockGroup.getMembers().add(existingMember);

        when(readModelService.getGroup(groupId)).thenReturn(Optional.of(mockGroup));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            joinGroupCommand.createJoinRequest(groupId, userId);
        });

        assertEquals("User is already a member of this group", exception.getMessage());
        verify(eventStoreService, never()).appendEvent(any(), any());
        verify(readModelService, never()).rebuildState(any());
    }

    @Test
    @DisplayName("Should throw exception when user already has pending request")
    void testCreateJoinRequest_UserHasPendingRequest_ThrowsException() {
        // Arrange
        String groupId = "group-123";
        String userId = "user-456";

        Group mockGroup = createMockGroup(groupId, "Public", 5, 10);
        // Add existing pending request for the user
        Group.JoinRequest existingRequest = new Group.JoinRequest();
        existingRequest.setUserId(userId);
        existingRequest.setState("waiting");
        mockGroup.getRequests().add(existingRequest);

        when(readModelService.getGroup(groupId)).thenReturn(Optional.of(mockGroup));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            joinGroupCommand.createJoinRequest(groupId, userId);
        });

        assertEquals("User already has a pending request for this group", exception.getMessage());
        verify(eventStoreService, never()).appendEvent(any(), any());
        verify(readModelService, never()).rebuildState(any());
    }

    @Test
    @DisplayName("Should throw exception when group is full")
    void testCreateJoinRequest_GroupFull_ThrowsException() {
        // Arrange
        String groupId = "group-123";
        String userId = "user-456";

        Group mockGroup = createMockGroup(groupId, "Public", 10, 10); // Full group
        when(readModelService.getGroup(groupId)).thenReturn(Optional.of(mockGroup));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            joinGroupCommand.createJoinRequest(groupId, userId);
        });

        assertEquals("Group is full", exception.getMessage());
        verify(eventStoreService, never()).appendEvent(any(), any());
        verify(readModelService, never()).rebuildState(any());
    }

    @Test
    @DisplayName("Should throw exception for private group without invite")
    void testCreateJoinRequest_PrivateGroupWithoutInvite_ThrowsException() {
        // Arrange
        String groupId = "group-123";
        String userId = "user-456";

        Group mockGroup = createMockGroup(groupId, "Private", 5, 10);
        when(readModelService.getGroup(groupId)).thenReturn(Optional.of(mockGroup));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            joinGroupCommand.createJoinRequest(groupId, userId);
        });

        assertEquals("Cannot request to join private groups without an invite link", exception.getMessage());
        verify(eventStoreService, never()).appendEvent(any(), any());
        verify(readModelService, never()).rebuildState(any());
    }

    @Test
    @DisplayName("Should allow request for user with processed (non-waiting) request")
    void testCreateJoinRequest_UserHasProcessedRequest_Success() {
        // Arrange
        String groupId = "group-123";
        String userId = "user-456";

        Group mockGroup = createMockGroup(groupId, "Public", 5, 10);
        // Add existing processed (rejected) request for the user
        Group.JoinRequest existingRequest = new Group.JoinRequest();
        existingRequest.setUserId(userId);
        existingRequest.setState("rejected"); // Not "waiting"
        mockGroup.getRequests().add(existingRequest);

        when(readModelService.getGroup(groupId)).thenReturn(Optional.of(mockGroup));

        Event mockEvent = new Event();
        mockEvent.setId("event-789");
        when(eventStoreService.appendEvent(eq("JoinRequestCreated"), any(Map.class))).thenReturn(mockEvent);

        // Act
        String result = joinGroupCommand.createJoinRequest(groupId, userId);

        // Assert
        assertEquals("event-789", result);
        verify(eventStoreService).appendEvent(eq("JoinRequestCreated"), any(Map.class));
        verify(readModelService).rebuildState(groupId);
    }

    @Test
    @DisplayName("Should create request when group has null requests list")
    void testCreateJoinRequest_NullRequestsList_Success() {
        // Arrange
        String groupId = "group-123";
        String userId = "user-456";

        Group mockGroup = createMockGroup(groupId, "Public", 5, 10);
        mockGroup.setRequests(null); // Null requests list

        when(readModelService.getGroup(groupId)).thenReturn(Optional.of(mockGroup));

        Event mockEvent = new Event();
        mockEvent.setId("event-789");
        when(eventStoreService.appendEvent(eq("JoinRequestCreated"), any(Map.class))).thenReturn(mockEvent);

        // Act
        String result = joinGroupCommand.createJoinRequest(groupId, userId);

        // Assert
        assertEquals("event-789", result);
        verify(eventStoreService).appendEvent(eq("JoinRequestCreated"), any(Map.class));
        verify(readModelService).rebuildState(groupId);
    }

    @Test
    @DisplayName("Should create request when group has empty requests list")
    void testCreateJoinRequest_EmptyRequestsList_Success() {
        // Arrange
        String groupId = "group-123";
        String userId = "user-456";

        Group mockGroup = createMockGroup(groupId, "Public", 5, 10);
        mockGroup.setRequests(new ArrayList<>()); // Empty requests list

        when(readModelService.getGroup(groupId)).thenReturn(Optional.of(mockGroup));

        Event mockEvent = new Event();
        mockEvent.setId("event-789");
        when(eventStoreService.appendEvent(eq("JoinRequestCreated"), any(Map.class))).thenReturn(mockEvent);

        // Act
        String result = joinGroupCommand.createJoinRequest(groupId, userId);

        // Assert
        assertEquals("event-789", result);
        verify(eventStoreService).appendEvent(eq("JoinRequestCreated"), any(Map.class));
        verify(readModelService).rebuildState(groupId);
    }

    @Test
    @DisplayName("Deprecated execute method should throw exception")
    void testExecute_DeprecatedMethod_ThrowsException() {
        // Arrange
        String groupId = "group-123";
        String userId = "user-456";

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            joinGroupCommand.execute(groupId, userId);
        });

        assertEquals("Direct joining is not allowed. All users must request to join and wait for admin approval.", 
                    exception.getMessage());
        verify(eventStoreService, never()).appendEvent(any(), any());
        verify(readModelService, never()).rebuildState(any());
    }
}

    // @Test
    // @DisplayName("Should generate unique request IDs")
    // void testCreateJoinRequest_GeneratesUniqueRequestIds() {
    //     // Arrange
    //     String groupId = "group-123";
    //     String userId1 = "user-456";
    //     String userId2 = "user-789";

    //     Group mockGroup1 = createMockGroup(groupId, "Public", 5, 10);
    //     Group mockGroup2 = createMockGroup(groupId, "Public", 5, 10);

    //     when(readModelService.getGroup(groupId))
    //             .thenReturn(Optional.of(mockGroup1))
    //             .thenReturn(Optional.of(mockGroup2));

    //     Event mockEvent1 = new Event();
    //     mockEvent1.setId("event-123");
    //     Event mockEvent2 = new Event();
    //     mockEvent2.setId("event-456");

    //     when(eventStoreService.appendEvent(eq("JoinRequestCreate
