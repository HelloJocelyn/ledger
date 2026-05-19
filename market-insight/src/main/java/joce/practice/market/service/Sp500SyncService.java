package joce.practice.market.service;


import joce.practice.market.infra.entity.StockIndexConstituentEntity;
import joce.practice.market.infra.entity.StockSymbolEntity;
import joce.practice.market.infra.repository.StockIndexConstituentRepository;
import joce.practice.market.infra.repository.StockSymbolRepository;
import joce.practice.market.provider.Sp500WikipediaClient;
import joce.practice.market.provider.dto.Sp500ConstituentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class Sp500SyncService {

    private static final String INDEX_CODE = "SP500";
    private static final String EXCHANGE_UNKNOWN = null;
    private static final String COUNTRY_US = "United States";
    private static final String CURRENCY_USD = "USD";
    private static final String TYPE_COMMON_STOCK = "Common Stock";

    private final Sp500WikipediaClient sp500WikipediaClient;
    private final StockSymbolRepository stockSymbolRepository;
    private final StockIndexConstituentRepository constituentRepository;

    @Transactional
    public void syncSp500() {
        LocalDate today = LocalDate.now();

        var latestList = sp500WikipediaClient.fetchCurrentConstituents();

        Map<String, Sp500ConstituentDto> latestBySymbol = latestList.stream()
            .collect(Collectors.toMap(
                dto -> normalizeSymbol(dto.getSymbol()),
                Function.identity(),
                (a, b) -> a
            ));

        // 1. 先 upsert stock_symbol
        latestBySymbol.values().forEach(this::upsertStockSymbol);

        // 2. 再同步 stock_index_constituent
        syncIndexConstituents(latestBySymbol, today);

        log.info("SP500 sync finished. latestCount={}", latestBySymbol.size());
    }

    private void upsertStockSymbol(Sp500ConstituentDto dto) {
        String symbol = normalizeSymbol(dto.getSymbol());

        StockSymbolEntity entity = stockSymbolRepository.findBySymbol(symbol)
            .orElseGet(() -> StockSymbolEntity.builder()
                .symbol(symbol)
                .isActive(true)
                .build());

        entity.setName(dto.getSecurityName());
        entity.setCountry(COUNTRY_US);
        entity.setCurrency(CURRENCY_USD);
        entity.setType(TYPE_COMMON_STOCK);

        // Wikipedia S&P500 表不一定可靠提供 exchange，所以这里先不强行写
        if (entity.getExchange() == null) {
            entity.setExchange(EXCHANGE_UNKNOWN);
        }

        entity.setGicsSector(dto.getGicsSector());
        entity.setGicsSubIndustry(dto.getGicsSubIndustry());
        entity.setIsActive(true);

        stockSymbolRepository.save(entity);
    }

    private void syncIndexConstituents(
        Map<String, Sp500ConstituentDto> latestBySymbol,
        LocalDate today
    ) {
        var currentEntities =
            constituentRepository.findByIndexCodeAndIsCurrentTrue(INDEX_CODE);

        Map<String, StockIndexConstituentEntity> currentBySymbol =
            currentEntities.stream()
                .collect(Collectors.toMap(
                    e -> normalizeSymbol(e.getSymbol()),
                    Function.identity(),
                    (a, b) -> a
                ));

        Set<String> latestSymbols = latestBySymbol.keySet();
        Set<String> currentSymbols = currentBySymbol.keySet();

        // 新加入 SP500
        for (String symbol : latestSymbols) {
            if (!currentSymbols.contains(symbol)) {
                StockIndexConstituentEntity entity =
                    StockIndexConstituentEntity.builder()
                        .indexCode(INDEX_CODE)
                        .symbol(symbol)
                        .effectiveFrom(today)
                        .effectiveTo(null)
                        .isCurrent(true)
                        .build();

                constituentRepository.save(entity);

                log.info("Added SP500 constituent. symbol={}", symbol);
            }
        }

        // 退出 SP500
        for (String symbol : currentSymbols) {
            if (!latestSymbols.contains(symbol)) {
                StockIndexConstituentEntity entity = currentBySymbol.get(symbol);

                entity.setEffectiveTo(today);
                entity.setIsCurrent(false);

                constituentRepository.save(entity);

                log.info("Removed SP500 constituent. symbol={}", symbol);
            }
        }

        // 仍然在 SP500 的，不需要更新 constituent
        // sector/name 已经更新到 stock_symbol 了
    }

    private String normalizeSymbol(String symbol) {
        if (symbol == null) {
            return null;
        }

        return symbol.trim().toUpperCase();
    }
}