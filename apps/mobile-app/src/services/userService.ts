// src/services/userService.ts

import authService from './authService';
import * as DocumentPicker from 'expo-document-picker';

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

interface BankStatementUploadResponse {
  success: boolean;
  message: string;
  extractionResult: {
    transactionsExtracted: number;
    qualityScore: number;
    dateRange: {
      start: string;
      end: string;
    };
    warnings: string[];
    recommendations: string[];
  };
  affordabilityResult: {
    tier: number;
    tierName: string;
    confidence: number;
    contributionRange: {
      min: number;
      max: number;
    };
    analysisDetails: any;
  };
  timestamp: number;
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
      tier: number;
      tierName: string;
      contributionRange?: { min: number; max: number };
      confidence?: number;
      lastAnalyzed?: string;
      needsReanalysis?: boolean;
    };
  }> {
    try {
      console.log('Fetching user profile...');
      const response = await authService.apiRequest(`${this.baseUrl}/profile`);
      const data = await response.json();

      console.log('Raw profile response:', data);

      // Ensure affordability tier is properly structured
      const tier = data.user?.affordabilityTier || 
                   data.affordability?.tier || 
                   0;
      
      const tierName = data.affordability?.tierName || 
                      this.getTierName(tier);

      const profileData = {
        ...data,
        affordability: {
          tier: tier,
          tierName: tierName,
          contributionRange: this.getContributionRange(tier),
          confidence: data.user?.affordabilityConfidence,
          lastAnalyzed: data.user?.affordabilityAnalyzedAt,
          needsReanalysis: data.affordability?.needsReanalysis || false,
        }
      };

      console.log('Processed profile data:', {
        tier: profileData.affordability.tier,
        tierName: profileData.affordability.tierName,
        confidence: profileData.affordability.confidence
      });

      return profileData;
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
    tierName: string;
    confidence?: number;
    contributionRange?: { min: number; max: number };
    lastAnalyzed?: string;
    needsReanalysis?: boolean;
  }> {
    try {
      console.log('Fetching user affordability tier...');
      const response = await authService.apiRequest(`${this.baseUrl}/affordability`);
      const data = await response.json();

      console.log('Affordability tier response:', data);

      return {
        userId: data.userId,
        tier: data.tier || 0,
        tierName: this.getTierName(data.tier),
        confidence: data.confidence,
        contributionRange: this.getContributionRange(data.tier),
        lastAnalyzed: data.lastAnalyzed,
        needsReanalysis: data.needsReanalysis,
      };
    } catch (error) {
      console.error('Error fetching affordability tier:', error);
      
      // Return default tier if error
      return {
        userId: '',
        tier: 0,
        tierName: 'Unanalyzed',
      };
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


 /**
 * Upload bank statement PDF for affordability analysis
 * This can be used during registration or later from the profile screen
 */
  async uploadBankStatement(
    bankStatementFile: DocumentPicker.DocumentPickerAsset
  ): Promise<BankStatementUploadResponse> {
    try {
      console.log('Preparing bank statement for upload...');
      console.log('File details:', {
        name: bankStatementFile.name,
        type: bankStatementFile.mimeType,
        size: bankStatementFile.size,
        uri: bankStatementFile.uri.substring(0, 50) + '...',
      });

      // Validate file
      if (!bankStatementFile.mimeType || !bankStatementFile.mimeType.includes('pdf')) {
        throw new Error('Only PDF files are accepted for bank statement analysis');
      }

      // Create FormData for file upload
      const formData = new FormData();

      // For React Native, we need to create a proper blob file object
      // The file object will be constructed from the URI
      const fileObject: any = {
        uri: bankStatementFile.uri,
        type: bankStatementFile.mimeType || 'application/pdf',
        name: bankStatementFile.name || 'bank-statement.pdf',
      };

      // Append to FormData - React Native will handle the URI conversion
      formData.append('bankStatement', fileObject);

      console.log('Uploading bank statement for affordability analysis...');
      console.log('FormData prepared with file:', {
        filename: fileObject.name,
        mimeType: fileObject.type,
      });

      const response = await authService.apiRequest(
        `${this.baseUrl}/affordability/analyze-pdf`,
        {
          method: 'POST',
          body: formData,
          headers: {
            // DO NOT set Content-Type - FormData will set it automatically with boundary
            // Setting it manually will break the multipart encoding
          },
        }
      );

      // Check if response is ok before parsing
      if (!response.ok) {
        const errorText = await response.text();
        console.error('Upload error response status:', response.status);
        console.error('Upload error response:', errorText);
        
        try {
          const errorData = JSON.parse(errorText);
          throw new Error(errorData.message || errorData.error || `Upload failed: ${response.status}`);
        } catch (parseError) {
          throw new Error(`Upload failed with status ${response.status}: ${errorText}`);
        }
      }

      const data = await response.json();
      console.log('Bank statement upload response:', data);

      if (data.success) {
        console.log('Bank statement analyzed successfully:', {
          tier: data.affordabilityResult.tier,
          tierName: data.affordabilityResult.tierName,
          confidence: data.affordabilityResult.confidence,
          transactionsExtracted: data.extractionResult.transactionsExtracted,
        });

        return data;
      } else {
        throw new Error(data.message || 'Bank statement analysis failed');
      }
    } catch (error: any) {
      console.error('Error uploading bank statement:', error);

      // Handle specific error scenarios
      if (error.message?.includes('Network')) {
        throw new Error('Network error: Unable to connect to server. Please check your internet connection.');
      }

      if (error.message?.includes('only PDF')) {
        throw new Error(error.message);
      }

      if (error.response?.status === 400) {
        const errorMsg = error.response.data?.message || 
                        error.response.data?.error || 
                        'Invalid bank statement format';
        throw new Error(errorMsg);
      }

      if (error.response?.status === 413) {
        throw new Error('File is too large. Maximum file size is 10MB.');
      }

      throw new Error(
        error.message || 
        error.response?.data?.message || 
        'Failed to upload bank statement'
      );
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
    if (tier === undefined || tier === 0) return 'Unanalyzed';
    
    const tierNames: Record<number, string> = {
      1: 'Essential Savers',
      2: 'Steady Builders',
      3: 'Balanced Savers',
      4: 'Growth Investors',
      5: 'Premium Accumulators',
      6: 'Elite Circle',
    };
    
    return tierNames[tier] || 'Unknown Tier';
  }

  getContributionRange(tier?: number): { min: number; max: number } {
    if (tier === undefined || tier === 0) return { min: 0, max: 0 };

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

  /**
   * Check if user needs to reanalyze their affordability tier
   * (Recommended after 90 days)
   */
  needsReanalysis(lastAnalyzed?: string): boolean {
    if (!lastAnalyzed) return true;

    const lastAnalyzedDate = new Date(lastAnalyzed);
    const daysSinceAnalysis = Math.floor(
      (Date.now() - lastAnalyzedDate.getTime()) / (1000 * 60 * 60 * 24)
    );

    return daysSinceAnalysis > 90; // Recommend reanalysis after 3 months
  }
}

const userService = new UserService();
export default userService;