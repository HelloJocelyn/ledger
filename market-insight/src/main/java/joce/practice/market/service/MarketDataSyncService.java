package joce.practice.market.service;


import joce.practice.market.infra.entity.StockPriceDaily;
import joce.practice.market.provider.MarketDataProvider;
import joce.practice.market.provider.dto.DailyPrice;
import joce.practice.market.infra.repository.StockPriceDailyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketDataSyncService {

    private final MarketDataProvider marketDataProvider;
    private final StockPriceDailyRepository stockPriceDailyRepository;

    @Transactional
    public void syncDailyPrices(String symbol, LocalDate from,
                                LocalDate to) {
        List<DailyPrice> dailyPrices = marketDataProvider.fetchDailyPrices(symbol,  from, to);

        if (dailyPrices.isEmpty()) {
            log.warn("No daily prices found for symbol={}", symbol);
            return ;
        }

        int inserted = 0;
        int updated = 0;

        for (DailyPrice dailyPrice : dailyPrices) {
            StockPriceDaily entity = stockPriceDailyRepository
                    .findBySymbolAndTradeDate(dailyPrice.symbol(), dailyPrice.tradeDate())
                    .orElseGet(StockPriceDaily::new);

            boolean isNew = entity.getId() == null;

            entity.setSymbol(dailyPrice.symbol());
            entity.setMarket(resolveMarket(dailyPrice.symbol()));
            entity.setTradeDate(dailyPrice.tradeDate());
            entity.setOpenPrice(dailyPrice.openPrice());
            entity.setHighPrice(dailyPrice.highPrice());
            entity.setLowPrice(dailyPrice.lowPrice());
            entity.setClosePrice(dailyPrice.closePrice());
            entity.setVolume(dailyPrice.volume());
            entity.setSource(marketDataProvider.getSource());

            stockPriceDailyRepository.save(entity);

            if (isNew) {
                inserted++;
            } else {
                updated++;
            }
        }

        log.info(
                "Synced daily prices for symbol={}, total={}, inserted={}, updated={}",
                symbol,
                dailyPrices.size(),
                inserted,
                updated
        );
    }

    private String resolveMarket(String symbol) {
        if (symbol == null) {
            return null;
        }

        String lower = symbol.toLowerCase();

        if (lower.endsWith(".us")) {
            return "US";
        }

        if (lower.endsWith(".jp")) {
            return "JP";
        }

        if (lower.endsWith(".hk")) {
            return "HK";
        }

        if (lower.startsWith("^")) {
            return "INDEX";
        }

        return null;
    }
}