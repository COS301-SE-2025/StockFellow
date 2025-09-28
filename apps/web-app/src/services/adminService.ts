// src/services/adminService.ts
import axios, { AxiosRequestConfig, AxiosResponse } from 'axios';

// Types for API responses
interface DashboardSummary {
  groupMetrics: {
    activeGroups: number;
    totalGroups: number;
    error?: string;
  };
  transactionMetrics: {
    totalTransactions: number;
    successfulTransactions: number;
    error?: string;
  };
  userMetrics: {
    totalUsers: number;
    verifiedUsers: number;
    error?: string;
  };
  revenueData: {
    period: string;
    projectedMonthly: number;
    totalRevenue: number;
    dailyBreakdown: Array<{
      date: string;
      revenue: number;
    }>;
  };
  pendingRequestsCount: number;
  recentSuspiciousActivity: any[];
  staleRequests: any[];
  generatedAt: string;
  dataFreshness: string;
}

interface AnalyticsDashboard {
  groupMetrics: {
    activeGroups: number;
    totalGroups: number;
    error?: string;
  };
  transactionMetrics: {
    totalTransactions: number;
    successfulTransactions: number;
    error?: string;
  };
  userMetrics: {
    totalUsers: number;
    verifiedUsers: number;
    error?: string;
  };
  growthTrends: Array<{
    date: string;
    volume: number;
    newUsers: number;
    newGroups: number;
    activeUsers: number;
    transactions: number;
  }>;
  timeRange: string;
  generatedAt: string;
}

interface RevenueAnalytics {
  period: string;
  projectedMonthly: number;
  totalRevenue: number;
  dailyBreakdown: Array<{
    date: string;
    revenue: number;
  }>;
}

// Token management with debugging
class TokenManager {
  private static readonly TOKEN_KEY = 'admin_access_token';
  private static readonly REFRESH_TOKEN_KEY = 'admin_refresh_token';
  private static readonly TOKEN_EXPIRY_KEY = 'admin_token_expiry';

  static setToken(token: string, refreshToken?: string, expiresIn?: number): void {
    console.log('Setting token:', { 
      tokenLength: token?.length, 
      hasRefreshToken: !!refreshToken, 
      expiresIn 
    });
    
    localStorage.setItem(this.TOKEN_KEY, token);
    
    if (refreshToken) {
      localStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken);
    }
    
    if (expiresIn) {
      const expiryTime = Date.now() + (expiresIn * 1000);
      localStorage.setItem(this.TOKEN_EXPIRY_KEY, expiryTime.toString());
      console.log('Token expires at:', new Date(expiryTime).toISOString());
    }
  }

  static getToken(): string | null {
    const token = localStorage.getItem(this.TOKEN_KEY);
    const expiry = localStorage.getItem(this.TOKEN_EXPIRY_KEY);
    
    console.log('Getting token:', { 
      hasToken: !!token, 
      tokenLength: token?.length,
      expiry: expiry ? new Date(parseInt(expiry)).toISOString() : null,
      isExpired: expiry ? Date.now() > parseInt(expiry) : false
    });
    
    if (!token) return null;
    
    if (expiry && Date.now() > parseInt(expiry)) {
      console.log('Token expired, clearing tokens');
      this.clearTokens();
      return null;
    }
    
    return token;
  }

  static getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  static clearTokens(): void {
    console.log('Clearing all tokens');
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.TOKEN_EXPIRY_KEY);
  }

  static isTokenExpiringSoon(): boolean {
    const expiry = localStorage.getItem(this.TOKEN_EXPIRY_KEY);
    if (!expiry) return false;
    
    const expiryTime = parseInt(expiry);
    const fiveMinutesFromNow = Date.now() + (5 * 60 * 1000);
    
    return expiryTime < fiveMinutesFromNow;
  }

  // Debug method to inspect token payload
  static debugToken(): void {
    const token = this.getToken();
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        console.log('Token payload:', payload);
        console.log('Token roles:', payload.realm_access?.roles);
        console.log('Token issuer:', payload.iss);
        console.log('Token expiry:', new Date(payload.exp * 1000).toISOString());
      } catch (e) {
        console.error('Failed to decode token:', e);
      }
    } else {
      console.log('No token found');
    }
  }
}

// HTTP client with authentication
class AdminHttpClient {
  private baseURL: string;
  private refreshPromise: Promise<string> | null = null;

  constructor(baseURL: string = 'http://localhost:4060') {
    this.baseURL = baseURL;
  }

  private async getAuthHeaders(): Promise<{ Authorization?: string; 'Content-Type': string }> {
    const token = TokenManager.getToken();
    const headers: { Authorization?: string; 'Content-Type': string } = {
      'Content-Type': 'application/json'
    };
    
    if (token) {
      headers.Authorization = `Bearer ${token}`;
      console.log('Added Authorization header:', `Bearer ${token.substring(0, 20)}...`);
    } else {
      console.warn('No token available for request');
    }
    
    return headers;
  }

  private async refreshTokenIfNeeded(): Promise<void> {
    if (!TokenManager.isTokenExpiringSoon()) return;
    
    if (this.refreshPromise) {
      await this.refreshPromise;
      return;
    }

    const refreshToken = TokenManager.getRefreshToken();
    if (!refreshToken) throw new Error('No refresh token available');

    this.refreshPromise = this.performTokenRefresh(refreshToken);
    
    try {
      await this.refreshPromise;
    } finally {
      this.refreshPromise = null;
    }
  }

  private async performTokenRefresh(refreshToken: string): Promise<string> {
    try {
      console.log('Refreshing token...');
      const response = await axios.post(`${this.baseURL}/api/admin/auth/refresh`, {
        refreshToken
      });
      
      const { access_token, refresh_token, expires_in } = response.data;
      TokenManager.setToken(access_token, refresh_token || refreshToken, expires_in);
      
      console.log('Token refreshed successfully');
      return access_token;
    } catch (error: any) {
      console.error('Token refresh failed:', error.response?.data || error.message);
      TokenManager.clearTokens();
      throw new Error('Token refresh failed');
    }
  }

  async request<T>(config: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    await this.refreshTokenIfNeeded();
    
    const authHeaders = await this.getAuthHeaders();
    
    const requestConfig: AxiosRequestConfig = {
      ...config,
      baseURL: this.baseURL,
      headers: {
        ...authHeaders,
        ...config.headers,
      },
    };

    console.log('Making request:', {
      method: requestConfig.method,
      url: requestConfig.url,
      baseURL: requestConfig.baseURL,
      hasAuth: !!requestConfig.headers?.Authorization
    });

    try {
      const response = await axios(requestConfig);
      console.log('Request successful:', response.status, response.statusText);
      return response;
    } catch (error: any) {
      console.error('Request failed:', {
        status: error.response?.status,
        statusText: error.response?.statusText,
        data: error.response?.data,
        url: `${requestConfig.baseURL}${requestConfig.url}`
      });

      if (error.response?.status === 401) {
        console.log('Authentication failed, debugging token...');
        TokenManager.debugToken();
        
        // Try refresh token once
        const refreshToken = TokenManager.getRefreshToken();
        if (refreshToken && !this.refreshPromise) {
          try {
            await this.performTokenRefresh(refreshToken);
            // Retry the original request
            const retryHeaders = await this.getAuthHeaders();
            const retryConfig = {
              ...requestConfig,
              headers: {
                ...retryHeaders,
                ...config.headers,
              }
            };
            console.log('Retrying request with new token...');
            return await axios(retryConfig);
          } catch (refreshError) {
            console.error('Retry after refresh failed:', refreshError);
          }
        }
        
        TokenManager.clearTokens();
        window.location.href = '/admin/login';
        throw new Error('Authentication required');
      }
      throw error;
    }
  }

  async get<T>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.request<T>({ ...config, method: 'GET', url });
  }

  async post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.request<T>({ ...config, method: 'POST', url, data });
  }

  async put<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.request<T>({ ...config, method: 'PUT', url, data });
  }

  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.request<T>({ ...config, method: 'DELETE', url });
  }
}

// Admin service class
class AdminService {
  private client: AdminHttpClient;

  constructor() {
    this.client = new AdminHttpClient();
  }

  // Authentication
  async login(username: string, password: string): Promise<void> {
    try {
      console.log('Attempting login for username:', username);
      
      const requestBody = {
        username: username.trim(),
        password: password.trim()
      };
      
      console.log('Sending login request to:', 'http://localhost:4060/api/admin/auth/login');
      
      const response = await axios.post('http://localhost:4060/api/admin/auth/login', requestBody, {
        headers: {
          'Content-Type': 'application/json'
        }
      });
      
      console.log('Login response:', response.data);
      
      const { access_token, refresh_token, expires_in } = response.data;
      TokenManager.setToken(access_token, refresh_token, expires_in);
      
      console.log(TokenManager.getToken() ? 'Login successful, token stored' : 'Login failed, no token received');
      
    } catch (error: any) {
      console.error('Login request failed:', {
        status: error.response?.status,
        statusText: error.response?.statusText,
        data: error.response?.data,
        message: error.message
      });
      
      // Enhanced error message based on response
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message);
      } else if (error.response?.status === 401) {
        throw new Error('Invalid username or password');
      } else if (error.response?.status >= 500) {
        throw new Error('Authentication service temporarily unavailable');
      } else {
        throw new Error('Login failed');
      }
    }
  }

  async logout(): Promise<void> {
    try {
      const refreshToken = TokenManager.getRefreshToken();
      if (refreshToken) {
        await axios.post('http://localhost:4060/api/admin/auth/logout', {
          refreshToken
        });
      }
    } catch (error) {
      console.warn('Backend logout failed, but clearing local tokens:', error);
    } finally {
      TokenManager.clearTokens();
      window.location.href = '/admin/login';
    }
  }

  isAuthenticated(): boolean {
    const token = TokenManager.getToken();
    const isAuthenticated = token !== null;
    console.log('Authentication check:', { isAuthenticated, hasToken: !!token });
    return isAuthenticated;
  }

  // Dashboard endpoints
  async getDashboardSummary(): Promise<DashboardSummary> {
    console.log('Fetching dashboard summary...');
    const response = await this.client.get<DashboardSummary>('/api/admin/dashboard/summary');
    console.log('Dashboard summary response:', response.data);
    return response.data;
  }


  async getAnalyticsDashboard(timeRange: '7d' | '30d' = '7d'): Promise<AnalyticsDashboard> {
    const response = await this.client.get<AnalyticsDashboard>(
      `/api/admin/analytics/dashboard?timeRange=${timeRange}`
    );
    console.log('Analytics dashboard response:', response.data);
    return response.data;
  }

  async getRevenueAnalytics(period: '30d' | '90d' = '30d'): Promise<RevenueAnalytics> {
    const response = await this.client.get<RevenueAnalytics>(
      `/api/admin/analytics/revenue?period=${period}`
    );
    return response.data;
  }

  // Individual metrics
  async getUserStats(): Promise<any> {
    const response = await this.client.get('/api/admin/analytics/users/stats');
    return response.data;
  }

  async getGroupStats(): Promise<any> {
    const response = await this.client.get('/api/admin/analytics/groups/stats');
    return response.data;
  }

  async getTransactionStats(): Promise<any> {
    const response = await this.client.get('/api/admin/analytics/transactions/stats');
    return response.data;
  }

  // Audit endpoints
  async getAuditLogs(params?: {
    userId?: string;
    endpoint?: string;
    startDate?: string;
    endDate?: string;
    flaggedOnly?: boolean;
    page?: number;
    size?: number;
  }): Promise<any> {
    const queryParams = new URLSearchParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined) {
          queryParams.append(key, value.toString());
        }
      });
    }
    
    const response = await this.client.get(`/api/admin/audit/logs?${queryParams}`);
    return response.data;
  }

  async getSuspiciousActivity(): Promise<any> {
    const response = await this.client.get('/api/admin/audit/fraud/suspicious');
    return response.data;
  }

  async markForInvestigation(logId: string, reason: string): Promise<any> {
    const response = await this.client.post('/api/admin/audit/fraud/investigate', {
      logId,
      reason
    });
    return response.data;
  }

  // Request management
  async getPendingRequests(params?: {
    requestType?: string;
    page?: number;
    size?: number;
  }): Promise<any> {
    const queryParams = new URLSearchParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined) {
          queryParams.append(key, value.toString());
        }
      });
    }
    
    const response = await this.client.get(`/api/admin/requests/pending?${queryParams}`);
    return response.data;
  }

  async approveRequest(requestId: string, adminNotes?: string): Promise<any> {
    const response = await this.client.post(`/api/admin/requests/${requestId}/approve`, {
      adminNotes
    });
    return response.data;
  }

  async rejectRequest(requestId: string, adminNotes?: string): Promise<any> {
    const response = await this.client.post(`/api/admin/requests/${requestId}/reject`, {
      adminNotes
    });
    return response.data;
  }

  async getRequestDetails(requestId: string): Promise<any> {
    const response = await this.client.get(`/api/admin/requests/${requestId}/details`);
    return response.data;
  }
}

// Export singleton instance
export const adminService = new AdminService();
export { TokenManager };
export type { DashboardSummary, AnalyticsDashboard, RevenueAnalytics };