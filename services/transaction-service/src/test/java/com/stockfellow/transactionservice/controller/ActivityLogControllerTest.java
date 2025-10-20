package com.stockfellow.transactionservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import com.stockfellow.transactionservice.model.ActivityLog;
import com.stockfellow.transactionservice.service.ActivityLogService;

@WebMvcTest(ActivityLogController.class)
class ActivityLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActivityLogService activityLogService;

    private ActivityLog sampleLog;
    private Page<ActivityLog> samplePage;
    private UUID userId;
    private UUID cycleId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        cycleId = UUID.randomUUID();

        sampleLog = new ActivityLog();
        // Set sample log properties
        samplePage = new PageImpl<>(Arrays.asList(sampleLog));
    }

    @Test
    void getActivityLogsByUser_ReturnsOkStatus() throws Exception {
        when(activityLogService.getLogsByUser(any(UUID.class), any(Pageable.class)))
            .thenReturn(samplePage);

        mockMvc.perform(get("/api/activity-logs/user/" + userId)
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").exists())
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getActivityLogsByCycle_ReturnsOkStatus() throws Exception {
        when(activityLogService.getLogsByCycle(any(UUID.class), any(Pageable.class)))
            .thenReturn(samplePage);

        mockMvc.perform(get("/api/activity-logs/cycle/" + cycleId)
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").exists());
    }

    // @Test
    // void getActivityLogsByEntity_ReturnsOkStatus() throws Exception {
    //     when(activityLogService.getLogsByEntity(any(ActivityLog.EntityType.class), any(Pageable.class)))
    //         .thenReturn(samplePage);

    //     mockMvc.perform(get("/api/activity-logs/entity/GROUP")
    //             .param("page", "0")
    //             .param("size", "10"))
    //         .andExpect(status().isOk())
    //         .andExpect(jsonPath("$.content").exists());
    // }

    // @Test
    // void getActivityLogsByEntityId_ReturnsOkStatus() throws Exception {
    //     UUID entityId = UUID.randomUUID();
    //     when(activityLogService.getLogsByEntityId(
    //             any(ActivityLog.EntityType.class), 
    //             any(UUID.class), 
    //             any(Pageable.class)))
    //         .thenReturn(samplePage);

    //     mockMvc.perform(get("/api/activity-logs/entity/GROUP/" + entityId)
    //             .param("page", "0")
    //             .param("size", "10"))
    //         .andExpect(status().isOk())
    //         .andExpect(jsonPath("$.content").exists());
    // }
}