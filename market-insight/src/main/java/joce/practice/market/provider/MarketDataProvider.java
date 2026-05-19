package joce.practice.market.provider;

import joce.practice.market.provider.dto.DailyPrice;

import java.time.LocalDate;
import java.util.List;

public interface MarketDataProvider {
    List<DailyPrice> fetchDailyPrices(String symbol, LocalDate from,
                                      LocalDate to);
    String getSource();
}