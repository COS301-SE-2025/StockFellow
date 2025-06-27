package com.stockfellow.transactionservice.controller;

import com.stockfellow.transactionservice.dto.CreateMandateRequest;
import com.stockfellow.transactionservice.dto.MandateResponse;
import com.stockfellow.transactionservice.model.Mandate;
import com.stockfellow.transactionservice.service.MandateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MandateController.class)
class MandateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MandateService mandateService;

    private Mandate testMandate;
    private MandateResponse testMandateResponse;
    private CreateMandateRequest createMandateRequest;
    private UUID testMandateId;
    private UUID testGroupId;
    private UUID testPayerUserId;
    private UUID testPaymentMethodId;

    @BeforeEach
    void setUp() {
        // Initialize test UUIDs
        testMandateId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
        testGroupId = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");
        testPayerUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        testPaymentMethodId = UUID.fromString("456e7890-a12b-34c5-d678-901234567def");

        // Create test mandate entity
        testMandate = new Mandate();
        testMandate.setMandateId(testMandateId);
        testMandate.setPayerUserId(testPayerUserId);
        testMandate.setGroupId(testGroupId);
        testMandate.setPaymentMethodId(testPaymentMethodId);
        testMandate.setStatus("ACTIVE");
        testMandate.setSignedDate(LocalDateTime.now());
        testMandate.setDocumentReference("DOC-REF-2025-001");
        testMandate.setIpAddress("192.168.1.100");
        testMandate.setCreatedAt(LocalDateTime.now());
        testMandate.setUpdatedAt(LocalDateTime.now());

        // Create test mandate response DTO
        testMandateResponse = MandateResponse.from(testMandate);

        // Create test request DTO
        createMandateRequest = new CreateMandateRequest();
        createMandateRequest.setPayerUserId(testPayerUserId);
        createMandateRequest.setGroupId(testGroupId);
        createMandateRequest.setPaymentMethodId(testPaymentMethodId);
        createMandateRequest.setDocumentReference("DOC-REF-2025-001");
        createMandateRequest.setIpAddress("192.168.1.100");
    }

    @Test
    void createMandate_ShouldReturnCreatedMandate_WhenValidRequest() throws Exception {
        when(mandateService.createMandate(any(CreateMandateRequest.class))).thenReturn(testMandate);

        mockMvc.perform(post("/api/mandates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createMandateRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mandateId").value(testMandateId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.ipAddress").value("192.168.1.100"));
    }

    @Test
    void createMandate_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        when(mandateService.createMandate(any(CreateMandateRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid request"));

        mockMvc.perform(post("/api/mandates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createMandateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMandate_ShouldReturnConflict_WhenBusinessLogicError() throws Exception {
        when(mandateService.createMandate(any(CreateMandateRequest.class)))
                .thenThrow(new IllegalStateException("Mandate already exists"));

        mockMvc.perform(post("/api/mandates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createMandateRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void getAllMandates_ShouldReturnListOfMandateResponses() throws Exception {
        List<MandateResponse> mandates = Arrays.asList(testMandateResponse);
        when(mandateService.getAllMandates()).thenReturn(mandates);

        mockMvc.perform(get("/api/mandates"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].mandateId").value(testMandateId.toString()))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].ipAddress").value("192.168.1.100"));
    }

    @Test
    void getAllMandates_ShouldReturnEmptyList_WhenNoMandates() throws Exception {
        when(mandateService.getAllMandates()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/mandates"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getMandateById_ShouldReturnMandate_WhenExists() throws Exception {
        when(mandateService.getMandateById(testMandateId)).thenReturn(testMandate);

        mockMvc.perform(get("/api/mandates/{mandateId}", testMandateId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mandateId").value(testMandateId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.documentReference").value("DOC-REF-2025-001"));
    }

    @Test
    void getMandateById_ShouldReturn404_WhenNotExists() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(mandateService.getMandateById(nonExistentId))
                .thenThrow(new IllegalArgumentException("Mandate not found"));

        mockMvc.perform(get("/api/mandates/{mandateId}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deactivateMandate_ShouldReturnOk_WhenExists() throws Exception {
        doNothing().when(mandateService).deactivateMandate(testMandateId);

        mockMvc.perform(put("/api/mandates/{mandateId}/deactivate", testMandateId))
                .andExpect(status().isOk());
    }

    @Test
    void deactivateMandate_ShouldReturn404_WhenNotExists() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        doThrow(new IllegalArgumentException("Mandate not found"))
                .when(mandateService).deactivateMandate(nonExistentId);

        mockMvc.perform(put("/api/mandates/{mandateId}/deactivate", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMandatesByGroup_ShouldReturnMandates_WhenGroupExists() throws Exception {
        List<MandateResponse> mandates = Arrays.asList(testMandateResponse);
        when(mandateService.getMandatesByGroup(testGroupId)).thenReturn(mandates);

        mockMvc.perform(get("/api/mandates/group/{groupId}", testGroupId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].groupId").value(testGroupId.toString()));
    }

    @Test
    void getMandatesByGroup_ShouldReturnEmptyList_WhenGroupHasNoMandates() throws Exception {
        UUID emptyGroupId = UUID.randomUUID();
        when(mandateService.getMandatesByGroup(emptyGroupId)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/mandates/group/{groupId}", emptyGroupId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getMandatesByStatus_ShouldReturnMandates_WhenStatusExists() throws Exception {
        List<MandateResponse> activeMandates = Arrays.asList(testMandateResponse);
        when(mandateService.getMandatesByStatus("ACTIVE")).thenReturn(activeMandates);

        mockMvc.perform(get("/api/mandates/status/{status}", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void getMandatesByStatus_ShouldReturnEmptyList_WhenStatusHasNoMandates() throws Exception {
        when(mandateService.getMandatesByStatus("INACTIVE")).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/mandates/status/{status}", "INACTIVE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getActiveMandatesForGroup_ShouldReturnActiveMandates() throws Exception {
        List<MandateResponse> activeMandates = Arrays.asList(testMandateResponse);
        when(mandateService.getActiveMandatesByGroup(testGroupId)).thenReturn(activeMandates);

        mockMvc.perform(get("/api/mandates/group/{groupId}/active", testGroupId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].groupId").value(testGroupId.toString()));
    }

    @Test
    void getActiveMandatesForGroup_ShouldReturnEmptyList_WhenNoActiveMandates() throws Exception {
        UUID groupWithNoActiveMandates = UUID.randomUUID();
        when(mandateService.getActiveMandatesByGroup(groupWithNoActiveMandates))
                .thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/mandates/group/{groupId}/active", groupWithNoActiveMandates))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getMandateById_ShouldReturn400_WhenInvalidUUID() throws Exception {
        mockMvc.perform(get("/api/mandates/{mandateId}", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMandatesByGroup_ShouldReturn400_WhenInvalidGroupUUID() throws Exception {
        mockMvc.perform(get("/api/mandates/group/{groupId}", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }
}