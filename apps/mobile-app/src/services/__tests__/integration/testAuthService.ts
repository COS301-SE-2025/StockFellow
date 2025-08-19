// Simple in-memory token storage for testing
const tokenStorage = {
    accessToken: null as string | null,
    refreshToken: null as string | null,
};

const API_BASE_URL = process.env.API_BASE_URL || 'http://10.0.2.2:3000/api';

class TestAuthService {
    // Get stored tokens
    async getTokens() {
        return {
            accessToken: tokenStorage.accessToken,
            refreshToken: tokenStorage.refreshToken,
        };
    }

    // Save tokens to memory
    async saveTokens(accessToken: string, refreshToken: string) {
        tokenStorage.accessToken = accessToken;
        tokenStorage.refreshToken = refreshToken;
    }

    // Clear tokens from memory
    async clearTokens() {
        tokenStorage.accessToken = null;
        tokenStorage.refreshToken = null;
    }

    async isAuthenticated() {
        return !!tokenStorage.accessToken;
    }

    // Test login method
    async testLogin(username: string, password: string) {
        try {
            const response = await fetch(`${API_BASE_URL}/auth/test/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    username,
                    password,
                }),
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error || data.details || 'Test login failed');
            }

            // Save tokens if successful
            await this.saveTokens(data.access_token, data.refresh_token);

            return {
                success: true,
                data,
            };
        } catch (error) {
            console.error('Test login error:', error);

            // Handle the 'unknown' error type safely
            let errorMessage = 'Test login failed';
            if (error instanceof Error) {
                errorMessage = error.message;
            } else if (typeof error === 'string') {
                errorMessage = error;
            }

            return {
                success: false,
                error: errorMessage,
            };
        }
    }

    // Make authenticated API requests
    async apiRequest(url: string, options: any = {}) {
        const { accessToken } = await this.getTokens();

        const headers = {
            'Content-Type': 'application/json',
            ...options.headers,
        };

        if (accessToken) {
            headers.Authorization = `Bearer ${accessToken}`;
        }

        try {
            const response = await fetch(`${API_BASE_URL}${url}`, {
                ...options,
                headers,
            });

            return response;
        } catch (error) {
            console.error('API request failed:', error);
            throw error;
        }
    }

    // Logout user
    async logout() {
        try {
            const { accessToken } = await this.getTokens();

            if (accessToken) {
                // Call logout endpoint to blacklist token
                await fetch(`${API_BASE_URL}/auth/logout`, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${accessToken}`,
                    },
                });
            }
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            // Always clear local tokens
            await this.clearTokens();
        }
    }
}

export default new TestAuthService();