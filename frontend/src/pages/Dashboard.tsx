/**
 * Dashboard Page
 * 
 * This is the main protected page that shows after successful login
 */

import React from 'react';
import { useAuth } from '../hooks/useAuth';

const Dashboard: React.FC = () => {
  const { user } = useAuth();

  return (
    <div className="dashboard">
      <h2>Welcome, {user?.name}!</h2>
      <p>Your email: {user?.email}</p>
      <div className="dashboard-content">
        <section>
          <h3>Accounts</h3>
          <p>Your accounts will appear here...</p>
        </section>
        <section>
          <h3>Recent Transactions</h3>
          <p>Your transactions will appear here...</p>
        </section>
        <section>
          <h3>Loans</h3>
          <p>Your loans will appear here...</p>
        </section>
      </div>
    </div>
  );
};

export default Dashboard;
