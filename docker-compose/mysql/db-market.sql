CREATE TABLE stock_price_daily (
   id BIGINT PRIMARY KEY AUTO_INCREMENT,

   symbol VARCHAR(32) NOT NULL COMMENT '股票代码，例如 nvda.us',
   market VARCHAR(16) NULL COMMENT '市场，例如 US / JP',

   trade_date DATE NOT NULL COMMENT '交易日期',

   open_price DECIMAL(20, 4) NOT NULL,
   high_price DECIMAL(20, 4) NOT NULL,
   low_price DECIMAL(20, 4) NOT NULL,
   close_price DECIMAL(20, 4) NOT NULL,

   volume BIGINT NULL COMMENT '成交量',

   source VARCHAR(32) NOT NULL DEFAULT 'STOOQ' COMMENT '数据来源',

   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
   updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
       ON UPDATE CURRENT_TIMESTAMP,

   CONSTRAINT uk_symbol_trade_date
       UNIQUE (symbol, trade_date),

   INDEX idx_symbol (symbol),
   INDEX idx_trade_date (trade_date),
   INDEX idx_symbol_trade_date (symbol, trade_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



CREATE TABLE stock_signal_daily (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    symbol VARCHAR(32) NOT NULL,
    trade_date DATE NOT NULL,

    close_price DECIMAL(20, 4) NOT NULL,

    return_1d DECIMAL(10, 4) NULL COMMENT '1日涨跌幅%',
    return_5d DECIMAL(10, 4) NULL COMMENT '5日涨跌幅%',
    return_14d DECIMAL(10, 4) NULL COMMENT '14日涨跌幅%',
    return_30d DECIMAL(10, 4) NULL COMMENT '30日涨跌幅%',

    consecutive_up_days INT NOT NULL DEFAULT 0 COMMENT '连续上涨天数',
    consecutive_down_days INT NOT NULL DEFAULT 0 COMMENT '连续下跌天数',

    max_drawdown_14d DECIMAL(10, 4) NULL COMMENT '14日最大回撤%',

    volatility_14d DECIMAL(10, 4) NULL COMMENT '14日波动率',

    relative_strength_14d DECIMAL(10, 4) NULL COMMENT '相对指数强度',

    is_new_high_30d BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否30日新高',

    trend_score DECIMAL(10, 4) NULL COMMENT '趋势评分',
    heat_score DECIMAL(10, 4) NULL COMMENT '过热评分',

    signal_type VARCHAR(32) NULL COMMENT 'TREND / SENTIMENT / GAME',

    signal_reason VARCHAR(255) NULL COMMENT '触发原因',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_signal_symbol_trade_date
        UNIQUE (symbol, trade_date),

    INDEX idx_signal_symbol (symbol),
    INDEX idx_signal_trade_date (trade_date),
    INDEX idx_signal_type (signal_type),
    INDEX idx_trend_score (trend_score),
    INDEX idx_heat_score (heat_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE stock_symbol (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    symbol VARCHAR(32) NOT NULL COMMENT '股票代码，例如 AAPL',
    name VARCHAR(255) NULL COMMENT '股票名称',

    exchange VARCHAR(32) NULL COMMENT '交易所，例如 NASDAQ / NYSE',
    mic_code VARCHAR(32) NULL COMMENT 'MIC code，例如 XNAS',

    currency VARCHAR(16) NULL COMMENT '货币，例如 USD',
    country VARCHAR(64) NULL COMMENT '国家，例如 United States',

    type VARCHAR(64) NULL COMMENT '证券类型，例如 Common Stock',

    gics_sector VARCHAR(128) NULL COMMENT 'GICS 一级行业',
    gics_sub_industry VARCHAR(128) NULL COMMENT 'GICS 子行业',

    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '当前是否有效',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
      ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_stock_symbol_symbol
      UNIQUE (symbol),

    INDEX idx_stock_symbol_exchange (exchange),
    INDEX idx_stock_symbol_type (type),
    INDEX idx_stock_symbol_sector (gics_sector),
    INDEX idx_stock_symbol_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE stock_index (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    code VARCHAR(32) NOT NULL COMMENT '指数代码，例如 SP500',
    name VARCHAR(255) NOT NULL COMMENT '指数名称',

    provider VARCHAR(64) NULL COMMENT '数据来源，例如 WIKIPEDIA',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
     ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_stock_index_code
     UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE stock_index_constituent (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    index_code VARCHAR(32) NOT NULL COMMENT '指数代码，例如 SP500',

    symbol VARCHAR(32) NOT NULL COMMENT '股票代码，例如 AAPL',

    effective_from DATE NOT NULL COMMENT '加入指数日期',

    effective_to DATE NULL COMMENT '退出指数日期，NULL 表示当前仍有效',

    is_current BOOLEAN NOT NULL DEFAULT TRUE
     COMMENT '当前是否属于该指数',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
     ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_stock_index_constituent
     UNIQUE (index_code, symbol, effective_from),

    INDEX idx_stock_index_constituent_current
     (index_code, is_current),

    INDEX idx_stock_index_constituent_symbol
     (symbol),

    INDEX idx_stock_index_constituent_effective
     (effective_from, effective_to)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;