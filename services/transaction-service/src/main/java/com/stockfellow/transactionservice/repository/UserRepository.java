package com.stockfellow.transactionservice.repository;

import com.stockfellow.transactionservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
