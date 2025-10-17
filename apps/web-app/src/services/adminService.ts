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


 /**
   * Mark a log entry for investigation (updated method name to match usage)
   */
  async markLogForInvestigation(logId: string, reason: string): Promise<{
    success: boolean;
    message: string;
    logId: string;
  }> {
    console.log('Marking log for investigation:', { logId, reason });
    
    try {
      const response = await this.client.post<{
        success: boolean;
        message: string;
        logId: string;
      }>('/api/admin/audit/fraud/investigate', {
        logId,
        reason
      });
      
      console.log('Investigation response:', response.data);
      return response.data;
    } catch (error: any) {
      console.error('Error marking log for investigation:', error);
      
      // Handle specific error cases
      if (error.response?.status === 400 && error.response?.data?.message?.includes('UUID')) {
        throw new Error('Invalid log ID format. Please ensure the log ID is valid.');
      }
      
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message);
      }
      
      if (error.response?.data?.error) {
        throw new Error(`${error.response.data.error}: ${error.response.data.message || 'Failed to mark log for investigation'}`);
      }
      
      throw new Error('Failed to mark log for investigation');
    }
  }

  /**
   * Get user activity for a specific user
   */
  async getUserActivity(userId: string): Promise<{
    userId: string;
    activities: any[];
    count: number;
    period: string;
  }> {
    console.log(`Fetching activity for user: ${userId}`);
    
    try {
      const response = await this.client.get<{
        userId: string;
        activities: any[];
        count: number;
        period: string;
      }>(`/api/admin/audit/user/${encodeURIComponent(userId)}/activity`);
      
      console.log('User activity response:', response.data);
      
      return {
        userId: response.data.userId || userId,
        activities: response.data.activities || [],
        count: response.data.count || 0,
        period: response.data.period || 'Last 30 days'
      };
    } catch (error: any) {
      console.error(`Error fetching user activity for ${userId}:`, error);
      
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message);
      }
      
      throw new Error(`Failed to fetch activity for user ${userId}`);
    }
  }




  // // Request management
  // async getPendingRequests(params?: {
  //   requestType?: string;
  //   page?: number;
  //   size?: number;
  // }): Promise<any> {
  //   const queryParams = new URLSearchParams();
  //   if (params) {
  //     Object.entries(params).forEach(([key, value]) => {
  //       if (value !== undefined) {
  //         queryParams.append(key, value.toString());
  //       }
  //     });
  //   }
    
  //   const response = await this.client.get(`/api/admin/requests/pending?${queryParams}`);
  //   return response.data;
  // }

  // async approveRequest(requestId: string, adminNotes?: string): Promise<any> {
  //   const response = await this.client.post(`/api/admin/requests/${requestId}/approve`, {
  //     adminNotes
  //   });
  //   return response.data;
  // }

  // async rejectRequest(requestId: string, adminNotes?: string): Promise<any> {
  //   const response = await this.client.post(`/api/admin/requests/${requestId}/reject`, {
  //     adminNotes
  //   });
  //   return response.data;
  // }

  // async getRequestDetails(requestId: string): Promise<any> {
  //   const response = await this.client.get(`/api/admin/requests/${requestId}/details`);
  //   return response.data;
  // }

  // Request management methods with proper typing

  /**
   * Get pending requests with enhanced typing
   */
  async getPendingRequests(params?: {
    requestType?: string;
    page?: number;
    size?: number;
  }): Promise<{
    content: any[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
  }> {
    const queryParams = new URLSearchParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined) {
          queryParams.append(key, value.toString());
        }
      });
    }
    
    console.log('Fetching pending requests with params:', params);
    
    try {
      const response = await this.client.get<{
        content: any[];
        totalElements: number;
        totalPages: number;
        size: number;
        number: number;
        first: boolean;
        last: boolean;
      }>(`/api/admin/requests/pending?${queryParams}`);
      
      console.log('Pending requests response:', response.data);
      return response.data;
    } catch (error: any) {
      console.error('Error fetching pending requests:', error);
      
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message);
      }
      
      throw new Error('Failed to fetch pending requests');
    }
  }

  /**
   * Approve a request with proper error handling
   */
  async approveRequest(requestId: string, adminNotes?: string): Promise<{
    success: boolean;
    message: string;
    request: any;
  }> {
    console.log('Approving request:', { requestId, adminNotes });
    
    try {
      const response = await this.client.post<{
        success: boolean;
        message: string;
        request: any;
      }>(`/api/admin/requests/${requestId}/approve`, {
        adminNotes: adminNotes || ''
      });
      
      console.log('Approval response:', response.data);
      return response.data;
    } catch (error: any) {
      console.error('Error approving request:', error);
      
      if (error.response?.status === 404) {
        throw new Error('Request not found. It may have been deleted or processed already.');
      }
      
      if (error.response?.status === 400) {
        if (error.response?.data?.message?.includes('already been processed')) {
          throw new Error('This request has already been processed.');
        }
        throw new Error(error.response.data.message || 'Invalid request data.');
      }
      
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message);
      }
      
      if (error.response?.data?.error) {
        throw new Error(`${error.response.data.error}: ${error.response.data.message || 'Failed to approve request'}`);
      }
      
      throw new Error('Failed to approve request');
    }
  }

  /**
   * Reject a request with proper error handling
   */
  async rejectRequest(requestId: string, adminNotes?: string): Promise<{
    success: boolean;
    message: string;
    request: any;
  }> {
    console.log('Rejecting request:', { requestId, adminNotes });
    
    // Require admin notes for rejection
    if (!adminNotes || !adminNotes.trim()) {
      throw new Error('Admin notes are required when rejecting a request');
    }
    
    try {
      const response = await this.client.post<{
        success: boolean;
        message: string;
        request: any;
      }>(`/api/admin/requests/${requestId}/reject`, {
        adminNotes: adminNotes.trim()
      });
      
      console.log('Rejection response:', response.data);
      return response.data;
    } catch (error: any) {
      console.error('Error rejecting request:', error);
      
      if (error.response?.status === 404) {
        throw new Error('Request not found. It may have been deleted or processed already.');
      }
      
      if (error.response?.status === 400) {
        if (error.response?.data?.message?.includes('already been processed')) {
          throw new Error('This request has already been processed.');
        }
        throw new Error(error.response.data.message || 'Invalid request data.');
      }
      
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message);
      }
      
      if (error.response?.data?.error) {
        throw new Error(`${error.response.data.error}: ${error.response.data.message || 'Failed to reject request'}`);
      }
      
      throw new Error('Failed to reject request');
    }
  }

  /**
   * Get detailed information about a specific request
   */
  async getRequestDetails(requestId: string): Promise<{
    request: any;
    user?: any;
    group?: any;
    userError?: string;
    groupError?: string;
  }> {
    console.log('Fetching request details for:', requestId);
    
    try {
      const response = await this.client.get<{
        request: any;
        user?: any;
        group?: any;
        userError?: string;
        groupError?: string;
      }>(`/api/admin/requests/${requestId}/details`);
      
      console.log('Request details response:', response.data);
      return response.data;
    } catch (error: any) {
      console.error('Error fetching request details:', error);
      
      if (error.response?.status === 404) {
        throw new Error('Request not found. The request may have been deleted or the ID is invalid.');
      }
      
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message);
      }
      
      if (error.response?.data?.error) {
        throw new Error(`${error.response.data.error}: ${error.response.data.message || 'Failed to fetch request details'}`);
      }
      
      throw new Error('Failed to fetch request details');
    }
  }
}

// Export singleton instance
export const adminService = new AdminService();
export { TokenManager };
export type { DashboardSummary, AnalyticsDashboard, RevenueAnalytics };