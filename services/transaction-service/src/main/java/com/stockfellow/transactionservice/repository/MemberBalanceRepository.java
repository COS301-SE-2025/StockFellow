package com.stockfellow.transactionservice.repository;

import com.stockfellow.transactionservice.model.MemberBalance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberBalanceRepository extends JpaRepository<MemberBalance, String> {
    
}
