import Cards from './Cards';
import WalletSummaryComponent from './WalletSummary';
import type { WalletSummary } from '../types';

interface RightSidebarProps {
  walletSummary: WalletSummary;
}

export default function RightSidebar({ walletSummary }: RightSidebarProps) {
  return (
    <aside style={styles.rightSidebar}>
      {/* Header */}
      <div style={styles.rightHeader}>
        <button style={styles.iconButton}>
          <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
            <path fillRule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clipRule="evenodd" />
          </svg>
        </button>
        <button style={styles.iconButton}>
          <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
            <path d="M10 2a6 6 0 00-6 6v3.586l-.707.707A1 1 0 004 14h12a1 1 0 00.707-1.707L16 11.586V8a6 6 0 00-6-6zM10 18a3 3 0 01-3-3h6a3 3 0 01-3 3z" />
          </svg>
        </button>
        <div style={styles.avatar}>
          <div style={styles.avatarInner}>AR</div>
        </div>
      </div>

      <Cards />
      <WalletSummaryComponent summary={walletSummary} />
    </aside>
  );
}

const styles: Record<string, React.CSSProperties> = {
  rightSidebar: {
    width: '320px',
    background: 'rgba(255, 255, 255, 0.03)',
    borderLeft: '1px solid rgba(255, 255, 255, 0.1)',
    padding: '20px',
    display: 'flex',
    flexDirection: 'column',
    gap: '30px',
  },
  rightHeader: {
    display: 'flex',
    justifyContent: 'flex-end',
    alignItems: 'center',
    gap: '12px',
  },
  iconButton: {
    width: '36px',
    height: '36px',
    background: 'rgba(255, 255, 255, 0.05)',
    border: '1px solid rgba(255, 255, 255, 0.1)',
    borderRadius: '8px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    color: '#e8eefc',
    cursor: 'pointer',
  },
  avatar: {
    width: '36px',
    height: '36px',
    borderRadius: '50%',
    background: '#ff6b35',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarInner: {
    color: '#fff',
    fontSize: '12px',
    fontWeight: '600',
  },
};

