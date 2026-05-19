package joce.practice.market.infra.repository;

import joce.practice.market.infra.entity.StockIndexConstituentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockIndexConstituentRepository
        extends JpaRepository<StockIndexConstituentEntity, Long> {

    List<StockIndexConstituentEntity> findByIndexCodeAndIsCurrentTrue(String indexCode);

    Optional<StockIndexConstituentEntity> findByIndexCodeAndSymbolAndIsCurrentTrue(
        String indexCode,
        String symbol
    );
}