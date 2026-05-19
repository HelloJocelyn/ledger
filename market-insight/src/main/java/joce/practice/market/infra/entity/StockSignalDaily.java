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
        name = "stock_signal_daily",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_signal_symbol_trade_date",
                        columnNames = {"symbol", "trade_date"}
                )
        },
        indexes = {
                @Index(name = "idx_signal_symbol", columnList = "symbol"),
                @Index(name = "idx_signal_trade_date", columnList = "trade_date"),
                @Index(name = "idx_signal_type", columnList = "signal_type"),
                @Index(name = "idx_trend_score", columnList = "trend_score"),
                @Index(name = "idx_heat_score", columnList = "heat_score")
        }
)
public class StockSignalDaily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    @Column(name = "close_price", nullable = false, precision = 20, scale = 4)
    private BigDecimal closePrice;

    @Column(name = "return_1d", precision = 10, scale = 4)
    private BigDecimal return1d;

    @Column(name = "return_5d", precision = 10, scale = 4)
    private BigDecimal return5d;

    @Column(name = "return_14d", precision = 10, scale = 4)
    private BigDecimal return14d;

    @Column(name = "return_30d", precision = 10, scale = 4)
    private BigDecimal return30d;

    @Column(name = "consecutive_up_days", nullable = false)
    private Integer consecutiveUpDays = 0;

    @Column(name = "consecutive_down_days", nullable = false)
    private Integer consecutiveDownDays = 0;

    @Column(name = "max_drawdown_14d", precision = 10, scale = 4)
    private BigDecimal maxDrawdown14d;

    @Column(name = "volatility_14d", precision = 10, scale = 4)
    private BigDecimal volatility14d;

    @Column(name = "relative_strength_14d", precision = 10, scale = 4)
    private BigDecimal relativeStrength14d;

    @Column(name = "is_new_high_30d", nullable = false)
    private Boolean isNewHigh30d = false;

    @Column(name = "trend_score", precision = 10, scale = 4)
    private BigDecimal trendScore;

    @Column(name = "heat_score", precision = 10, scale = 4)
    private BigDecimal heatScore;

    @Column(name = "signal_type", length = 32)
    private String signalType;

    @Column(name = "signal_reason", length = 255)
    private String signalReason;

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