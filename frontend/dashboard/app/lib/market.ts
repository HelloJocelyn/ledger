import { apiGet, MARKET_API_BASE } from './api';
import type { StockSignalDaily } from '@/app/(protected)/dashboard/types';

export const WATCHLIST_SYMBOLS = ['AAPL', 'NVDA', 'MSFT', '7203.T', '9984.T'];

export async function fetchLatestSignal(symbol: string): Promise<StockSignalDaily> {
  return apiGet<StockSignalDaily>(
    `/api/stock-signals/${encodeURIComponent(symbol)}/latest`,
    MARKET_API_BASE
  );
}

export async function fetchSignalHistory(
  symbol: string,
  limit = 30
): Promise<StockSignalDaily[]> {
  return apiGet<StockSignalDaily[]>(
    `/api/stock-signals/${encodeURIComponent(symbol)}?limit=${limit}`,
    MARKET_API_BASE
  );
}
