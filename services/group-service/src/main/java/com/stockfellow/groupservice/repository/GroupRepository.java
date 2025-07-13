package com.stockfellow.groupservice.repository;

import com.stockfellow.groupservice.model.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends MongoRepository<Group, String> {
    
    // Find group by groupId field (not the MongoDB _id)
    Optional<Group> findByGroupId(String groupId);

    // Find groups where the user is a member
    @Query("{ 'members.userId': ?0 }")
    List<Group> findGroupsByUserId(String userId);

    // Legacy name for query: findGroupsByUserId
    @Query("{ 'members.userId': ?0 }")
    List<Group> findByMembersUserId(String userId);
    
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
    
    // Find public groups by name containing search term (case-insensitive)
    @Query("{ 'visibility': 'Public', 'name': { $regex: ?0, $options: 'i' } }")
    List<Group> findPublicGroupsByNameContaining(String name);
    
    // Advanced search for public groups (name or description)
    @Query("{ 'visibility': 'Public', $or: [ " +
           "{ 'name': { $regex: ?0, $options: 'i' } }, " +
           "{ 'description': { $regex: ?0, $options: 'i' } } ] }")
    List<Group> findPublicGroupsByNameOrDescriptionContaining(String searchTerm);
    
    // Check if a user is already a member of a group
    @Query("{ '_id': ?0, 'members': ?1 }")
    Optional<Group> findByGroupIdAndMembersContaining(String groupId, String userId);
    
    // Count total members in a group
    @Query(value = "{ '_id': ?0 }", fields = "{ 'members': 1 }")
    Optional<Group> findGroupMembersOnly(String groupId);

    // Get groups where user is admin or member (useful for user dashboard)
    @Query("{ $or: [ { 'adminId': ?0 }, { 'members.userId': ?0 } ] }")
    List<Group> findByAdminIdOrMembersUserId(String userId);

    // Count groups where user is a member
    @Query(value = "{ 'members.userId': ?0 }", count = true)
    long countByMembersUserId(String userId);

    // Find groups by member role
    @Query("{ 'members': { $elemMatch: { 'userId': ?0, 'role': ?1 } } }")
    List<Group> findByMemberUserIdAndRole(String userId, String role);
}