package com.stockfellow.transactionservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.stockfellow.transactionservice.repository")
@EnableTransactionManagement
public class DatabaseConfig {
    // spring boot already does it?
}