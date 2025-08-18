package com.stockfellow.userservice.dto;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class AffordabilityTierResult {
    private String userId;
    private int tier;
    private String tierName;
    private double confidence;
    private int recommendedContributionMin;
    private int recommendedContributionMax;
    
    private BankStatementAnalysis analysis;
    private Map<String, Integer> scores;
    private List<String> riskFactors;
    private List<String> recommendations;
    private Date analyzedAt;
    
    public AffordabilityTierResult() {}
    
    // Getters and Setters
    public String getUserId() { 
        return userId; 
    }
    
    public void setUserId(String userId) { 
        this.userId = userId; 
    }
    
    public int getTier() { 
        return tier; 
    }
    
    public void setTier(int tier) { 
        this.tier = tier; 
    }
    
    public String getTierName() { 
        return tierName; 
    }
    
    public void setTierName(String tierName) { 
        this.tierName = tierName; 
    }
    
    public double getConfidence() { 
        return confidence; 
    }
    
    public void setConfidence(double confidence) { 
        this.confidence = confidence; 
    }
    
    public int getRecommendedContributionMin() { 
        return recommendedContributionMin; 
    }
    
    public void setRecommendedContributionMin(int recommendedContributionMin) { 
        this.recommendedContributionMin = recommendedContributionMin; 
    }
    
    public int getRecommendedContributionMax() { 
        return recommendedContributionMax; 
    }
    
    public void setRecommendedContributionMax(int recommendedContributionMax) { 
        this.recommendedContributionMax = recommendedContributionMax; 
    }
    
    public BankStatementAnalysis getAnalysis() { 
        return analysis; 
    }
    
    public void setAnalysis(BankStatementAnalysis analysis) { 
        this.analysis = analysis; 
    }
    
    public Map<String, Integer> getScores() { 
        return scores; 
    }
    
    public void setScores(Map<String, Integer> scores) { 
        this.scores = scores; 
    }
    
    public List<String> getRiskFactors() { 
        return riskFactors; 
    }
    
    public void setRiskFactors(List<String> riskFactors) { 
        this.riskFactors = riskFactors; 
    }
    
    public List<String> getRecommendations() { 
        return recommendations; 
    }
    
    public void setRecommendations(List<String> recommendations) { 
        this.recommendations = recommendations; 
    }
    
    public Date getAnalyzedAt() { 
        return analyzedAt; 
    }
    
    public void setAnalyzedAt(Date analyzedAt) { 
        this.analyzedAt = analyzedAt; 
    }
}