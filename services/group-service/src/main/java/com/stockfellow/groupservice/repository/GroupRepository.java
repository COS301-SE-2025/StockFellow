package com.stockfellow.groupservice.repository;

import com.stockfellow.groupservice.model.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends MongoRepository<Group, String> {
    
    // Find groups where the user is a member
    List<Group> findByMemberIdsContaining(String userId);
    
    // Find groups by admin ID
    List<Group> findByAdminId(String adminId);
    
    // Find groups by visibility
    List<Group> findByVisibility(String visibility);
    
    // Find public groups for discovery
    @Query("{ 'visibility': 'Public' }")
    List<Group> findPublicGroups();
    
    // Find groups by name (case-insensitive)
    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    List<Group> findByNameContainingIgnoreCase(String name);
    
    // Check if a user is already a member of a group
    @Query("{ '_id': ?0, 'memberIds': ?1 }")
    Optional<Group> findByGroupIdAndMemberIdsContaining(String groupId, String userId);
    
    // Count total members in a group
    @Query(value = "{ '_id': ?0 }", fields = "{ 'memberIds': 1 }")
    Optional<Group> findGroupMembersOnly(String groupId);
}