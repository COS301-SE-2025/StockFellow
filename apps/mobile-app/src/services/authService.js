import * as SecureStore from 'expo-secure-store';

const API_BASE_URL = 'http://10.0.2.2:3000/api';

class AuthService {
  // Get stored tokens
  async getTokens() {
    try {
      const accessToken = await SecureStore.getItemAsync('access_token');
      const refreshToken = await SecureStore.getItemAsync('refresh_token');
      return { accessToken, refreshToken };
    } catch (error) {
      console.error('Error getting tokens:', error);
      return { accessToken: null, refreshToken: null };
    }
  }

  // Save tokens to secure storage
  async saveTokens(accessToken, refreshToken) {
    try {
      await SecureStore.setItemAsync('access_token', accessToken);
      await SecureStore.setItemAsync('refresh_token', refreshToken);
    } catch (error) {
      console.error('Error saving tokens:', error);
    }
  }

  // Clear tokens from storage
  async clearTokens() {
    try {
      await SecureStore.deleteItemAsync('access_token');
      await SecureStore.deleteItemAsync('refresh_token');
    } catch (error) {
      console.error('Error clearing tokens:', error);
    }
  }

  async isAuthenticated() {
    const { accessToken } = await this.getTokens();
    return !!accessToken;
  }

  // Login with username/password
  async login(username, password) {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
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
        throw new Error(data.error || data.details || 'Login failed');
      }

      await this.saveTokens(data.access_token, data.refresh_token);

      return {
        success: true,
        data,
      };
    } catch (error) {
      console.error('Login error:', error);
      return {
        success: false,
        error: error.message || 'Login failed',
      };
    }
  }

  // Register new user
  async register(userData) {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/register`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username: userData.username,
          firstName: userData.firstName,
          lastName: userData.lastName,
          email: userData.email,
          password: userData.password,
          contactNumber: userData.contactNumber,
          idNumber: userData.idNumber,
        }),
      });

      const data = await response.json();

      if (!response.ok) {
        if (response.status === 409) {
          throw new Error('User already exists');
        }
        throw new Error(data.error || data.details || 'Registration failed');
      }

      return {
        success: true,
        data,
      };
    } catch (error) {
      console.error('Registration error:', error);
      return {
        success: false,
        error: error.message || 'Registration failed',
      };
    }
  }

  // Refresh access token
  async refreshToken() {
    try {
      const { refreshToken } = await this.getTokens();
      
      if (!refreshToken) {
        throw new Error('No refresh token available');
      }

      const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          refreshToken,
        }),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.error || 'Token refresh failed');
      }

      // Save new tokens
      await this.saveTokens(data.access_token, data.refresh_token);

      return {
        success: true,
        data,
      };
    } catch (error) {
      console.error('Token refresh error:', error);
      // Clear tokens if refresh fails
      await this.clearTokens();
      return {
        success: false,
        error: error.message || 'Token refresh failed',
      };
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

  // Make authenticated API requests
  async apiRequest(url, options = {}) {
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

      // Handle token expiration
      if (response.status === 401) {
        const errorData = await response.json();
        
        if (errorData.error === 'token_expired') {
          console.log('Token expired, attempting refresh...');
          
          // Try to refresh token
          const refreshResult = await this.refreshToken();
          
          if (refreshResult.success) {
            // Retry original request with new token
            const { accessToken: newToken } = await this.getTokens();
            headers.Authorization = `Bearer ${newToken}`;
            
            const retryResponse = await fetch(`${API_BASE_URL}${url}`, {
              ...options,
              headers,
            });
            
            return retryResponse;
          } else {
            // Refresh failed, user needs to login again
            throw new Error('Authentication failed. Please login again.');
          }
        } else {
          throw new Error('Unauthorized');
        }
      }

      return response;
    } catch (error) {
      console.error('API request failed:', error);
      throw error;
    }
  }

  // Validate current token
  async validateToken() {
    try {
      const { accessToken } = await this.getTokens();
      
      if (!accessToken) {
        return { valid: false };
      }

      const response = await fetch(`${API_BASE_URL}/auth/validate`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
        },
      });

      const data = await response.json();
      return data;
    } catch (error) {
      console.error('Token validation error:', error);
      return { valid: false };
    }
  }

  async verifyMfaCode(email, code) {
  try {
    const response = await this.apiRequest('/auth/mfa/verify', {
      method: 'POST',
      body: JSON.stringify({ email, code })
    });
    
    if (!response.ok) {
      const error = await response.json();
      return { success: false, error: error.message };
    }
    
    const data = await response.json();
    // Store tokens or session as needed
    return { success: true };
  } catch (error) {
    return { success: false, error: error.message };
  }
};

async resendMfaCode(email) {
  try {
    const response = await this.apiRequest('/auth/mfa/resend', {
      method: 'POST',
      body: JSON.stringify({ email })
    });
    
    if (!response.ok) {
      throw new Error('Failed to resend code');
    }
    
    return { success: true };
  } catch (error) {
    return { success: false, error: error.message };
  }
};

}


export default new AuthService();