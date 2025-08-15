package com.stockfellow.groupservice.service;

import com.stockfellow.groupservice.model.Event;
import com.stockfellow.groupservice.dto.NextPayeeResult;
import com.stockfellow.groupservice.model.Group;
import com.stockfellow.groupservice.repository.GroupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GroupMemberServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private EventStoreService eventStoreService;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private GroupMemberService groupMemberService;

    @Test
    public void getNextPayee_ReturnsValidRecipient() {
        Group group = new Group("group_123");
        group.initializePayoutOrder();
        group.getMembers().add(new Group.Member("user1", "User One", "member"));
        
        when(groupRepository.findByGroupId("group_123")).thenReturn(Optional.of(group));

        NextPayeeResult result = groupMemberService.getNextPayee("group_123");
        
        assertEquals("user1", result.getRecipientId());
    }

    @Test
    public void recordPayout_UpdatesPosition() {
        Group group = new Group("group_123");
        group.initializePayoutOrder();
        group.getMembers().add(new Group.Member("user1", "User One", "member"));
        
        when(groupRepository.findByGroupId("group_123")).thenReturn(Optional.of(group));

        NextPayeeResult result = groupMemberService.recordPayout("group_123", "user1", 1000.0);
        
        verify(eventStoreService).saveEvent(eq("group_123"), any(Event.class));
    }
}