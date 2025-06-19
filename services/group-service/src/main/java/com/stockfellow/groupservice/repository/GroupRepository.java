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
    List<Group> findByMembersContaining(String userId);
    
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
    @Query("{ '_id': ?0, 'members': ?1 }")
    Optional<Group> findByGroupIdAndMembersContaining(String groupId, String userId);
    
    // Count total members in a group
    @Query(value = "{ '_id': ?0 }", fields = "{ 'members': 1 }")
    Optional<Group> findGroupMembersOnly(String groupId);
}