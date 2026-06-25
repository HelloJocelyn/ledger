use crate::model::Rating;

pub fn earnings_score(
    revenue_growth_pct: f64,
    operating_margin_pct: f64,
    eps_growth_pct: f64,
) -> u8 {
    let growth = normalize(revenue_growth_pct, -10.0, 30.0);
    let margin = normalize(operating_margin_pct, 0.0, 35.0);
    let eps = normalize(eps_growth_pct, -20.0, 40.0);
    ((growth * 0.35 + margin * 0.35 + eps * 0.30) * 100.0).round() as u8
}

pub fn rating(score: u8) -> Rating {
    match score {
        80..=100 => Rating::Strong,
        65..=79 => Rating::Positive,
        45..=64 => Rating::Neutral,
        _ => Rating::Cautious,
    }
}

pub fn ipo_score(revenue_growth_pct: f64, profitable: bool, deal_size_millions: f64) -> u8 {
    let growth = normalize(revenue_growth_pct, -10.0, 50.0);
    let profitability = if profitable { 1.0 } else { 0.35 };
    let liquidity = normalize(deal_size_millions, 20.0, 1_000.0);
    ((growth * 0.45 + profitability * 0.35 + liquidity * 0.20) * 100.0).round() as u8
}

fn normalize(value: f64, min: f64, max: f64) -> f64 {
    ((value - min) / (max - min)).clamp(0.0, 1.0)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn strong_fundamentals_receive_a_high_score() {
        assert!(earnings_score(25.0, 30.0, 35.0) >= 80);
    }

    #[test]
    fn score_is_always_bounded() {
        assert_eq!(earnings_score(-100.0, -10.0, -100.0), 0);
        assert_eq!(earnings_score(100.0, 100.0, 100.0), 100);
    }

    #[test]
    fn profitable_ipo_scores_above_equivalent_unprofitable_ipo() {
        assert!(ipo_score(20.0, true, 300.0) > ipo_score(20.0, false, 300.0));
    }
}
