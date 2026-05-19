package joce.practice.market.infra.repository;

import joce.practice.market.infra.entity.StockPriceDaily;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockPriceDailyRepository extends JpaRepository<StockPriceDaily, Long> {

    Optional<StockPriceDaily> findBySymbolAndTradeDate(String symbol, LocalDate tradeDate);

    boolean existsBySymbolAndTradeDate(String symbol, LocalDate tradeDate);

    List<StockPriceDaily> findBySymbolOrderByTradeDateAsc(String symbol);

    List<StockPriceDaily> findBySymbolAndTradeDateBetweenOrderByTradeDateAsc(
            String symbol,
            LocalDate fromDate,
            LocalDate toDate
    );

    List<StockPriceDaily> findTop30BySymbolAndTradeDateLessThanEqualOrderByTradeDateDesc(
            String symbol,
            LocalDate tradeDate
    );

    Optional<StockPriceDaily> findTopBySymbolOrderByTradeDateDesc(String symbol);
}