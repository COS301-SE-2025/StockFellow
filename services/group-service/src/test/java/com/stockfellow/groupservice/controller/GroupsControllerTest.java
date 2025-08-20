package com.stockfellow.groupservice.controller;

import com.stockfellow.groupservice.dto.CreateGroupResult;
import com.stockfellow.groupservice.dto.NextPayeeResult;
import com.stockfellow.groupservice.dto.UpdateGroupRequest;
import com.stockfellow.groupservice.model.Group;
import com.stockfellow.groupservice.service.EventStoreService;
import com.stockfellow.groupservice.service.GroupMemberService;
import com.stockfellow.groupservice.service.GroupService;
import com.stockfellow.groupservice.service.ReadModelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(GroupsController.class)
public class GroupsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GroupService groupService;

    @MockBean
    private GroupMemberService groupMemberService;

    @MockBean
    private ReadModelService readModelService;

    @MockBean
    private EventStoreService eventStoreService;

    @Test
    public void updateGroup_Success() throws Exception {
        when(readModelService.isUserAdminOfGroup(anyString(), anyString())).thenReturn(true);
        when(groupService.updateGroup(anyString(), any(UpdateGroupRequest.class)))
            .thenReturn(new Group("group_123"));

        mockMvc.perform(put("/api/groups/group_123")
                .header("X-User-Id", "user123")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Updated Name\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void joinOrCreateStokvel_Success() throws Exception {
        when(groupService.createGroupForTier(anyInt(), anyString(), anyString()))
            .thenReturn(new CreateGroupResult("group_123", null, "Success"));

        mockMvc.perform(post("/api/groups/join-tier?tier=3")
                .header("X-User-Id", "user123")
                .header("X-Username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId").exists());
    }

    @Test
    public void getNextPayee_Success() throws Exception {
        when(groupMemberService.getNextPayee(anyString()))
            .thenReturn(new NextPayeeResult("group_123", "Test Group", "user456", 
                "recipient", "member", 1, 5, 1000.0, null, null, "Monthly", null));

        mockMvc.perform(get("/api/groups/group_123/next-payee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId").value("group_123"));
    }
}