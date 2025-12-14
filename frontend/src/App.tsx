/**
 * App.tsx – Main Application Component
 * 
 * Demonstrates how to:
 * - Wrap app with AuthProvider
 * - Use GoogleLoginButton for sign-in
 * - Use useAuth hook to access auth state
 * - Protect routes based on authentication
 */

import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './hooks/useAuth';
import { GoogleLoginButton } from './components/GoogleLoginButton';
import Dashboard from './pages/Dashboard';
import './index.css';

/**
 * LoginPage Component
 * Professional login screen with Google OAuth only
 */
const LoginPage: React.FC = () => {
  return (
    <div className="login-page">
      <div className="login-container">
        <div className="login-logo">💰</div>
        <h1>Personal <span>Finance</span></h1>
        <p className="login-subtitle">
          Manage your accounts, transactions, and loans securely in one place.
        </p>
        <div className="security-badge">
          Secure authentication. No passwords stored.
        </div>
        <GoogleLoginButton
          onLoginSuccess={() => {
            // Navigation happens automatically via router
          }}
          onLoginError={(error) => {
            console.error('Login failed:', error);
          }}
        />
        <div className="login-footer">
          By signing in, you agree to our 
          <a href="#terms"> Terms of Service</a> and 
          <a href="#privacy"> Privacy Policy</a>
        </div>
      </div>
    </div>
  );
};

/**
 * Protected Route Wrapper
 */
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return <div className="loading">Loading...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};

/**
 * Layout with header
 */
const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, logout } = useAuth();

  return (
    <div className="layout">
      <header className="header">
        <div className="header-content">
          <a href="/" className="header-logo">
            Personal Finance
          </a>
          <div className="header-right">
            {user && (
              <>
                <span className="user-email">{user.email}</span>
                <button onClick={logout} className="logout-btn">
                  Sign Out
                </button>
              </>
            )}
          </div>
        </div>
      </header>
      <main className="main-content">{children}</main>
    </div>
  );
};

/**
 * Main App Component
 */
export const App: React.FC = () => {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Layout>
                  <Dashboard />
                </Layout>
              </ProtectedRoute>
            }
          />
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
};

export default App;
