package com.stockfellow.webauthn.repository;

import com.stockfellow.webauthn.entity.KeycloakWebAuthnCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KeycloakWebAuthnCredentialRepository extends JpaRepository<KeycloakWebAuthnCredential, String> {

    // find all webauthn credentails for user
    @Query("SELECT c FROM KeycloakWebAuthnCredential c WHERE c.userId = :userId AND c.type = 'webauthn'")
    List<KeycloakWebAuthnCredential> findByUserIdAndType(@Param("userId") String userId);

    // check if user has any credentials
    @Query("SELECT COUNT(c) > 0 FROM KeycloakWebAuthnCredential c WHERE c.userId = :userId AND c.type = 'webauthn'")
    boolean existsByUserIdAndType(@Param("userId") String userId);

    // find credential by userId and userLabel (device name)
    @Query("SELECT c FROM KeycloakWebAuthnCredential c WHERE c.userId = :userId AND c.userLabel = :userLabel AND c.type = 'webauthn'")
    Optional<KeycloakWebAuthnCredential> findByUserIdAndUserLabel(@Param("userId") String userId,
            @Param("userLabel") String userLabel);
}
