package com.stockfellow.transactionservice.service;

import com.stockfellow.transactionservice.model.ActivityLog;
import com.stockfellow.transactionservice.repository.ActivityLogRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.UUID;
public class ActivityLogService {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    public Page<ActivityLog> getLogsByUser(UUID userID, Pageable pageable){
        Page<ActivityLog> logs = activityLogRepository.findByUserId(userID, pageable);
        return logs;
    }

    public Page<ActivityLog> getLogsByCycle(UUID cycleId, Pageable pageable){
        Page<ActivityLog> logs = activityLogRepository.findByCycleId(cycleId, pageable);
        return logs;
    }

    public Page<ActivityLog> getLogsByEntity(ActivityLog.EntityType entityType, Pageable pageable){
        Page<ActivityLog> logs = activityLogRepository.findByEntityType(entityType, pageable);
        return logs;
    }


    public Page<ActivityLog> getLogsByEntityId(ActivityLog.EntityType entityType, UUID entityId, Pageable pageable){
        Page<ActivityLog> logs = activityLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
        return logs;
    }
}
