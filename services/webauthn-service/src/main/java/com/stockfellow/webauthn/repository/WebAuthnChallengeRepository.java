package com.stockfellow.webauthn.repository;

import com.stockfellow.webauthn.entity.WebAuthnChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface WebAuthnChallengeRepository extends JpaRepository<WebAuthnChallenge, Long> {

    // find challenge by challenge string, not used, not expired
    Optional<WebAuthnChallenge> findByChallengeAndIsUsedFalseAndExpiresAtAfter(
            String challenge, LocalDateTime now);

    // Delete expired challenges
    void deleteByExpiresAtBefore(LocalDateTime now);

    // Delete used challenges
    void deleteByIsUsedTrue();
}
