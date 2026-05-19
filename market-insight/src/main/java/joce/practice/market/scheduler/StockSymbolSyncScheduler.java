package joce.practice.market.scheduler;

import joce.practice.market.service.StockSymbolSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockSymbolSyncScheduler {

    private final StockSymbolSyncService stockSymbolSyncService;

    // 每天日本时间凌晨 3 点同步一次
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Tokyo")
    public void syncSymbolsDaily() {
        log.info("Scheduled symbol sync started");

        stockSymbolSyncService.syncUsCommonStockSymbols();

        log.info("Scheduled symbol sync finished");
    }
}