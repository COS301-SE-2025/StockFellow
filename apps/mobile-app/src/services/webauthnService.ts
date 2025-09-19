// src/services/webauthnService.ts

import authService from './authService';
import { PasskeyCreateRequest, PasskeyGetRequest } from 'react-native-passkey';

// Base64URL utility functions (critical for React Native Passkey compatibility)
const base64url = {
  toBase64: (base64url: string): string => {
    let base64 = base64url.replace(/-/g, '+').replace(/_/g, '/');
    // Add padding if needed
    const padding = base64.length % 4;
    if (padding) {
      base64 += '='.repeat(4 - padding);
    }
    return base64;
  },
  
  fromBase64: (base64: string): string => {
    return base64.replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
  }
};

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

  private convertToPasskeyCreateRequest(response: RegistrationStartResponse): PasskeyCreateRequest {
    return {
      // Convert challenge from base64url to base64 for react-native-passkey
      challenge: base64url.toBase64(response.challenge),
      rp: {
        id: response.rpId,
        name: response.rpName
      },
      user: {
        // Convert user ID from base64url to base64
        id: base64url.toBase64(response.user.id),
        name: response.user.name,
        displayName: response.user.displayName
      },
      pubKeyCredParams: response.pubKeyCredParams,
      authenticatorSelection: response.authenticatorSelection,
      timeout: response.timeout,
      attestation: response.attestation
    };
  }

  private convertToPasskeyGetRequest(response: AuthenticationStartResponse): PasskeyGetRequest {
    return {
      // Convert challenge from base64url to base64
      challenge: base64url.toBase64(response.challenge),
      rpId: response.rpId,
      allowCredentials: response.allowCredentials.map(cred => ({
        ...cred,
        // Convert credential ID from base64url to base64
        id: base64url.toBase64(cred.id)
      })),
      userVerification: response.userVerification,
      timeout: response.timeout
    } as PasskeyGetRequest; 
  }

  private convertRegistrationResponse(result: any): RegistrationCompleteRequest {
    return {
      // Convert all base64 values back to base64url for the backend
      credentialId: base64url.fromBase64(result.id),
      credentialType: result.type ?? "public-key",
      clientDataJSON: base64url.fromBase64(result.response.clientDataJSON),
      attestationObject: base64url.fromBase64(result.response.attestationObject),
    };
  }

  private convertAuthenticationResponse(result: any): AuthenticationCompleteRequest {
    return {
      // Convert all base64 values back to base64url for the backend
      credentialId: base64url.fromBase64(result.id),
      credentialType: result.type ?? "public-key",
      clientDataJSON: base64url.fromBase64(result.response.clientDataJSON),
      authenticatorData: base64url.fromBase64(result.response.authenticatorData),
      signature: base64url.fromBase64(result.response.signature),
      userHandle: result.response.userHandle ? base64url.fromBase64(result.response.userHandle) : undefined,
    };
  }

  async startRegistration(request: RegistrationStartRequest): Promise<PasskeyCreateRequest> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/register/start`, {
        method: 'POST',
        body: JSON.stringify(request),
      });
      const data: ApiResponse<RegistrationStartResponse> = await response.json();
      
      if (data.success && data.data) {
        return this.convertToPasskeyCreateRequest(data.data);
      } else {
        throw new Error(data.error || 'Registration start failed');
      }
    } catch (error) {
      console.error('Error starting WebAuthn registration:', error);
      throw error;
    }
  }

  async completeRegistration(challenge: string, passkeyResult: any): Promise<{ message: string }> {
    try {
      // Convert the passkey result to the correct format
      const request = this.convertRegistrationResponse(passkeyResult);
      
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

  async startAuthentication(request: AuthenticationStartRequest): Promise<PasskeyGetRequest> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/authenticate/start`, {
        method: 'POST',
        body: JSON.stringify(request),
      });
      const data: ApiResponse<AuthenticationStartResponse> = await response.json();
      
      if (data.success && data.data) {
        return this.convertToPasskeyGetRequest(data.data);
      } else {
        throw new Error(data.error || 'Authentication start failed');
      }
    } catch (error) {
      console.error('Error starting WebAuthn authentication:', error);
      throw error;
    }
  }

  async completeAuthentication(challenge: string, passkeyResult: any): Promise<AuthenticationResponse> {
    try {
      // Convert the passkey result to the correct format
      const request = this.convertAuthenticationResponse(passkeyResult);
      
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