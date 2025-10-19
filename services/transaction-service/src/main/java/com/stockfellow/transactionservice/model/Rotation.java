package com.stockfellow.transactionservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rotations")
public class Rotation {
    
    @Id
    @Column(name = "id")
    private UUID id;
    
    @Column(name = "group_id", nullable = false)
    private String groupId;
    
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "member_ids", nullable = false, columnDefinition = "uuid[]")
    private UUID[] memberIds;
    
    @Column(name = "position", nullable = false)
    private Integer position = 0;
    
    @Column(name = "collection_date")
    private LocalDate collectionDate;
    
    @Column(name = "payout_date")
    private LocalDate payoutDate;
    
    @Column(name = "status")
    private String status;

    @Column(name = "frequency")
    private String frequency;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Rotation() {}

    public Rotation(String groupId, BigDecimal amount, UUID[] memberIds, LocalDate collectionDate, LocalDate payoutDate, String frequency) {
        this.groupId = groupId;
        this.amount = amount;
        this.memberIds = memberIds;
        this.collectionDate = collectionDate;
        this.payoutDate = payoutDate;
        this.position = 0;
        this.status = "inactive";
        this.frequency = frequency;
    }

    // Getters
    public UUID getId() { return id; }
    public String getGroupId() { return groupId; }
    public BigDecimal getAmount() { return amount; }
    public UUID[] getMemberIds() { return memberIds; }
    public Integer getPosition() { return position; }
    public LocalDate getCollectionDate() { return collectionDate; }
    public LocalDate getPayoutDate() { return payoutDate; }
    public String getStatus() { return status; }
    public String getFrequency() { return frequency; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(UUID id) { this.id = id; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setMemberIds(UUID[] memberIds) { this.memberIds = memberIds; }
    public void setPosition(Integer position) { this.position = position; }
    public void setCollectionDate(LocalDate collectionDate) { this.collectionDate = collectionDate; }
    public void setPayoutDate(LocalDate payoutDate) { this.payoutDate = payoutDate; }
    public void setStatus(String status) { this.status = status; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "inactive";
        }
        if (position == null) {
            position = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper methods for working with the rotation
    public UUID getCurrentRecipient() {
        if (memberIds == null || memberIds.length == 0) {
            return null;
        }
        return memberIds[position % memberIds.length];
    }
    
    public void advancePosition() {
        if (memberIds != null && memberIds.length > 0) {
            this.position = (this.position + 1) % memberIds.length;
        }
    }

    public void advanceDates() {
        this.collectionDate = this.collectionDate.plusMonths(1);
        this.payoutDate = this.payoutDate.plusMonths(1);
    }
    
    /**
     * Adds a new member to the rotation
     * @param memberId UUID of the member to add
     * @throws IllegalArgumentException if memberId is null
     * @throws IllegalStateException if member already exists
     */
    public void addMember(UUID memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("Member ID cannot be null");
        }
        
        List<UUID> memberList = new ArrayList<>(Arrays.asList(this.memberIds));
        
        if (memberList.contains(memberId)) {
            throw new IllegalStateException("Member already exists in rotation");
        }
        
        memberList.add(memberId);
        
        this.memberIds = memberList.toArray(new UUID[0]);
    }
}