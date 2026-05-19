package joce.practice.market.service;


import joce.practice.market.infra.entity.StockSymbolEntity;
import joce.practice.market.infra.repository.StockSymbolRepository;
import joce.practice.market.provider.TwelveDataSymbolProvider;
import joce.practice.market.provider.dto.TwelveDataStockDto;
import joce.practice.market.provider.dto.TwelveDataStocksResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockSymbolSyncService {

    private static final Set<String> TARGET_EXCHANGES = Set.of("NASDAQ", "NYSE");

    private final TwelveDataSymbolProvider twelveDataSymbolProvider;
    private final StockSymbolRepository stockSymbolRepository;

    @Transactional
    public void syncUsCommonStockSymbols() {
        for (String exchange : TARGET_EXCHANGES) {
            syncExchange(exchange);
        }
    }

    private void syncExchange(String exchange) {
        log.info("Start syncing stock symbols. exchange={}", exchange);

        TwelveDataStocksResponse response =
                twelveDataSymbolProvider.fetchStocksByExchange(exchange);

        if (response == null || response.getData() == null) {
            log.warn("Empty symbol response. exchange={}", exchange);
            return;
        }

        List<TwelveDataStockDto> validSymbols = response.getData().stream()
            .filter(this::isTargetSymbol)
            .toList();

        for (TwelveDataStockDto dto : validSymbols) {
            upsertSymbol(dto);
        }

        log.info("Finished syncing stock symbols. exchange={}, count={}",
            exchange, validSymbols.size());
    }

    private boolean isTargetSymbol(TwelveDataStockDto dto) {
        return dto.getSymbol() != null
            && "United States".equalsIgnoreCase(dto.getCountry())
            && "USD".equalsIgnoreCase(dto.getCurrency())
            && "Common Stock".equalsIgnoreCase(dto.getType())
            && TARGET_EXCHANGES.contains(dto.getExchange());
    }

    private void upsertSymbol(TwelveDataStockDto dto) {
        LocalDateTime now = LocalDateTime.now();

        StockSymbolEntity entity = stockSymbolRepository
            .findBySymbol(dto.getSymbol())
            .orElseGet(() -> StockSymbolEntity.builder()
                .symbol(dto.getSymbol())
                .createdAt(now)
                .isActive(true)
                .build());

        entity.setName(dto.getName());
        entity.setExchange(dto.getExchange());
        entity.setMicCode(dto.getMicCode());
        entity.setCurrency(dto.getCurrency());
        entity.setCountry(dto.getCountry());
        entity.setType(dto.getType());
        entity.setIsActive(true);
        entity.setUpdatedAt(now);

        stockSymbolRepository.save(entity);
    }
}