import { useState } from 'react';
import type { TabType, PeriodType, Transaction } from '../types';
import Tabs from './Tabs';
import IncomeSection from './IncomeSection';
import TransactionsSection from './TransactionsSection';
import CardsContent from './CardsContent';
import CreditHistoryContent from './CreditHistoryContent';
import InstallmentsContent from './InstallmentsContent';

interface MainContentProps {
  transactions: Transaction[];
}

export default function MainContent({ transactions }: MainContentProps) {
  const [selectedTab, setSelectedTab] = useState<TabType>('summary');
  const [selectedPeriod, setSelectedPeriod] = useState<PeriodType>('1w');
  const [selectedDate, setSelectedDate] = useState<string>('Apr 04');

  const renderContent = () => {
    switch (selectedTab) {
      case 'summary':
        return (
          <>
            <IncomeSection
              selectedPeriod={selectedPeriod}
              selectedDate={selectedDate}
              onPeriodChange={setSelectedPeriod}
              onDateChange={setSelectedDate}
            />
            <TransactionsSection transactions={transactions} />
          </>
        );
      case 'cards':
        return <CardsContent />;
      case 'history':
        return <CreditHistoryContent />;
      case 'installments':
        return <InstallmentsContent />;
      default:
        return null;
    }
  };

  return (
    <main style={styles.mainContent}>
      <Tabs selectedTab={selectedTab} onTabChange={setSelectedTab} />
      {renderContent()}
    </main>
  );
}

const styles: Record<string, React.CSSProperties> = {
  mainContent: {
    flex: 1,
    padding: '30px',
    overflowY: 'auto',
  },
};

