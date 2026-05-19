package joce.practice.market.scheduler;

import joce.practice.market.infra.repository.StockSymbolRepository;
import joce.practice.market.provider.dto.DailyPrice;
import joce.practice.market.service.MarketAnalysisService;
import joce.practice.market.service.MarketDataSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketDataScheduler {

    private final MarketDataSyncService marketDataSyncService;
    private final MarketAnalysisService marketAnalysisService;
    private final StockSymbolRepository stockSymbolRepository;

    private static final List<String> SYMBOLS = List.of(
            "NVDA",
            "AAPL",
            "MSFT",
            "TSLA",
            "SPY",
            "QQQ",
            "SNDK",
            "MU"
    );

    /**
     * 每天早上7点跑
     */
    @Scheduled(cron = "0 0 7 * * *")
    public void syncDailyMarketData() {

        log.info("Start syncing market data");
        LocalDate yesterday = LocalDate.now().minusDays(1);

        LocalDate startDate = yesterday;
        LocalDate endDate = yesterday;
        List<String> symbols = stockSymbolRepository.findAll().stream().map(e -> e.getSymbol()).toList();

        for (String symbol : symbols) {
            try {
                marketDataSyncService.syncDailyPrices(symbol,startDate,endDate);
                marketAnalysisService.analyzeSymbol(symbol);
            } catch (Exception e) {
                log.error("Failed to sync symbol: {}", symbol, e);
            }
        }

        log.info("Finished syncing market data");
    }
}