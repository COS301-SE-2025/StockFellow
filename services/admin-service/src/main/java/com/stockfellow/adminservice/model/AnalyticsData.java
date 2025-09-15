package com.stockfellow.adminservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Entity
@Table(name = "daily_metrics")
public class DailyMetrics {
    @Id
    @Column(name = "date")
    private LocalDate date;

    @Column(name = "new_users")
    private Long newUsers = 0L;

    @Column(name = "active_users")
    private Long activeUsers = 0L;

    @Column(name = "new_groups")
    private Long newGroups = 0L;

    @Column(name = "total_transactions")
    private Long totalTransactions = 0L;

    @Column(name = "transaction_volume", precision = 15, scale = 2)
    private BigDecimal transactionVolume = BigDecimal.ZERO;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    // Constructors
    public DailyMetrics() {}

    public DailyMetrics(LocalDate date) {
        this.date = date;
    }

    // Getters and Setters
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Long getNewUsers() { return newUsers; }
    public void setNewUsers(Long newUsers) { this.newUsers = newUsers; }

    public Long getActiveUsers() { return activeUsers; }
    public void setActiveUsers(Long activeUsers) { this.activeUsers = activeUsers; }

    public Long getNewGroups() { return newGroups; }
    public void setNewGroups(Long newGroups) { this.newGroups = newGroups; }

    public Long getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(Long totalTransactions) { this.totalTransactions = totalTransactions; }

    public BigDecimal getTransactionVolume() { return transactionVolume; }
    public void setTransactionVolume(BigDecimal transactionVolume) { this.transactionVolume = transactionVolume; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
}