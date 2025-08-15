import * as SecureStore from 'expo-secure-store';

const API_BASE_URL = 'http://10.0.2.2:3000/api';

interface UserInfo {
  id: string;
  email: string;
  username: string;
  firstName: string;
  lastName: string;
  contactNumber?: string;
  idNumber?: string;
}

class AuthService {
  private userCache: UserInfo | null = null;

  // Helper function to safely get error message
  private getErrorMessage(error: unknown): string {
    if (error instanceof Error) {
      return error.message;
    }
    if (typeof error === 'string') {
      return error;
    }
    return 'Unknown error occurred';
  }

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

  // Decode JWT token to extract user information
  private decodeJWT(token: string): any {
    try {
      // JWT has 3 parts separated by dots: header.payload.signature
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (error) {
      console.error('Error decoding JWT:', error);
      return null;
    }
  }

  // Get current user info from JWT token
  async getCurrentUser(): Promise<{id: string, email: string}> {
    try {
      // Return cached user if available
      if (this.userCache) {
        return {
          id: this.userCache.id,
          email: this.userCache.email
        };
      }

      // Try to get from secure storage first
      const storedUser = await SecureStore.getItemAsync('user_info');
      if (storedUser) {
        const userInfo = JSON.parse(storedUser);
        this.userCache = userInfo;
        return {
          id: userInfo.id,
          email: userInfo.email
        };
      }

      // Extract from JWT token
      const { accessToken } = await this.getTokens();
      if (!accessToken) {
        throw new Error('No access token available');
      }

      const decodedToken = this.decodeJWT(accessToken);
      if (!decodedToken) {
        throw new Error('Failed to decode access token');
      }

      // Extract user info from token payload
      // Adjust these field names based on your JWT structure
      const userInfo: UserInfo = {
        id: decodedToken.sub || decodedToken.userId || decodedToken.id,
        email: decodedToken.email,
        username: decodedToken.username || decodedToken.preferred_username,
        firstName: decodedToken.firstName || decodedToken.given_name,
        lastName: decodedToken.lastName || decodedToken.family_name,
        contactNumber: decodedToken.contactNumber || decodedToken.phone_number,
        idNumber: decodedToken.idNumber
      };

      // Validate required fields
      if (!userInfo.id || !userInfo.email) {
        throw new Error('Invalid token: missing required user information');
      }

      // Cache the user info
      this.userCache = userInfo;
      await SecureStore.setItemAsync('user_info', JSON.stringify(userInfo));
      
      return {
        id: userInfo.id,
        email: userInfo.email
      };
    } catch (error) {
      console.error('Error getting current user:', error);
      throw new Error(`Unable to get current user information: ${this.getErrorMessage(error)}`);
    }
  }

  // Get current user ID only
  async getCurrentUserId(): Promise<string> {
    try {
      const user = await this.getCurrentUser();
      return user.id;
    } catch (error) {
      console.error('Error getting current user ID:', error);
      throw new Error(`Unable to get current user ID: ${this.getErrorMessage(error)}`);
    }
  }

  // Get current username
  async getCurrentUsername(): Promise<string> {
    try {
      const userInfo = await this.getCurrentUserFull();
      if (!userInfo.username) {
        throw new Error('Username not available in token');
      }
      return userInfo.username;
    } catch (error) {
      console.error('Error getting current username:', error);
      throw new Error(`Unable to get current username: ${this.getErrorMessage(error)}`);
    }
  }

  // Get full user info (extended version)
  async getCurrentUserFull(): Promise<UserInfo> {
    try {
      // Return cached user if available
      if (this.userCache) {
        return this.userCache;
      }

      // Try to get from secure storage first
      const storedUser = await SecureStore.getItemAsync('user_info');
      if (storedUser) {
        const userInfo = JSON.parse(storedUser);
        this.userCache = userInfo;
        return userInfo;
      }

      // Extract from JWT token
      const { accessToken } = await this.getTokens();
      if (!accessToken) {
        throw new Error('No access token available');
      }

      const decodedToken = this.decodeJWT(accessToken);
      if (!decodedToken) {
        throw new Error('Failed to decode access token');
      }

      // Extract user info from token payload
      const userInfo: UserInfo = {
        id: decodedToken.sub || decodedToken.userId || decodedToken.id,
        email: decodedToken.email,
        username: decodedToken.username || decodedToken.preferred_username,
        firstName: decodedToken.firstName || decodedToken.given_name,
        lastName: decodedToken.lastName || decodedToken.family_name,
        contactNumber: decodedToken.contactNumber || decodedToken.phone_number,
        idNumber: decodedToken.idNumber
      };

      // Validate required fields
      if (!userInfo.id || !userInfo.email) {
        throw new Error('Invalid token: missing required user information');
      }

      // Cache the user info
      this.userCache = userInfo;
      await SecureStore.setItemAsync('user_info', JSON.stringify(userInfo));
      
      return userInfo;
    } catch (error) {
      console.error('Error getting full user info:', error);
      throw new Error(`Unable to get user information: ${this.getErrorMessage(error)}`);
    }
  }

  // Save tokens to secure storage
  async saveTokens(accessToken: string, refreshToken: string) {
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
      await SecureStore.deleteItemAsync('user_info');
      this.userCache = null; // Clear cache
    } catch (error) {
      console.error('Error clearing tokens:', error);
    }
  }

  async isAuthenticated() {
    const { accessToken } = await this.getTokens();
    return !!accessToken;
  }

  // Login with username/password
  async login(username: string, password: string) {
    try {
      const requestBody = {
        username,
        password,
      };
      
      const headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'User-Agent': 'StockFellow-Mobile-App', // Add custom user agent
      };
      
      console.log('=== LOGIN DEBUG INFO ===');
      console.log('API_BASE_URL:', API_BASE_URL);
      console.log('Full URL:', `${API_BASE_URL}/auth/login`);
      console.log('Request headers:', headers);
      console.log('Request body:', JSON.stringify(requestBody));
      console.log('Username length:', username.length);
      console.log('Password length:', password.length);
      
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers,
        body: JSON.stringify(requestBody),
      });

      console.log('Response status:', response.status);
      console.log('Response status text:', response.statusText);
      console.log('Response URL:', response.url);
      
      // Log response headers
      for (let [key, value] of response.headers.entries()) {
        console.log(`Response header ${key}:`, value);
      }

      // Check if response has content
      const responseText = await response.text();
      console.log('Raw response length:', responseText.length);
      console.log('Raw response:', responseText);

      let data;
      try {
        data = responseText ? JSON.parse(responseText) : {};
      } catch (parseError) {
        console.error('JSON parse error:', parseError);
        console.error('Response text was:', responseText);
        throw new Error(`Server returned invalid JSON: ${responseText.substring(0, 100)}`);
      }

      if (!response.ok) {
        console.error('Login failed with data:', data);
        throw new Error(data.error || data.details || data.message || `Login failed with status ${response.status}`);
      }

      // Check if we have the required tokens
      if (!data.access_token || !data.refresh_token) {
        console.error('Missing tokens in response:', data);
        throw new Error('Server response missing required tokens');
      }

      await this.saveTokens(data.access_token, data.refresh_token);

      // If user info is returned in login response, cache it
      if (data.user) {
        this.userCache = data.user;
        await SecureStore.setItemAsync('user_info', JSON.stringify(data.user));
      }

      console.log('Login successful!');
      return {
        success: true,
        data,
      };
    } catch (error) {
      console.error('Login error:', error);
      return {
        success: false,
        error: this.getErrorMessage(error) || 'Login failed',
      };
    }
  }

  // Register new user
  async register(userData: any) {
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
        error: this.getErrorMessage(error) || 'Registration failed',
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
        error: this.getErrorMessage(error) || 'Token refresh failed',
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
      // Always clear local tokens and cache
      await this.clearTokens();
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
      console.log('API Request to:', `${API_BASE_URL}${url}`);
      
      const response = await fetch(`${API_BASE_URL}${url}`, {
        ...options,
        headers,
      });

      console.log('API Response status:', response.status);

      // Handle token expiration
      if (response.status === 401) {
        // Get response text for better error handling
        const responseText = await response.text();
        let errorData;
        
        try {
          errorData = responseText ? JSON.parse(responseText) : { error: 'Unauthorized' };
        } catch {
          errorData = { error: 'Unauthorized' };
        }
        
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

  async verifyMfaCode(email: string, code: string) {
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
      return { success: false, error: this.getErrorMessage(error) };
    }
  }

  async resendMfaCode(email: string) {
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
      return { success: false, error: this.getErrorMessage(error) };
    }
  }

  // Clear user cache (useful for testing or force refresh)
  clearUserCache() {
    this.userCache = null;
  }

  // Force refresh user info from API
  async refreshUserInfo(): Promise<UserInfo> {
    this.userCache = null;
    await SecureStore.deleteItemAsync('user_info');
    return await this.getCurrentUserFull();
  }
}

export default new AuthService();