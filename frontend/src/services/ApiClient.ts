/**
 * API Client – Handles authenticated API requests
 * 
 * All API calls to the backend use the internal JWT token,
 * NOT the Google ID Token.
 * 
 * Usage:
 * const response = await apiClient.get('/transactions');
 * const data = await apiClient.post('/accounts', { name: 'Savings' });
 */

import { AuthService } from './AuthService';

interface RequestOptions extends RequestInit {
  params?: Record<string, any>;
}

class ApiClient {
  private baseUrl: string;

  constructor(baseUrl: string = process.env.REACT_APP_API_URL || 'http://localhost:3000/api') {
    this.baseUrl = baseUrl;
  }

  /**
   * Build URL with query parameters
   */
  private buildUrl(path: string, params?: Record<string, any>): string {
    const url = new URL(path.startsWith('/') ? path : `/${path}`, this.baseUrl);
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          url.searchParams.append(key, String(value));
        }
      });
    }
    return url.toString();
  }

  /**
   * Get Authorization header with internal JWT
   */
  private getAuthHeaders(): Record<string, string> {
    const token = AuthService.getToken();
    if (!token) {
      throw new Error('Not authenticated. Please log in first.');
    }
    return {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    };
  }

  /**
   * Handle API errors
   */
  private handleError(response: Response): never {
    if (response.status === 401) {
      // Token expired or invalid
      AuthService.logout();
      throw new Error('Session expired. Please log in again.');
    }
    throw new Error(`API Error: ${response.status} ${response.statusText}`);
  }

  /**
   * GET request
   */
  async get<T>(path: string, options?: RequestOptions): Promise<T> {
    const url = this.buildUrl(path, options?.params);
    const response = await fetch(url, {
      method: 'GET',
      headers: this.getAuthHeaders(),
      ...options,
    });

    if (!response.ok) {
      this.handleError(response);
    }

    return response.json();
  }

  /**
   * POST request
   */
  async post<T>(path: string, data?: any, options?: RequestOptions): Promise<T> {
    const url = this.buildUrl(path, options?.params);
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getAuthHeaders(),
      body: data ? JSON.stringify(data) : undefined,
      ...options,
    });

    if (!response.ok) {
      this.handleError(response);
    }

    return response.json();
  }

  /**
   * PUT request
   */
  async put<T>(path: string, data?: any, options?: RequestOptions): Promise<T> {
    const url = this.buildUrl(path, options?.params);
    const response = await fetch(url, {
      method: 'PUT',
      headers: this.getAuthHeaders(),
      body: data ? JSON.stringify(data) : undefined,
      ...options,
    });

    if (!response.ok) {
      this.handleError(response);
    }

    return response.json();
  }

  /**
   * DELETE request
   */
  async delete<T>(path: string, options?: RequestOptions): Promise<T> {
    const url = this.buildUrl(path, options?.params);
    const response = await fetch(url, {
      method: 'DELETE',
      headers: this.getAuthHeaders(),
      ...options,
    });

    if (!response.ok) {
      this.handleError(response);
    }

    return response.json();
  }
}

export const apiClient = new ApiClient();
