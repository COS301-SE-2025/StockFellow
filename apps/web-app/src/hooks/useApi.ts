// src/hooks/useApi.ts
import { useState, useEffect } from 'react';
import { adminService } from '../services/adminService';

interface UseApiState<T> {
  data: T | null;
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
}

export function useApi<T>(
  apiCall: () => Promise<T>,
  dependencies: any[] = []
): UseApiState<T> {
  const [state, setState] = useState<UseApiState<T>>({
    data: null,
    loading: true,
    error: null,
    refetch: async () => {}
  });

  const fetchData = async () => {
    setState(prev => ({ ...prev, loading: true, error: null }));
    
    try {
      const result = await apiCall();
      setState(prev => ({ 
        ...prev, 
        data: result, 
        loading: false, 
        error: null 
      }));
    } catch (error: any) {
      setState(prev => ({ 
        ...prev, 
        loading: false, 
        error: error.message || 'An error occurred' 
      }));
      
      // Handle authentication errors
      if (error.message === 'Authentication required') {
        adminService.logout();
      }
    }
  };

  useEffect(() => {
    fetchData();
  }, dependencies);

  const refetch = async () => {
    await fetchData();
  };

  return {
    ...state,
    refetch
  };
}