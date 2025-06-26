package com.stockfellow.transactionservice.controller;

import com.stockfellow.transactionservice.model.Mandate;
import com.stockfellow.transactionservice.repository.MandateRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MandateController.class)
class MandateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MandateRepository mandateRepository;

    private Mandate testMandate;
    private UUID testMandateId;
    private UUID testGroupId;
    private UUID testPayerUserId;
    private UUID testPaymentMethodId;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testMandateId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
        testGroupId = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");
        testPayerUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        testPaymentMethodId = UUID.fromString("456e7890-a12b-34c5-d678-901234567def");

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
    }

    @Test
    void getAllMandates_ShouldReturnListOfMandates() throws Exception {

        List<Mandate> mandates = Arrays.asList(testMandate);
        when(mandateRepository.findAll()).thenReturn(mandates);

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

        when(mandateRepository.findAll()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/mandates"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getMandate_ShouldReturnMandate_WhenExists() throws Exception {

        when(mandateRepository.findById(testMandateId)).thenReturn(Optional.of(testMandate));

        mockMvc.perform(get("/api/mandates/{mandateId}", testMandateId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mandateId").value(testMandateId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.documentReference").value("DOC-REF-2025-001"));
    }

    @Test
    void getMandate_ShouldReturn404_WhenNotExists() throws Exception {

        UUID nonExistentId = UUID.randomUUID();
        when(mandateRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/mandates/{mandateId}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMandatesByGroup_ShouldReturnMandates_WhenGroupExists() throws Exception {

        List<Mandate> mandates = Arrays.asList(testMandate);
        when(mandateRepository.findByGroupId(testGroupId)).thenReturn(mandates);

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
        when(mandateRepository.findByGroupId(emptyGroupId)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/mandates/group/{groupId}", emptyGroupId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getMandatesByStatus_ShouldReturnMandates_WhenStatusExists() throws Exception {

        List<Mandate> activeMandates = Arrays.asList(testMandate);
        when(mandateRepository.findByStatus("ACTIVE")).thenReturn(activeMandates);

        mockMvc.perform(get("/api/mandates/status/{status}", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void getMandatesByStatus_ShouldReturnEmptyList_WhenStatusHasNoMandates() throws Exception {

        when(mandateRepository.findByStatus("INACTIVE")).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/mandates/status/{status}", "INACTIVE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getActiveMandatesForGroup_ShouldReturnActiveMandates() throws Exception {

        List<Mandate> activeMandates = Arrays.asList(testMandate);
        when(mandateRepository.findByGroupIdAndStatus(testGroupId, "ACTIVE")).thenReturn(activeMandates);

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
        when(mandateRepository.findByGroupIdAndStatus(groupWithNoActiveMandates, "ACTIVE"))
                .thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/mandates/group/{groupId}/active", groupWithNoActiveMandates))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getMandate_ShouldReturn400_WhenInvalidUUID() throws Exception {

        mockMvc.perform(get("/api/mandates/{mandateId}", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMandatesByGroup_ShouldReturn400_WhenInvalidGroupUUID() throws Exception {

        mockMvc.perform(get("/api/mandates/group/{groupId}", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }
}