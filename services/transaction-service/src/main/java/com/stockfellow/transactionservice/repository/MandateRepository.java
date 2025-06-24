package com.stockfellow.transactionservice.repository;

import com.stockfellow.transactionservice.model.Mandate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MandateRepository extends JpaRepository<Mandate, UUID> {

    // Find mandate by payer and group
    Optional<Mandate> findByPayerUserIdAndGroupId(UUID payerUserId, UUID groupId);

    // Find mandates by group
    List<Mandate> findByGroupId(UUID groupId);

    // Find mandates by status
    List<Mandate> findByStatus(String status);

    // Find active mandates for a group
    List<Mandate> findByGroupIdAndStatus(UUID groupId, String status);
}
