// src/services/webAuthnService.ts

import authService from './authService';

// Request & Response interfaces
interface RegistrationStartRequest {
  userId: string;
  username: string;
  authenticatorName?: string;
}

interface RegistrationStartResponse {
  challenge: string;
  rpId: string;
  rpName: string;
  timeout: number;
  attestation: string;
  user: {
    id: string;
    name: string;
    displayName: string;
  };
  pubKeyCredParams: Array<{
    type: string;
    alg: number;
  }>;
  authenticatorSelection: {
    authenticatorAttachment: string;
    userVerification: string;
    requireResidentKey: boolean;
  };
}

interface RegistrationCompleteRequest {
  credentialId: string;
  credentialType: string;
  clientDataJSON: string;
  attestationObject: string;
  authenticatorName?: string;
}

interface AuthenticationStartRequest {
  username: string;
}

interface AuthenticationStartResponse {
  challenge: string;
  rpId: string;
  timeout: number;
  userVerification: string;
  allowCredentials: Array<{
    type: string;
    id: string;
    transports: string[];
  }>;
}

interface AuthenticationCompleteRequest {
  credentialId: string;
  credentialType: string;
  clientDataJSON: string;
  authenticatorData: string;
  signature: string;
  userHandle?: string;
}

interface AuthenticationResponse {
  token: string;
  userId: string;
  username: string;
  tokenType: string;
  expiresIn: number;
}

interface WebAuthnCredential {
  id: string;
  userId: string;
  userLabel: string;
  credentialData: string;
  secretData: string;
}

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data?: T;
  error?: string;
}

class WebAuthnService {
  private baseUrl = '/webauthn';

  async getServiceInfo(): Promise<any> {
    try {
      const response = await authService.apiRequest(this.baseUrl);
      return await response.json();
    } catch (error) {
      console.error('Error getting WebAuthn service info:', error);
      throw error;
    }
  }

  async startRegistration(request: RegistrationStartRequest): Promise<RegistrationStartResponse> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/register/start`, {
        method: 'POST',
        body: JSON.stringify(request),
      });
      const data: ApiResponse<RegistrationStartResponse> = await response.json();
      
      if (data.success && data.data) {
        return data.data;
      } else {
        throw new Error(data.error || 'Registration start failed');
      }
    } catch (error) {
      console.error('Error starting WebAuthn registration:', error);
      throw error;
    }
  }

  async completeRegistration(challenge: string, request: RegistrationCompleteRequest): Promise<{ message: string }> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/register/complete/${challenge}`, {
        method: 'POST',
        body: JSON.stringify(request),
      });
      const data: ApiResponse<string> = await response.json();
      
      if (data.success) {
        return { message: data.message };
      } else {
        throw new Error(data.error || 'Registration completion failed');
      }
    } catch (error) {
      console.error('Error completing WebAuthn registration:', error);
      throw error;
    }
  }

  async startAuthentication(request: AuthenticationStartRequest): Promise<AuthenticationStartResponse> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/authenticate/start`, {
        method: 'POST',
        body: JSON.stringify(request),
      });
      const data: ApiResponse<AuthenticationStartResponse> = await response.json();
      
      if (data.success && data.data) {
        return data.data;
      } else {
        throw new Error(data.error || 'Authentication start failed');
      }
    } catch (error) {
      console.error('Error starting WebAuthn authentication:', error);
      throw error;
    }
  }

  async completeAuthentication(challenge: string, request: AuthenticationCompleteRequest): Promise<AuthenticationResponse> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/authenticate/complete/${challenge}`, {
        method: 'POST',
        body: JSON.stringify(request),
      });
      const data: ApiResponse<AuthenticationResponse> = await response.json();
      
      if (data.success && data.data) {
        return data.data;
      } else {
        throw new Error(data.error || 'Authentication completion failed');
      }
    } catch (error) {
      console.error('Error completing WebAuthn authentication:', error);
      throw error;
    }
  }

  async getUserCredentials(userId: string): Promise<WebAuthnCredential[]> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/credentials/${userId}`);
      const data: ApiResponse<WebAuthnCredential[]> = await response.json();
      
      if (data.success && data.data) {
        return data.data;
      } else {
        throw new Error(data.error || 'Failed to fetch credentials');
      }
    } catch (error) {
      console.error('Error getting user credentials:', error);
      throw error;
    }
  }

  async hasCredentials(userId: string): Promise<boolean> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/credentials/${userId}/exists`);
      const data: ApiResponse<boolean> = await response.json();
      
      if (data.success && data.data !== undefined) {
        return data.data;
      } else {
        throw new Error(data.error || 'Failed to check credentials');
      }
    } catch (error) {
      console.error('Error checking credentials:', error);
      throw error;
    }
  }

  async deleteCredential(userId: string, credentialId: string): Promise<{ message: string }> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/credentials/${userId}/${credentialId}`, {
        method: 'DELETE',
      });
      const data: ApiResponse<string> = await response.json();
      
      if (data.success) {
        return { message: data.message };
      } else {
        throw new Error(data.error || 'Failed to delete credential');
      }
    } catch (error) {
      console.error('Error deleting credential:', error);
      throw error;
    }
  }
}

const webAuthnService = new WebAuthnService();
export default webAuthnService;