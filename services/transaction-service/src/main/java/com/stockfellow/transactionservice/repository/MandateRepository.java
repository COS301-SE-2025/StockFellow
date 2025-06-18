package com.stockfellow.transactionservice.repository;

import com.stockfellow.transactionservice.model.Mandate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MandateRepository extends JpaRepository<Mandate, String> {
    Mandate findByUserIdAndStatus(String userId, String status);
}
