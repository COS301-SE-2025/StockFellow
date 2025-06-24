package com.stockfellow.transactionservice.repository;

import com.stockfellow.transactionservice.model.MemberBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberBalanceRepository extends JpaRepository<MemberBalance, UUID> { // CHANGED: String to UUID

    // Find balance by user and group
    Optional<MemberBalance> findByUserIdAndGroupId(UUID userId, UUID groupId);

    // Find all balances for a group
    List<MemberBalance> findByGroupIdOrderByTotalContributedDesc(UUID groupId);

    // Find all balances for a user across groups
    List<MemberBalance> findByUserIdOrderByTotalContributedDesc(UUID userId);

}
