'use client';

import { useCallback, useEffect, useMemo, useState } from 'react';
import { fetchAnalysisOverview } from '@/app/lib/analysis';
import type {
  AnalysisCountry,
  AnalysisMarket,
  AnalysisOverview,
  EarningsAnalysis,
  IpoAnalysis,
} from '../types';

const ALL = 'ALL' as const;

function pct(value: number): string {
  return `${value > 0 ? '+' : ''}${value.toFixed(1)}%`;
}

function compactNumber(value: number, currency: string): string {
  return `${new Intl.NumberFormat('en-US', { maximumFractionDigits: 0 }).format(value)}M ${currency}`;
}

function scoreColor(score: number): string {
  if (score >= 80) return '#4ade80';
  if (score >= 65) return '#60a5fa';
  if (score >= 45) return '#facc15';
  return '#f87171';
}

function FilterButton({
  active,
  children,
  onClick,
}: {
  active: boolean;
  children: React.ReactNode;
  onClick: () => void;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      style={{ ...styles.filterButton, ...(active ? styles.filterButtonActive : {}) }}
    >
      {children}
    </button>
  );
}

function EarningsCard({ item }: { item: EarningsAnalysis }) {
  return (
    <article style={styles.card}>
      <div style={styles.cardTop}>
        <div>
          <div style={styles.eyebrow}>{item.market.replace('_', ' · ')}</div>
          <h3 style={styles.cardTitle}>{item.company}</h3>
          <div style={styles.symbol}>{item.symbol} · {item.fiscalPeriod}</div>
        </div>
        <div style={{ ...styles.score, color: scoreColor(item.qualityScore) }}>
          {item.qualityScore}
          <span style={styles.scoreUnit}>/100</span>
        </div>
      </div>
      <div style={styles.metrics}>
        <div><span style={styles.metricLabel}>Revenue</span><strong>{compactNumber(item.revenueMillions, item.currency)}</strong></div>
        <div><span style={styles.metricLabel}>Growth</span><strong style={{ color: scoreColor(item.revenueGrowthPct >= 10 ? 80 : 55) }}>{pct(item.revenueGrowthPct)}</strong></div>
        <div><span style={styles.metricLabel}>Op. margin</span><strong>{item.operatingMarginPct.toFixed(1)}%</strong></div>
        <div><span style={styles.metricLabel}>EPS growth</span><strong>{pct(item.epsGrowthPct)}</strong></div>
      </div>
      <div style={styles.note}><span style={styles.goodDot} />{item.highlights[0]}</div>
      <div style={styles.note}><span style={styles.riskDot} />{item.risks[0]}</div>
    </article>
  );
}

function IpoRow({ item }: { item: IpoAnalysis }) {
  return (
    <tr style={styles.tr}>
      <td style={styles.td}>
        <strong>{item.company}</strong>
        <div style={styles.muted}>{item.ticker ?? 'Ticker pending'} · {item.sector}</div>
      </td>
      <td style={styles.td}>{item.country === 'US' ? 'United States' : 'Japan'}</td>
      <td style={styles.td}>{item.exchange}</td>
      <td style={styles.td}>{new Date(`${item.expectedDate}T00:00:00`).toLocaleDateString()}</td>
      <td style={styles.td}>{item.priceRange ?? 'TBD'}</td>
      <td style={{ ...styles.td, color: scoreColor(item.opportunityScore), fontWeight: 700 }}>
        {item.opportunityScore}
      </td>
    </tr>
  );
}

export default function AnalysisContent() {
  const [data, setData] = useState<AnalysisOverview | null>(null);
  const [market, setMarket] = useState<AnalysisMarket | typeof ALL>(ALL);
  const [country, setCountry] = useState<AnalysisCountry | typeof ALL>(ALL);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setData(await fetchAnalysisOverview());
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Unable to load analysis');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const earnings = useMemo(
    () => data?.earnings.filter((item) => market === ALL || item.market === market) ?? [],
    [data, market],
  );
  const ipos = useMemo(
    () => data?.ipos.filter((item) => country === ALL || item.country === country) ?? [],
    [data, country],
  );

  if (loading) return <div style={styles.state}>Loading company and IPO analysis…</div>;
  if (error || !data) {
    return (
      <div style={styles.error}>
        <div>{error ?? 'Analysis data is unavailable.'}</div>
        <button type="button" onClick={load} style={styles.retry}>Retry</button>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <header style={styles.header}>
        <div>
          <div style={styles.kicker}>FUNDAMENTALS & NEW LISTINGS</div>
          <h2 style={styles.title}>Company Analysis</h2>
          <p style={styles.subtitle}>Financial statements across the S&P 500 and Nikkei 200, plus US and Japan IPO watchlists.</p>
        </div>
        {data.isDemo && <span style={styles.demoBadge}>DEMO DATA</span>}
      </header>

      <section style={styles.summaryGrid}>
        <div style={styles.summaryCard}><span>Companies</span><strong>{data.summary.companiesAnalyzed}</strong></div>
        <div style={styles.summaryCard}><span>Positive results</span><strong>{data.summary.positiveEarnings}</strong></div>
        <div style={styles.summaryCard}><span>Average quality</span><strong>{data.summary.averageQualityScore.toFixed(1)}</strong></div>
        <div style={styles.summaryCard}><span>IPO pipeline</span><strong>{data.summary.upcomingIpos}</strong></div>
      </section>

      <section style={styles.section}>
        <div style={styles.sectionHeader}>
          <div>
            <h3 style={styles.sectionTitle}>Financial statement analysis</h3>
            <p style={styles.sectionSubtitle}>Growth, profitability, earnings momentum, and key risks.</p>
          </div>
          <div style={styles.filters}>
            <FilterButton active={market === ALL} onClick={() => setMarket(ALL)}>All</FilterButton>
            <FilterButton active={market === 'SP500'} onClick={() => setMarket('SP500')}>S&P 500</FilterButton>
            <FilterButton active={market === 'NIKKEI_200'} onClick={() => setMarket('NIKKEI_200')}>Nikkei 200</FilterButton>
          </div>
        </div>
        <div style={styles.cardGrid}>{earnings.map((item) => <EarningsCard key={`${item.market}-${item.symbol}`} item={item} />)}</div>
      </section>

      <section style={styles.section}>
        <div style={styles.sectionHeader}>
          <div>
            <h3 style={styles.sectionTitle}>IPO calendar</h3>
            <p style={styles.sectionSubtitle}>Upcoming and filed listings, ranked by opportunity score.</p>
          </div>
          <div style={styles.filters}>
            <FilterButton active={country === ALL} onClick={() => setCountry(ALL)}>All</FilterButton>
            <FilterButton active={country === 'US'} onClick={() => setCountry('US')}>United States</FilterButton>
            <FilterButton active={country === 'JP'} onClick={() => setCountry('JP')}>Japan</FilterButton>
          </div>
        </div>
        <div style={styles.tableWrap}>
          <table style={styles.table}>
            <thead><tr><th style={styles.th}>Company</th><th style={styles.th}>Market</th><th style={styles.th}>Exchange</th><th style={styles.th}>Expected</th><th style={styles.th}>Range</th><th style={styles.th}>Score</th></tr></thead>
            <tbody>{ipos.map((item) => <IpoRow key={`${item.country}-${item.company}`} item={item} />)}</tbody>
          </table>
        </div>
      </section>

      <p style={styles.source}>Source: {data.dataSource} · Generated {new Date(data.generatedAt).toLocaleString()}</p>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  container: { padding: '16px 0 40px' },
  state: { padding: '60px 20px', textAlign: 'center', color: 'rgba(232,238,252,.55)' },
  error: { padding: '24px', border: '1px solid rgba(248,113,113,.3)', borderRadius: 14, color: '#f87171', background: 'rgba(248,113,113,.08)' },
  retry: { marginTop: 12, padding: '8px 16px', borderRadius: 8, border: '1px solid rgba(255,255,255,.15)', color: '#fff', background: 'rgba(255,255,255,.08)', cursor: 'pointer' },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 20, marginBottom: 24 },
  kicker: { color: '#ff6b35', fontSize: 11, fontWeight: 800, letterSpacing: 1.2, marginBottom: 8 },
  title: { margin: 0, fontSize: 30, letterSpacing: '-.5px' },
  subtitle: { margin: '8px 0 0', maxWidth: 680, color: 'rgba(232,238,252,.58)', lineHeight: 1.55 },
  demoBadge: { padding: '6px 10px', border: '1px solid rgba(250,204,21,.35)', borderRadius: 999, color: '#facc15', background: 'rgba(250,204,21,.1)', fontSize: 10, fontWeight: 800, letterSpacing: .7 },
  summaryGrid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fit,minmax(140px,1fr))', gap: 12, marginBottom: 34 },
  summaryCard: { display: 'flex', flexDirection: 'column', gap: 8, padding: 18, borderRadius: 12, border: '1px solid rgba(255,255,255,.09)', background: 'linear-gradient(145deg,rgba(255,255,255,.06),rgba(255,255,255,.025))', color: 'rgba(232,238,252,.55)', fontSize: 12 },
  section: { marginBottom: 38 },
  sectionHeader: { display: 'flex', flexWrap: 'wrap', justifyContent: 'space-between', alignItems: 'flex-end', gap: 16, marginBottom: 16 },
  sectionTitle: { margin: 0, fontSize: 18 },
  sectionSubtitle: { margin: '5px 0 0', color: 'rgba(232,238,252,.48)', fontSize: 13 },
  filters: { display: 'flex', flexWrap: 'wrap', gap: 7 },
  filterButton: { padding: '7px 11px', border: '1px solid rgba(255,255,255,.1)', borderRadius: 7, color: 'rgba(232,238,252,.6)', background: 'rgba(255,255,255,.035)', cursor: 'pointer', fontSize: 12 },
  filterButtonActive: { borderColor: '#ff6b35', color: '#ff8c5a', background: 'rgba(255,107,53,.12)' },
  cardGrid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fit,minmax(280px,1fr))', gap: 14 },
  card: { padding: 19, borderRadius: 14, border: '1px solid rgba(255,255,255,.09)', background: 'rgba(255,255,255,.035)' },
  cardTop: { display: 'flex', justifyContent: 'space-between', gap: 16, marginBottom: 18 },
  eyebrow: { color: '#ff8c5a', fontWeight: 700, fontSize: 10, letterSpacing: .8 },
  cardTitle: { margin: '5px 0 3px', fontSize: 18 },
  symbol: { color: 'rgba(232,238,252,.45)', fontSize: 12 },
  score: { fontSize: 27, fontWeight: 750, whiteSpace: 'nowrap' },
  scoreUnit: { fontSize: 10, color: 'rgba(232,238,252,.35)', marginLeft: 2 },
  metrics: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 13, padding: '14px 0', marginBottom: 13, borderTop: '1px solid rgba(255,255,255,.07)', borderBottom: '1px solid rgba(255,255,255,.07)', fontSize: 13 },
  metricLabel: { display: 'block', marginBottom: 4, color: 'rgba(232,238,252,.4)', fontSize: 10, textTransform: 'uppercase' },
  note: { display: 'flex', alignItems: 'flex-start', gap: 8, marginTop: 9, color: 'rgba(232,238,252,.63)', fontSize: 12, lineHeight: 1.45 },
  goodDot: { width: 6, height: 6, flex: '0 0 auto', marginTop: 5, borderRadius: 999, background: '#4ade80' },
  riskDot: { width: 6, height: 6, flex: '0 0 auto', marginTop: 5, borderRadius: 999, background: '#f87171' },
  tableWrap: { overflowX: 'auto', borderRadius: 13, border: '1px solid rgba(255,255,255,.09)' },
  table: { width: '100%', minWidth: 720, borderCollapse: 'collapse', background: 'rgba(255,255,255,.025)', fontSize: 12 },
  th: { padding: '12px 14px', textAlign: 'left', color: 'rgba(232,238,252,.4)', borderBottom: '1px solid rgba(255,255,255,.09)', textTransform: 'uppercase', fontSize: 10, letterSpacing: .5 },
  tr: { borderBottom: '1px solid rgba(255,255,255,.055)' },
  td: { padding: '13px 14px', color: 'rgba(232,238,252,.78)' },
  muted: { marginTop: 4, color: 'rgba(232,238,252,.38)', fontSize: 10 },
  source: { color: 'rgba(232,238,252,.3)', fontSize: 10, textAlign: 'right' },
};
