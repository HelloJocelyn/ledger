package joce.practice.market.infra.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "stock_index_constituent",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_stock_index_constituent",
            columnNames = {
                "indexCode",
                "symbol",
                "effectiveFrom"
            }
        )
    },
    indexes = {
        @Index(
            name = "idx_stock_index_constituent_current",
            columnList = "indexCode,isCurrent"
        ),
        @Index(
            name = "idx_stock_index_constituent_symbol",
            columnList = "symbol"
        ),
        @Index(
            name = "idx_stock_index_constituent_effective",
            columnList = "effectiveFrom,effectiveTo"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockIndexConstituentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 指数代码，例如 SP500
     */
    @Column(nullable = false, length = 32)
    private String indexCode;

    /**
     * 股票代码，例如 AAPL
     */
    @Column(nullable = false, length = 32)
    private String symbol;

    /**
     * 加入指数日期
     */
    @Column(nullable = false)
    private LocalDate effectiveFrom;

    /**
     * 退出指数日期
     */
    private LocalDate effectiveTo;

    /**
     * 当前是否仍属于该指数
     */
    @Column(nullable = false)
    private Boolean isCurrent;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (isCurrent == null) {
            isCurrent = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}