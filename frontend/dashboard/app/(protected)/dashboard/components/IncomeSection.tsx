import type { PeriodType } from '../types';

interface IncomeSectionProps {
  selectedPeriod: PeriodType;
  selectedDate: string;
  onPeriodChange: (period: PeriodType) => void;
  onDateChange: (date: string) => void;
}

export default function IncomeSection({
  selectedPeriod,
  selectedDate,
  onPeriodChange,
  onDateChange,
}: IncomeSectionProps) {
  const dates = ['Apr 01', 'Apr 02', 'Apr 03', 'Apr 04', 'Apr 05', 'Apr 06', 'Apr 07'];
  const periods: PeriodType[] = ['1d', '1w', '1m', '3m', '1y', 'all'];

  return (
    <section style={styles.section}>
      <h2 style={styles.sectionTitle}>Income</h2>

      {/* Chart Placeholder */}
      <div style={styles.chartContainer}>
        <svg width="100%" height="200" style={styles.chart}>
          <path
            d="M 20 150 Q 60 100, 100 80 T 180 60 T 260 70 T 340 50 T 420 40"
            stroke="#ff6b35"
            strokeWidth="2"
            fill="none"
          />
          <path
            d="M 20 160 Q 60 120, 100 100 T 180 80 T 260 90 T 340 70 T 420 60"
            stroke="#9d4edd"
            strokeWidth="2"
            fill="none"
          />
          <circle cx="180" cy="60" r="4" fill="#9d4edd" />
          <text x="180" y="50" textAnchor="middle" fill="#9d4edd" fontSize="12">
            05.04.20
          </text>
          <text x="180" y="40" textAnchor="middle" fill="#9d4edd" fontSize="12" fontWeight="bold">
            $840.00
          </text>
        </svg>
      </div>

      {/* Date Range */}
      <div style={styles.dateRange}>
        {dates.map((date) => (
          <button
            key={date}
            onClick={() => onDateChange(date)}
            style={{
              ...styles.dateButton,
              ...(selectedDate === date ? styles.dateButtonActive : {}),
            }}
          >
            {date}
          </button>
        ))}
      </div>

      {/* Period Filters */}
      <div style={styles.periodFilters}>
        {periods.map((period) => (
          <button
            key={period}
            onClick={() => onPeriodChange(period)}
            style={{
              ...styles.periodButton,
              ...(selectedPeriod === period ? styles.periodButtonActive : {}),
            }}
          >
            {period}
          </button>
        ))}
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
  chartContainer: {
    background: 'rgba(255, 255, 255, 0.05)',
    borderRadius: '12px',
    padding: '20px',
    marginBottom: '20px',
  },
  chart: {
    width: '100%',
  },
  dateRange: {
    display: 'flex',
    gap: '10px',
    marginBottom: '20px',
    flexWrap: 'wrap',
  },
  dateButton: {
    padding: '8px 16px',
    background: 'rgba(255, 255, 255, 0.05)',
    border: '1px solid rgba(255, 255, 255, 0.1)',
    borderRadius: '8px',
    color: 'rgba(232, 238, 252, 0.6)',
    cursor: 'pointer',
    fontSize: '12px',
    transition: 'all 0.2s',
  },
  dateButtonActive: {
    background: 'rgba(255, 107, 53, 0.2)',
    borderColor: '#ff6b35',
    color: '#ff6b35',
  },
  periodFilters: {
    display: 'flex',
    gap: '10px',
  },
  periodButton: {
    padding: '8px 16px',
    background: 'rgba(255, 255, 255, 0.05)',
    border: '1px solid rgba(255, 255, 255, 0.1)',
    borderRadius: '8px',
    color: 'rgba(232, 238, 252, 0.6)',
    cursor: 'pointer',
    fontSize: '12px',
    transition: 'all 0.2s',
  },
  periodButtonActive: {
    background: '#ff6b35',
    borderColor: '#ff6b35',
    color: '#fff',
  },
};

