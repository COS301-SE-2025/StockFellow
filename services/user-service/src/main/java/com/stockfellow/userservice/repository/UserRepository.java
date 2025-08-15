package com.stockfellow.userservice.repository;

import com.stockfellow.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by Keycloak user ID
     */
    Optional<User> findByUserId(String userId);
    
    /**
     * Find user by email address
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find user by South African ID number
     */
    Optional<User> findByIdNumber(String idNumber);
    
    /**
     * Check if user exists by Keycloak user ID
     */
    boolean existsByUserId(String userId);
    
    /**
     * Check if email is already registered
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if SA ID number is already registered
     */
    boolean existsByIdNumber(String idNumber);
    
    /**
     * Find users by ID verification status
     */
    List<User> findByIdVerified(boolean idVerified);
    
    /**
     * Find users by email verification status
     */
    List<User> findByEmailVerified(boolean emailVerified);
    
    /**
     * Search users by name (first name, last name, or username)
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> findByNameContaining(@Param("name") String name);
    
    /**
     * Find users created after a specific date
     */
    List<User> findByCreatedAtAfter(LocalDateTime createdAt);
    
    /**
     * Count users created after a specific date
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate")
    long countUsersCreatedAfter(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Find users with verified IDs
     */
    @Query("SELECT u FROM User u WHERE u.idVerified = true AND u.alfrescoDocumentId IS NOT NULL")
    List<User> findVerifiedUsers();
    
    /**
     * Find users by gender (from ID verification)
     */
    List<User> findByGender(String gender);
    
    /**
     * Find users by citizenship status
     */
    List<User> findByCitizenship(String citizenship);
    
    /**
     * Find users who need ID verification
     */
    @Query("SELECT u FROM User u WHERE u.idVerified = false AND u.idNumber IS NOT NULL")
    List<User> findUsersNeedingIdVerification();
    
    /**
     * Find users with incomplete profiles
     */
    @Query("SELECT u FROM User u WHERE " +
           "u.firstName IS NULL OR u.firstName = '' OR " +
           "u.lastName IS NULL OR u.lastName = '' OR " +
           "u.contactNumber IS NULL OR u.contactNumber = ''")
    List<User> findUsersWithIncompleteProfiles();
}