package joce.practice.market.provider.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyPrice(
        String symbol,
        LocalDate tradeDate,
        BigDecimal openPrice,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        BigDecimal closePrice,
        Long volume
) {
}