// src/services/cardService.ts

import authService from './authService';

//const API_BASE_URL = process.env.API_BASE_URL || '';

// interface BankDetails {
//   id: string;
//   userId: string;
//   bank: string;
//   last4Digits: string;
//   cardHolder: string;
//   expiryMonth: number;
//   expiryYear: number;
//   cardType: string;
//   isActive: boolean;
//   createdAt: string;
//   updatedAt: string;
// }

interface CreateBankDetailsRequest {
  bank: string;
  cardNumber: string;
  cardHolder: string;
  expiryMonth: number;
  expiryYear: number;
  cardType: string;
}

interface BankDetailResponse {
  id: string;
  bank: string;
  last4Digits: string; // Might want to mask this in the UI
  cardHolder: string;
  expiryMonth: number;
  expiryYear: number;
  cardType: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

const cardService = {
  /**
   * Add new bank details for the authenticated user
   */
  async addBankDetails(data: CreateBankDetailsRequest): Promise<BankDetailResponse> {
    try {
      const response = await authService.apiRequest('/transaction/bank-details', {
        method: 'POST',
        body: JSON.stringify(data),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to add bank details');
      }

      return await response.json();
    } catch (error) {
      console.error('Error adding bank details:', error);
      throw error;
    }
  },

  /**
   * Get all bank details for the authenticated user
   */
  async getUserBankDetails(): Promise<BankDetailResponse[]> {
    try {
      const response = await authService.apiRequest('/transaction/bank-details/user');

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to fetch bank details');
      }

      return await response.json();
    } catch (error) {
      console.error('Error fetching user bank details:', error);
      throw error;
    }
  },

  /**
   * Get the active bank details for the authenticated user
   */
  async getActiveBankDetails(): Promise<BankDetailResponse> {
    try {
      const response = await authService.apiRequest('/transaction/bank-details/user/active');

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to fetch active bank details');
      }

      return await response.json();
    } catch (error) {
      console.error('Error fetching active bank details:', error);
      throw error;
    }
  },

  /**
   * Get specific bank details by ID
   */
  async getBankDetailsById(bankDetailsId: string): Promise<BankDetailResponse> {
    try {
      const response = await authService.apiRequest(
        `/transaction/bank-details/${bankDetailsId}`
      );

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to fetch bank details');
      }

      return await response.json();
    } catch (error) {
      console.error('Error fetching bank details by ID:', error);
      throw error;
    }
  },

  /**
   * Activate a specific bank details record
   */
  async activateBankDetails(bankDetailsId: string): Promise<BankDetailResponse> {
    try {
      const response = await authService.apiRequest(
        `/transaction/bank-details/${bankDetailsId}/activate`,
        {
          method: 'PUT',
        }
      );

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to activate bank details');
      }

      return await response.json();
    } catch (error) {
      console.error('Error activating bank details:', error);
      throw error;
    }
  },

  /**
   * Deactivate a specific bank details record
   */
  async deactivateBankDetails(bankDetailsId: string): Promise<void> {
    try {
      const response = await authService.apiRequest(
        `/transaction/bank-details/${bankDetailsId}/deactivate`,
        {
          method: 'PUT',
        }
      );

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to deactivate bank details');
      }
    } catch (error) {
      console.error('Error deactivating bank details:', error);
      throw error;
    }
  },

  /**
   * Delete a specific bank details record
   */
  async deleteBankDetails(bankDetailsId: string): Promise<void> {
    try {
      const response = await authService.apiRequest(
        `/transaction/bank-details/${bankDetailsId}`,
        {
          method: 'DELETE',
        }
      );

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to delete bank details');
      }
    } catch (error) {
      console.error('Error deleting bank details:', error);
      throw error;
    }
  },

  /**
   * Get count of bank details for the authenticated user
   */
  async getBankDetailsCount(): Promise<number> {
    try {
      const response = await authService.apiRequest('/transaction/bank-details/user/count');

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to get bank details count');
      }

      return await response.json();
    } catch (error) {
      console.error('Error getting bank details count:', error);
      throw error;
    }
  },

  

  /**
   * Format expiry date for display
   */
  formatExpiryDate(month: number, year: number): string {
    return `${month.toString().padStart(2, '0')}/${year.toString().slice(-2)}`;
  },
};

export default cardService;