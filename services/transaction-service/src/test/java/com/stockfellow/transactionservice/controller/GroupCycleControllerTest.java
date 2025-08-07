// package com.stockfellow.transactionservice.controller;

// import com.stockfellow.transactionservice.dto.CreateCycleRequest;
// import com.stockfellow.transactionservice.dto.CycleResponse;
// import com.stockfellow.transactionservice.model.GroupCycle;
// import com.stockfellow.transactionservice.service.CycleService;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.BeforeEach;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;

// import java.math.BigDecimal;
// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.util.Arrays;
// import java.util.List;
// import java.util.UUID;

// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.when;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest(GroupCycleController.class)
// class GroupCycleControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @Autowired
//     private ObjectMapper objectMapper;

//     @MockBean
//     private CycleService cycleService;

//     private GroupCycle testGroupCycle;
//     private CycleResponse testCycleResponse;
//     private CreateCycleRequest createCycleRequest;
//     private UUID testCycleId;
//     private UUID testGroupId;
//     private UUID testRecipientUserId;
//     private UUID testRecipientPaymentMethodId;

//     @BeforeEach
//     void setUp() {
//         // Initialize test UUIDs
//         testCycleId = UUID.fromString("11111111-2222-3333-4444-555555555555");
//         testGroupId = UUID.fromString("66666666-7777-8888-9999-000000000000");
//         testRecipientUserId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
//         testRecipientPaymentMethodId = UUID.fromString("ffffffff-0000-1111-2222-333333333333");

//         // Create test entity
//         testGroupCycle = new GroupCycle();
//         testGroupCycle.setCycleId(testCycleId);
//         testGroupCycle.setGroupId(testGroupId);
//         testGroupCycle.setCycleMonth("2025-07");
//         testGroupCycle.setRecipientUserId(testRecipientUserId);
//         testGroupCycle.setRecipientPaymentMethodId(testRecipientPaymentMethodId);
//         testGroupCycle.setContributionAmount(new BigDecimal("1000.00"));
//         testGroupCycle.setCollectionDate(LocalDate.of(2025, 7, 15));
//         testGroupCycle.setStatus("PENDING");
//         testGroupCycle.setTotalExpectedAmount(new BigDecimal("5000.00"));
//         testGroupCycle.setSuccessfulPayments(0);
//         testGroupCycle.setFailedPayments(0);
//         testGroupCycle.setCreatedAt(LocalDateTime.of(2025, 6, 1, 10, 0, 0));
//         testGroupCycle.setUpdatedAt(LocalDateTime.of(2025, 6, 1, 10, 0, 0));

//         // Create test response DTO
//         testCycleResponse = CycleResponse.from(testGroupCycle);

//         // Create test request DTO
//         createCycleRequest = new CreateCycleRequest();
//         createCycleRequest.setGroupId(testGroupId);
//         createCycleRequest.setCycleMonth("2025-07");
//         createCycleRequest.setRecipientUserId(testRecipientUserId);
//         createCycleRequest.setRecipientPaymentMethodId(testRecipientPaymentMethodId);
//         createCycleRequest.setContributionAmount(new BigDecimal("1000.00"));
//         createCycleRequest.setCollectionDate(LocalDate.of(2025, 7, 15));
//         createCycleRequest.setTotalExpectedAmount(new BigDecimal("5000.00"));
//     }

//     @Test
//     void createGroupCycle_ShouldReturnCreatedCycle_WhenValidRequest() throws Exception {
//         when(cycleService.createGroupCycle(any(CreateCycleRequest.class))).thenReturn(testGroupCycle);

//         mockMvc.perform(post("/api/cycles")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(createCycleRequest)))
//                 .andExpect(status().isCreated())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$.cycleId").value(testCycleId.toString()))
//                 .andExpect(jsonPath("$.status").value("PENDING"))
//                 .andExpect(jsonPath("$.cycleMonth").value("2025-07"))
//                 .andExpect(jsonPath("$.contributionAmount").value(1000.00));
//     }

//     @Test
//     void createGroupCycle_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
//         when(cycleService.createGroupCycle(any(CreateCycleRequest.class)))
//                 .thenThrow(new IllegalArgumentException("Invalid request"));

//         mockMvc.perform(post("/api/cycles")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(createCycleRequest)))
//                 .andExpect(status().isBadRequest());
//     }

//     @Test
//     void createGroupCycle_ShouldReturnConflict_WhenBusinessLogicError() throws Exception {
//         when(cycleService.createGroupCycle(any(CreateCycleRequest.class)))
//                 .thenThrow(new IllegalStateException("Cycle already exists"));

//         mockMvc.perform(post("/api/cycles")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(createCycleRequest)))
//                 .andExpect(status().isConflict());
//     }

//     @Test
//     void getAllCycles_ShouldReturnListOfCycleResponses() throws Exception {
//         List<CycleResponse> cycles = Arrays.asList(testCycleResponse);
//         when(cycleService.getAllCycles()).thenReturn(cycles);

//         mockMvc.perform(get("/api/cycles"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$").isArray())
//                 .andExpect(jsonPath("$.length()").value(1))
//                 .andExpect(jsonPath("$[0].cycleId").value(testCycleId.toString()))
//                 .andExpect(jsonPath("$[0].status").value("PENDING"))
//                 .andExpect(jsonPath("$[0].cycleMonth").value("2025-07"))
//                 .andExpect(jsonPath("$[0].contributionAmount").value(1000.00));
//     }

//     @Test
//     void getAllCycles_ShouldReturnEmptyList_WhenNoCycles() throws Exception {
//         when(cycleService.getAllCycles()).thenReturn(Arrays.asList());

//         mockMvc.perform(get("/api/cycles"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$").isArray())
//                 .andExpect(jsonPath("$.length()").value(0));
//     }

//     @Test
//     void getCycle_ShouldReturnCycle_WhenExists() throws Exception {
//         when(cycleService.getCycleById(testCycleId)).thenReturn(testGroupCycle);

//         mockMvc.perform(get("/api/cycles/{cycleId}", testCycleId))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$.cycleId").value(testCycleId.toString()))
//                 .andExpect(jsonPath("$.groupId").value(testGroupId.toString()))
//                 .andExpect(jsonPath("$.status").value("PENDING"))
//                 .andExpect(jsonPath("$.totalExpectedAmount").value(5000.00));
//     }

//     @Test
//     void getCycle_ShouldReturn404_WhenNotExists() throws Exception {
//         UUID nonExistentId = UUID.randomUUID();
//         when(cycleService.getCycleById(nonExistentId))
//                 .thenThrow(new IllegalArgumentException("Cycle not found"));

//         mockMvc.perform(get("/api/cycles/{cycleId}", nonExistentId))
//                 .andExpect(status().isNotFound());
//     }

//     @Test
//     void getCyclesByGroup_ShouldReturnCycles_WhenGroupExists() throws Exception {
//         List<CycleResponse> cycles = Arrays.asList(testCycleResponse);
//         when(cycleService.getCyclesByGroup(testGroupId)).thenReturn(cycles);

//         mockMvc.perform(get("/api/cycles/group/{groupId}", testGroupId))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$").isArray())
//                 .andExpect(jsonPath("$.length()").value(1))
//                 .andExpect(jsonPath("$[0].groupId").value(testGroupId.toString()));
//     }

//     @Test
//     void getCyclesByGroup_ShouldReturnEmptyList_WhenGroupHasNoCycles() throws Exception {
//         UUID emptyGroupId = UUID.randomUUID();
//         when(cycleService.getCyclesByGroup(emptyGroupId)).thenReturn(Arrays.asList());

//         mockMvc.perform(get("/api/cycles/group/{groupId}", emptyGroupId))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$").isArray())
//                 .andExpect(jsonPath("$.length()").value(0));
//     }

//     @Test
//     void getCyclesByStatus_ShouldReturnCycles_WhenStatusExists() throws Exception {
//         List<CycleResponse> pendingCycles = Arrays.asList(testCycleResponse);
//         when(cycleService.getCyclesByStatus("PENDING")).thenReturn(pendingCycles);

//         mockMvc.perform(get("/api/cycles/status/{status}", "PENDING"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$").isArray())
//                 .andExpect(jsonPath("$.length()").value(1))
//                 .andExpect(jsonPath("$[0].status").value("PENDING"));
//     }

//     @Test
//     void getCyclesByStatus_ShouldReturnEmptyList_WhenStatusHasNoCycles() throws Exception {
//         when(cycleService.getCyclesByStatus("COMPLETED")).thenReturn(Arrays.asList());

//         mockMvc.perform(get("/api/cycles/status/{status}", "COMPLETED"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$").isArray())
//                 .andExpect(jsonPath("$.length()").value(0));
//     }

//     @Test
//     void getNextCycleForGroup_ShouldReturnCycle_WithDefaultStatus() throws Exception {
//         when(cycleService.getNextCycleForGroup(testGroupId, "PENDING")).thenReturn(testGroupCycle);

//         mockMvc.perform(get("/api/cycles/group/{groupId}/next", testGroupId))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$.cycleId").value(testCycleId.toString()))
//                 .andExpect(jsonPath("$.status").value("PENDING"));
//     }

//     @Test
//     void getNextCycleForGroup_ShouldReturnCycle_WithCustomStatus() throws Exception {
//         GroupCycle activeCycle = new GroupCycle();
//         activeCycle.setCycleId(testCycleId);
//         activeCycle.setStatus("ACTIVE");
//         activeCycle.setGroupId(testGroupId);
//         activeCycle.setCycleMonth("2025-07");
//         activeCycle.setRecipientUserId(testRecipientUserId);
//         activeCycle.setRecipientPaymentMethodId(testRecipientPaymentMethodId);
//         activeCycle.setContributionAmount(new BigDecimal("1000.00"));
//         activeCycle.setCollectionDate(LocalDate.of(2025, 7, 15));
//         activeCycle.setTotalExpectedAmount(new BigDecimal("5000.00"));
//         activeCycle.setSuccessfulPayments(0);
//         activeCycle.setFailedPayments(0);
//         activeCycle.setCreatedAt(LocalDateTime.now());
//         activeCycle.setUpdatedAt(LocalDateTime.now());

//         when(cycleService.getNextCycleForGroup(testGroupId, "ACTIVE")).thenReturn(activeCycle);

//         mockMvc.perform(get("/api/cycles/group/{groupId}/next", testGroupId)
//                 .param("status", "ACTIVE"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$.status").value("ACTIVE"));
//     }

//     @Test
//     void getNextCycleForGroup_ShouldReturn404_WhenNoNextCycle() throws Exception {
//         when(cycleService.getNextCycleForGroup(testGroupId, "PENDING"))
//                 .thenThrow(new IllegalArgumentException("No upcoming cycle found"));

//         mockMvc.perform(get("/api/cycles/group/{groupId}/next", testGroupId))
//                 .andExpect(status().isNotFound());
//     }

//     @Test
//     void getNextUpcomingCycle_ShouldReturnCycle_WithDefaultStatus() throws Exception {
//         when(cycleService.getNextUpcomingCycle("PENDING")).thenReturn(testGroupCycle);

//         mockMvc.perform(get("/api/cycles/upcoming"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$.cycleId").value(testCycleId.toString()))
//                 .andExpect(jsonPath("$.status").value("PENDING"));
//     }

//     @Test
//     void getNextUpcomingCycle_ShouldReturnCycle_WithCustomStatus() throws Exception {
//         when(cycleService.getNextUpcomingCycle("ACTIVE")).thenReturn(testGroupCycle);

//         mockMvc.perform(get("/api/cycles/upcoming")
//                 .param("status", "ACTIVE"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$.cycleId").value(testCycleId.toString()));
//     }

//     @Test
//     void getNextUpcomingCycle_ShouldReturn404_WhenNoUpcomingCycle() throws Exception {
//         when(cycleService.getNextUpcomingCycle("PENDING"))
//                 .thenThrow(new IllegalArgumentException("No upcoming cycle found"));

//         mockMvc.perform(get("/api/cycles/upcoming"))
//                 .andExpect(status().isNotFound());
//     }

//     @Test
//     void getCycleByGroupAndMonth_ShouldReturnCycle_WhenExists() throws Exception {
//         when(cycleService.getCycleByGroupAndMonth(testGroupId, "2025-07")).thenReturn(testGroupCycle);

//         mockMvc.perform(get("/api/cycles/group/{groupId}/month/{cycleMonth}", testGroupId, "2025-07"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$.cycleId").value(testCycleId.toString()))
//                 .andExpect(jsonPath("$.cycleMonth").value("2025-07"));
//     }

//     @Test
//     void getCycleByGroupAndMonth_ShouldReturn404_WhenNotExists() throws Exception {
//         when(cycleService.getCycleByGroupAndMonth(testGroupId, "2025-08"))
//                 .thenThrow(new IllegalArgumentException("No cycle found"));

//         mockMvc.perform(get("/api/cycles/group/{groupId}/month/{cycleMonth}", testGroupId, "2025-08"))
//                 .andExpect(status().isNotFound());
//     }

//     @Test
//     void getEarliestCyclesForGroup_ShouldReturnCycles() throws Exception {
//         List<CycleResponse> earliestCycles = Arrays.asList(testCycleResponse);
//         when(cycleService.getEarliestCyclesForGroup(testGroupId)).thenReturn(earliestCycles);

//         mockMvc.perform(get("/api/cycles/group/{groupId}/earliest", testGroupId))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$").isArray())
//                 .andExpect(jsonPath("$.length()").value(1))
//                 .andExpect(jsonPath("$[0].groupId").value(testGroupId.toString()));
//     }

//     @Test
//     void getEarliestCyclesForGroup_ShouldReturnEmptyList_WhenNoCycles() throws Exception {
//         when(cycleService.getEarliestCyclesForGroup(testGroupId)).thenReturn(Arrays.asList());

//         mockMvc.perform(get("/api/cycles/group/{groupId}/earliest", testGroupId))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$").isArray())
//                 .andExpect(jsonPath("$.length()").value(0));
//     }

//     @Test
//     void getCycle_ShouldReturn400_WhenInvalidUUID() throws Exception {
//         mockMvc.perform(get("/api/cycles/{cycleId}", "invalid-uuid"))
//                 .andExpect(status().isBadRequest());
//     }

//     @Test
//     void getCyclesByGroup_ShouldReturn400_WhenInvalidGroupUUID() throws Exception {
//         mockMvc.perform(get("/api/cycles/group/{groupId}", "invalid-uuid"))
//                 .andExpect(status().isBadRequest());
//     }

//     @Test
//     void getNextCycleForGroup_ShouldReturn400_WhenInvalidGroupUUID() throws Exception {
//         mockMvc.perform(get("/api/cycles/group/{groupId}/next", "invalid-uuid"))
//                 .andExpect(status().isBadRequest());
//     }

//     @Test
//     void getCycleByGroupAndMonth_ShouldReturn400_WhenInvalidGroupUUID() throws Exception {
//         mockMvc.perform(get("/api/cycles/group/{groupId}/month/{cycleMonth}", "invalid-uuid", "2025-07"))
//                 .andExpect(status().isBadRequest());
//     }
// }