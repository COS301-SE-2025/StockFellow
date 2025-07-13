package com.stockfellow.groupservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Document(collection = "groups")
public class Group {
    @Id
    private String id;
    private String groupId;
    private String name;
    private String adminId;
    private Double minContribution;
    private Double balance;
    private Integer maxMembers;
    private String description;
    private String profileImage;
    private String visibility;
    private String contributionFrequency;
    private Date contributionDate;
    private String payoutFrequency;
    private Date payoutDate;
    private Date createdAt;
    private List<Member> members;
    private List<JoinRequest> requests;

    public Group() {
        this.members = new ArrayList<>();
        this.requests = new ArrayList<>();
    }

    public Group(String groupId) {
        this.groupId = groupId;
        this.members = new ArrayList<>();
        this.requests = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public Double getMinContribution() {
        return minContribution;
    }

    public void setMinContribution(Double minContribution) {
        this.minContribution = minContribution;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getContributionFrequency() {
        return contributionFrequency;
    }

    public void setContributionFrequency(String contributionFrequency) {
        this.contributionFrequency = contributionFrequency;
    }

    public Date getContributionDate() {
        return contributionDate;
    }

    public void setContributionDate(Date contributionDate) {
        this.contributionDate = contributionDate;
    }

    public String getPayoutFrequency() {
        return payoutFrequency;
    }

    public void setPayoutFrequency(String payoutFrequency) {
        this.payoutFrequency = payoutFrequency;
    }

    public Date getPayoutDate() {
        return payoutDate;
    }

    public void setPayoutDate(Date payoutDate) {
        this.payoutDate = payoutDate;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }

    public List<JoinRequest> getRequests() {
        return requests;
    }

    public void setRequests(List<JoinRequest> requests) {
        this.requests = requests;
    }

    // Inner Classes
    public static class Member {
        private String userId;
        private String username;
        private String role;
        private Date joinedAt;
        private Double contribution;
        private Date lastActive;

        public Member() {
            this.joinedAt = new Date();
            this.contribution = 0.0;
            this.lastActive = new Date();
        }

        public Member(String userId, String userame, String role) {
            this.userId = userId;
            this.username = userame;
            this.role = role;
            this.joinedAt = new Date();
            this.contribution = 0.0;
            this.lastActive = new Date();
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public Date getJoinedAt() {
            return joinedAt;
        }

        public void setJoinedAt(Date joinedAt) {
            this.joinedAt = joinedAt;
        }

        public Double getContribution() {
            return contribution;
        }

        public void setContribution(Double contribution) {
            this.contribution = contribution;
        }

        public Date getLastActive() {
            return lastActive;
        }

        public void setLastActive(Date lastActive) {
            this.lastActive = lastActive;
        }
    }

    public static class JoinRequest {
        private String requestId;
        private String userId;
        private String username;
        private String state; // "waiting", "accepted", "rejected"
        private Date timestamp;

        public JoinRequest() {
            this.requestId = UUID.randomUUID().toString().substring(0, 12);
            this.timestamp = new Date();
            this.state = "waiting";
        }

        public JoinRequest(String userId, String username) {
            this.requestId = UUID.randomUUID().toString().substring(0, 12);
            this.userId = userId;
            this.username = username;
            this.state = "waiting";
            this.timestamp = new Date();
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
        }
    }
}