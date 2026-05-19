package joce.practice.market.service;


import joce.practice.market.infra.entity.StockIndexConstituentEntity;
import joce.practice.market.infra.repository.StockIndexConstituentRepository;
import joce.practice.market.provider.Sp500WikipediaClient;
import joce.practice.market.provider.dto.Sp500ConstituentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockIndexConstituentSyncService {

    private static final String SP500 = "SP500";

    private final Sp500WikipediaClient sp500WikipediaClient;
    private final StockIndexConstituentRepository repository;

    @Transactional
    public void syncSp500CurrentConstituents() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        List<Sp500ConstituentDto> latestList = sp500WikipediaClient.fetchCurrentConstituents();

        Map<String, Sp500ConstituentDto> latestBySymbol = latestList.stream()
            .collect(Collectors.toMap(
                Sp500ConstituentDto::getSymbol,
                Function.identity(),
                (a, b) -> a
            ));

        var currentEntities = repository.findByIndexCodeAndIsCurrentTrue(SP500);

        Map<String, StockIndexConstituentEntity> currentBySymbol =
            currentEntities.stream()
                .collect(Collectors.toMap(
                    StockIndexConstituentEntity::getSymbol,
                    Function.identity()
                ));

        Set<String> latestSymbols = latestBySymbol.keySet();
        Set<String> currentSymbols = currentBySymbol.keySet();

        // 1. 新增成分股
        for (String symbol : latestSymbols) {
            if (!currentSymbols.contains(symbol)) {
                Sp500ConstituentDto dto = latestBySymbol.get(symbol);

                StockIndexConstituentEntity entity =
                    StockIndexConstituentEntity.builder()
                        .indexCode(SP500)
                        .symbol(dto.getSymbol())
//                        .securityName(dto.getSecurityName())
//                        .gicsSector(dto.getGicsSector())
//                        .gicsSubIndustry(dto.getGicsSubIndustry())
                        .effectiveFrom(today)
                        .effectiveTo(null)
                        .isCurrent(true)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

                repository.save(entity);

                log.info("Added SP500 constituent. symbol={}", symbol);
            }
        }

        // 2. 移除已经不在当前 SP500 的股票
        for (String symbol : currentSymbols) {
            if (!latestSymbols.contains(symbol)) {
                StockIndexConstituentEntity entity = currentBySymbol.get(symbol);

                entity.setEffectiveTo(today);
                entity.setIsCurrent(false);
                entity.setUpdatedAt(now);

                repository.save(entity);

                log.info("Removed SP500 constituent. symbol={}", symbol);
            }
        }

        // 3. 更新当前仍在 SP500 的基础信息
        for (String symbol : latestSymbols) {
            if (currentSymbols.contains(symbol)) {
                Sp500ConstituentDto dto = latestBySymbol.get(symbol);
                StockIndexConstituentEntity entity = currentBySymbol.get(symbol);

//                entity.setSecurityName(dto.getSecurityName());
//                entity.setGicsSector(dto.getGicsSector());
//                entity.setGicsSubIndustry(dto.getGicsSubIndustry());
                entity.setUpdatedAt(now);

                repository.save(entity);
            }
        }

        log.info("SP500 sync finished. latestCount={}, currentCount={}",
            latestSymbols.size(), currentSymbols.size());
    }
}