// src/utils/errorHandler.ts
export class AuthError extends Error {
  constructor(
    message: string,
    public code?: string,
    public statusCode?: number
  ) {
    super(message);
    this.name = 'AuthError';
  }
}

export class NetworkError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'NetworkError';
  }
}

export function handleAuthError(error: any): string {
  // Handle axios errors
  if (error.response) {
    const { status, data } = error.response;
    
    switch (status) {
      case 400:
        if (data?.message?.includes('Username is required')) {
          return 'Please enter your username';
        }
        if (data?.message?.includes('Password is required')) {
          return 'Please enter your password';
        }
        return data?.message || 'Invalid request. Please check your input.';
        
      case 401:
        if (data?.message?.includes('Invalid username or password')) {
          return 'Invalid username or password. Please try again.';
        }
        return 'Authentication failed. Please check your credentials.';
        
      case 500:
        if (data?.message?.includes('Authentication service temporarily unavailable')) {
          return 'Authentication service is temporarily unavailable. Please try again in a few minutes.';
        }
        return 'Server error occurred. Please try again later.';
        
      case 503:
        return 'Service is temporarily unavailable. Please try again later.';
        
      default:
        return data?.message || `Server error (${status}). Please try again.`;
    }
  }
  
  // Handle network errors
  if (error.request) {
    return 'Unable to connect to the server. Please check your internet connection.';
  }
  
  // Handle other errors
  if (error.message) {
    return error.message;
  }
  
  return 'An unexpected error occurred. Please try again.';
}

export function isNetworkError(error: any): boolean {
  return !error.response && error.request;
}

export function shouldRetry(error: any): boolean {
  if (isNetworkError(error)) return true;
  
  const status = error.response?.status;
  return status === 500 || status === 502 || status === 503 || status === 504;
}



