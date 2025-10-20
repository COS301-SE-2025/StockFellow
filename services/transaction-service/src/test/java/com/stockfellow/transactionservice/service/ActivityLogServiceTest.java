package com.stockfellow.transactionservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.stockfellow.transactionservice.model.ActivityLog;
import com.stockfellow.transactionservice.repository.ActivityLogRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class ActivityLogServiceTest {

    @Mock
    private ActivityLogRepository activityLogRepository;

    @InjectMocks
    private ActivityLogService activityLogService;

    private UUID userId;
    private UUID cycleId;
    private UUID entityId;
    private Pageable pageable;
    private ActivityLog sampleLog;
    private Page<ActivityLog> samplePage;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        cycleId = UUID.randomUUID();
        entityId = UUID.randomUUID();
        pageable = PageRequest.of(0, 10);
        
        sampleLog = new ActivityLog();
        // Set up sample log properties
        samplePage = new PageImpl<>(Arrays.asList(sampleLog));
    }

    @Test
    void getLogsByUser_ReturnsPageOfLogs() {
        when(activityLogRepository.findByUserId(userId, pageable))
            .thenReturn(samplePage);

        Page<ActivityLog> result = activityLogService.getLogsByUser(userId, pageable);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getContent().size());
        verify(activityLogRepository).findByUserId(userId, pageable);
    }

    @Test
    void getLogsByCycle_ReturnsPageOfLogs() {
        when(activityLogRepository.findByCycleId(cycleId, pageable))
            .thenReturn(samplePage);

        Page<ActivityLog> result = activityLogService.getLogsByCycle(cycleId, pageable);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getContent().size());
        verify(activityLogRepository).findByCycleId(cycleId, pageable);
    }

    @Test
    void getLogsByEntity_ReturnsPageOfLogs() {
        ActivityLog.EntityType entityType = ActivityLog.EntityType.group;
        when(activityLogRepository.findByEntityType(entityType, pageable))
            .thenReturn(samplePage);

        Page<ActivityLog> result = activityLogService.getLogsByEntity(entityType, pageable);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getContent().size());
        verify(activityLogRepository).findByEntityType(entityType, pageable);
    }

    @Test
    void getLogsByEntityId_ReturnsPageOfLogs() {
        ActivityLog.EntityType entityType = ActivityLog.EntityType.group;
        when(activityLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable))
            .thenReturn(samplePage);

        Page<ActivityLog> result = activityLogService.getLogsByEntityId(entityType, entityId, pageable);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getContent().size());
        verify(activityLogRepository).findByEntityTypeAndEntityId(entityType, entityId, pageable);
    }
}