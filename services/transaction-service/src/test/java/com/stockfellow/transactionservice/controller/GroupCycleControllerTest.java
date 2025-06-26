package com.stockfellow.transactionservice.controller;

import com.stockfellow.transactionservice.model.GroupCycle;
import com.stockfellow.transactionservice.repository.GroupCycleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GroupCycleController.class)
class GroupCycleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GroupCycleRepository groupCycleRepository;

    private GroupCycle testGroupCycle;
    private UUID testCycleId;
    private UUID testGroupId;
    private UUID testRecipientUserId;
    private UUID testRecipientPaymentMethodId;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testCycleId = UUID.fromString("11111111-2222-3333-4444-555555555555");
        testGroupId = UUID.fromString("66666666-7777-8888-9999-000000000000");
        testRecipientUserId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        testRecipientPaymentMethodId = UUID.fromString("ffffffff-0000-1111-2222-333333333333");

        testGroupCycle = new GroupCycle();
        testGroupCycle.setCycleId(testCycleId);
        testGroupCycle.setGroupId(testGroupId);
        testGroupCycle.setCycleMonth("2025-07");
        testGroupCycle.setRecipientUserId(testRecipientUserId);
        testGroupCycle.setRecipientPaymentMethodId(testRecipientPaymentMethodId);
        testGroupCycle.setContributionAmount(new BigDecimal("1000.00"));
        testGroupCycle.setCollectionDate(LocalDate.of(2025, 7, 15));
        testGroupCycle.setStatus("PENDING");
        testGroupCycle.setTotalExpectedAmount(new BigDecimal("5000.00"));
        testGroupCycle.setCreatedAt(LocalDateTime.of(2025, 6, 1, 10, 0, 0));
        testGroupCycle.setUpdatedAt(LocalDateTime.of(2025, 6, 1, 10, 0, 0));
    }

    @Test
    void getAllCycles_ShouldReturnListOfCycles() throws Exception {

        List<GroupCycle> cycles = Arrays.asList(testGroupCycle);
        when(groupCycleRepository.findAll()).thenReturn(cycles);

        mockMvc.perform(get("/api/cycles"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].cycleId").value(testCycleId.toString()))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].cycleMonth").value("2025-07"))
                .andExpect(jsonPath("$[0].contributionAmount").value(1000.00));
    }

    @Test
    void getAllCycles_ShouldReturnEmptyList_WhenNoCycles() throws Exception {

        when(groupCycleRepository.findAll()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/cycles"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getCycle_ShouldReturnCycle_WhenExists() throws Exception {

        when(groupCycleRepository.findById(testCycleId)).thenReturn(Optional.of(testGroupCycle));

        mockMvc.perform(get("/api/cycles/{cycleId}", testCycleId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cycleId").value(testCycleId.toString()))
                .andExpect(jsonPath("$.groupId").value(testGroupId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalExpectedAmount").value(5000.00));
    }

    @Test
    void getCycle_ShouldReturn404_WhenNotExists() throws Exception {

        UUID nonExistentId = UUID.randomUUID();
        when(groupCycleRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/cycles/{cycleId}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCyclesByGroup_ShouldReturnCycles_WhenGroupExists() throws Exception {

        List<GroupCycle> cycles = Arrays.asList(testGroupCycle);
        when(groupCycleRepository.findByGroupIdOrderByCollectionDateDesc(testGroupId)).thenReturn(cycles);

        mockMvc.perform(get("/api/cycles/group/{groupId}", testGroupId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].groupId").value(testGroupId.toString()));
    }

    @Test
    void getCyclesByGroup_ShouldReturnEmptyList_WhenGroupHasNoCycles() throws Exception {

        UUID emptyGroupId = UUID.randomUUID();
        when(groupCycleRepository.findByGroupIdOrderByCollectionDateDesc(emptyGroupId)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/cycles/group/{groupId}", emptyGroupId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getCyclesByStatus_ShouldReturnCycles_WhenStatusExists() throws Exception {

        List<GroupCycle> pendingCycles = Arrays.asList(testGroupCycle);
        when(groupCycleRepository.findByStatus("PENDING")).thenReturn(pendingCycles);

        mockMvc.perform(get("/api/cycles/status/{status}", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getCyclesByStatus_ShouldReturnEmptyList_WhenStatusHasNoCycles() throws Exception {

        when(groupCycleRepository.findByStatus("COMPLETED")).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/cycles/status/{status}", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getNextCycleForGroup_ShouldReturnCycle_WithDefaultStatus() throws Exception {

        when(groupCycleRepository.findFirstByGroupIdAndStatusOrderByCollectionDateAsc(testGroupId, "PENDING"))
                .thenReturn(Optional.of(testGroupCycle));

        mockMvc.perform(get("/api/cycles/group/{groupId}/next", testGroupId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cycleId").value(testCycleId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getNextCycleForGroup_ShouldReturnCycle_WithCustomStatus() throws Exception {

        GroupCycle activeCycle = new GroupCycle();
        activeCycle.setCycleId(testCycleId);
        activeCycle.setStatus("ACTIVE");

        when(groupCycleRepository.findFirstByGroupIdAndStatusOrderByCollectionDateAsc(testGroupId, "ACTIVE"))
                .thenReturn(Optional.of(activeCycle));

        mockMvc.perform(get("/api/cycles/group/{groupId}/next", testGroupId)
                .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getNextCycleForGroup_ShouldReturn404_WhenNoNextCycle() throws Exception {

        when(groupCycleRepository.findFirstByGroupIdAndStatusOrderByCollectionDateAsc(testGroupId, "PENDING"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/cycles/group/{groupId}/next", testGroupId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getNextUpcomingCycle_ShouldReturnCycle_WithDefaultStatus() throws Exception {

        when(groupCycleRepository.findFirstByStatusAndCollectionDateGreaterThanEqualOrderByCollectionDateAsc(
                eq("PENDING"), any(LocalDate.class)))
                .thenReturn(Optional.of(testGroupCycle));

        mockMvc.perform(get("/api/cycles/upcoming"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cycleId").value(testCycleId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getNextUpcomingCycle_ShouldReturnCycle_WithCustomStatus() throws Exception {

        when(groupCycleRepository.findFirstByStatusAndCollectionDateGreaterThanEqualOrderByCollectionDateAsc(
                eq("ACTIVE"), any(LocalDate.class)))
                .thenReturn(Optional.of(testGroupCycle));

        mockMvc.perform(get("/api/cycles/upcoming")
                .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cycleId").value(testCycleId.toString()));
    }

    @Test
    void getNextUpcomingCycle_ShouldReturn404_WhenNoUpcomingCycle() throws Exception {

        when(groupCycleRepository.findFirstByStatusAndCollectionDateGreaterThanEqualOrderByCollectionDateAsc(
                eq("PENDING"), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/cycles/upcoming"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCycleByGroupAndMonth_ShouldReturnCycle_WhenExists() throws Exception {

        when(groupCycleRepository.findByGroupIdAndCycleMonth(testGroupId, "2025-07"))
                .thenReturn(Optional.of(testGroupCycle));

        mockMvc.perform(get("/api/cycles/group/{groupId}/month/{cycleMonth}", testGroupId, "2025-07"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cycleId").value(testCycleId.toString()))
                .andExpect(jsonPath("$.cycleMonth").value("2025-07"));
    }

    @Test
    void getCycleByGroupAndMonth_ShouldReturn404_WhenNotExists() throws Exception {

        when(groupCycleRepository.findByGroupIdAndCycleMonth(testGroupId, "2025-08"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/cycles/group/{groupId}/month/{cycleMonth}", testGroupId, "2025-08"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getEarliestCyclesForGroup_ShouldReturnCycles() throws Exception {

        List<GroupCycle> earliestCycles = Arrays.asList(testGroupCycle);
        when(groupCycleRepository.findFirstByGroupIdOrderByCollectionDateAsc(testGroupId))
                .thenReturn(earliestCycles);

        mockMvc.perform(get("/api/cycles/group/{groupId}/earliest", testGroupId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].groupId").value(testGroupId.toString()));
    }

    @Test
    void getEarliestCyclesForGroup_ShouldReturnEmptyList_WhenNoCycles() throws Exception {

        when(groupCycleRepository.findFirstByGroupIdOrderByCollectionDateAsc(testGroupId))
                .thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/cycles/group/{groupId}/earliest", testGroupId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getCycle_ShouldReturn400_WhenInvalidUUID() throws Exception {

        mockMvc.perform(get("/api/cycles/{cycleId}", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCyclesByGroup_ShouldReturn400_WhenInvalidGroupUUID() throws Exception {

        mockMvc.perform(get("/api/cycles/group/{groupId}", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getNextCycleForGroup_ShouldReturn400_WhenInvalidGroupUUID() throws Exception {

        mockMvc.perform(get("/api/cycles/group/{groupId}/next", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCycleByGroupAndMonth_ShouldReturn400_WhenInvalidGroupUUID() throws Exception {

        mockMvc.perform(get("/api/cycles/group/{groupId}/month/{cycleMonth}", "invalid-uuid", "2025-07"))
                .andExpect(status().isBadRequest());
    }
}
