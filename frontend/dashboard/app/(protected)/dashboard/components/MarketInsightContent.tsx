'use client';

import { useCallback, useEffect, useState } from 'react';
import type { StockSignalDaily } from '../types';
import {
  WATCHLIST_SYMBOLS,
  fetchLatestSignal,
  fetchSignalHistory,
} from '@/app/lib/market';

function formatPct(value: number | null | undefined, signed = true): string {
  if (value == null) return '—';
  const prefix = signed && value > 0 ? '+' : '';
  return `${prefix}${value.toFixed(2)}%`;
}

function formatNum(value: number | null | undefined, decimals = 2): string {
  if (value == null) return '—';
  return value.toLocaleString('en-US', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  });
}

function formatDate(iso: string): string {
  return new Date(iso + 'T00:00:00').toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
}

function changeColor(value: number | null | undefined): string {
  if (value == null || value === 0) return 'rgba(232, 238, 252, 0.6)';
  return value > 0 ? '#4ade80' : '#f87171';
}

function signalTypeStyle(type: string | null): React.CSSProperties {
  const map: Record<string, React.CSSProperties> = {
    TREND: { background: 'rgba(74, 222, 128, 0.15)', color: '#4ade80', borderColor: 'rgba(74, 222, 128, 0.35)' },
    GAME: { background: 'rgba(255, 107, 53, 0.15)', color: '#ff6b35', borderColor: 'rgba(255, 107, 53, 0.35)' },
    SENTIMENT: { background: 'rgba(157, 78, 221, 0.15)', color: '#c084fc', borderColor: 'rgba(157, 78, 221, 0.35)' },
  };
  return map[type ?? ''] ?? {
    background: 'rgba(255, 255, 255, 0.08)',
    color: 'rgba(232, 238, 252, 0.6)',
    borderColor: 'rgba(255, 255, 255, 0.15)',
  };
}

function ScoreBar({ label, value, max }: { label: string; value: number | null; max: number }) {
  const pct = value != null ? Math.min(100, Math.max(0, (value / max) * 100)) : 0;
  return (
    <div style={styles.scoreBarWrap}>
      <div style={styles.scoreBarHeader}>
        <span style={styles.scoreBarLabel}>{label}</span>
        <span style={styles.scoreBarValue}>{formatNum(value, 1)}</span>
      </div>
      <div style={styles.scoreBarTrack}>
        <div style={{ ...styles.scoreBarFill, width: `${pct}%` }} />
      </div>
    </div>
  );
}

function MetricCard({
  label,
  value,
  color,
}: {
  label: string;
  value: string;
  color?: string;
}) {
  return (
    <div style={styles.metricCard}>
      <div style={styles.metricLabel}>{label}</div>
      <div style={{ ...styles.metricValue, color: color ?? '#e8eefc' }}>{value}</div>
    </div>
  );
}

export default function MarketInsightContent() {
  const [symbol, setSymbol] = useState(WATCHLIST_SYMBOLS[0]);
  const [latest, setLatest] = useState<StockSignalDaily | null>(null);
  const [history, setHistory] = useState<StockSignalDaily[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadData = useCallback(async (sym: string) => {
    setLoading(true);
    setError(null);
    try {
      const [latestData, historyData] = await Promise.all([
        fetchLatestSignal(sym),
        fetchSignalHistory(sym, 30),
      ]);
      setLatest(latestData);
      setHistory(historyData);
    } catch (e) {
      setLatest(null);
      setHistory([]);
      setError(e instanceof Error ? e.message : 'Failed to load analysis data');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData(symbol);
  }, [symbol, loadData]);

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <div>
          <h2 style={styles.title}>Market Insight</h2>
          <p style={styles.subtitle}>Daily stock signal analysis</p>
        </div>
        <div style={styles.symbolPicker}>
          {WATCHLIST_SYMBOLS.map((sym) => (
            <button
              key={sym}
              type="button"
              onClick={() => setSymbol(sym)}
              style={{
                ...styles.symbolBtn,
                ...(symbol === sym ? styles.symbolBtnActive : {}),
              }}
            >
              {sym}
            </button>
          ))}
        </div>
      </div>

      {loading && (
        <div style={styles.stateBox}>Loading analysis for {symbol}…</div>
      )}

      {!loading && error && (
        <div style={styles.errorBox}>
          <p>{error}</p>
          <button type="button" style={styles.retryBtn} onClick={() => loadData(symbol)}>
            Retry
          </button>
        </div>
      )}

      {!loading && !error && latest && (
        <>
          <section style={styles.heroSection}>
            <div style={styles.heroLeft}>
              <div style={styles.heroTop}>
                <span style={styles.heroSymbol}>{latest.symbol}</span>
                {latest.signalType && (
                  <span style={{ ...styles.signalBadge, ...signalTypeStyle(latest.signalType) }}>
                    {latest.signalType}
                  </span>
                )}
                {latest.isNewHigh30d && <span style={styles.newHighBadge}>30D HIGH</span>}
              </div>
              <div style={styles.heroPrice}>{formatNum(latest.closePrice)}</div>
              <div style={styles.heroMeta}>As of {formatDate(latest.tradeDate)}</div>
              {latest.signalReason && (
                <p style={styles.signalReason}>{latest.signalReason}</p>
              )}
            </div>
            <div style={styles.heroScores}>
              <ScoreBar label="Trend score" value={latest.trendScore} max={50} />
              <ScoreBar label="Heat score" value={latest.heatScore} max={85} />
            </div>
          </section>

          <section style={styles.section}>
            <h3 style={styles.sectionTitle}>Returns</h3>
            <div style={styles.metricsGrid}>
              <MetricCard label="1D" value={formatPct(latest.return1d)} color={changeColor(latest.return1d)} />
              <MetricCard label="5D" value={formatPct(latest.return5d)} color={changeColor(latest.return5d)} />
              <MetricCard label="14D" value={formatPct(latest.return14d)} color={changeColor(latest.return14d)} />
              <MetricCard label="30D" value={formatPct(latest.return30d)} color={changeColor(latest.return30d)} />
            </div>
          </section>

          <section style={styles.section}>
            <h3 style={styles.sectionTitle}>Risk & strength</h3>
            <div style={styles.metricsGrid}>
              <MetricCard label="Max drawdown (14D)" value={formatPct(latest.maxDrawdown14d)} color={changeColor(latest.maxDrawdown14d)} />
              <MetricCard label="Volatility (14D)" value={formatPct(latest.volatility14d, false)} />
              <MetricCard label="Relative strength (14D)" value={formatNum(latest.relativeStrength14d)} color={changeColor(latest.relativeStrength14d)} />
              <MetricCard
                label="Streak"
                value={`↑${latest.consecutiveUpDays} / ↓${latest.consecutiveDownDays}`}
              />
            </div>
          </section>

          {history.length > 0 && (
            <section style={styles.section}>
              <h3 style={styles.sectionTitle}>Recent history</h3>
              <div style={styles.tableWrap}>
                <table style={styles.table}>
                  <thead>
                    <tr>
                      <th style={styles.th}>Date</th>
                      <th style={{ ...styles.th, textAlign: 'right' }}>Close</th>
                      <th style={{ ...styles.th, textAlign: 'right' }}>1D</th>
                      <th style={{ ...styles.th, textAlign: 'right' }}>14D</th>
                      <th style={{ ...styles.th, textAlign: 'right' }}>Trend</th>
                      <th style={{ ...styles.th, textAlign: 'right' }}>Heat</th>
                      <th style={{ ...styles.th, textAlign: 'center' }}>Signal</th>
                    </tr>
                  </thead>
                  <tbody>
                    {[...history].reverse().map((row) => (
                      <tr key={row.id} style={styles.tr}>
                        <td style={styles.td}>{formatDate(row.tradeDate)}</td>
                        <td style={{ ...styles.td, textAlign: 'right' }}>{formatNum(row.closePrice)}</td>
                        <td style={{ ...styles.td, textAlign: 'right', color: changeColor(row.return1d) }}>
                          {formatPct(row.return1d)}
                        </td>
                        <td style={{ ...styles.td, textAlign: 'right', color: changeColor(row.return14d) }}>
                          {formatPct(row.return14d)}
                        </td>
                        <td style={{ ...styles.td, textAlign: 'right' }}>{formatNum(row.trendScore, 1)}</td>
                        <td style={{ ...styles.td, textAlign: 'right' }}>{formatNum(row.heatScore, 1)}</td>
                        <td style={{ ...styles.td, textAlign: 'center' }}>
                          {row.signalType ? (
                            <span style={{ ...styles.signalBadgeSm, ...signalTypeStyle(row.signalType) }}>
                              {row.signalType}
                            </span>
                          ) : (
                            '—'
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </section>
          )}
        </>
      )}
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  container: { padding: '20px 0' },
  header: {
    display: 'flex',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    gap: '20px',
    marginBottom: '28px',
  },
  title: { fontSize: '24px', fontWeight: '600', marginBottom: '6px', color: '#e8eefc' },
  subtitle: { fontSize: '14px', color: 'rgba(232, 238, 252, 0.5)' },
  symbolPicker: { display: 'flex', flexWrap: 'wrap', gap: '8px' },
  symbolBtn: {
    padding: '8px 14px',
    background: 'rgba(255, 255, 255, 0.05)',
    border: '1px solid rgba(255, 255, 255, 0.12)',
    borderRadius: '8px',
    color: 'rgba(232, 238, 252, 0.7)',
    fontSize: '13px',
    fontWeight: '600',
    cursor: 'pointer',
  },
  symbolBtnActive: {
    background: 'rgba(255, 107, 53, 0.15)',
    borderColor: '#ff6b35',
    color: '#ff6b35',
  },
  stateBox: {
    padding: '48px',
    textAlign: 'center',
    color: 'rgba(232, 238, 252, 0.5)',
    fontSize: '15px',
  },
  errorBox: {
    padding: '24px',
    background: 'rgba(248, 113, 113, 0.1)',
    border: '1px solid rgba(248, 113, 113, 0.25)',
    borderRadius: '12px',
    color: '#f87171',
    fontSize: '14px',
  },
  retryBtn: {
    marginTop: '12px',
    padding: '8px 16px',
    background: 'rgba(255, 255, 255, 0.08)',
    border: '1px solid rgba(255, 255, 255, 0.15)',
    borderRadius: '8px',
    color: '#e8eefc',
    cursor: 'pointer',
    fontSize: '13px',
  },
  heroSection: {
    display: 'grid',
    gridTemplateColumns: '1fr minmax(240px, 320px)',
    gap: '24px',
    marginBottom: '32px',
    padding: '24px',
    background: 'rgba(255, 255, 255, 0.04)',
    borderRadius: '14px',
    border: '1px solid rgba(255, 255, 255, 0.1)',
  },
  heroLeft: { minWidth: 0 },
  heroTop: { display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap', marginBottom: '12px' },
  heroSymbol: { fontSize: '20px', fontWeight: '700', color: '#ff6b35' },
  heroPrice: { fontSize: '32px', fontWeight: '600', color: '#e8eefc', marginBottom: '6px' },
  heroMeta: { fontSize: '13px', color: 'rgba(232, 238, 252, 0.5)', marginBottom: '14px' },
  signalReason: {
    fontSize: '14px',
    lineHeight: 1.5,
    color: 'rgba(232, 238, 252, 0.75)',
    margin: 0,
    padding: '12px 14px',
    background: 'rgba(255, 255, 255, 0.04)',
    borderRadius: '8px',
    borderLeft: '3px solid #ff6b35',
  },
  heroScores: { display: 'flex', flexDirection: 'column', gap: '18px', justifyContent: 'center' },
  signalBadge: {
    display: 'inline-block',
    padding: '4px 10px',
    borderRadius: '6px',
    fontSize: '11px',
    fontWeight: '700',
    letterSpacing: '0.4px',
    border: '1px solid',
  },
  signalBadgeSm: {
    display: 'inline-block',
    padding: '3px 8px',
    borderRadius: '5px',
    fontSize: '10px',
    fontWeight: '700',
    letterSpacing: '0.3px',
    border: '1px solid',
  },
  newHighBadge: {
    padding: '4px 8px',
    borderRadius: '6px',
    fontSize: '10px',
    fontWeight: '700',
    background: 'rgba(250, 204, 21, 0.15)',
    color: '#facc15',
    border: '1px solid rgba(250, 204, 21, 0.35)',
  },
  scoreBarWrap: {},
  scoreBarHeader: { display: 'flex', justifyContent: 'space-between', marginBottom: '6px' },
  scoreBarLabel: { fontSize: '12px', color: 'rgba(232, 238, 252, 0.55)', textTransform: 'uppercase', letterSpacing: '0.4px' },
  scoreBarValue: { fontSize: '14px', fontWeight: '600', color: '#e8eefc' },
  scoreBarTrack: { height: '8px', background: 'rgba(255, 255, 255, 0.08)', borderRadius: '4px', overflow: 'hidden' },
  scoreBarFill: { height: '100%', background: 'linear-gradient(90deg, #ff6b35, #ff8c5a)', borderRadius: '4px', transition: 'width 0.3s' },
  section: { marginBottom: '32px' },
  sectionTitle: {
    fontSize: '14px',
    fontWeight: '600',
    marginBottom: '14px',
    color: 'rgba(232, 238, 252, 0.85)',
    textTransform: 'uppercase',
    letterSpacing: '0.5px',
  },
  metricsGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(160px, 1fr))',
    gap: '12px',
  },
  metricCard: {
    padding: '16px',
    background: 'rgba(255, 255, 255, 0.04)',
    borderRadius: '10px',
    border: '1px solid rgba(255, 255, 255, 0.08)',
  },
  metricLabel: { fontSize: '11px', color: 'rgba(232, 238, 252, 0.5)', marginBottom: '8px', textTransform: 'uppercase', letterSpacing: '0.4px' },
  metricValue: { fontSize: '18px', fontWeight: '600' },
  tableWrap: {
    overflowX: 'auto',
    borderRadius: '12px',
    border: '1px solid rgba(255, 255, 255, 0.1)',
    background: 'rgba(255, 255, 255, 0.03)',
  },
  table: { width: '100%', borderCollapse: 'collapse', fontSize: '13px' },
  th: {
    padding: '12px 14px',
    textAlign: 'left',
    fontWeight: '600',
    color: 'rgba(232, 238, 252, 0.5)',
    fontSize: '11px',
    textTransform: 'uppercase',
    letterSpacing: '0.4px',
    borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
  },
  tr: { borderBottom: '1px solid rgba(255, 255, 255, 0.06)' },
  td: { padding: '12px 14px', color: '#e8eefc' },
};
