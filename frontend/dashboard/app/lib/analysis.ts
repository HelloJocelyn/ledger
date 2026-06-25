import { ANALYSIS_API_BASE, apiGet } from './api';
import type { AnalysisOverview } from '@/app/(protected)/dashboard/types';

export async function fetchAnalysisOverview(): Promise<AnalysisOverview> {
  return apiGet<AnalysisOverview>('/api/analysis/overview', ANALYSIS_API_BASE);
}
