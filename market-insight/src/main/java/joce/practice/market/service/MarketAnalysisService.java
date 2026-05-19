package joce.practice.market.service;

import joce.practice.market.infra.entity.StockPriceDaily;
import joce.practice.market.infra.entity.StockSignalDaily;
import joce.practice.market.infra.repository.StockPriceDailyRepository;
import joce.practice.market.infra.repository.StockSignalDailyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MarketAnalysisService {
    private final StockPriceDailyRepository priceRepository;
    private final StockSignalDailyRepository signalRepository;

    public void analyzeSymbol(String symbol) {
        List<StockPriceDaily> prices = priceRepository.findBySymbolOrderByTradeDateAsc(symbol);

        if (prices.size() < 31) {
            return;
        }

        for (int i = 0; i < prices.size(); i++) {
            StockSignalDaily signal = analyzeOneDay(prices, i);
            if (signal != null) {
                upsert(signal);
            }
        }
    }
    private void upsert(StockSignalDaily newSignal) {
        StockSignalDaily signal = signalRepository
                .findBySymbolAndTradeDate(newSignal.getSymbol(), newSignal.getTradeDate())
                .orElse(newSignal);

        signal.setClosePrice(newSignal.getClosePrice());

        signal.setReturn1d(newSignal.getReturn1d());
        signal.setReturn5d(newSignal.getReturn5d());
        signal.setReturn14d(newSignal.getReturn14d());
        signal.setReturn30d(newSignal.getReturn30d());

        signal.setConsecutiveUpDays(newSignal.getConsecutiveUpDays());
        signal.setConsecutiveDownDays(newSignal.getConsecutiveDownDays());

        signal.setMaxDrawdown14d(newSignal.getMaxDrawdown14d());
        signal.setVolatility14d(newSignal.getVolatility14d());
        signal.setRelativeStrength14d(newSignal.getRelativeStrength14d());
        signal.setIsNewHigh30d(newSignal.getIsNewHigh30d());

        signal.setTrendScore(newSignal.getTrendScore());
        signal.setHeatScore(newSignal.getHeatScore());

        signal.setSignalType(newSignal.getSignalType());
        signal.setSignalReason(newSignal.getSignalReason());

        signalRepository.save(signal);
    }

    private StockSignalDaily analyzeOneDay(List<StockPriceDaily> prices, int index) {
        StockPriceDaily current = prices.get(index);

        BigDecimal close = current.getClosePrice();

        BigDecimal return1d = calcReturn(prices, index, 1);
        BigDecimal return5d = calcReturn(prices, index, 5);
        BigDecimal return14d = calcReturn(prices, index, 14);
        BigDecimal return30d = calcReturn(prices, index, 30);

        int consecutiveUpDays = calcConsecutiveUpDays(prices, index);
        int consecutiveDownDays = calcConsecutiveDownDays(prices, index);

        BigDecimal maxDrawdown14d = calcMaxDrawdown(prices, index, 14);
        BigDecimal volatility14d = calcVolatility(prices, index, 14);

        BigDecimal relativeStrength14d = calcRelativeStrength(return14d, volatility14d);

        boolean isNewHigh30d = calcIsNewHigh(prices, index, 30);

        BigDecimal trendScore = calcTrendScore(
                return5d,
                return14d,
                return30d,
                consecutiveUpDays,
                isNewHigh30d
        );

        BigDecimal heatScore = calcHeatScore(
                return5d,
                return14d,
                consecutiveUpDays,
                maxDrawdown14d,
                volatility14d
        );

        String signalType = judgeSignalType(trendScore, heatScore);
        String reason = buildReason(
                return5d,
                return14d,
                return30d,
                consecutiveUpDays,
                consecutiveDownDays,
                isNewHigh30d,
                trendScore,
                heatScore
        );

        StockSignalDaily signal = new StockSignalDaily();
        signal.setSymbol(current.getSymbol());
        signal.setTradeDate(current.getTradeDate());
        signal.setClosePrice(close);

        signal.setReturn1d(return1d);
        signal.setReturn5d(return5d);
        signal.setReturn14d(return14d);
        signal.setReturn30d(return30d);

        signal.setConsecutiveUpDays(consecutiveUpDays);
        signal.setConsecutiveDownDays(consecutiveDownDays);

        signal.setMaxDrawdown14d(maxDrawdown14d);
        signal.setVolatility14d(volatility14d);

        signal.setRelativeStrength14d(relativeStrength14d);
        signal.setIsNewHigh30d(isNewHigh30d);

        signal.setTrendScore(trendScore);
        signal.setHeatScore(heatScore);

        signal.setSignalType(signalType);
        signal.setSignalReason(reason);

        return signal;
    }
    private BigDecimal calcReturn(List<StockPriceDaily> prices, int index, int days) {
        if (index < days) {
            return null;
        }

        BigDecimal current = prices.get(index).getClosePrice();
        BigDecimal previous = prices.get(index - days).getClosePrice();

        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return current.subtract(previous)
                .divide(previous, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(4, RoundingMode.HALF_UP);
    }

    private int calcConsecutiveUpDays(List<StockPriceDaily> prices, int index) {
        int count = 0;

        for (int i = index; i > 0; i--) {
            BigDecimal today = prices.get(i).getClosePrice();
            BigDecimal yesterday = prices.get(i - 1).getClosePrice();

            if (today.compareTo(yesterday) > 0) {
                count++;
            } else {
                break;
            }
        }

        return count;
    }

    private int calcConsecutiveDownDays(List<StockPriceDaily> prices, int index) {
        int count = 0;

        for (int i = index; i > 0; i--) {
            BigDecimal today = prices.get(i).getClosePrice();
            BigDecimal yesterday = prices.get(i - 1).getClosePrice();

            if (today.compareTo(yesterday) < 0) {
                count++;
            } else {
                break;
            }
        }

        return count;
    }

    private BigDecimal calcMaxDrawdown(List<StockPriceDaily> prices, int index, int days) {
        if (index < days) {
            return null;
        }

        BigDecimal peak = prices.get(index - days).getClosePrice();
        BigDecimal maxDrawdown = BigDecimal.ZERO;

        for (int i = index - days + 1; i <= index; i++) {
            BigDecimal close = prices.get(i).getClosePrice();

            if (close.compareTo(peak) > 0) {
                peak = close;
            }

            if (peak.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal drawdown = close.subtract(peak)
                        .divide(peak, 6, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

                if (drawdown.compareTo(maxDrawdown) < 0) {
                    maxDrawdown = drawdown;
                }
            }
        }

        return maxDrawdown.setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal calcVolatility(List<StockPriceDaily> prices, int index, int days) {
        if (index < days) {
            return null;
        }

        List<BigDecimal> returns = new ArrayList<>();

        for (int i = index - days + 1; i <= index; i++) {
            BigDecimal today = prices.get(i).getClosePrice();
            BigDecimal yesterday = prices.get(i - 1).getClosePrice();

            if (yesterday.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            BigDecimal dailyReturn = today.subtract(yesterday)
                    .divide(yesterday, 8, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            returns.add(dailyReturn);
        }

        if (returns.isEmpty()) {
            return null;
        }

        double avg = returns.stream()
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0);

        double variance = returns.stream()
                .mapToDouble(r -> Math.pow(r.doubleValue() - avg, 2))
                .average()
                .orElse(0);

        return BigDecimal.valueOf(Math.sqrt(variance))
                .setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal calcRelativeStrength(BigDecimal return14d, BigDecimal volatility14d) {
        if (return14d == null || volatility14d == null) {
            return null;
        }

        if (volatility14d.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return return14d.divide(volatility14d, 4, RoundingMode.HALF_UP);
    }

    private boolean calcIsNewHigh(List<StockPriceDaily> prices, int index, int days) {
        if (index < days) {
            return false;
        }

        BigDecimal current = prices.get(index).getClosePrice();

        for (int i = index - days; i < index; i++) {
            if (prices.get(i).getClosePrice().compareTo(current) >= 0) {
                return false;
            }
        }

        return true;
    }

    private BigDecimal calcTrendScore(
            BigDecimal return5d,
            BigDecimal return14d,
            BigDecimal return30d,
            int consecutiveUpDays,
            boolean isNewHigh30d
    ) {
        double score = 0;

        score += safe(return5d) * 1.5;
        score += safe(return14d) * 1.0;
        score += safe(return30d) * 0.5;

        score += Math.min(consecutiveUpDays, 5) * 2;

        if (isNewHigh30d) {
            score += 10;
        }

        return BigDecimal.valueOf(score).setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal calcHeatScore(
            BigDecimal return5d,
            BigDecimal return14d,
            int consecutiveUpDays,
            BigDecimal maxDrawdown14d,
            BigDecimal volatility14d
    ) {
        double score = 0;

        if (safe(return5d) > 8) {
            score += 20;
        }

        if (safe(return14d) > 15) {
            score += 20;
        }

        if (consecutiveUpDays >= 5) {
            score += 20;
        }

        if (safe(volatility14d) > 4) {
            score += 15;
        }

        if (safe(maxDrawdown14d) < -8) {
            score += 10;
        }

        return BigDecimal.valueOf(score).setScale(4, RoundingMode.HALF_UP);
    }

    private String judgeSignalType(BigDecimal trendScore, BigDecimal heatScore) {
        double trend = safe(trendScore);
        double heat = safe(heatScore);

        if (trend >= 30 && heat < 50) {
            return "TREND";
        }

        if (heat >= 50) {
            return "GAME";
        }

        if (trend <= -10) {
            return "SENTIMENT";
        }

        return null;
    }

    private String buildReason(
            BigDecimal return5d,
            BigDecimal return14d,
            BigDecimal return30d,
            int consecutiveUpDays,
            int consecutiveDownDays,
            boolean isNewHigh30d,
            BigDecimal trendScore,
            BigDecimal heatScore
    ) {
        List<String> reasons = new ArrayList<>();

        if (safe(return5d) > 5) {
            reasons.add("近5日上涨明显");
        }

        if (safe(return14d) > 10) {
            reasons.add("近14日趋势较强");
        }

        if (safe(return30d) > 20) {
            reasons.add("近30日涨幅较大");
        }

        if (consecutiveUpDays >= 3) {
            reasons.add("连续上涨" + consecutiveUpDays + "日");
        }

        if (consecutiveDownDays >= 3) {
            reasons.add("连续下跌" + consecutiveDownDays + "日");
        }

        if (isNewHigh30d) {
            reasons.add("创30日新高");
        }

        if (safe(heatScore) >= 50) {
            reasons.add("短期过热");
        }

        if (reasons.isEmpty()) {
            reasons.add("暂无明显信号");
        }

        return String.join("，", reasons);
    }

    private double safe(BigDecimal value) {
        return value == null ? 0 : value.doubleValue();
    }

}
