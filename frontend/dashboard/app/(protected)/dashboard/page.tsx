'use client';

import { useState } from 'react';
import type { Transaction, WalletSummary } from './types';
import Sidebar from './components/Sidebar';
import MainContent from './components/MainContent';
import RightSidebar from './components/RightSidebar';

export default function DashboardPage() {
  // Mock data
  const [transactions] = useState<Transaction[]>([
    {
      id: '1',
      title: 'Transfer to Mikey',
      description: 'Online food order',
      amount: 1250.60,
      type: 'expense',
      date: new Date('2024-01-22'),
    },
    {
      id: '2',
      title: 'Salary For the Month of Apr',
      description: 'Monthly Salary',
      amount: 12840.00,
      type: 'income',
      date: new Date('2024-01-22'),
    },
    {
      id: '3',
      title: 'Grocery Shopping',
      description: 'Supermarket purchase',
      amount: 450.30,
      type: 'expense',
      date: new Date('2024-01-21'),
    },
  ]);

  const walletSummary: WalletSummary = {
    outcome: 460.00,
    income: 840.00,
    thisWeek: { value: '3.45k', change: '+6.4%', trend: 'up' },
    thisMonth: { value: '12.9k', change: '-3.1%', trend: 'down' },
    upcoming: { value: '14.4k', change: '+10.3%', trend: 'up' },
  };

  return (
    <div style={styles.container}>
      <Sidebar />
      <MainContent transactions={transactions} />
      <RightSidebar walletSummary={walletSummary} />
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  container: {
    display: 'flex',
    minHeight: '100vh',
    background: 'linear-gradient(180deg, #0b1020 0%, #0a0f1a 60%, #070b12 100%)',
    color: '#e8eefc',
    fontFamily: 'system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
  },
};
