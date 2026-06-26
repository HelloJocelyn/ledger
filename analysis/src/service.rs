use std::sync::Arc;

use chrono::Utc;

use crate::{
    model::{AnalysisOverview, Country, EarningsAnalysis, Market, OverviewSummary},
    repository::AnalysisRepository,
};

#[derive(Clone)]
pub struct AnalysisService {
    repository: Arc<dyn AnalysisRepository>,
}

impl AnalysisService {
    pub fn new(repository: Arc<dyn AnalysisRepository>) -> Self {
        Self { repository }
    }

    pub fn earnings(&self, market: Option<Market>) -> Vec<EarningsAnalysis> {
        self.repository
            .earnings()
            .into_iter()
            .filter(|item| market.is_none_or(|value| item.market == value))
            .collect()
    }

    pub fn ipos(&self, country: Option<Country>) -> Vec<crate::model::IpoAnalysis> {
        self.repository
            .ipos()
            .into_iter()
            .filter(|item| country.is_none_or(|value| item.country == value))
            .collect()
    }

    pub fn overview(&self) -> AnalysisOverview {
        let earnings = self.earnings(None);
        let ipos = self.ipos(None);
        let companies_analyzed = earnings.len();
        let average_quality_score = if companies_analyzed == 0 {
            0.0
        } else {
            earnings
                .iter()
                .map(|item| f64::from(item.quality_score))
                .sum::<f64>()
                / companies_analyzed as f64
        };
        let positive_earnings = earnings
            .iter()
            .filter(|item| item.quality_score >= 65)
            .count();

        AnalysisOverview {
            generated_at: Utc::now().to_rfc3339(),
            data_source: self.repository.source_name().into(),
            is_demo: self.repository.is_demo(),
            summary: OverviewSummary {
                companies_analyzed,
                upcoming_ipos: ipos.len(),
                average_quality_score,
                positive_earnings,
            },
            earnings,
            ipos,
        }
    }
}
