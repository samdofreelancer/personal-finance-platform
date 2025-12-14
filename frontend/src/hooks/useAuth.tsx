/**
 * useAuth Hook – Manages authentication state and operations
 * 
 * Responsibilities:
 * - Manage login/logout state
 * - Handle authentication flow
 * - Provide auth state to components
 * - Load user info on mount
 * 
 * Usage:
 * const { user, isLoading, isAuthenticated, login, logout } = useAuth();
 */

import { useState, useEffect, useCallback, useContext, createContext, ReactNode } from 'react';
import { AuthService, User } from '../services/AuthService';

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (googleIdToken: string) => Promise<void>;
  logout: () => void;
  error: string | null;
}

/**
 * Create Auth Context
 */
const AuthContext = createContext<AuthContextType | undefined>(undefined);

/**
 * Auth Provider Component
 * Wrap your app with this to provide auth state to all children
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  /**
   * Check if user is logged in on component mount
   */
  useEffect(() => {
    const loadUser = async () => {
      setIsLoading(true);
      try {
        const currentUser = await AuthService.getCurrentUser();
        setUser(currentUser);
        setError(null);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load user');
        setUser(null);
      } finally {
        setIsLoading(false);
      }
    };

    loadUser();
  }, []);

  /**
   * Login with Google ID Token
   */
  const login = useCallback(async (googleIdToken: string) => {
    setIsLoading(true);
    setError(null);
    try {
      await AuthService.loginWithGoogle(googleIdToken);
      const currentUser = await AuthService.getCurrentUser();
      setUser(currentUser);
    } catch (err) {
      const errorMsg = err instanceof Error ? err.message : 'Login failed';
      setError(errorMsg);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  /**
   * Logout
   */
  const logout = useCallback(() => {
    AuthService.logout();
    setUser(null);
    setError(null);
  }, []);

  const value: AuthContextType = {
    user,
    isLoading,
    isAuthenticated: AuthService.isAuthenticated(),
    login,
    logout,
    error,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

/**
 * useAuth Hook
 * Use this hook in any component to access authentication state and operations
 */
export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
