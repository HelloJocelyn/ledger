package joce.practice.market.infra.repository;

import joce.practice.market.infra.entity.StockSymbolEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockSymbolRepository extends JpaRepository<StockSymbolEntity, Long> {

    Optional<StockSymbolEntity> findBySymbol(String symbol);
}