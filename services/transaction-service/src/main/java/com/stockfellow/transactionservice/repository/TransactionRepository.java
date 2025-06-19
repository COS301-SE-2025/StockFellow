package com.stockfellow.transactionservice.repository;

import com.stockfellow.transactionservice.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
}
