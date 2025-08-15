// src/services/cardService.ts

import authService from './authService';

interface InitializeCardAuthRequest {
    userId: string;
    email: string;
    type: string; // e.g., "DEBIT_CARD"
}

interface InitializeCardAuthResponse {
    status: boolean;
    message: string;
    data?: {
        authorization_url: string;
        access_code: string;
        reference: string;
    };
}

interface PayerDetailsResponse {
    payerId: string;
    userId: string;
    type: string;
    email: string;
    authCode?: string;
    cardType?: string;
    last4?: string;
    expMonth?: number;
    expYear?: number;
    bin?: string;
    bank?: string;
    signature?: string;
    isActive: boolean;
    isAuthenticated: boolean;
    createdAt: string;
    updatedAt: string;
}

// Frontend card format for UI
interface FormattedCard {
    id: string;
    bank: string;
    last4Digits: string;
    cardHolder: string;
    expiryMonth: string;
    expiryYear: string;
    cardType: 'mastercard' | 'visa';
    isActive: boolean;
}

const cardService = {
    /**
     * Initialize card authorization with Paystack
     * This replaces the old addBankDetails method
     */
    async initializeCardAuthorization(userId: string, email: string): Promise<InitializeCardAuthResponse> {
        try {
            const requestData: InitializeCardAuthRequest = {
                userId,
                email,
                type: "DEBIT_CARD"
            };

            const response = await authService.apiRequest('/transaction/payment-methods/payer/initialize', {
                method: 'POST',
                body: JSON.stringify(requestData),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to initialize card authorization');
            }

            return await response.json();
        } catch (error) {
            console.error('Error initializing card authorization:', error);
            throw error;
        }
    },

    /**
     * Get all saved cards for the authenticated user
     */
    async getUserBankDetails(): Promise<FormattedCard[]> {
        try {
            console.log('=== FETCHING USER BANK DETAILS ===');

            // No need to get user ID - backend extracts it from headers
            const response = await authService.apiRequest(`/transaction/payment-methods/payer/user`);

            console.log('Response status:', response.status);
            console.log('Response status text:', response.statusText);
            console.log('Response headers:');
            for (let [key, value] of response.headers.entries()) {
                console.log(`  ${key}: ${value}`);
            }

            // Get the raw response text first
            const responseText = await response.text();
            console.log('Raw response text:', responseText);
            console.log('Raw response length:', responseText.length);
            console.log('Raw response type:', typeof responseText);

            if (!response.ok) {
                console.log('Response not OK, status:', response.status);

                let errorData;
                try {
                    errorData = responseText ? JSON.parse(responseText) : { message: 'Empty error response' };
                } catch (parseError) {
                    console.error('Failed to parse error response as JSON:', parseError);
                    errorData = { message: `Server returned: ${responseText}` };
                }

                console.log('Error data:', errorData);
                throw new Error(errorData.message || `Failed to fetch user cards (${response.status})`);
            }

            // Try to parse the JSON
            let payerDetails;
            try {
                if (!responseText || responseText.trim() === '') {
                    console.log('Empty response body, returning empty array');
                    return [];
                }

                payerDetails = JSON.parse(responseText);
                console.log('Parsed JSON successfully:', payerDetails);
                console.log('Parsed data type:', typeof payerDetails);
                console.log('Is array:', Array.isArray(payerDetails));

                if (Array.isArray(payerDetails)) {
                    console.log('Array length:', payerDetails.length);
                    if (payerDetails.length > 0) {
                        console.log('First item structure:', payerDetails[0]);
                        console.log('First item keys:', Object.keys(payerDetails[0]));
                    }
                }

            } catch (parseError) {
                console.error('JSON parse error:', parseError);
                console.error('Failed to parse response text:', responseText);
                throw new Error(`Server returned invalid JSON: ${responseText.substring(0, 200)}`);
            }

            // Ensure it's an array
            if (!Array.isArray(payerDetails)) {
                console.error('Response is not an array:', payerDetails);
                throw new Error('Server returned invalid data format (expected array)');
            }

            console.log(`Found ${payerDetails.length} payer details from server`);

            // Filter and log
            const authenticatedCards = payerDetails.filter(card => {
                console.log(`Card ${card.payerId || card.id}: hasAuthCode=${!!card.authCode}`);
                return card.authCode;
            });

            console.log(`${authenticatedCards.length} authenticated cards found`);

            // Format for UI
            const formattedCards = authenticatedCards.map((card, index) => {
                console.log(`Formatting card ${index}:`, card);
                try {
                    const formatted = this.formatCardForUI(card);
                    console.log(`Formatted card ${index}:`, formatted);
                    return formatted;
                } catch (formatError) {
                    console.error(`Error formatting card ${index}:`, formatError);
                    console.error('Card data that failed:', card);
                    throw formatError;
                }
            });

            console.log('Final formatted cards:', formattedCards);
            return formattedCards;

        } catch (error) {
            console.error('=== ERROR IN getUserBankDetails ===');
            console.error('Error type:', typeof error);
            console.error('Error message:', error instanceof Error ? error.message : error);
            console.error('Full error:', error);
            throw error;
        }
    },

    /**
     * Activate/Set a card as the active payment method
     */
    async activateBankDetails(payerId: string): Promise<PayerDetailsResponse> {
        try {
            // Note: You'll need to add this endpoint to your backend
            const response = await authService.apiRequest(
                `/transaction/payment-methods/payer/${payerId}/activate`,
                {
                    method: 'PUT',
                }
            );

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to activate card');
            }

            return await response.json();
        } catch (error) {
            console.error('Error activating card:', error);
            throw error;
        }
    },

    /**
     * Deactivate a card (soft delete)
     */
    async deleteBankDetails(payerId: string): Promise<void> {
        try {
            const response = await authService.apiRequest(
                `/transaction/payment-methods/payer/${payerId}/deactivate`,
                {
                    method: 'PUT',
                }
            );

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to deactivate card');
            }
        } catch (error) {
            console.error('Error deactivating card:', error);
            throw error;
        }
    },

    /**
     * Get the active card for the authenticated user
     */
    async getActiveBankDetails(): Promise<FormattedCard | null> {
        try {
            const cards = await this.getUserBankDetails();
            return cards.find(card => card.isActive) || null;
        } catch (error) {
            console.error('Error fetching active card:', error);
            throw error;
        }
    },

    /**
     * Format backend PayerDetails to frontend card format
     */
    formatCardForUI(payerDetail: PayerDetailsResponse): FormattedCard {
        // Helper function to safely format year
        const formatYear = (year: any): string => {
            const numYear = Number(year);
            if (isNaN(numYear) || numYear <= 0) return '**';
            return (numYear % 100).toString().padStart(2, '0');
        };

        // Helper function to safely format month
        const formatMonth = (month: any): string => {
            const numMonth = Number(month);
            if (isNaN(numMonth) || numMonth <= 0 || numMonth > 12) return '**';
            return numMonth.toString().padStart(2, '0');
        };

        return {
            id: payerDetail.payerId,
            bank: payerDetail.bank || 'Unknown Bank',
            last4Digits: payerDetail.last4 || '****',
            cardHolder: payerDetail.email, // Using email as cardholder for now
            expiryMonth: formatMonth(payerDetail.expMonth),
            expiryYear: formatYear(payerDetail.expYear),
            cardType: (payerDetail.cardType?.toLowerCase() as 'mastercard' | 'visa') || 'mastercard',
            isActive: payerDetail.isActive
        };
    },

    /**
     * Open Paystack authorization URL in browser/webview
     */
    async openPaystackAuthorization(): Promise<string> {
        try {
            // Get user info from JWT token - no API call needed!
            const userInfo = await authService.getCurrentUser();

            const response = await this.initializeCardAuthorization(userInfo.id, userInfo.email);
            console.log("==============UserID is: " + userInfo.id);
            console.log("==============User email is: " + userInfo.email);
            if (response.status && response.data?.authorization_url) {
                return response.data.authorization_url;
            } else {
                throw new Error(response.message || 'Failed to get authorization URL');
            }
        } catch (error) {
            console.error('Error opening Paystack authorization:', error);
            throw error;
        }
    },

    /**
     * Poll for card updates after Paystack callback
     * Call this after user returns from Paystack
     */
    async checkForNewCard(maxAttempts: number = 10, intervalMs: number = 2000): Promise<boolean> {
        let attempts = 0;

        const checkCards = async (): Promise<boolean> => {
            try {
                const cards = await this.getUserBankDetails();
                // Check if we have more cards than before or if any card status changed
                return cards.length > 0; // Adjust this logic based on your needs
            } catch (error) {
                console.error('Error checking for new cards:', error);
                return false;
            }
        };

        return new Promise((resolve) => {
            const interval = setInterval(async () => {
                attempts++;
                const hasNewCard = await checkCards();

                if (hasNewCard || attempts >= maxAttempts) {
                    clearInterval(interval);
                    resolve(hasNewCard);
                }
            }, intervalMs);
        });
    },

    /**
     * Format expiry date for display
     */
    formatExpiryDate(month: number, year: number): string {
        return `${month.toString().padStart(2, '0')}/${year.toString().slice(-2)}`;
    },
};

export default cardService;