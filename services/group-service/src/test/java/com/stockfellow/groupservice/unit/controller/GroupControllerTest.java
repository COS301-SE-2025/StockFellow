package com.stockfellow.groupservice.unit.controller;

import com.stockfellow.groupservice.controller.GroupsController;
import com.stockfellow.groupservice.command.CreateGroupCommand;
import com.stockfellow.groupservice.command.JoinGroupCommand;
import com.stockfellow.groupservice.command.ProcessJoinRequestCommand;
import com.stockfellow.groupservice.model.Event;
import com.stockfellow.groupservice.model.Group;
import com.stockfellow.groupservice.repository.GroupRepository;
import com.stockfellow.groupservice.service.EventStoreService;
import com.stockfellow.groupservice.service.ReadModelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class GroupsControllerTest {

    @Mock
    private CreateGroupCommand createGroupCommand;
    
    @Mock
    private JoinGroupCommand joinGroupCommand;
    
    @Mock
    private ProcessJoinRequestCommand processJoinRequestCommand;
    
    @Mock
    private ReadModelService readModelService;
    
    @Mock
    private EventStoreService eventStoreService;
    
    @Mock
    private GroupRepository groupRepository;
    
    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private GroupsController groupsController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Group testGroup;
    private final String TEST_USER_ID = "user123";
    private final String TEST_USERNAME = "testuser";
    private final String TEST_GROUP_ID = "group123";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(groupsController).build();
        objectMapper = new ObjectMapper();
        
        // Setup test group
        testGroup = new Group();
        testGroup.setId("1");
        testGroup.setGroupId(TEST_GROUP_ID);
        testGroup.setName("Test Group");
        testGroup.setDescription("Test Description");
        testGroup.setAdminId(TEST_USER_ID);
        testGroup.setMinContribution(100.0);
        testGroup.setMaxMembers(10);
        testGroup.setVisibility("Public");
        testGroup.setContributionFrequency("Monthly");
        testGroup.setPayoutFrequency("Monthly");
        testGroup.setBalance(100.0);
        testGroup.setCreatedAt(new Date());
        
        // Setup members list
        List<Group.Member> members = new ArrayList<>();
        Group.Member adminMember = new Group.Member(TEST_USER_ID, "founder");
        adminMember.setContribution(100.0);
        members.add(adminMember);
        testGroup.setMembers(members);
        
        // Setup requests list
        testGroup.setRequests(new ArrayList<>());
    }

    @Test
    void getServiceInfo_ShouldReturnServiceInformation() {
        // When
        Map<String, Object> response = groupsController.getServiceInfo();

        // Then
        assertNotNull(response);
        assertEquals("Group Service", response.get("service"));
        assertEquals("1.0.0", response.get("version"));
        assertNotNull(response.get("endpoints"));
        assertTrue(response.get("endpoints") instanceof List);
    }

    @Test
    void searchPublicGroups_WithQuery_ShouldReturnMatchingGroups() {
        // Given
        List<Group> groups = Arrays.asList(testGroup);
        when(groupRepository.findPublicGroupsByNameContaining("Test")).thenReturn(groups);

        // When
        ResponseEntity<?> response = groupsController.searchPublicGroups("Test");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertEquals(1, responseBody.get("totalCount"));
        assertEquals("Test", responseBody.get("query"));
        
        List<Map<String, Object>> searchResults = (List<Map<String, Object>>) responseBody.get("groups");
        assertEquals(1, searchResults.size());
        assertEquals(TEST_GROUP_ID, searchResults.get(0).get("groupId"));
    }

    @Test
    void searchPublicGroups_WithoutQuery_ShouldReturnAllPublicGroups() {
        // Given
        List<Group> groups = Arrays.asList(testGroup);
        when(groupRepository.findPublicGroups()).thenReturn(groups);

        // When
        ResponseEntity<?> response = groupsController.searchPublicGroups(null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(groupRepository).findPublicGroups();
    }

    @Test
    void searchPublicGroups_WithException_ShouldReturnInternalServerError() {
        // Given
        when(groupRepository.findPublicGroupsByNameContaining(anyString()))
                .thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<?> response = groupsController.searchPublicGroups("Test");

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Internal server error during search", responseBody.get("error"));
    }

    @Test
    void viewGroup_WithValidUser_ShouldReturnGroupDetails() {
        // Given
        when(httpServletRequest.getHeader("X-User-Id")).thenReturn(TEST_USER_ID);
        when(httpServletRequest.getHeader("X-User-Name")).thenReturn(TEST_USERNAME);
        when(readModelService.getGroup(TEST_GROUP_ID)).thenReturn(Optional.of(testGroup));
        when(eventStoreService.getEvents(TEST_GROUP_ID)).thenReturn(new ArrayList<>());

        // When
        ResponseEntity<?> response = groupsController.viewGroup(TEST_GROUP_ID, httpServletRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertEquals(testGroup, responseBody.get("group"));
        assertNotNull(responseBody.get("userPermissions"));
    }

    @Test
    void viewGroup_WithoutUserId_ShouldReturnUnauthorized() {
        // Given
        when(httpServletRequest.getHeader("X-User-Id")).thenReturn(null);

        // When
        ResponseEntity<?> response = groupsController.viewGroup(TEST_GROUP_ID, httpServletRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("User ID not found in request", responseBody.get("error"));
    }

    @Test
    void viewGroup_WithNonExistentGroup_ShouldReturnNotFound() {
        // Given
        when(httpServletRequest.getHeader("X-User-Id")).thenReturn(TEST_USER_ID);
        when(readModelService.getGroup(TEST_GROUP_ID)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = groupsController.viewGroup(TEST_GROUP_ID, httpServletRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Group not found", responseBody.get("error"));
    }

    @Test
    void viewGroup_PrivateGroupNonMember_ShouldReturnForbidden() {
        // Given
        testGroup.setVisibility("Private");
        testGroup.setMembers(new ArrayList<>()); // No members
        when(httpServletRequest.getHeader("X-User-Id")).thenReturn("differentUser");
        when(readModelService.getGroup(TEST_GROUP_ID)).thenReturn(Optional.of(testGroup));

        // When
        ResponseEntity<?> response = groupsController.viewGroup(TEST_GROUP_ID, httpServletRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Access denied. You must be a member to view this private group.", responseBody.get("error"));
    }

    @Test
    void createGroup_WithValidData_ShouldCreateGroup() {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("name", "New Group");
        request.put("minContribution", 100.0);
        request.put("maxMembers", 10);
        request.put("description", "New Group Description");
        request.put("visibility", "Public");
        request.put("contributionFrequency", "Monthly");
        request.put("payoutFrequency", "Monthly");

        when(httpServletRequest.getHeader("X-User-Id")).thenReturn(TEST_USER_ID);
        when(httpServletRequest.getHeader("X-User-Name")).thenReturn(TEST_USERNAME);
        when(createGroupCommand.execute(anyString(), eq(TEST_USER_ID), eq("New Group"), 
                eq(100.0), eq(10), eq("New Group Description"), isNull(), eq("Public"), 
                eq("Monthly"), isNull(), eq("Monthly"), isNull(), anyList()))
                .thenReturn("event123");

        // When
        ResponseEntity<?> response = groupsController.createGroup(request, httpServletRequest);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Group created successfully", responseBody.get("message"));
        assertNotNull(responseBody.get("groupId"));
        assertEquals("event123", responseBody.get("eventId"));
    }

    @Test
    void createGroup_WithoutUserId_ShouldReturnUnauthorized() {
        // Given
        Map<String, Object> request = new HashMap<>();
        when(httpServletRequest.getHeader("X-User-Id")).thenReturn(null);

        // When
        ResponseEntity<?> response = groupsController.createGroup(request, httpServletRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("User ID not found in request", responseBody.get("error"));
    }

    @Test
    void createGroup_WithMissingName_ShouldReturnBadRequest() {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("minContribution", 100.0);
        request.put("maxMembers", 10);
        request.put("visibility", "Public");
        request.put("contributionFrequency", "Monthly");
        request.put("payoutFrequency", "Monthly");

        when(httpServletRequest.getHeader("X-User-Id")).thenReturn(TEST_USER_ID);

        // When
        ResponseEntity<?> response = groupsController.createGroup(request, httpServletRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Group name is required", responseBody.get("error"));
    }

    @Test
    void createGroup_WithInvalidContributionFrequency_ShouldReturnBadRequest() {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Test Group");
        request.put("minContribution", 100.0);
        request.put("maxMembers", 10);
        request.put("visibility", "Public");
        request.put("contributionFrequency", "Invalid");
        request.put("payoutFrequency", "Monthly");

        when(httpServletRequest.getHeader("X-User-Id")).thenReturn(TEST_USER_ID);

        // When
        ResponseEntity<?> response = groupsController.createGroup(request, httpServletRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Invalid contribution frequency. Must be: Monthly, Bi-weekly, or Weekly", responseBody.get("error"));
    }

    @Test
    void createGroup_WithInvalidMinContribution_ShouldReturnBadRequest() {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Test Group");
        request.put("minContribution", -100.0);
        request.put("maxMembers", 10);
        request.put("visibility", "Public");
        request.put("contributionFrequency", "Monthly");
        request.put("payoutFrequency", "Monthly");

        when(httpServletRequest.getHeader("X-User-Id")).thenReturn(TEST_USER_ID);

        // When
        ResponseEntity<?> response = groupsController.createGroup(request, httpServletRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Minimum contribution must be greater than 0", responseBody.get("error"));
    }

    @Test
    void createGroup_WithCommandException_ShouldReturnInternalServerError() {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Test Group");
        request.put("minContribution", 100.0);
        request.put("maxMembers", 10);
        request.put("visibility", "Public");
        request.put("contributionFrequency", "Monthly");
        request.put("payoutFrequency", "Monthly");

        when(httpServletRequest.getHeader("X-User-Id")).thenReturn(TEST_USER_ID);
        when(createGroupCommand.execute(anyString(), anyString(), anyString(), anyDouble(), 
                anyInt(), anyString(), anyString(), anyString(), anyString(), any(), 
                anyString(), any(), anyList()))
                .thenThrow(new RuntimeException("Command failed"));

        // When
        ResponseEntity<?> response = groupsController.createGroup(request, httpServletRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Internal server error", responseBody.get("error"));
    }

    @Test
    void getUserGroups_WithValidUser_ShouldReturnGroups() {
        // Given
        List<Group> groups = Arrays.asList(testGroup);
        when(httpServletRequest.getHeader("X-User-Id")).thenReturn(TEST_USER_ID);
        when(readModelService.getUserGroups(TEST_USER_ID)).thenReturn(groups);

        // When
        ResponseEntity<?> response = groupsController.getUserGroups(httpServletRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(groups, response.getBody());
    }

    @Test
    void getUserGroups_WithoutUserId_ShouldReturnUnauthorized() {
        // Given
        when(httpServletRequest.getHeader("X-User-Id")).thenReturn(null);

        // When
        ResponseEntity<?> response = groupsController.getUserGroups(httpServletRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("User ID not found in request", responseBody.get("error"));
    }

    @Test
    void requestToJoinGroup_WithValidPublicGroup_ShouldCreateJoinRequest() {
        // Given
        when(httpServletRequest.getHeader("X-User-Id")).thenReturn("newUser");
        when(readModelService.getGroup(TEST_GROUP_ID)).thenReturn(Optional.of(testGroup));
        when(joinGroupCommand.createJoinRequest(TEST_GROUP_ID, "newUser")).thenReturn("event123");

        // When
        ResponseEntity<?> response = groupsController.requestToJoinGroup(TEST_GROUP_ID, httpServletRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Join request sent successfully. Waiting for admin approval.", responseBody.get("message"));
        assertEquals("pending", responseBody.get("status"));
    }

    @Test
    void requestToJoinGroup_WithPrivateGroup_ShouldReturnForbidden() {
        // Given
        testGroup.setVisibility("Private");
        when(httpServletRequest.getHeader("X-User-Id")).thenReturn("newUser");
        when(readModelService.getGroup(TEST_GROUP_ID)).thenReturn(Optional.of(testGroup));

        // When
        ResponseEntity<?> response = groupsController.requestToJoinGroup(TEST_GROUP_ID, httpServletRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Cannot request to join private groups. You need an invite link.", responseBody.get("error"));
    }

    @Test
    void requestToJoinGroup_WithExistingMember_ShouldReturnBadRequest() {
        // Given
        when(httpServletRequest.getHeader("X-User-Id")).thenReturn(TEST_USER_ID);
        when(readModelService.getGroup(TEST_GROUP_ID)).thenReturn(Optional.of(testGroup));

        // When
        ResponseEntity<?> response = groupsController.requestToJoinGroup(TEST_GROUP_ID, httpServletRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("You are already a member of this group", responseBody.get("error"));
    }

    @Test
    void requestToJoinGroup_WithFullGroup_ShouldReturnBadRequest() {
        // Given
        // Fill the group to max capacity
        for (int i = 1; i < 10; i++) {
            testGroup.getMembers().add(new Group.Member("user" + i, "member"));
        }
        
        when(httpServletRequest.getHeader("X-User-Id")).thenReturn("newUser");
        when(readModelService.getGroup(TEST_GROUP_ID)).thenReturn(Optional.of(testGroup));

        // When
        ResponseEntity<?> response = groupsController.requestToJoinGroup(TEST_GROUP_ID, httpServletRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Group is full", responseBody.get("error"));
    }

    @Test
    void getGroupJoinRequests_WithValidAdmin_ShouldReturnRequests() {
        // Given
        Group.JoinRequest request = new Group.JoinRequest();
        request.setRequestId("req123");
        request.setUserId("user456");
        request.setState("waiting");
        testGroup.getRequests().add(request);

        when(httpServletRequest.getHeader("X-User-Id")).thenReturn(TEST_USER_ID);
        when(readModelService.getGroup(TEST_GROUP_ID)).thenReturn(Optional.of(testGroup));

        // When
        ResponseEntity<?> response = groupsController.getGroupJoinRequests(TEST_GROUP_ID, httpServletRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals(TEST_GROUP_ID, responseBody.get("groupId"));
        assertEquals(1, responseBody.get("totalPendingRequests"));
    }

    @Test
    void getGroupJoinRequests_WithNonAdmin_ShouldReturnForbidden() {
        // Given
        when(httpServletRequest.getHeader("X-User-Id")).thenReturn("nonAdmin");
        when(readModelService.getGroup(TEST_GROUP_ID)).thenReturn(Optional.of(testGroup));

        // When
        ResponseEntity<?> response = groupsController.getGroupJoinRequests(TEST_GROUP_ID, httpServletRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Access denied. Only group admins can view join requests.", responseBody.get("error"));
    }

    @Test
    void processJoinRequest_WithValidAcceptAction_ShouldAcceptRequest() {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("requestId", "req123");
        request.put("action", "accept");

        when(httpServletRequest.getHeader("X-User-Id")).thenReturn(TEST_USER_ID);
        when(readModelService.getGroup(TEST_GROUP_ID)).thenReturn(Optional.of(testGroup));
        when(processJoinRequestCommand.execute(TEST_GROUP_ID, "req123", "accept", TEST_USER_ID))
                .thenReturn("event123");

        // When
        ResponseEntity<?> response = groupsController.processJoinRequest(TEST_GROUP_ID, request, httpServletRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Join request accepted successfully", responseBody.get("message"));
        assertEquals("accept", responseBody.get("action"));
    }

    @Test
    void processJoinRequest_WithValidRejectAction_ShouldRejectRequest() {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("requestId", "req123");
        request.put("action", "reject");

        when(httpServletRequest.getHeader("X-User-Id")).thenReturn(TEST_USER_ID);
        when(readModelService.getGroup(TEST_GROUP_ID)).thenReturn(Optional.of(testGroup));
        when(processJoinRequestCommand.execute(TEST_GROUP_ID, "req123", "reject", TEST_USER_ID))
                .thenReturn("event123");

        // When
        ResponseEntity<?> response = groupsController.processJoinRequest(TEST_GROUP_ID, request, httpServletRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Join request rejected successfully", responseBody.get("message"));
        assertEquals("reject", responseBody.get("action"));
    }

    @Test
    void processJoinRequest_WithInvalidAction_ShouldReturnBadRequest() {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("requestId", "req123");
        request.put("action", "invalid");

        when(httpServletRequest.getHeader("X-User-Id")).thenReturn(TEST_USER_ID);

        // When
        ResponseEntity<?> response = groupsController.processJoinRequest(TEST_GROUP_ID, request, httpServletRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Action must be 'accept' or 'reject'", responseBody.get("error"));
    }

    @Test
    void processJoinRequest_WithMissingRequestId_ShouldReturnBadRequest() {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("action", "accept");

        when(httpServletRequest.getHeader("X-User-Id")).thenReturn(TEST_USER_ID);

        // When
        ResponseEntity<?> response = groupsController.processJoinRequest(TEST_GROUP_ID, request, httpServletRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Request ID is required", responseBody.get("error"));
    }

    @Test
    void processJoinRequest_WithNonAdmin_ShouldReturnForbidden() {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("requestId", "req123");
        request.put("action", "accept");

        when(httpServletRequest.getHeader("X-User-Id")).thenReturn("nonAdmin");
        when(readModelService.getGroup(TEST_GROUP_ID)).thenReturn(Optional.of(testGroup));

        // When
        ResponseEntity<?> response = groupsController.processJoinRequest(TEST_GROUP_ID, request, httpServletRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Only group admins can process join requests", responseBody.get("error"));
    }
}
