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

export type TabType = 'summary' | 'cards' | 'history' | 'installments';
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

