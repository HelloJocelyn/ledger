import type { Transaction } from '../types';
import { groupTransactionsByDate, formatAmount, isToday, isYesterday } from '../utils/transactions';

interface TransactionsSectionProps {
  transactions: Transaction[];
}

export default function TransactionsSection({ transactions }: TransactionsSectionProps) {
  const groupedTransactions = groupTransactionsByDate(transactions);

  return (
    <section style={styles.section}>
      <div style={styles.transactionHeader}>
        <h2 style={styles.sectionTitle}>Recent Transactions</h2>
        <div style={styles.transactionControls}>
          <select style={styles.select}>
            <option>Sort by: Month</option>
            <option>Sort by: Week</option>
            <option>Sort by: Day</option>
          </select>
          <button style={styles.downloadButton}>
            <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M3 17a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm3.293-7.707a1 1 0 011.414 0L9 10.586V3a1 1 0 112 0v7.586l1.293-1.293a1 1 0 111.414 1.414l-3 3a1 1 0 01-1.414 0l-3-3a1 1 0 010-1.414z" clipRule="evenodd" />
            </svg>
          </button>
        </div>
      </div>

      <div style={styles.transactionsList}>
        {Object.entries(groupedTransactions).map(([dateKey, txs]) => {
          const firstTx = txs[0];
          const dateLabel = isToday(firstTx.date)
            ? 'TODAY'
            : isYesterday(firstTx.date)
            ? 'YESTERDAY'
            : '';
          return (
            <div key={dateKey} style={styles.transactionGroup}>
              <div style={styles.transactionDateHeader}>
                {dateLabel ? `${dateLabel} | ` : ''}
                {dateKey.toUpperCase()}
              </div>
              {txs.map((tx) => (
                <div key={tx.id} style={styles.transactionItem}>
                  <div style={styles.transactionIcon}>
                    {tx.type === 'income' ? (
                      <div style={{ ...styles.iconCircle, background: 'rgba(157, 78, 221, 0.2)' }}>
                        <span style={{ color: '#9d4edd' }}>+</span>
                      </div>
                    ) : (
                      <div style={{ ...styles.iconCircle, background: 'rgba(231, 76, 60, 0.2)' }}>
                        <span style={{ color: '#e74c3c' }}>-</span>
                      </div>
                    )}
                  </div>
                  <div style={styles.transactionDetails}>
                    <div style={styles.transactionTitle}>{tx.title}</div>
                    <div style={styles.transactionDescription}>{tx.description}</div>
                  </div>
                  <div
                    style={{
                      ...styles.transactionAmount,
                      color: tx.type === 'income' ? '#9d4edd' : '#e74c3c',
                    }}
                  >
                    {formatAmount(tx.amount)}
                  </div>
                </div>
              ))}
            </div>
          );
        })}
      </div>
    </section>
  );
}

const styles: Record<string, React.CSSProperties> = {
  section: {
    marginBottom: '40px',
  },
  sectionTitle: {
    fontSize: '20px',
    fontWeight: '600',
    marginBottom: '20px',
    color: '#e8eefc',
  },
  transactionHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '20px',
  },
  transactionControls: {
    display: 'flex',
    gap: '10px',
    alignItems: 'center',
  },
  select: {
    padding: '8px 12px',
    background: 'rgba(255, 255, 255, 0.05)',
    border: '1px solid rgba(255, 255, 255, 0.1)',
    borderRadius: '8px',
    color: '#e8eefc',
    fontSize: '12px',
    cursor: 'pointer',
  },
  downloadButton: {
    padding: '8px',
    background: 'rgba(255, 255, 255, 0.05)',
    border: '1px solid rgba(255, 255, 255, 0.1)',
    borderRadius: '8px',
    color: '#e8eefc',
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  transactionsList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '20px',
  },
  transactionGroup: {
    display: 'flex',
    flexDirection: 'column',
    gap: '12px',
  },
  transactionDateHeader: {
    fontSize: '12px',
    fontWeight: '600',
    color: 'rgba(232, 238, 252, 0.5)',
    marginBottom: '8px',
    textTransform: 'uppercase',
  },
  transactionItem: {
    display: 'flex',
    alignItems: 'center',
    gap: '16px',
    padding: '16px',
    background: 'rgba(255, 255, 255, 0.05)',
    borderRadius: '12px',
    border: '1px solid rgba(255, 255, 255, 0.1)',
  },
  transactionIcon: {
    flexShrink: 0,
  },
  iconCircle: {
    width: '40px',
    height: '40px',
    borderRadius: '50%',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '18px',
    fontWeight: 'bold',
  },
  transactionDetails: {
    flex: 1,
  },
  transactionTitle: {
    fontSize: '14px',
    fontWeight: '600',
    marginBottom: '4px',
    color: '#e8eefc',
  },
  transactionDescription: {
    fontSize: '12px',
    color: 'rgba(232, 238, 252, 0.6)',
  },
  transactionAmount: {
    fontSize: '16px',
    fontWeight: '600',
  },
};

