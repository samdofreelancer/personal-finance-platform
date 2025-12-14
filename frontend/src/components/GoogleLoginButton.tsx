/**
 * GoogleLoginButton Component
 * 
 * Handles complete Google OIDC OAuth 2.0 ID Token flow:
 * 
 * Flow:
 * 1. Load Google Identity Services SDK
 * 2. User clicks "Sign in with Google" button
 * 3. Google Identity popup opens
 * 4. User authenticates with Google
 * 5. Google returns ID Token to frontend
 * 6. Frontend sends ID Token to backend: POST /api/auth/google
 * 7. Backend verifies Google signature, creates user, returns JWT
 * 8. Frontend stores JWT and redirects to dashboard
 * 
 * SECURITY:
 * - Google ID Token is used ONLY for initial verification at backend
 * - Backend verifies signature against Google's public keys
 * - Backend returns an internal JWT (signed with our secret)
 * - Frontend stores internal JWT and uses it for all API calls
 * - No passwords are ever transmitted
 */

import React, { useEffect, useState, useCallback } from 'react';
import { useAuth } from '../hooks/useAuth';

interface GoogleLoginButtonProps {
  onLoginSuccess?: () => void;
  onLoginError?: (error: Error) => void;
}

// Declare google global type for TypeScript
declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize: (config: any) => void;
          renderButton: (element: HTMLElement, options: any) => void;
          oneTap: (config: any) => void;
        };
      };
    };
  }
}

export const GoogleLoginButton: React.FC<GoogleLoginButtonProps> = ({
  onLoginSuccess,
  onLoginError,
}) => {
  const { login } = useAuth();
  const [isInitializing, setIsInitializing] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const googleButtonRef = React.useRef<HTMLDivElement>(null);

  /**
   * Handle Google Sign-In response
   * 
   * Google provides the ID Token in the response.
   * We immediately send it to backend for verification and JWT exchange.
   */
  const handleGoogleSignIn = useCallback(async (response: any) => {
    try {
      if (!response.credential) {
        throw new Error('No Google ID Token received');
      }

      setIsLoading(true);
      setError(null);

      console.log('Google ID Token received, exchanging for internal JWT...');

      // Exchange Google ID Token for internal JWT
      // This sends the ID Token to our backend
      await login(response.credential);

      console.log('Authentication successful, redirecting to dashboard...');
      onLoginSuccess?.();
    } catch (err) {
      const errorMsg = err instanceof Error ? err.message : 'Login failed';
      console.error('Login error:', errorMsg);
      setError(errorMsg);
      onLoginError?.(err instanceof Error ? err : new Error(errorMsg));
    } finally {
      setIsLoading(false);
    }
  }, [login, onLoginSuccess, onLoginError]);

  useEffect(() => {
    // Load Google Identity Services script
    const loadGoogleScript = () => {
      // Check if script is already loaded
      if (document.getElementById('google-gsi-script')) {
        initializeGoogle();
        return;
      }

      const script = document.createElement('script');
      script.id = 'google-gsi-script';
      script.src = 'https://accounts.google.com/gsi/client';
      script.async = true;
      script.defer = true;

      script.onload = () => {
        initializeGoogle();
      };

      script.onerror = () => {
        setError('Failed to load Google Sign-In SDK. Please check your internet connection.');
        setIsInitializing(false);
      };

      document.head.appendChild(script);
    };

    const initializeGoogle = () => {
      if (!window.google?.accounts) {
        console.error('Google accounts API not available');
        setError('Google Sign-In service is unavailable');
        setIsInitializing(false);
        return;
      }

      try {
        // Initialize Google Sign-In with our Client ID
        window.google.accounts.id.initialize({
          client_id: process.env.REACT_APP_GOOGLE_CLIENT_ID,
          callback: handleGoogleSignIn,
          // Optional: auto-select if user previously signed in
          auto_select: false,
        });

        // Render the Google Sign-In button
        if (googleButtonRef.current) {
          window.google.accounts.id.renderButton(googleButtonRef.current, {
            // 'outline' | 'filled_blue' | 'filled_black'
            theme: 'outline',
            // 'large' | 'medium' | 'small'
            size: 'large',
            // 'signin_with' | 'signup_with' | 'signin' | 'signup'
            text: 'signin_with',
            // 'center' | 'left' | 'right'
            width: '100%',
          });

          console.log('Google Sign-In button rendered');
        }

        setIsInitializing(false);
      } catch (err) {
        console.error('Failed to initialize Google Sign-In:', err);
        setError('Failed to initialize Google Sign-In');
        setIsInitializing(false);
      }
    };

    loadGoogleScript();
  }, [handleGoogleSignIn]);

  if (isInitializing) {
    return (
      <div className="google-login-container">
        <div className="google-login-button loading">
          <span>Initializing Google Sign-In...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="google-login-container">
      <div 
        ref={googleButtonRef} 
        className={`google-login-button ${isLoading ? 'loading' : ''}`}
      />
      {error && (
        <div className="error-message">
          <strong>Authentication Error:</strong> {error}
        </div>
      )}
      {isLoading && (
        <div className="loading-message">
          Authenticating... Please wait.
        </div>
      )}
    </div>
  );
};

export default GoogleLoginButton;
