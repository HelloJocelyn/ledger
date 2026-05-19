package joce.practice.market.scheduler;

import joce.practice.market.service.StockIndexConstituentSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockIndexConstituentSyncScheduler {

    private final StockIndexConstituentSyncService syncService;

    // 每天日本时间 4 点同步一次
    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Tokyo")
    public void syncSp500Daily() {
        log.info("Scheduled SP500 constituent sync started");

        syncService.syncSp500CurrentConstituents();

        log.info("Scheduled SP500 constituent sync finished");
    }
}