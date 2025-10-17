package com.stockfellow.groupservice.dto;

import java.util.Date;

public class UpdateGroupRequest {
    private String name;
    private Integer maxMembers;
    private String description;
    private String profileImage;
    private String visibility;
    private String contributionFrequency;
    private Date contributionDate;
    private String payoutFrequency;
    private Date payoutDate;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getMaxMembers() { return maxMembers; }
    public void setMaxMembers(Integer maxMembers) { this.maxMembers = maxMembers; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    public String getContributionFrequency() { return contributionFrequency; }
    public void setContributionFrequency(String contributionFrequency) { this.contributionFrequency = contributionFrequency; }
    public Date getContributionDate() { return contributionDate; }
    public void setContributionDate(Date contributionDate) { this.contributionDate = contributionDate; }
    public String getPayoutFrequency() { return payoutFrequency; }
    public void setPayoutFrequency(String payoutFrequency) { this.payoutFrequency = payoutFrequency; }
    public Date getPayoutDate() { return payoutDate; }
    public void setPayoutDate(Date payoutDate) { this.payoutDate = payoutDate; }
}