import type { WalletSummary } from '../types';

interface WalletSummaryProps {
  summary: WalletSummary;
}

export default function WalletSummaryComponent({ summary }: WalletSummaryProps) {
  return (
    <section style={styles.rightSection}>
      <div style={styles.rightSectionHeader}>
        <h3 style={styles.rightSectionTitle}>Wallet Summary</h3>
        <svg width="16" height="16" viewBox="0 0 20 20" fill="currentColor">
          <path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" />
        </svg>
      </div>
      <div style={styles.summaryGrid}>
        <div style={styles.summaryCard}>
          <div style={styles.summaryIcon}>↑</div>
          <div style={styles.summaryLabel}>Outcome</div>
          <div style={styles.summaryValue}>${summary.outcome.toFixed(2)}</div>
        </div>
        <div style={styles.summaryCard}>
          <div style={styles.summaryIcon}>↓</div>
          <div style={styles.summaryLabel}>Income</div>
          <div style={styles.summaryValue}>${summary.income.toFixed(2)}</div>
        </div>
        <div style={styles.summaryCard}>
          <div style={styles.summaryIcon}>⚡</div>
          <div style={styles.summaryLabel}>This Week</div>
          <div style={styles.summaryValue}>{summary.thisWeek.value}</div>
          <div style={{ ...styles.summaryChange, color: '#2ecc71' }}>
            {summary.thisWeek.change}
          </div>
        </div>
        <div style={styles.summaryCard}>
          <div style={styles.summaryIcon}>↓</div>
          <div style={styles.summaryLabel}>This Month</div>
          <div style={styles.summaryValue}>${summary.thisMonth.value}</div>
          <div style={{ ...styles.summaryChange, color: '#e74c3c' }}>
            {summary.thisMonth.change}
          </div>
        </div>
        <div style={styles.summaryCard}>
          <div style={styles.summaryIcon}>🕐</div>
          <div style={styles.summaryLabel}>Upcoming</div>
          <div style={styles.summaryValue}>${summary.upcoming.value}</div>
          <div style={{ ...styles.summaryChange, color: '#2ecc71' }}>
            {summary.upcoming.change}
          </div>
        </div>
      </div>
      <div style={styles.actionButtons}>
        <button style={styles.primaryActionButton}>Request Money</button>
        <button style={styles.secondaryActionButton}>Send Invoice</button>
      </div>
    </section>
  );
}

const styles: Record<string, React.CSSProperties> = {
  rightSection: {
    display: 'flex',
    flexDirection: 'column',
    gap: '16px',
  },
  rightSectionHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  rightSectionTitle: {
    fontSize: '14px',
    fontWeight: '600',
    color: '#e8eefc',
  },
  summaryGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(2, 1fr)',
    gap: '12px',
  },
  summaryCard: {
    background: 'rgba(255, 255, 255, 0.05)',
    border: '1px solid rgba(255, 255, 255, 0.1)',
    borderRadius: '12px',
    padding: '16px',
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
  },
  summaryIcon: {
    fontSize: '20px',
    marginBottom: '4px',
  },
  summaryLabel: {
    fontSize: '11px',
    color: 'rgba(232, 238, 252, 0.6)',
    textTransform: 'uppercase',
  },
  summaryValue: {
    fontSize: '18px',
    fontWeight: '600',
    color: '#e8eefc',
  },
  summaryChange: {
    fontSize: '11px',
    fontWeight: '600',
  },
  actionButtons: {
    display: 'flex',
    flexDirection: 'column',
    gap: '10px',
  },
  primaryActionButton: {
    padding: '12px',
    background: '#ff6b35',
    border: 'none',
    borderRadius: '8px',
    color: '#fff',
    fontSize: '14px',
    fontWeight: '600',
    cursor: 'pointer',
  },
  secondaryActionButton: {
    padding: '12px',
    background: 'transparent',
    border: '1px solid rgba(255, 255, 255, 0.2)',
    borderRadius: '8px',
    color: '#e8eefc',
    fontSize: '14px',
    fontWeight: '600',
    cursor: 'pointer',
  },
};

