package joce.practice.market.controller;

import joce.practice.market.infra.entity.StockSignalDaily;
import joce.practice.market.infra.repository.StockSignalDailyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/stock-signals")
@RequiredArgsConstructor
public class StockSignalController {

    private final StockSignalDailyRepository stockSignalDailyRepository;

    @GetMapping("/{symbol}/latest")
    public StockSignalDaily latest(@PathVariable String symbol) {
        return stockSignalDailyRepository
                .findTopBySymbolOrderByTradeDateDesc(symbol)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No signal data found for symbol: " + symbol
                ));
    }

    @GetMapping("/{symbol}")
    public List<StockSignalDaily> listBySymbol(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "120") int limit
    ) {
        int safeLimit = Math.min(Math.max(limit, 1), 500);

        Pageable pageable = PageRequest.of(
                0,
                safeLimit,
                Sort.by(Sort.Direction.DESC, "tradeDate")
        );

        return stockSignalDailyRepository
                .findBySymbol(symbol, pageable)
                .stream()
                .sorted(Comparator.comparing(StockSignalDaily::getTradeDate))
                .toList();
    }

    @GetMapping("/{symbol}/range")
    public List<StockSignalDaily> listByDateRange(
            @PathVariable String symbol,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        if (from.isAfter(to)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "`from` must be before or equal to `to`"
            );
        }

        return stockSignalDailyRepository
                .findBySymbolAndTradeDateBetweenOrderByTradeDateAsc(symbol, from, to);
    }
}