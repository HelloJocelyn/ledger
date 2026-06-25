use chrono::NaiveDate;
use serde::{Deserialize, Serialize};

#[derive(Clone, Copy, Debug, Deserialize, PartialEq, Eq, Serialize)]
pub enum Market {
    #[serde(rename = "SP500")]
    Sp500,
    #[serde(rename = "NIKKEI_200")]
    Nikkei200,
}

#[derive(Clone, Copy, Debug, Deserialize, PartialEq, Eq, Serialize)]
#[serde(rename_all = "UPPERCASE")]
pub enum Country {
    Us,
    Jp,
}

#[derive(Clone, Debug, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct EarningsAnalysis {
    pub symbol: String,
    pub company: String,
    pub market: Market,
    pub fiscal_period: String,
    pub currency: String,
    pub revenue_millions: f64,
    pub revenue_growth_pct: f64,
    pub operating_margin_pct: f64,
    pub eps: f64,
    pub eps_growth_pct: f64,
    pub quality_score: u8,
    pub rating: Rating,
    pub highlights: Vec<String>,
    pub risks: Vec<String>,
}

#[derive(Clone, Debug, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct IpoAnalysis {
    pub company: String,
    pub ticker: Option<String>,
    pub country: Country,
    pub exchange: String,
    pub expected_date: NaiveDate,
    pub status: IpoStatus,
    pub price_range: Option<String>,
    pub sector: String,
    pub opportunity_score: u8,
    pub summary: String,
}

#[derive(Clone, Copy, Debug, PartialEq, Eq, Serialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum Rating {
    Strong,
    Positive,
    Neutral,
    Cautious,
}

#[derive(Clone, Copy, Debug, PartialEq, Eq, Serialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum IpoStatus {
    Expected,
    Filed,
    Priced,
}

#[derive(Debug, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct AnalysisOverview {
    pub generated_at: String,
    pub data_source: String,
    pub is_demo: bool,
    pub earnings: Vec<EarningsAnalysis>,
    pub ipos: Vec<IpoAnalysis>,
    pub summary: OverviewSummary,
}

#[derive(Debug, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct OverviewSummary {
    pub companies_analyzed: usize,
    pub upcoming_ipos: usize,
    pub average_quality_score: f64,
    pub positive_earnings: usize,
}

#[derive(Debug, Deserialize)]
pub struct EarningsQuery {
    pub market: Option<Market>,
}

#[derive(Debug, Deserialize)]
pub struct IpoQuery {
    pub country: Option<Country>,
}
