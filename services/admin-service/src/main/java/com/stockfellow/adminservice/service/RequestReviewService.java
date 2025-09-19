package com.stockfellow.adminservice.service;

import com.stockfellow.adminservice.model.AdminRequest;
import com.stockfellow.adminservice.repository.AdminRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class RequestReviewService {

    @Autowired
    private AdminRequestRepository requestRepository;

    @Autowired
    private RestTemplate restTemplate;

    public AdminRequest createRequest(String userId, String requestType, String reason, 
                                    String groupId, String cardId) {
        AdminRequest request = new AdminRequest();
        request.setUserId(userId);
        request.setRequestType(requestType);
        request.setReason(reason);
        request.setGroupId(groupId);
        request.setCardId(cardId);
        request.setStatus("PENDING");

        return requestRepository.save(request);
    }

    public Page<AdminRequest> getPendingRequests(String requestType, Pageable pageable) {
        return requestRepository.findPendingRequestsWithFilters(requestType, pageable);
    }

    public AdminRequest approveRequest(String requestId, String adminUserId, String adminNotes) {
        Optional<AdminRequest> requestOpt = requestRepository.findById(requestId);
        if (!requestOpt.isPresent()) {
            throw new IllegalArgumentException("Request not found");
        }

        AdminRequest request = requestOpt.get();
        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("Request has already been processed");
        }

        // Execute the requested action
        boolean success = executeRequestAction(request);
        
        if (success) {
            request.setStatus("COMPLETED");
        } else {
            request.setStatus("APPROVED");
        }
        
        request.setAdminUserId(adminUserId);
        request.setAdminNotes(adminNotes);
        request.setProcessedAt(LocalDateTime.now());

        return requestRepository.save(request);
    }

    public AdminRequest rejectRequest(String requestId, String adminUserId, String adminNotes) {
        Optional<AdminRequest> requestOpt = requestRepository.findById(requestId);
        if (!requestOpt.isPresent()) {
            throw new IllegalArgumentException("Request not found");
        }

        AdminRequest request = requestOpt.get();
        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("Request has already been processed");
        }

        request.setStatus("REJECTED");
        request.setAdminUserId(adminUserId);
        request.setAdminNotes(adminNotes);
        request.setProcessedAt(LocalDateTime.now());

        return requestRepository.save(request);
    }

    public Map<String, Object> getRequestDetails(String requestId) {
        Optional<AdminRequest> requestOpt = requestRepository.findById(requestId);
        if (!requestOpt.isPresent()) {
            throw new IllegalArgumentException("Request not found");
        }

        AdminRequest request = requestOpt.get();
        Map<String, Object> details = new HashMap<>();
        details.put("request", request);

        // Get user details
        try {
            ResponseEntity<Map> userResponse = restTemplate.getForEntity(
                "http://user-service:4020/api/users/" + request.getUserId(),
                Map.class
            );
            details.put("user", userResponse.getBody());
        } catch (Exception e) {
            details.put("userError", "Failed to fetch user details");
        }

        // Get group details if applicable
        if (request.getGroupId() != null) {
            try {
                ResponseEntity<Map> groupResponse = restTemplate.getForEntity(
                    "http://group-service:4040/api/groups/" + request.getGroupId() + "/view",
                    Map.class
                );
                details.put("group", groupResponse.getBody());
            } catch (Exception e) {
                details.put("groupError", "Failed to fetch group details");
            }
        }

        return details;
    }

    public List<AdminRequest> getStaleRequests() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7); // 7 days old
        return requestRepository.findStaleRequests(cutoffDate);
    }

    private boolean executeRequestAction(AdminRequest request) {
        try {
            switch (request.getRequestType()) {
                case "LEAVE_GROUP":
                    return executeLeaveGroupAction(request);
                case "DELETE_CARD":
                    return executeDeleteCardAction(request);
                case "CLOSE_ACCOUNT":
                    return executeCloseAccountAction(request);
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private boolean executeLeaveGroupAction(AdminRequest request) {
        try {
            // Call group service to remove user from group
            Map<String, Object> leaveRequest = new HashMap<>();
            leaveRequest.put("userId", request.getUserId());
            leaveRequest.put("reason", "ADMIN_APPROVED_REQUEST");

            ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://group-service:4040/api/groups/" + request.getGroupId() + "/admin/remove-member",
                leaveRequest,
                Map.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean executeDeleteCardAction(AdminRequest request) {
        try {
            // Call user service to delete payment method
            ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://user-service:4020/api/users/admin/delete-card/" + request.getCardId(),
                Map.of("reason", "ADMIN_APPROVED_REQUEST"),
                Map.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean executeCloseAccountAction(AdminRequest request) {
        try {
            // Call user service to deactivate account
            ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://user-service:4020/api/users/admin/deactivate/" + request.getUserId(),
                Map.of("reason", "ADMIN_APPROVED_REQUEST"),
                Map.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

}