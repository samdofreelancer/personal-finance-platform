/**
 * GoogleLoginButton Component
 * 
 * Simplified Google OIDC OAuth 2.0 implementation
 */

import React, { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

interface GoogleLoginButtonProps {
  onLoginSuccess?: () => void;
  onLoginError?: (error: Error) => void;
}

declare global {
  interface Window {
    google?: any;
  }
}

export const GoogleLoginButton: React.FC<GoogleLoginButtonProps> = ({
  onLoginSuccess,
  onLoginError,
}) => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const buttonRef = useRef<HTMLDivElement>(null);
  const initAttempts = useRef(0);

  useEffect(() => {
    const initializeButton = async () => {
      // Check if Google is loaded
      if (!window.google) {
        initAttempts.current++;
        if (initAttempts.current < 50) {
          setTimeout(initializeButton, 100);
        }
        return;
      }

      if (!buttonRef.current) return;

      try {
        console.log('Initializing Google with Client ID:', process.env.REACT_APP_GOOGLE_CLIENT_ID);
        
        // Initialize Google
        window.google.accounts.id.initialize({
          client_id: process.env.REACT_APP_GOOGLE_CLIENT_ID || '',
          callback: async (response: any) => {
            try {
              console.log('Google response received');
              if (!response.credential) {
                throw new Error('No credential in response');
              }

              // Exchange for internal JWT
              await login(response.credential);
              console.log('Login successful, navigating to dashboard');
              navigate('/dashboard');
              onLoginSuccess?.();
            } catch (error) {
              console.error('Login error:', error);
              onLoginError?.(error instanceof Error ? error : new Error(String(error)));
            }
          },
        });

        // Render button
        window.google.accounts.id.renderButton(buttonRef.current, {
          theme: 'outline',
          size: 'large',
          text: 'signin_with',
          width: '100%',
        });

        console.log('Google button rendered');
      } catch (error) {
        console.error('Google initialization error:', error);
      }
    };

    initializeButton();
  }, [login, navigate, onLoginSuccess, onLoginError]);

  return (
    <div className="google-login-container">
      <div ref={buttonRef} className="google-login-button" />
    </div>
  );
};

export default GoogleLoginButton;
