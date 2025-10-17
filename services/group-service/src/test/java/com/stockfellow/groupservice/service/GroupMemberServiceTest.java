package com.stockfellow.groupservice.service;

import org.bson.Document;
import com.stockfellow.groupservice.dto.NextPayeeResult;
import com.stockfellow.groupservice.model.Group;
import com.stockfellow.groupservice.repository.GroupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        group.getMembers().add(new Group.Member("user1", "User One", "member"));
        group.initializePayoutOrder(); // Initialize before mocking

        when(groupRepository.findByGroupId("group_123")).thenReturn(Optional.of(group));

        NextPayeeResult result = groupMemberService.getNextPayee("group_123");

        assertEquals("user1", result.getRecipientId());
    }

    @Test
    public void recordPayout_UpdatesPosition() {
        // Setup group with proper payout order
        Group group = new Group("group_123");
        group.setPayoutOrder(Arrays.asList("user1", "user2", "user3"));
        group.setCurrentPayoutPosition(0);

        // Add members
        Group.Member member1 = new Group.Member("user1", "User One", "member");
        Group.Member member2 = new Group.Member("user2", "User Two", "member");
        Group.Member member3 = new Group.Member("user3", "User Three", "member");
        group.setMembers(Arrays.asList(member1, member2, member3));

        // Create a second group instance to simulate the updated state
        Group updatedGroup = new Group("group_123");
        updatedGroup.setPayoutOrder(Arrays.asList("user1", "user2", "user3"));
        updatedGroup.setCurrentPayoutPosition(1); // Position advanced
        updatedGroup.setMembers(Arrays.asList(member1, member2, member3));

        when(groupRepository.findByGroupId("group_123"))
                .thenReturn(Optional.of(group)) // First call returns original group
                .thenReturn(Optional.of(updatedGroup)); // Second call returns updated group

        // Test the method
        NextPayeeResult result = groupMemberService.recordPayout("group_123", "user1", 1000.0);

        // Verify the result - should now point to user2
        assertNotNull(result);
        assertEquals("user2", result.getRecipientId());

        // Verify the position was advanced - update the matcher to work with Document
        verify(mongoTemplate).updateFirst(
                any(Query.class),
                argThat(update -> {
                    Update actualUpdate = (Update) update;
                    Document updateObject = actualUpdate.getUpdateObject(); // Changed to Document

                    // Check that $inc operation is present for currentPayoutPosition
                    boolean hasIncrement = updateObject.containsKey("$inc") &&
                            updateObject.get("$inc", Document.class).equals(new Document("currentPayoutPosition", 1));

                    // Check that $set operations are present for lastPayoutRecipient and
                    // lastPayoutDate
                    boolean hasSetOperations = updateObject.containsKey("$set");
                    if (hasSetOperations) {
                        Document setObject = updateObject.get("$set", Document.class);
                        boolean hasLastPayoutRecipient = setObject.containsKey("lastPayoutRecipient") &&
                                "user1".equals(setObject.get("lastPayoutRecipient"));
                        boolean hasLastPayoutDate = setObject.containsKey("lastPayoutDate");

                        return hasIncrement && hasSetOperations && hasLastPayoutRecipient && hasLastPayoutDate;
                    }

                    return hasIncrement;
                }),
                eq(Group.class));
    }

}