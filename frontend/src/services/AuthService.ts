/**
 * AuthService – Handles Google OIDC authentication flow
 * 
 * Responsibilities:
 * - Exchange Google ID Token for internal JWT
 * - Store internal JWT securely
 * - Retrieve stored JWT
 * - Clear authentication state
 * 
 * CRITICAL: Google ID Token is used ONLY for the initial exchange.
 * All subsequent API calls use the internal JWT.
 */

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:3000/api';
const TOKEN_KEY = 'auth_token';

export interface AuthResponse {
  accessToken: string;
}

export interface User {
  id: string;
  email: string;
  name: string;
}

/**
 * Exchange Google ID Token for internal JWT
 * 
 * POST /auth/login
 * Authorization: Bearer <google-id-token>
 * 
 * Response: { accessToken: string }
 */
async function loginWithGoogle(googleIdToken: string): Promise<AuthResponse> {
  try {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${googleIdToken}`,
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error(`Authentication failed: ${response.statusText}`);
    }

    const data: AuthResponse = await response.json();
    
    // Store internal JWT securely
    storeToken(data.accessToken);
    
    return data;
  } catch (error) {
    console.error('Google login failed:', error);
    throw error;
  }
}

/**
 * Store JWT token in localStorage
 * NOTE: In production, consider httpOnly cookies for better security
 */
function storeToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

/**
 * Retrieve stored JWT token
 */
function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

/**
 * Check if user is authenticated
 */
function isAuthenticated(): boolean {
  return getToken() !== null;
}

/**
 * Clear authentication state (logout)
 */
function logout(): void {
  localStorage.removeItem(TOKEN_KEY);
}

/**
 * Get current user info from backend
 * Uses internal JWT for authentication
 */
async function getCurrentUser(): Promise<User | null> {
  const token = getToken();
  if (!token) {
    return null;
  }

  try {
    const response = await fetch(`${API_BASE_URL}/auth/me`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      if (response.status === 401) {
        // Token expired or invalid
        logout();
        return null;
      }
      throw new Error(`Failed to fetch user: ${response.statusText}`);
    }

    return await response.json();
  } catch (error) {
    console.error('Failed to get current user:', error);
    return null;
  }
}

export const AuthService = {
  loginWithGoogle,
  logout,
  getToken,
  isAuthenticated,
  getCurrentUser,
};
