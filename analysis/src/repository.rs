use chrono::NaiveDate;

use crate::{
    model::{Country, EarningsAnalysis, IpoAnalysis, IpoStatus, Market},
    scoring::{earnings_score, ipo_score, rating},
};

pub trait AnalysisRepository: Send + Sync {
    fn earnings(&self) -> Vec<EarningsAnalysis>;
    fn ipos(&self) -> Vec<IpoAnalysis>;
    fn source_name(&self) -> &'static str;
    fn is_demo(&self) -> bool;
}

#[derive(Default)]
pub struct EmbeddedRepository;

impl AnalysisRepository for EmbeddedRepository {
    fn earnings(&self) -> Vec<EarningsAnalysis> {
        vec![
            earnings(
                "MSFT", "Microsoft", Market::Sp500, "FY2025 Q3", "USD",
                70_066.0, 13.3, 45.7, 3.46, 17.7,
                &["Cloud revenue and operating income expanded."],
                &["AI infrastructure spending remains capital intensive."],
            ),
            earnings(
                "AAPL", "Apple", Market::Sp500, "FY2025 Q2", "USD",
                95_359.0, 5.1, 31.8, 1.65, 8.6,
                &["Services revenue supports resilient margins."],
                &["Hardware demand and China exposure require monitoring."],
            ),
            earnings(
                "NVDA", "NVIDIA", Market::Sp500, "FY2026 Q1", "USD",
                44_062.0, 69.2, 61.7, 0.76, 31.0,
                &["Data-center demand remains the primary growth engine."],
                &["Export restrictions and customer concentration add volatility."],
            ),
            earnings(
                "7203", "Toyota Motor", Market::Nikkei200, "FY2025", "JPY",
                48_036_704.0, 6.5, 10.0, 682.1, -3.6,
                &["Hybrid demand and global scale support cash generation."],
                &["Currency sensitivity and tariff uncertainty pressure forecasts."],
            ),
            earnings(
                "6758", "Sony Group", Market::Nikkei200, "FY2025", "JPY",
                12_957_064.0, -2.7, 10.2, 186.8, 10.8,
                &["Gaming, music, and imaging diversify earnings."],
                &["Console cycle and content costs can create uneven quarters."],
            ),
            earnings(
                "9984", "SoftBank Group", Market::Nikkei200, "FY2025", "JPY",
                7_243_383.0, 7.2, 17.3, 437.8, 142.0,
                &["Investment gains improved reported profitability."],
                &["Results remain highly sensitive to portfolio valuations."],
            ),
        ]
    }

    fn ipos(&self) -> Vec<IpoAnalysis> {
        vec![
            ipo(
                "Northstar Robotics", Some("NSTR"), Country::Us, "NASDAQ",
                "2026-07-16", IpoStatus::Filed, Some("$18–$21"), "Industrials",
                32.0, false, 420.0,
                "Warehouse automation platform with fast growth and execution risk.",
            ),
            ipo(
                "Harbor Health Systems", None, Country::Us, "NYSE",
                "2026-08-06", IpoStatus::Expected, None, "Health Care",
                18.0, true, 650.0,
                "Profitable care-management software business preparing its range.",
            ),
            ipo(
                "Sakura Compute", Some("459X"), Country::Jp, "Tokyo Growth",
                "2026-07-29", IpoStatus::Filed, Some("¥1,400–¥1,650"), "Technology",
                41.0, false, 110.0,
                "Domestic GPU cloud provider benefiting from sovereign AI demand.",
            ),
            ipo(
                "Mirai Mobility", Some("286B"), Country::Jp, "Tokyo Prime",
                "2026-09-10", IpoStatus::Expected, None, "Consumer Discretionary",
                14.0, true, 380.0,
                "Fleet software and leasing platform with recurring enterprise revenue.",
            ),
        ]
    }

    fn source_name(&self) -> &'static str {
        "embedded demonstration snapshot"
    }

    fn is_demo(&self) -> bool {
        true
    }
}

#[allow(clippy::too_many_arguments)]
fn earnings(
    symbol: &str,
    company: &str,
    market: Market,
    fiscal_period: &str,
    currency: &str,
    revenue_millions: f64,
    revenue_growth_pct: f64,
    operating_margin_pct: f64,
    eps: f64,
    eps_growth_pct: f64,
    highlights: &[&str],
    risks: &[&str],
) -> EarningsAnalysis {
    let quality_score = earnings_score(revenue_growth_pct, operating_margin_pct, eps_growth_pct);
    EarningsAnalysis {
        symbol: symbol.into(),
        company: company.into(),
        market,
        fiscal_period: fiscal_period.into(),
        currency: currency.into(),
        revenue_millions,
        revenue_growth_pct,
        operating_margin_pct,
        eps,
        eps_growth_pct,
        quality_score,
        rating: rating(quality_score),
        highlights: highlights.iter().map(|value| (*value).into()).collect(),
        risks: risks.iter().map(|value| (*value).into()).collect(),
    }
}

#[allow(clippy::too_many_arguments)]
fn ipo(
    company: &str,
    ticker: Option<&str>,
    country: Country,
    exchange: &str,
    expected_date: &str,
    status: IpoStatus,
    price_range: Option<&str>,
    sector: &str,
    revenue_growth_pct: f64,
    profitable: bool,
    deal_size_millions: f64,
    summary: &str,
) -> IpoAnalysis {
    IpoAnalysis {
        company: company.into(),
        ticker: ticker.map(str::to_owned),
        country,
        exchange: exchange.into(),
        expected_date: NaiveDate::parse_from_str(expected_date, "%Y-%m-%d")
            .expect("embedded IPO date must be valid"),
        status,
        price_range: price_range.map(str::to_owned),
        sector: sector.into(),
        opportunity_score: ipo_score(revenue_growth_pct, profitable, deal_size_millions),
        summary: summary.into(),
    }
}
