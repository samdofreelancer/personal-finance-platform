/**
 * GoogleLoginButton Component
 * 
 * Handles Google Sign-In and exchanges ID Token for internal JWT
 * 
 * Usage:
 * <GoogleLoginButton />
 * 
 * or with callback:
 * <GoogleLoginButton onLoginSuccess={() => navigate('/dashboard')} />
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
  const [error, setError] = useState<string | null>(null);
  const googleButtonRef = React.useRef<HTMLDivElement>(null);

  /**
   * Handle Google Sign-In response
   * Google provides the ID Token in the response
   */
  const handleGoogleSignIn = useCallback(async (response: any) => {
    try {
      if (!response.credential) {
        throw new Error('No Google ID Token received');
      }

      // Exchange Google ID Token for internal JWT
      await login(response.credential);

      setError(null);
      onLoginSuccess?.();
    } catch (err) {
      const errorMsg = err instanceof Error ? err.message : 'Login failed';
      setError(errorMsg);
      onLoginError?.(err instanceof Error ? err : new Error(errorMsg));
    }
  }, [login, onLoginSuccess, onLoginError]);

  useEffect(() => {
    // Load Google Identity Services script
    const loadGoogleScript = () => {
      const script = document.createElement('script');
      script.src = 'https://accounts.google.com/gsi/client';
      script.async = true;
      script.defer = true;

      script.onload = () => {
        if (window.google?.accounts) {
          // Initialize Google Sign-In
          window.google.accounts.id.initialize({
            client_id: process.env.REACT_APP_GOOGLE_CLIENT_ID,
            callback: handleGoogleSignIn,
          });

          // Render the sign-in button
          if (googleButtonRef.current) {
            window.google.accounts.id.renderButton(googleButtonRef.current, {
              theme: 'outline',
              size: 'large',
              text: 'signin_with',
            });
          }

          setIsInitializing(false);
        }
      };

      script.onerror = () => {
        setError('Failed to load Google Sign-In script');
        setIsInitializing(false);
      };

      document.head.appendChild(script);
    };

    loadGoogleScript();
  }, [handleGoogleSignIn]);

  if (isInitializing) {
    return <div className="google-login-button loading">Loading...</div>;
  }

  return (
    <div className="google-login-container">
      <div ref={googleButtonRef} className="google-login-button" />
      {error && <div className="error-message">{error}</div>}
    </div>
  );
};

export default GoogleLoginButton;
