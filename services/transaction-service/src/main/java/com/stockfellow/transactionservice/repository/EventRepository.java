package com.stockfellow.transactionservice.repository;

import com.stockfellow.transactionservice.model.TransactionEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<TransactionEvent, String> {
    
}
