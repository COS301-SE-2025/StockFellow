package com.stockfellow.adminservice.controller;

import com.stockfellow.adminservice.model.AdminRequest;
import com.stockfellow.adminservice.service.RequestReviewService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/requests")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class AdminRequestsController {

    private static final Logger logger = LoggerFactory.getLogger(AdminRequestsController.class);

    @Autowired
    private RequestReviewService requestReviewService;

    @GetMapping("/pending")
    public ResponseEntity<Page<AdminRequest>> getPendingRequests(
            @RequestParam(required = false) String requestType,
            Pageable pageable) {
        
        try {
            logger.info("Fetching pending requests with type filter: {}", requestType);
            Page<AdminRequest> pendingRequests = requestReviewService.getPendingRequests(requestType, pageable);
            logger.info("Retrieved {} pending requests", pendingRequests.getTotalElements());
            return ResponseEntity.ok(pendingRequests);
            
        } catch (Exception e) {
            logger.error("Error fetching pending requests: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Page.empty());
        }
    }

    @PostMapping("/{requestId}/approve")
    public ResponseEntity<?> approveRequest(
            @PathVariable String requestId,
            @RequestBody ApprovalRequest approval,
            HttpServletRequest httpRequest) {
        
        try {
            String adminUserId = httpRequest.getHeader("X-User-Id");
            if (adminUserId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Admin user ID not found",
                    "message", "Request must include admin user identification"
                ));
            }

            logger.info("Admin {} approving request {}", adminUserId, requestId);
            
            AdminRequest updatedRequest = requestReviewService.approveRequest(
                requestId, adminUserId, approval.getAdminNotes()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Request approved successfully",
                "request", updatedRequest
            ));
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid approval request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Invalid request",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Error approving request {}: {}", requestId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal server error",
                "message", "Failed to approve request"
            ));
        }
    }

    @PostMapping("/{requestId}/reject")
    public ResponseEntity<?> rejectRequest(
            @PathVariable String requestId,
            @RequestBody RejectionRequest rejection,
            HttpServletRequest httpRequest) {
        
        try {
            String adminUserId = httpRequest.getHeader("X-User-Id");
            if (adminUserId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Admin user ID not found",
                    "message", "Request must include admin user identification"
                ));
            }

            logger.info("Admin {} rejecting request {}", adminUserId, requestId);
            
            AdminRequest updatedRequest = requestReviewService.rejectRequest(
                requestId, adminUserId, rejection.getAdminNotes()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Request rejected successfully",
                "request", updatedRequest
            ));
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid rejection request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Invalid request",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Error rejecting request {}: {}", requestId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal server error",
                "message", "Failed to reject request"
            ));
        }
    }

    @GetMapping("/{requestId}/details")
    public ResponseEntity<?> getRequestDetails(@PathVariable String requestId) {
        try {
            logger.info("Fetching details for request: {}", requestId);
            Map<String, Object> requestDetails = requestReviewService.getRequestDetails(requestId);
            return ResponseEntity.ok(requestDetails);
            
        } catch (IllegalArgumentException e) {
            logger.error("Request not found: {}", e.getMessage());
            return ResponseEntity.status(404).body(Map.of(
                "error", "Not found",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Error fetching request details for {}: {}", requestId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal server error",
                "message", "Failed to fetch request details"
            ));
        }
    }

    // DTOs
    public static class ApprovalRequest {
        private String adminNotes;

        public String getAdminNotes() { return adminNotes; }
        public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    }

    public static class RejectionRequest {
        private String adminNotes;

        public String getAdminNotes() { return adminNotes; }
        public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    }
}
