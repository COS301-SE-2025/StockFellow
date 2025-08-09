package com.stockfellow.transactionservice.service;

import com.stockfellow.transactionservice.dto.UpdateUserStatusDto;
import com.stockfellow.transactionservice.dto.SyncUserDto;
import com.stockfellow.transactionservice.repository.UserRepository;
// import com.stockfellow.transactionservice.model.ActivityLog;
import com.stockfellow.transactionservice.model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User syncUser(SyncUserDto syncDto){
        logger.info("Syncing user: {}", syncDto.getUserId());

        User user = new User();
        user.setUserId(syncDto.getUserId());
        user.setEmail(syncDto.getEmail());
        user.setFirstName(syncDto.getFirstName());
        user.setLastName(syncDto.getLastName());
        user.setPhone(syncDto.getPhone());
        user.setStatus(User.UserStatus.pending);

        user = userRepository.save(user);

        // activityLogService.logActivity(
        //     syncDto.getUserId(), 
        //     ActivityLog.EntityType.USER,
        //     user.getUserId(),
        //     "USER_SYNCED", 
        //     null, 
        //     null
        // );

        logger.info("User synced successfully with ID: {}", user.getUserId());
        return user;

    }

    public User findById(UUID userId){
        logger.info("Searching for user with UserId: {}", userId);
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    public User updateUserStatus(UUID userId, UpdateUserStatusDto status){
        logger.info("Updating user details with UserId: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        user.setStatus(status.getStatus());

        user = userRepository.save(user);
        return user;
    }

    public List<User> syncUsers(List<SyncUserDto> syncDtos) {
        logger.info("Syncing {} users", syncDtos.size());
        List<User> userList = new ArrayList<>();

        for(SyncUserDto userDto: syncDtos){
            User syncedUser = syncUser(userDto);
            userList.add(syncedUser);
        }

        return userList;
    }
}
