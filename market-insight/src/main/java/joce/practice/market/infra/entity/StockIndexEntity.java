package joce.practice.market.infra.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "stock_index",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_stock_index_code",
            columnNames = "code"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockIndexEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 指数代码，例如 SP500
     */
    @Column(nullable = false, length = 32)
    private String code;

    /**
     * 指数名称
     */
    @Column(nullable = false, length = 255)
    private String name;

    /**
     * 数据来源
     */
    @Column(length = 64)
    private String provider;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
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