export interface Transaction {
  id: string;
  title: string;
  description: string;
  amount: number;
  type: 'income' | 'expense';
  date: Date;
}

export interface WalletSummary {
  outcome: number;
  income: number;
  thisWeek: { value: string; change: string; trend: 'up' | 'down' };
  thisMonth: { value: string; change: string; trend: 'up' | 'down' };
  upcoming: { value: string; change: string; trend: 'up' | 'down' };
}

export type TabType = 'summary' | 'cards' | 'history' | 'installments' | 'market-insight' | 'analysis';

export type AnalysisMarket = 'SP500' | 'NIKKEI_200';
export type AnalysisCountry = 'US' | 'JP';
export type AnalysisRating = 'STRONG' | 'POSITIVE' | 'NEUTRAL' | 'CAUTIOUS';

export interface EarningsAnalysis {
  symbol: string;
  company: string;
  market: AnalysisMarket;
  fiscalPeriod: string;
  currency: string;
  revenueMillions: number;
  revenueGrowthPct: number;
  operatingMarginPct: number;
  eps: number;
  epsGrowthPct: number;
  qualityScore: number;
  rating: AnalysisRating;
  highlights: string[];
  risks: string[];
}

export interface IpoAnalysis {
  company: string;
  ticker: string | null;
  country: AnalysisCountry;
  exchange: string;
  expectedDate: string;
  status: 'EXPECTED' | 'FILED' | 'PRICED';
  priceRange: string | null;
  sector: string;
  opportunityScore: number;
  summary: string;
}

export interface AnalysisOverview {
  generatedAt: string;
  dataSource: string;
  isDemo: boolean;
  earnings: EarningsAnalysis[];
  ipos: IpoAnalysis[];
  summary: {
    companiesAnalyzed: number;
    upcomingIpos: number;
    averageQualityScore: number;
    positiveEarnings: number;
  };
}

/** Mirrors backend StockSignalDaily entity */
export interface StockSignalDaily {
  id: number;
  symbol: string;
  tradeDate: string;
  closePrice: number;
  return1d: number | null;
  return5d: number | null;
  return14d: number | null;
  return30d: number | null;
  consecutiveUpDays: number;
  consecutiveDownDays: number;
  maxDrawdown14d: number | null;
  volatility14d: number | null;
  relativeStrength14d: number | null;
  isNewHigh30d: boolean;
  trendScore: number | null;
  heatScore: number | null;
  signalType: 'TREND' | 'GAME' | 'SENTIMENT' | string | null;
  signalReason: string | null;
  createdAt: string;
  updatedAt: string;
}
export type PeriodType = '1d' | '1w' | '1m' | '3m' | '1y' | 'all';

export type CardType = 'paypay' | '7-11' | 'Mizuho' | 'Rakuten' | 'Visa';

export interface Card {
  id: string;
  number: string;
  holder: string;
  validThru: string;
  type: CardType;
  color?: string;
}

export interface CreateCardRequest {
  type: CardType;
  number: string;
  holder: string;
  validThru: string;
}
