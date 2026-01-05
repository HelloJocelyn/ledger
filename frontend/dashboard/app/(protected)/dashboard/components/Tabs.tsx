import type { TabType } from '../types';

interface TabsProps {
  selectedTab: TabType;
  onTabChange: (tab: TabType) => void;
}

export default function Tabs({ selectedTab, onTabChange }: TabsProps) {
  const tabs = [
    { key: 'summary' as TabType, label: 'summary' },
    { key: 'cards' as TabType, label: 'my cards' },
    { key: 'history' as TabType, label: 'credit history' },
    { key: 'installments' as TabType, label: 'installments' },
  ];

  return (
    <div style={styles.tabs}>
      {tabs.map((tab) => (
        <button
          key={tab.key}
          onClick={() => onTabChange(tab.key)}
          style={{
            ...styles.tab,
            ...(selectedTab === tab.key ? styles.tabActive : {}),
          }}
        >
          {tab.label}
        </button>
      ))}
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  tabs: {
    display: 'flex',
    gap: '20px',
    marginBottom: '30px',
    borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
  },
  tab: {
    padding: '12px 20px',
    background: 'transparent',
    border: 'none',
    color: 'rgba(232, 238, 252, 0.6)',
    cursor: 'pointer',
    fontSize: '14px',
    fontWeight: '500',
    textTransform: 'capitalize',
    borderBottom: '2px solid transparent',
    transition: 'all 0.2s',
  },
  tabActive: {
    color: '#ff6b35',
    borderBottomColor: '#ff6b35',
  },
};

