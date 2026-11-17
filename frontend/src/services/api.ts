import axios from 'axios';
import type { ApiErrorBody } from '../types';

// All requests share a base URL. Public frontend settings never contain credentials.
export const api = axios.create({ baseURL: import.meta.env.VITE_API_BASE_URL || '/api', timeout: 15000 });

/** Keeps server field errors available to forms. */
export class ApiError extends Error {
  constructor(message: string, public fieldErrors: Record<string, string> = {}) { super(message); }
}

// This interceptor converts HTTP and connection failures into readable errors.
api.interceptors.response.use((response) => response, (error: unknown) => {
  if (axios.isCancel(error)) return Promise.reject(error);
  if (axios.isAxiosError<ApiErrorBody>(error)) {
    return Promise.reject(new ApiError(error.response?.data?.message || (error.response ? 'The request could not be completed.' : 'Cannot reach the server. Check your connection and try again.'), error.response?.data?.fieldErrors));
  }
  return Promise.reject(new ApiError('An unexpected request error occurred.'));
});
export const errorMessage = (error: unknown) => error instanceof Error ? error.message : 'Something went wrong.';
