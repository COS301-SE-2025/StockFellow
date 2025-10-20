package com.stockfellow.transactionservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockfellow.transactionservice.dto.CreateGroupCycleDto;
import com.stockfellow.transactionservice.dto.UpdateCycleStatusDto;
import com.stockfellow.transactionservice.model.GroupCycle;
import com.stockfellow.transactionservice.service.GroupCycleService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

@WebMvcTest(GroupCycleController.class)
class GroupCycleControllerTest {

    // @Autowired
    // private MockMvc mockMvc;

    // @Autowired
    // private ObjectMapper objectMapper;

    // @MockBean
    // private GroupCycleService groupCycleService;

    // private GroupCycle sampleCycle;
    // private CreateGroupCycleDto createDto;
    // private UUID cycleId;
    // private UUID groupId;

    // @BeforeEach
    // void setUp() {
    //     cycleId = UUID.randomUUID();
    //     groupId = UUID.randomUUID();

    //     // Setup test data
    //     createDto = new CreateGroupCycleDto();
    //     createDto.setGroupId(groupId);
    //     createDto.setCyclePeriod("2025-09");
    //     createDto.setRecipientUserId(UUID.randomUUID());
    //     createDto.setContributionAmount(new BigDecimal("100.00"));
    //     createDto.setExpectedTotal(new BigDecimal("1000.00"));
    //     createDto.setCollectionStartDate(LocalDate.now().plusDays(1));
    //     createDto.setCollectionEndDate(LocalDate.now().plusDays(30));

    //     sampleCycle = new GroupCycle(
    //         createDto.getGroupId(),
    //         createDto.getCyclePeriod(),
    //         createDto.getRecipientUserId(),
    //         createDto.getContributionAmount(),
    //         createDto.getExpectedTotal(),
    //         createDto.getCollectionStartDate(),
    //         createDto.getCollectionEndDate()
    //     );
    // }

    // @Test
    // void createCycle_ValidRequest_ReturnsCreated() throws Exception {
    //     when(groupCycleService.createGroupCycle(any(CreateGroupCycleDto.class)))
    //         .thenReturn(sampleCycle);

    //     mockMvc.perform(post("/api/cycles")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(createDto)))
    //             .andExpect(status().isCreated())
    //             .andExpect(jsonPath("$.groupId").value(groupId.toString()));
    // }

    // @Test
    // void getCycle_ExistingCycle_ReturnsOk() throws Exception {
    //     when(groupCycleService.getCycleById(cycleId))
    //         .thenReturn(sampleCycle);

    //     mockMvc.perform(get("/api/cycles/{cycleId}", cycleId))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.groupId").value(groupId.toString()));
    // }

    // @Test
    // void updateCycleStatus_ValidRequest_ReturnsOk() throws Exception {
    //     UpdateCycleStatusDto statusDto = new UpdateCycleStatusDto();
    //     statusDto.setStatus("COMPLETED");

    //     when(groupCycleService.updateCycleStatus(any(UUID.class), any(String.class)))
    //         .thenReturn(sampleCycle);

    //     mockMvc.perform(put("/api/cycles/{cycleId}/status", cycleId)
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(statusDto)))
    //             .andExpect(status().isOk());
    // }

    // @Test
    // void getCyclesByGroup_ExistingGroup_ReturnsOk() throws Exception {
    //     when(groupCycleService.getCyclesByGroup(groupId))
    //         .thenReturn(Arrays.asList(sampleCycle));

    //     mockMvc.perform(get("/api/cycles/group/{groupId}", groupId))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$[0].groupId").value(groupId.toString()));
    // }
}