
package com.stockfellow.groupservice.unit.command;

import com.stockfellow.groupservice.command.ProcessJoinRequestCommand;
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
class ProcessJoinRequestCommandTest {

    @Mock
    private EventStoreService eventStoreService;

    @Mock
    private ReadModelService readModelService;

    private ProcessJoinRequestCommand processJoinRequestCommand;

    @BeforeEach
    void setUp() {
        processJoinRequestCommand = new ProcessJoinRequestCommand(eventStoreService, readModelService);
    }

    @Test
    @DisplayName("Should accept join request successfully by group admin")
    void testExecute_AcceptRequest_ByGroupAdmin_Success() {
        // Arrange
        String groupId = "group-123";
        String requestId = "request-456";
        String adminId = "admin-789";
        String userId = "user-101";

        Group mockGroup = createMockGroupWithRequest(groupId, adminId, userId, requestId, 5, 10);
        when(readModelService.getGroup(groupId)).thenReturn(Optional.of(mockGroup));

        Event mockEvent = new Event();
        mockEvent.setId("event-accepted");
        when(eventStoreService.appendEvent(eq("JoinRequestAccepted"), any(Map.class))).thenReturn(mockEvent);

        // Act
        String result = processJoinRequestCommand.execute(groupId, requestId, "accept", adminId);

        // Assert
        assertEquals("event-accepted", result);
        
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(eventStoreService).appendEvent(eq("JoinRequestAccepted"), dataCaptor.capture());
        verify(readModelService).rebuildState(groupId);

        Map<String, Object> capturedData = dataCaptor.getValue();
        assertEquals(groupId, capturedData.get("groupId"));
        assertEquals(requestId, capturedData.get("requestId"));
        assertEquals(userId, capturedData.get("userId"));
        assertEquals("accept", capturedData.get("action"));
        assertEquals(adminId, capturedData.get("processedBy"));
        assertEquals("member", capturedData.get("role"));
        assertNotNull(capturedData.get("processedAt"));
        assertNotNull(capturedData.get("joinedAt"));
    }

    @Test
    @DisplayName("Should reject join request successfully by group admin")
    void testExecute_RejectRequest_ByGroupAdmin_Success() {
        // Arrange
        String groupId = "group-123";
        String requestId = "request-456";
        String adminId = "admin-789";
        String userId = "user-101";

        Group mockGroup = createMockGroupWithRequest(groupId, adminId, userId, requestId, 5, 10);
        when(readModelService.getGroup(groupId)).thenReturn(Optional.of(mockGroup));

        Event mockEvent = new Event();
        mockEvent.setId("event-rejected");
        when(eventStoreService.appendEvent(eq("JoinRequestRejected"), any(Map.class))).thenReturn(mockEvent);

        // Act
        String result = processJoinRequestCommand.execute(groupId, requestId, "reject", adminId);

        // Assert
        assertEquals("event-rejected", result);
        
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(eventStoreService).appendEvent(eq("JoinRequestRejected"), dataCaptor.capture());
        verify(readModelService).rebuildState(groupId);

        Map<String, Object> capturedData = dataCaptor.getValue();
        assertEquals(groupId, capturedData.get("groupId"));
        assertEquals(requestId, capturedData.get("requestId"));
        assertEquals(userId, capturedData.get("userId"));
        assertEquals("reject", capturedData.get("action"));
        assertEquals(adminId, capturedData.get("processedBy"));
        assertEquals("Request rejected by admin", capturedData.get("reason"));
        assertNotNull(capturedData.get("processedAt"));
    }

    @Test
    @DisplayName("Should accept request by member with founder role")
    void testExecute_AcceptRequest_ByFounderMember_Success() {
        // Arrange
        String groupId = "group-123";
        String requestId = "request-456";
        String founderId = "founder-789";
        String userId = "user-101";

        Group mockGroup = createMockGroupWithRequest(groupId, "different-admin", userId, requestId, 5, 10);
        // Add founder member
        Group.Member founderMember = new Group.Member(founderId, "founder");
        mockGroup.getMembers().add(founderMember);

        when(readModelService.getGroup(groupId)).thenReturn(Optional.of(mockGroup));

        Event mockEvent = new Event();
        mockEvent.setId("event-accepted");
        when(eventStoreService.appendEvent(eq("JoinRequestAccepted"), any(Map.class))).thenReturn(mockEvent);

        // Act
        String result = processJoinRequestCommand.execute(groupId, requestId, "accept", founderId);

        // Assert
        assertEquals("event-accepted", result);
        verify(eventStoreService).appendEvent(eq("JoinRequestAccepted"), any(Map.class));
        verify(readModelService).rebuildState(groupId);
    }

    @Test
    @DisplayName("Should accept request by member with admin role")
    void testExecute_AcceptRequest_ByAdminMember_Success() {
        // Arrange
        String groupId = "group-123";
        String requestId = "request-456";
        String adminMemberId = "admin-member-789";
        String userId = "user-101";

        Group mockGroup = createMockGroupWithRequest(groupId, "different-admin", userId, requestId, 5, 10);
        // Add admin member
        Group.Member adminMember = new Group.Member(adminMemberId, "admin");
        mockGroup.getMembers().add(adminMember);

        when(readModelService.getGroup(groupId)).thenReturn(Optional.of(mockGroup));

        Event mockEvent = new Event();
        mockEvent.setId("event-accepted");
        when(eventStoreService.appendEvent(eq("JoinRequestAccepted"), any(Map.class))).thenReturn(mockEvent);

        // Act
        String result = processJoinRequestCommand.execute(groupId, requestId, "accept", adminMemberId);

        // Assert
        assertEquals("event-accepted", result);
        verify(eventStoreService).appendEvent(eq("JoinRequestAccepted"), any(Map.class));
        verify(readModelService).rebuildState(groupId);
    }

    @Test
    @DisplayName("Should throw exception when group not found")
    void testExecute_GroupNotFound_ThrowsException() {
        // Arrange
        String groupId = "non-existent-group";
        String requestId = "request-456";
        String adminId = "admin-789";

        when(readModelService.getGroup(groupId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            processJoinRequestCommand.execute(groupId, requestId, "accept", adminId);
        });

        assertEquals("Group not found with ID: " + groupId, exception.getMessage());
        verify(eventStoreService, never()).appendEvent(any(), any());
        verify(readModelService, never()).rebuildState(any());
    }

    @Test
    @DisplayName("Should throw exception when request not found")
    void testExecute_RequestNotFound_ThrowsException() {
        // Arrange
        String groupId = "group-123";
        String requestId = "non-existent-request";
        String adminId = "admin-789";

        Group mockGroup = createMockGroupWithRequest(groupId, adminId, "user-101", "different-request", 5, 10);
        when(readModelService.getGroup(groupId)).thenReturn(Optional.of(mockGroup));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            processJoinRequestCommand.execute(groupId, requestId, "accept", adminId);
        });

        assertEquals("Join request not found with ID: " + requestId, exception.getMessage());
        verify(eventStoreService, never()).appendEvent(any(), any());
        verify(readModelService, never()).rebuildState(any());
    }

    @Test
    @DisplayName("Should throw exception when user lacks permissions")
    void testExecute_UnauthorizedUser_ThrowsException() {
        // Arrange
        String groupId = "group-123";
        String requestId = "request-456";
        String unauthorizedId = "unauthorized-user";
        String userId = "user-101";

        Group mockGroup = createMockGroupWithRequest(groupId, "admin-789", userId, requestId, 5, 10);
        when(readModelService.getGroup(groupId)).thenReturn(Optional.of(mockGroup));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            processJoinRequestCommand.execute(groupId, requestId, "accept", unauthorizedId);
        });

        assertEquals("User does not have permission to process join requests for this group", exception.getMessage());
        verify(eventStoreService, never()).appendEvent(any(), any());
        verify(readModelService, never()).rebuildState(any());
    }

    @Test
    @DisplayName("Should throw exception when request already processed")
    void testExecute_RequestAlreadyProcessed_ThrowsException() {
        // Arrange
        String groupId = "group-123";
        String requestId = "request-456";
        String adminId = "admin-789";
        String userId = "user-101";

        Group mockGroup = createMockGroupWithRequest(groupId, adminId, userId, requestId, 5, 10);
        // Set request as already processed
        mockGroup.getRequests().get(0).setState("accepted");

        when(readModelService.getGroup(groupId)).thenReturn(Optional.of(mockGroup));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            processJoinRequestCommand.execute(groupId, requestId, "accept", adminId);
        });

        assertEquals("Join request has already been processed. Current state: accepted", exception.getMessage());
        verify(eventStoreService, never()).appendEvent(any(), any());
        verify(readModelService, never()).rebuildState(any());
    }

    @Test
    @DisplayName("Should throw exception when group is full during accept")
    void testExecute_GroupFullOnAccept_ThrowsException() {
        // Arrange
        String groupId = "group-123";
        String requestId = "request-456";
        String adminId = "admin-789";
        String userId = "user-101";

        Group mockGroup = createMockGroupWithRequest(groupId, adminId, userId, requestId, 10, 10); // Full group
        when(readModelService.getGroup(groupId)).thenReturn(Optional.of(mockGroup));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            processJoinRequestCommand.execute(groupId, requestId, "accept", adminId);
        });

        assertTrue(exception.getMessage().contains("Cannot accept join request: group is at maximum capacity"));
        verify(eventStoreService, never()).appendEvent(any(), any());
        verify(readModelService, never()).rebuildState(any());
    }

    @Test
    @DisplayName("Should throw exception when user is already a member during accept")
    void testExecute_UserAlreadyMemberOnAccept_ThrowsException() {
        // Arrange
        String groupId = "group-123";
        String requestId = "request-456";
        String adminId = "admin-789";
        String userId = "user-101";

        Group mockGroup = createMockGroupWithRequest(groupId, adminId, userId, requestId, 5, 10);
        // Add user as existing member
        Group.Member existingMember = new Group.Member(userId, "member");
        mockGroup.getMembers().add(existingMember);

        when(readModelService.getGroup(groupId)).thenReturn(Optional.of(mockGroup));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            processJoinRequestCommand.execute(groupId, requestId, "accept", adminId);
        });

        assertEquals("User is already a member of this group", exception.getMessage());
        verify(eventStoreService, never()).appendEvent(any(), any());
        verify(readModelService, never()).rebuildState(any());
    }

    @Test
    @DisplayName("Should throw exception for invalid input parameters")
    void testExecute_InvalidInputParameters_ThrowsException() {
        // Test null group ID
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () -> {
            processJoinRequestCommand.execute(null, "request-456", "accept", "admin-789");
        });
        assertEquals("Group ID cannot be null or empty", exception1.getMessage());

        // Test empty group ID
        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> {
            processJoinRequestCommand.execute("  ", "request-456", "accept", "admin-789");
        });
        assertEquals("Group ID cannot be null or empty", exception2.getMessage());

        // Test null request ID
        IllegalArgumentException exception3 = assertThrows(IllegalArgumentException.class, () -> {
            processJoinRequestCommand.execute("group-123", null, "accept", "admin-789");
        });
        assertEquals("Request ID cannot be null or empty", exception3.getMessage());

        // Test empty request ID
        IllegalArgumentException exception4 = assertThrows(IllegalArgumentException.class, () -> {
            processJoinRequestCommand.execute("group-123", "  ", "accept", "admin-789");
        });
        assertEquals("Request ID cannot be null or empty", exception4.getMessage());

        // Test null action
        IllegalArgumentException exception5 = assertThrows(IllegalArgumentException.class, () -> {
            processJoinRequestCommand.execute("group-123", "request-456", null, "admin-789");
        });
        assertEquals("Action cannot be null or empty", exception5.getMessage());

        // Test empty action
        IllegalArgumentException exception6 = assertThrows(IllegalArgumentException.class, () -> {
            processJoinRequestCommand.execute("group-123", "request-456", "  ", "admin-789");
        });
        assertEquals("Action cannot be null or empty", exception6.getMessage());

        // Test null admin ID
        IllegalArgumentException exception7 = assertThrows(IllegalArgumentException.class, () -> {
            processJoinRequestCommand.execute("group-123", "request-456", "accept", null);
        });
        assertEquals("Admin ID cannot be null or empty", exception7.getMessage());

        // Test empty admin ID
        IllegalArgumentException exception8 = assertThrows(IllegalArgumentException.class, () -> {
            processJoinRequestCommand.execute("group-123", "request-456", "accept", "  ");
        });
        assertEquals("Admin ID cannot be null or empty", exception8.getMessage());

        // Test invalid action
        IllegalArgumentException exception9 = assertThrows(IllegalArgumentException.class, () -> {
            processJoinRequestCommand.execute("group-123", "request-456", "invalid", "admin-789");
        });
        assertEquals("Action must be 'accept' or 'reject', but was: invalid", exception9.getMessage());
    }

    @Test
    @DisplayName("Should allow regular member with member role to be denied permissions")
    void testExecute_RegularMemberDeniedPermissions_ThrowsException() {
        // Arrange
        String groupId = "group-123";
        String requestId = "request-456";
        String regularMemberId = "regular-member";
        String userId = "user-101";

        Group mockGroup = createMockGroupWithRequest(groupId, "admin-789", userId, requestId, 5, 10);
        // Add regular member (not admin or founder)
        Group.Member regularMember = new Group.Member(regularMemberId, "member");
        mockGroup.getMembers().add(regularMember);

        when(readModelService.getGroup(groupId)).thenReturn(Optional.of(mockGroup));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            processJoinRequestCommand.execute(groupId, requestId, "accept", regularMemberId);
        });

        assertEquals("User does not have permission to process join requests for this group", exception.getMessage());
        verify(eventStoreService, never()).appendEvent(any(), any());
        verify(readModelService, never()).rebuildState(any());
    }

    @Test
    @DisplayName("Should handle group at capacity minus one successfully")
    void testExecute_GroupAtCapacityMinusOne_Success() {
        // Arrange
        String groupId = "group-123";
        String requestId = "request-456";
        String adminId = "admin-789";
        String userId = "user-101";

        Group mockGroup = createMockGroupWithRequest(groupId, adminId, userId, requestId, 9, 10); // 9/10 capacity
        when(readModelService.getGroup(groupId)).thenReturn(Optional.of(mockGroup));

        Event mockEvent = new Event();
        mockEvent.setId("event-accepted");
        when(eventStoreService.appendEvent(eq("JoinRequestAccepted"), any(Map.class))).thenReturn(mockEvent);

        // Act
        String result = processJoinRequestCommand.execute(groupId, requestId, "accept", adminId);

        // Assert
        assertEquals("event-accepted", result);
        verify(eventStoreService).appendEvent(eq("JoinRequestAccepted"), any(Map.class));
        verify(readModelService).rebuildState(groupId);
    }

    @Test
    @DisplayName("Should validate timestamp fields are properly set")
    void testExecute_ValidateTimestampFields() {
        // Arrange
        String groupId = "group-123";
        String requestId = "request-456";
        String adminId = "admin-789";
        String userId = "user-101";

        Group mockGroup = createMockGroupWithRequest(groupId, adminId, userId, requestId, 5, 10);
        when(readModelService.getGroup(groupId)).thenReturn(Optional.of(mockGroup));

        Event mockEvent = new Event();
        mockEvent.setId("event-accepted");
        when(eventStoreService.appendEvent(eq("JoinRequestAccepted"), any(Map.class))).thenReturn(mockEvent);

        long beforeExecution = System.currentTimeMillis();

        // Act
        processJoinRequestCommand.execute(groupId, requestId, "accept", adminId);

        long afterExecution = System.currentTimeMillis();

        // Assert
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(eventStoreService).appendEvent(eq("JoinRequestAccepted"), dataCaptor.capture());

        Map<String, Object> capturedData = dataCaptor.getValue();
        Long processedAt = (Long) capturedData.get("processedAt");
        Long joinedAt = (Long) capturedData.get("joinedAt");

        assertNotNull(processedAt);
        assertNotNull(joinedAt);
        assertTrue(processedAt >= beforeExecution && processedAt <= afterExecution);
        assertTrue(joinedAt >= beforeExecution && joinedAt <= afterExecution);
    }

    /**
     * Helper method to create a mock Group with a join request
     */
    private Group createMockGroupWithRequest(String groupId, String adminId, String userId, 
                                           String requestId, int currentMembers, int maxMembers) {
        Group group = new Group();
        group.setId(groupId);
        group.setAdminId(adminId);
        group.setMaxMembers(maxMembers);
        
        // Create members list
        List<Group.Member> members = new ArrayList<>();
        for (int i = 0; i < currentMembers; i++) {
            Group.Member member = new Group.Member("existing-member-" + i, "member");
            members.add(member);
        }
        group.setMembers(members);
        
        // Create join request
        Group.JoinRequest joinRequest = new Group.JoinRequest();
        joinRequest.setRequestId(requestId);
        joinRequest.setUserId(userId);
        joinRequest.setState("waiting");
        joinRequest.setTimestamp(new Date());
        
        List<Group.JoinRequest> requests = new ArrayList<>();
        requests.add(joinRequest);
        group.setRequests(requests);
        
        return group;
    }
}