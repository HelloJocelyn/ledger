package joce.practice.market.controller;

import joce.practice.market.infra.repository.StockSymbolRepository;
import joce.practice.market.service.MarketAnalysisService;
import joce.practice.market.service.MarketDataSyncService;
import joce.practice.market.service.Sp500SyncService;
import joce.practice.market.service.StockIndexConstituentSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/market-data")
@RequiredArgsConstructor
public class MarketDataTestController {

    private final MarketDataSyncService marketDataSyncService;
    private final MarketAnalysisService marketAnalysisService;
    private final Sp500SyncService sp500SyncService;
    private final StockSymbolRepository stockSymbolRepository;

    @PostMapping("/sync/{symbol}")
    public String sync(@PathVariable String symbol) {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        LocalDate startDate = yesterday;
        LocalDate endDate = yesterday;
        marketDataSyncService.syncDailyPrices(symbol,startDate,endDate);
        marketAnalysisService.analyzeSymbol(symbol);
        return "Synced: " + symbol;
    }
    @PostMapping("/stocks/sync/all")
    public String syncAll() {

        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate  startDate= LocalDate.now().minusDays(30);

        List<String> symbols = stockSymbolRepository.findAll().stream().map(e -> e.getSymbol()).toList();

        for (String symbol : symbols) {
            try {
                marketDataSyncService.syncDailyPrices(symbol,startDate,endDate);
                marketAnalysisService.analyzeSymbol(symbol);
                Thread.sleep(8000);
            } catch (Exception e) {
                log.error("Failed to sync symbol: {}", symbol, e);
            }
        }

        log.info("Finished syncing market data");
        return "Synced: SUCCESS " ;
    }


    @PostMapping("/sp500/sync")
    public String sync() {
        sp500SyncService.syncSp500();
        return "Synced: sucess";
    }
}