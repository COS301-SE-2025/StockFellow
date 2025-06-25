package com.stockfellow.groupservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Document(collection = "groups")
@Data
public class Group {
    @Id
    private String id; // MongoDB _id field
    private String groupId; // Your custom groupId
    private String name;
    private String adminId;
    private Double minContribution;
    private Double balance = 0.0; // Initialize balance to 0
    private Integer maxMembers;
    private String description;
    private String profileImage;
    private String visibility;
    private String contributionFrequency;
    private String payoutFrequency;
    private Date contributionDate;
    private Date payoutDate;
    private Date createdAt;
    private List<Member> members = new ArrayList<>(); // Changed from memberIds to members array
    
    public Group() {
        this.createdAt = new Date();
    }

    public Group(String groupId) {
        this.groupId = groupId;
        this.createdAt = new Date();
    }

    @Data
    public static class Member {
        private String userId;
        private String role; // "founder", "admin", "member"
        private Date joinedAt;
        private Double contribution = 0.0;
        private Date lastActive;

        public Member() {
            this.joinedAt = new Date();
            this.lastActive = new Date();
        }

        public Member(String userId, String role) {
            this.userId = userId;
            this.role = role;
            this.joinedAt = new Date();
            this.lastActive = new Date();
        }
    }
}