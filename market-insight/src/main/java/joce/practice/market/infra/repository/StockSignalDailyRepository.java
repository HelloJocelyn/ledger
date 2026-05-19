package joce.practice.market.infra.repository;

import joce.practice.market.infra.entity.StockSignalDaily;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockSignalDailyRepository extends JpaRepository<StockSignalDaily, Long> {

    Optional<StockSignalDaily> findBySymbolAndTradeDate(String symbol, LocalDate tradeDate);

    boolean existsBySymbolAndTradeDate(String symbol, LocalDate tradeDate);

    List<StockSignalDaily> findBySymbolOrderByTradeDateDesc(String symbol);

    List<StockSignalDaily> findByTradeDateOrderByTrendScoreDesc(LocalDate tradeDate);

    List<StockSignalDaily> findByTradeDateOrderByHeatScoreDesc(LocalDate tradeDate);

    List<StockSignalDaily> findByTradeDateAndSignalTypeOrderByTrendScoreDesc(
            LocalDate tradeDate,
            String signalType
    );

    List<StockSignalDaily> findTop20ByTradeDateOrderByReturn14dDesc(LocalDate tradeDate);

    List<StockSignalDaily> findTop20ByTradeDateOrderByConsecutiveUpDaysDesc(LocalDate tradeDate);

    Optional<StockSignalDaily> findTopBySymbolOrderByTradeDateDesc(String symbol);



    List<StockSignalDaily> findBySymbol(String symbol, Pageable pageable);

    List<StockSignalDaily> findBySymbolAndTradeDateBetweenOrderByTradeDateAsc(
            String symbol,
            LocalDate from,
            LocalDate to
    );
}