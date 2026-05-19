package joce.practice.market.infra.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "stock_price_daily",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_symbol_trade_date",
                        columnNames = {"symbol", "trade_date"}
                )
        },
        indexes = {
                @Index(name = "idx_symbol", columnList = "symbol"),
                @Index(name = "idx_trade_date", columnList = "trade_date"),
                @Index(name = "idx_symbol_trade_date", columnList = "symbol, trade_date")
        }
)
public class StockPriceDaily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(length = 16)
    private String market;

    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    @Column(name = "open_price", nullable = false, precision = 20, scale = 4)
    private BigDecimal openPrice;

    @Column(name = "high_price", nullable = false, precision = 20, scale = 4)
    private BigDecimal highPrice;

    @Column(name = "low_price", nullable = false, precision = 20, scale = 4)
    private BigDecimal lowPrice;

    @Column(name = "close_price", nullable = false, precision = 20, scale = 4)
    private BigDecimal closePrice;

    @Column
    private Long volume;

    @Column(nullable = false, length = 32)
    private String source = "STOOQ";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}