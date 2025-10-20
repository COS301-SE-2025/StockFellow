package com.stockfellow.transactionservice.controller;

import com.stockfellow.transactionservice.dto.UserResponseDto;
import com.stockfellow.transactionservice.dto.SyncUserDto;
import com.stockfellow.transactionservice.model.User;
import com.stockfellow.transactionservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.validation.Valid;
import java.util.UUID;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Users", description = "Operations related to users")
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserSyncController {
    
    @Autowired
    private UserService userService;
    
    // @Autowired
    // private ActivityLogService activityLogService;

    // Sync user from user service (called when user is created/updated in user service)
    @PostMapping("/sync")
    @Operation(summary = "Sync user from user service", 
                description = "Sync user from user service (called when user is created/updated in user service)")
    public ResponseEntity<UserResponseDto> syncUser(@Valid @RequestBody SyncUserDto syncDto) {
        User user = userService.syncUser(syncDto);
        // activityLogService.logActivity(user.getUserId(), null, 
        //                              ActivityLog.EntityType.USER, user.getUserId(), 
        //                              "USER_SYNCED", null, null);
        return ResponseEntity.ok(UserResponseDto.fromEntity(user));
    }
    
    // Get user details
    @GetMapping("/{userId}")
    @Operation(summary = "Get User details", 
                description = "Get details for a particular user")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable UUID userId) {
        User user = userService.findById(userId);
        return ResponseEntity.ok(UserResponseDto.fromEntity(user));
    }
    
    // Batch sync users (for initial data migration)
    @PostMapping("/sync/batch")
    @Operation(summary = "Sync batch of users", 
                description = "Sync a batch of users from the user service")
    public ResponseEntity<List<UserResponseDto>> syncUsers(@Valid @RequestBody List<SyncUserDto> syncDtos) {
        List<User> users = userService.syncUsers(syncDtos);
        return ResponseEntity.ok(users.stream()
                               .map(UserResponseDto::fromEntity)
                               .toList());
    }
}
