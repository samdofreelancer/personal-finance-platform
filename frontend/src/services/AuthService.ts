/**
 * AuthService – Handles Google OIDC authentication flow
 * 
 * Responsibilities:
 * - Exchange Google ID Token for internal JWT via backend
 * - Store internal JWT securely
 * - Retrieve stored JWT
 * - Clear authentication state
 * - Fetch current user information
 * 
 * CRITICAL SECURITY MODEL:
 * 1. Google ID Token comes from Google's OAuth popup
 * 2. Frontend sends ID Token to backend: POST /api/auth/google with idToken
 * 3. Backend verifies ID Token signature against Google's public keys
 * 4. Backend creates/updates user in database
 * 5. Backend generates internal JWT (signed with our secret)
 * 6. Backend returns internal JWT to frontend
 * 7. Frontend stores internal JWT in localStorage
 * 8. ALL subsequent API calls use internal JWT, NOT Google token
 * 9. Google token is never stored or used by frontend after exchange
 */

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
const TOKEN_KEY = 'auth_token';

export interface AuthResponse {
  accessToken: string;
}

export interface User {
  id: string;
  email: string;
  name: string;
  picture?: string;
}

/**
 * Exchange Google ID Token for internal JWT
 * 
 * Sends Google ID Token to backend for verification and JWT issuance.
 * 
 * Request:
 * POST /api/auth/google
 * Content-Type: application/json
 * { "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEifQ..." }
 * 
 * Response:
 * { "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." }
 * 
 * @param googleIdToken ID Token from Google
 * @returns Internal JWT
 */
async function loginWithGoogle(googleIdToken: string): Promise<AuthResponse> {
  try {
    console.log('Exchanging Google ID Token for internal JWT...');
    
    const response = await fetch(`${API_BASE_URL}/auth/google`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        idToken: googleIdToken,
      }),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      const errorMessage = errorData.error || `Authentication failed: ${response.statusText}`;
      throw new Error(errorMessage);
    }

    const data: AuthResponse = await response.json();
    
    if (!data.accessToken) {
      throw new Error('No access token received from server');
    }
    
    // Store internal JWT securely
    storeToken(data.accessToken);
    
    console.log('Authentication successful, JWT stored');
    return data;
  } catch (error) {
    const errorMessage = error instanceof Error ? error.message : 'Login failed';
    console.error('Google login exchange failed:', errorMessage);
    throw error;
  }
}

/**
 * Store JWT token in localStorage
 * 
 * NOTE: In production, consider httpOnly cookies for enhanced security.
 * However, for SPA applications, localStorage is acceptable if HTTPS is enforced
 * and XSS protection measures are in place.
 */
function storeToken(token: string): void {
  try {
    localStorage.setItem(TOKEN_KEY, token);
  } catch (error) {
    console.error('Failed to store token:', error);
    throw new Error('Failed to store authentication token');
  }
}

/**
 * Retrieve stored JWT token
 * 
 * @returns JWT token or null if not found
 */
function getToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_KEY);
  } catch (error) {
    console.error('Failed to retrieve token:', error);
    return null;
  }
}

/**
 * Check if user is authenticated
 * 
 * @returns true if JWT exists, false otherwise
 */
function isAuthenticated(): boolean {
  return getToken() !== null;
}

/**
 * Clear authentication state (logout)
 */
function logout(): void {
  try {
    localStorage.removeItem(TOKEN_KEY);
    console.log('User logged out, token cleared');
  } catch (error) {
    console.error('Failed to clear token:', error);
  }
}

/**
 * Get current user info from backend
 * 
 * Uses internal JWT for authentication.
 * 
 * Request:
 * GET /api/auth/me
 * Authorization: Bearer <internal-jwt>
 * 
 * Response:
 * {
 *   "id": "uuid",
 *   "email": "user@example.com",
 *   "name": "John Doe",
 *   "picture": "https://..."
 * }
 * 
 * @returns User object or null if not authenticated
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
      if (response.status === 401 || response.status === 403) {
        // Token expired or invalid
        console.warn('Token invalid or expired, logging out');
        logout();
        return null;
      }
      throw new Error(`Failed to fetch user: ${response.statusText}`);
    }

    const user: User = await response.json();
    console.log('Retrieved current user:', user.email);
    return user;
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

