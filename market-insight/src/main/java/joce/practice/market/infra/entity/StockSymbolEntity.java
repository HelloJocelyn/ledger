package joce.practice.market.infra.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "stock_symbol",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_stock_symbol_symbol",
            columnNames = "symbol"
        )
    },
    indexes = {
        @Index(
            name = "idx_stock_symbol_exchange",
            columnList = "exchange"
        ),
        @Index(
            name = "idx_stock_symbol_type",
            columnList = "type"
        ),
        @Index(
            name = "idx_stock_symbol_sector",
            columnList = "gicsSector"
        ),
        @Index(
            name = "idx_stock_symbol_active",
            columnList = "isActive"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockSymbolEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 股票代码，例如 AAPL
     */
    @Column(nullable = false, length = 32)
    private String symbol;

    /**
     * 股票名称
     */
    @Column(length = 255)
    private String name;

    /**
     * NASDAQ / NYSE
     */
    @Column(length = 32)
    private String exchange;

    /**
     * MIC code，例如 XNAS
     */
    @Column(length = 32)
    private String micCode;

    /**
     * USD
     */
    @Column(length = 16)
    private String currency;

    /**
     * United States
     */
    @Column(length = 64)
    private String country;

    /**
     * Common Stock
     */
    @Column(length = 64)
    private String type;

    /**
     * GICS 一级行业
     */
    @Column(length = 128)
    private String gicsSector;

    /**
     * GICS 子行业
     */
    @Column(length = 128)
    private String gicsSubIndustry;

    /**
     * 当前是否有效
     */
    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}