package com.stockfellow.groupservice.service;

import com.stockfellow.groupservice.dto.CreateGroupResult;
import com.stockfellow.groupservice.dto.UpdateGroupRequest;
import com.stockfellow.groupservice.model.Group;
import com.stockfellow.groupservice.repository.GroupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private EventStoreService eventStoreService;

    @Mock
    private GroupMemberService groupMemberService;

    @InjectMocks
    private GroupService groupService;

    @Test
    public void updateGroup_ValidRequest_Success() {
        Group existingGroup = new Group("group_123");
        existingGroup.setMaxMembers(10);
        
        when(groupRepository.findByGroupId("group_123")).thenReturn(Optional.of(existingGroup));
        when(groupRepository.save(any(Group.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateGroupRequest request = new UpdateGroupRequest();
        request.setName("Updated Name");
        request.setMaxMembers(15);

        Group result = groupService.updateGroup("group_123", request);
        
        assertEquals("Updated Name", result.getName());
        assertEquals(15, result.getMaxMembers());
    }

    @Test
    public void createGroupForTier_NewGroupCreated() {
        when(groupRepository.findByTierAndVisibility(anyInt(), anyString())).thenReturn(List.of());
        when(groupRepository.save(any(Group.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateGroupResult result = groupService.createGroupForTier(3, "user123", "testuser");
        
        assertNotNull(result.getGroupId());
        verify(groupRepository).save(any(Group.class));
    }
}