# Personal Finance Platform – Frontend

React-based SPA frontend for the Personal Finance & Loan Management System.

## 📋 Architecture Overview

### Authentication Flow

```
User clicks "Login with Google"
         ↓
Google Sign-In (handles by Google)
         ↓
Frontend receives Google ID Token
         ↓
POST /auth/login (with Google ID Token)
         ↓
Backend validates token & returns internal JWT
         ↓
Frontend stores JWT in localStorage
         ↓
All subsequent API calls use internal JWT
```

### Key Principles

- **Google OIDC Only** – No username/password authentication
- **Internal JWT** – Backend issues JWT for all authenticated requests
- **Google ID Token** – Used ONLY for initial login exchange
- **Clean Separation** – Auth logic isolated in `AuthService` and `useAuth` hook
- **Secure Token Storage** – JWT stored in localStorage (production: consider httpOnly cookies)

## 🗂️ Project Structure

```
frontend/
├── src/
│   ├── components/
│   │   └── GoogleLoginButton.tsx    # Google Sign-In button component
│   ├── hooks/
│   │   └── useAuth.tsx              # Auth state management hook
│   ├── services/
│   │   ├── AuthService.ts           # Google token exchange, JWT handling
│   │   └── ApiClient.ts             # Authenticated API client
│   ├── pages/
│   │   ├── Dashboard.tsx            # Main app page (protected)
│   │   └── Login.tsx                # Login page
│   ├── styles/
│   │   └── App.css                  # Global styles
│   └── App.tsx                      # Main app component & routing
├── public/
│   └── index.html
├── package.json
├── .env.example
└── README.md                        # This file
```

## 🚀 Getting Started

### 1. Prerequisites

- Node.js 16+ and npm/yarn
- Google OAuth 2.0 credentials (see "Google OAuth Setup" below)

### 2. Installation

```bash
cd frontend
npm install
```

### 3. Configuration

Copy `.env.example` to `.env.local` and update values:

```bash
cp .env.example .env.local
```

**Required environment variables:**

```env
REACT_APP_GOOGLE_CLIENT_ID=YOUR_GOOGLE_CLIENT_ID_HERE
REACT_APP_API_URL=http://localhost:3000/api
```

### 4. Running the App

```bash
npm start
```

The app will open at `http://localhost:3000`

## 🔐 Google OAuth Setup

### Step 1: Create a Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project: **Personal Finance Platform**
3. Enable the **Google Identity Services API**

### Step 2: Create OAuth 2.0 Credentials

1. Navigate to **Credentials** → **Create Credentials** → **OAuth 2.0 Client ID**
2. Choose **Web application**
3. Add authorized redirect URIs:
   - `http://localhost:3000` (development)
   - `http://localhost:3000/callback` (if using callback)
   - Your production domain

4. Copy the **Client ID** and paste into `.env.local`:

```env
REACT_APP_GOOGLE_CLIENT_ID=YOUR_CLIENT_ID.apps.googleusercontent.com
```

### Step 3: Test the Flow

1. Start the frontend: `npm start`
2. Click "Sign in with Google"
3. Verify backend receives the Google ID Token at `POST /auth/login`

## 📦 Components & Services

### AuthService

Handles Google OIDC token exchange and JWT storage.

**Key Methods:**

```typescript
// Exchange Google ID Token for internal JWT
await AuthService.loginWithGoogle(googleIdToken: string): Promise<AuthResponse>

// Logout and clear token
AuthService.logout(): void

// Get stored JWT
AuthService.getToken(): string | null

// Check if authenticated
AuthService.isAuthenticated(): boolean

// Fetch current user info
await AuthService.getCurrentUser(): Promise<User | null>
```

### useAuth Hook

React hook for managing authentication state across components.

**Usage:**

```typescript
import { useAuth } from './hooks/useAuth';

const MyComponent = () => {
  const { user, isLoading, isAuthenticated, login, logout, error } = useAuth();

  if (isLoading) return <div>Loading...</div>;

  if (!isAuthenticated) return <button onClick={() => login(token)}>Login</button>;

  return <div>Welcome, {user?.email}!</div>;
};
```

### GoogleLoginButton

Pre-built Google Sign-In button component.

**Usage:**

```typescript
import GoogleLoginButton from './components/GoogleLoginButton';

<GoogleLoginButton
  onLoginSuccess={() => navigate('/dashboard')}
  onLoginError={(error) => console.error(error)}
/>
```

### ApiClient

Authenticated HTTP client for backend API calls.

**Usage:**

```typescript
import { apiClient } from './services/ApiClient';

// GET request
const accounts = await apiClient.get('/accounts');

// POST request
const newAccount = await apiClient.post('/accounts', { name: 'Savings' });

// PUT request
const updated = await apiClient.put(`/accounts/${id}`, { name: 'New Name' });

// DELETE request
await apiClient.delete(`/accounts/${id}`);

// With query parameters
const transactions = await apiClient.get('/transactions', {
  params: { accountId: '123', limit: 10 }
});
```

## 🔄 Authentication Flow in Code

### 1. User Clicks Login Button

```typescript
// GoogleLoginButton.tsx
const handleGoogleSignIn = async (response: any) => {
  // response.credential contains the Google ID Token
  await login(response.credential);
};
```

### 2. Exchange Token in AuthService

```typescript
// AuthService.ts
async function loginWithGoogle(googleIdToken: string) {
  const response = await fetch(`${API_BASE_URL}/auth/login`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${googleIdToken}`, // ← Send Google token
    },
  });
  const data = await response.json(); // { accessToken: string }
  storeToken(data.accessToken); // ← Store internal JWT
}
```

### 3. Use JWT for API Calls

```typescript
// ApiClient.ts
private getAuthHeaders(): Record<string, string> {
  const token = AuthService.getToken(); // ← Retrieve internal JWT
  return {
    'Authorization': `Bearer ${token}`, // ← Use for all API calls
    'Content-Type': 'application/json',
  };
}
```

## 🛡️ Security Considerations

| Aspect | Implementation |
|--------|----------------|
| **Google ID Token** | Used ONLY once to exchange for internal JWT |
| **Internal JWT** | Stored in localStorage; consider httpOnly cookies for production |
| **Token Expiration** | Backend should set reasonable expiry; frontend checks 401 responses |
| **HTTPS** | Use HTTPS in production |
| **CORS** | Backend should properly configure CORS for frontend domain |
| **XSS Protection** | Avoid storing tokens in `window` or global scope |

## 🧪 Testing

### Unit Tests

Test individual auth components:

```bash
npm test
```

### Manual Testing Checklist

- [ ] Click "Sign in with Google" → redirects to Google login
- [ ] Sign in with Google account → redirected to dashboard
- [ ] Verify internal JWT in browser console (DevTools → Application → localStorage)
- [ ] Verify Google ID Token is NOT in localStorage
- [ ] Refresh page → remains logged in (JWT present)
- [ ] Click "Logout" → token cleared, redirected to login
- [ ] Try accessing protected route without token → redirected to login
- [ ] Make API call (e.g., fetch transactions) → uses internal JWT

## 📚 Backend Integration

The backend must provide:

1. **POST /auth/login** – Exchange Google ID Token for JWT
   - Request: `Authorization: Bearer <google-id-token>`
   - Response: `{ accessToken: string }`

2. **GET /auth/me** – Get current user info (protected)
   - Request: `Authorization: Bearer <internal-jwt>`
   - Response: `{ id, email, name }`

3. **All API endpoints** – Accept internal JWT in Authorization header
   - Request: `Authorization: Bearer <internal-jwt>`

## 🚢 Deployment

### Build for Production

```bash
npm run build
```

Output: `build/` directory with optimized assets

### Environment Variables (Production)

```env
REACT_APP_GOOGLE_CLIENT_ID=prod-client-id.apps.googleusercontent.com
REACT_APP_API_URL=https://api.yourapp.com
```

### Deploy to Vercel (Recommended)

```bash
npm install -g vercel
vercel
```

### Deploy to Other Platforms

- **Netlify**: Connect GitHub repo, set build command to `npm run build`
- **AWS S3 + CloudFront**: Upload `build/` to S3, use CloudFront for CDN
- **Docker**: See Dockerfile in root directory

## 📖 Additional Resources

- [Google Identity Services Docs](https://developers.google.com/identity/gsi/web)
- [React Router Documentation](https://reactrouter.com/)
- [Backend Architecture Docs](../docs/architecture/)

## ❓ Troubleshooting

### "Google Sign-In script failed to load"

- Check `REACT_APP_GOOGLE_CLIENT_ID` is set correctly
- Verify Google OAuth credentials exist in Google Cloud Console
- Check browser console for network errors

### "401 Unauthorized on API calls"

- Verify backend is issuing JWT correctly at `/auth/login`
- Check JWT is stored in localStorage after login
- Verify `REACT_APP_API_URL` points to correct backend

### "Token persists after logout"

- Verify `AuthService.logout()` is removing token from localStorage
- Check browser DevTools → Application → localStorage is cleared

## 📝 License

Same as main project.
