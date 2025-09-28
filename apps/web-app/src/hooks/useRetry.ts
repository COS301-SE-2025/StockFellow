// src/hooks/useRetry.ts
import { useState, useCallback } from 'react';

interface UseRetryOptions {
  maxAttempts?: number;
  delayMs?: number;
  shouldRetry?: (error: any) => boolean;
}

interface UseRetryReturn {
  retry: (fn: () => Promise<any>) => Promise<any>;
  isRetrying: boolean;
  attempt: number;
}

export function useRetry({
  maxAttempts = 3,
  delayMs = 1000,
  shouldRetry = () => true
}: UseRetryOptions = {}): UseRetryReturn {
  const [isRetrying, setIsRetrying] = useState(false);
  const [attempt, setAttempt] = useState(0);

  const retry = useCallback(async (fn: () => Promise<any>) => {
    setIsRetrying(true);
    setAttempt(0);

    for (let i = 0; i < maxAttempts; i++) {
      try {
        setAttempt(i + 1);
        const result = await fn();
        setIsRetrying(false);
        return result;
      } catch (error) {
        const isLastAttempt = i === maxAttempts - 1;
        
        if (isLastAttempt || !shouldRetry(error)) {
          setIsRetrying(false);
          throw error;
        }

        // Wait before retrying with exponential backoff
        await new Promise(resolve => 
          setTimeout(resolve, delayMs * Math.pow(2, i))
        );
      }
    }
  }, [maxAttempts, delayMs, shouldRetry]);

  return { retry, isRetrying, attempt };
}