package com.stockfellow.groupservice.dto;

import java.util.List;
import java.util.Date;
import java.util.ArrayList;

public class CreateGroupRequest {
    private String adminId;
    private String adminName;
    private String name;
    private Double minContribution;
    private Integer maxMembers;
    private String description;
    private String profileImage;
    private String visibility;
    private String contributionFrequency;
    private Date contributionDate;
    private String payoutFrequency;
    private Date payoutDate;
    private Integer tier;
    private List<String> members = new ArrayList<>();

    // Constructors
    public CreateGroupRequest() {
    }

    public CreateGroupRequest(String adminId, String adminName, String name, Double minContribution, Integer maxMembers,
            String description, String profileImage, String visibility,
            String contributionFrequency, Date contributionDate,
            String payoutFrequency, Date payoutDate, List<String> members) {
        this.adminId = adminId;
        this.adminName = adminName;
        this.name = name;
        this.minContribution = minContribution;
        this.maxMembers = maxMembers;
        this.description = description;
        this.profileImage = profileImage;
        this.visibility = visibility;
        this.contributionFrequency = contributionFrequency;
        this.contributionDate = contributionDate;
        this.payoutFrequency = payoutFrequency;
        this.payoutDate = payoutDate;
        this.members = members != null ? members : new ArrayList<>();
    }

    // Getters and setters
    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getMinContribution() {
        return minContribution;
    }

    public void setMinContribution(Double minContribution) {
        this.minContribution = minContribution;
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

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public Integer getTier() {
        return tier;
    }

    public void setTier(Integer tier) {
        this.tier = tier;
    }
}
