// src/services/userService.ts

import authService from './authService';

interface User {
  id: string;
  userId: string;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  emailVerified: boolean;
  contactNumber?: string;
  idNumber?: string;
  idVerified: boolean;
  alfrescoDocumentId?: string;
  createdAt: string;
  updatedAt: string;
  dateOfBirth?: string;
  gender?: string;
  citizenship?: string;
  affordabilityTier?: number;
  affordabilityConfidence?: number;
  affordabilityAnalyzedAt?: string;
}

interface AffordabilityTierResult {
  tier: number;
  confidence: number;
  analysisDetails: {
    monthlyIncome: number;
    monthlyExpenses: number;
    disposableIncome: number;
    savingsRate: number;
    debtToIncomeRatio: number;
    financialStabilityScore: number;
  };
}

interface BankStatementUploadRequest {
  transactions: Array<{
    date: string;
    description: string;
    amount: number;
    balance: number;
    category: string;
  }>;
}

interface IDVerificationResponse {
  success: boolean;
  message: string;
  idNumber: string;
  extractedInfo: {
    dateOfBirth: string;
    gender: string;
    citizenship: string;
  };
  documentId: string;
  verificationTimestamp: number;
  user: {
    id: string;
    userId: string;
    email: string;
    idVerified: boolean;
    updatedAt: string;
  };
  nextSteps: {
    message: string;
    endpoint: string;
  };
}

interface UserStats {
  totalUsers: number;
  verifiedUsers: number;
  unverifiedUsers: number;
  incompleteProfiles: number;
  verificationRate: number;
}

interface AffordabilityStats {
  totalUsers: number;
  analyzedUsers: number;
  unanalyzedUsers: number;
  analysisRate: number;
  tierDistribution: Record<number, number>;
  tierDistributionPercentage: Record<number, number>;
}

class UserService {
  private baseUrl = '/users';

  async getServiceInfo(): Promise<{
    service: string;
    version: string;
    database: string;
    endpoints: string[];
  }> {
    try {
      const response = await authService.apiRequest(this.baseUrl);
      return await response.json();
    } catch (error) {
      console.error('Error getting user service info:', error);
      throw error;
    }
  }

  async getProfile(): Promise<{
    user: User;
    affordability: {
      tier?: number;
      tierName: string;
      contributionRange: { min: number; max: number };
      confidence?: number;
      lastAnalyzed?: string;
      needsReanalysis: boolean;
    };
  }> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/profile`);
      return await response.json();
    } catch (error) {
      console.error('Error getting user profile:', error);
      throw error;
    }
  }

  async getUserById(userId: string): Promise<User> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/${userId}`);
      return await response.json();
    } catch (error) {
      console.error('Error getting user by ID:', error);
      throw error;
    }
  }

  async getUserAffordabilityTier(): Promise<{
    userId: string;
    tier: number;
    tierName?: string;
    confidence?: number;
    contributionRange?: { min: number; max: number };
    lastAnalyzed?: string;
    needsReanalysis?: boolean;
  }> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/affordability`);
      return await response.json();
    } catch (error) {
      console.error('Error getting user affordability tier:', error);
      throw error;
    }
  }

  async analyzeAffordability(
    userId: string,
    transactions: BankStatementUploadRequest['transactions']
  ): Promise<{
    success: boolean;
    message: string;
    result: {
      tier: number;
      tierName: string;
      confidence: number;
      contributionRange: { min: number; max: number };
      analysisDetails: any;
    };
    timestamp: number;
  }> {
    try {
      if (transactions.length < 50) {
        throw new Error('Minimum 50 transactions required for reliable analysis');
      }

      const response = await authService.apiRequest(`${this.baseUrl}/affordability/analyze`, {
        method: 'POST',
        body: JSON.stringify({ transactions }),
      });

      return await response.json();
    } catch (error) {
      console.error('Error analyzing affordability:', error);
      throw error;
    }
  }

  async searchUsers(name: string): Promise<{ users: User[]; count: number }> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/search?name=${encodeURIComponent(name)}`);
      return await response.json();
    } catch (error) {
      console.error('Error searching users:', error);
      throw error;
    }
  }

  async getVerifiedUsers(): Promise<{ verifiedUsers: User[]; count: number }> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/verified`);
      return await response.json();
    } catch (error) {
      console.error('Error getting verified users:', error);
      throw error;
    }
  }

  async getUserStats(): Promise<UserStats> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/stats`);
      return await response.json();
    } catch (error) {
      console.error('Error getting user stats:', error);
      throw error;
    }
  }

  async getAffordabilityStats(): Promise<AffordabilityStats> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/affordability/stats`);
      return await response.json();
    } catch (error) {
      console.error('Error getting affordability stats:', error);
      throw error;
    }
  }

  async verifyID(
    userId: string,
    file: File
  ): Promise<IDVerificationResponse> {
    try {
      if (!file.type.includes('pdf')) {
        throw new Error('Only PDF files are accepted for ID verification');
      }

      const formData = new FormData();
      formData.append('file', file);
      formData.append('userId', userId);

      const response = await authService.apiRequest(`${this.baseUrl}/verifyID`, {
        method: 'POST',
        body: formData,
        headers: {
          // Let the browser set the Content-Type with boundary
          'Content-Type': undefined,
        },
      });

      return await response.json();
    } catch (error) {
      console.error('Error verifying ID:', error);
      throw error;
    }
  }

  // Helper methods
  getTierName(tier?: number): string {
    if (tier === undefined) return 'Unanalyzed';
    switch (tier) {
      case 1: return 'Essential Savers';
      case 2: return 'Steady Builders';
      case 3: return 'Balanced Savers';
      case 4: return 'Growth Investors';
      case 5: return 'Premium Accumulators';
      case 6: return 'Elite Circle';
      default: return 'Unknown Tier';
    }
  }

  getContributionRange(tier?: number): { min: number; max: number } {
    if (tier === undefined) return { min: 0, max: 0 };

    const ranges: Record<number, { min: number; max: number }> = {
      1: { min: 50, max: 200 },
      2: { min: 200, max: 500 },
      3: { min: 500, max: 1000 },
      4: { min: 1000, max: 2500 },
      5: { min: 2500, max: 5000 },
      6: { min: 5000, max: 10000 },
    };

    return ranges[tier] || { min: 0, max: 0 };
  }
}

const userService = new UserService();
export default userService;