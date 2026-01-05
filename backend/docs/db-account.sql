-- =====================================
--   account table
-- =====================================

-- =========================
-- Ledger DDL (MySQL 8.0)
-- Charset: utf8mb4
-- =========================

SET NAMES utf8mb4;
SET time_zone = '+00:00';

-- -------------------------
-- 1) Account
-- -------------------------
CREATE TABLE IF NOT EXISTS account
(
    id            BIGINT UNSIGNED                                                     NOT NULL AUTO_INCREMENT,
    user_id       BIGINT UNSIGNED                                                     NOT NULL COMMENT 'Multi-tenant/user scope; if single-user can keep =1',
    provider      ENUM ('PAYPAY','SEVEN_BANK','RAKUTEN_BANK','MIZUHO','MUFG','OTHER') NOT NULL COMMENT 'Source/provider for imports',
    account_type  ENUM ('BANK','WALLET','CARD','CASH','BROKERAGE','OTHER')            NOT NULL COMMENT 'Used for future logic branching',
    account_name  VARCHAR(255)                                                        NOT NULL COMMENT 'Display name on dashboard cards',
    currency      CHAR(3)                                                             NOT NULL DEFAULT 'JPY',
    status        ENUM ('ACTIVE','ARCHIVED')                                          NOT NULL DEFAULT 'ACTIVE',
    display_order INT                                                                 NOT NULL DEFAULT 0,
    created_at    TIMESTAMP                                                           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP                                                           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    KEY idx_user_status (user_id, status),
    KEY idx_provider_type (provider, account_type)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


-- -------------------------
-- 2) Import Batch
-- -------------------------
CREATE TABLE IF NOT EXISTS import_batch
(
    id           BIGINT UNSIGNED                                                        NOT NULL AUTO_INCREMENT,
    user_id      BIGINT UNSIGNED                                                        NOT NULL,
    account_id   BIGINT UNSIGNED                                                        NOT NULL,
    provider     ENUM ('PAYPAY','SEVEN_BANK','RAKUTEN_BANK','MIZUHO','MUFG','OTHER')    NOT NULL,
    status       ENUM ('PENDING_OCR','REVIEW_REQUIRED','COMPLETED','FAILED','CANCELED') NOT NULL DEFAULT 'PENDING_OCR',
    total_images INT                                                                    NOT NULL DEFAULT 0,
    total_rows   INT                                                                    NOT NULL DEFAULT 0,
    success_rows INT                                                                    NOT NULL DEFAULT 0,
    review_rows  INT                                                                    NOT NULL DEFAULT 0,
    failed_rows  INT                                                                    NOT NULL DEFAULT 0,

    started_at   TIMESTAMP                                                              NULL     DEFAULT NULL,
    completed_at TIMESTAMP                                                              NULL     DEFAULT NULL,

    note         VARCHAR(512)                                                           NULL,

    created_at   TIMESTAMP                                                              NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP                                                              NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    KEY idx_account_time (account_id, completed_at),
    KEY idx_user_status (user_id, status),
    CONSTRAINT fk_import_batch_account
        FOREIGN KEY (account_id) REFERENCES account (id)
            ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


-- -------------------------
-- 3) Import Image
-- -------------------------
CREATE TABLE IF NOT EXISTS import_image
(
    id              BIGINT UNSIGNED                                                     NOT NULL AUTO_INCREMENT,
    user_id         BIGINT UNSIGNED                                                     NOT NULL,
    import_batch_id BIGINT UNSIGNED                                                     NOT NULL,
    account_id      BIGINT UNSIGNED                                                     NOT NULL,
    provider        ENUM ('PAYPAY','SEVEN_BANK','RAKUTEN_BANK','MIZUHO','MUFG','OTHER') NOT NULL,
    storage_key     VARCHAR(1024)                                                       NOT NULL COMMENT 'S3 key / object path',
    sha256          BINARY(32)                                                          NOT NULL COMMENT 'Content hash for dedup',
    captured_at     TIMESTAMP                                                           NULL     DEFAULT NULL COMMENT 'From EXIF or user input',
    ocr_status      ENUM ('PENDING','SUCCESS','FAILED')                                 NOT NULL DEFAULT 'PENDING',
    ocr_engine      VARCHAR(64)                                                         NULL,
    ocr_version     VARCHAR(32)                                                         NULL,

    error_code      VARCHAR(64)                                                         NULL,
    error_message   VARCHAR(1024)                                                       NULL,

    created_at      TIMESTAMP                                                           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP                                                           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    -- Dedup within account (same exact image content)
    UNIQUE KEY uk_account_sha256 (account_id, sha256),

    KEY idx_batch (import_batch_id),
    KEY idx_account_status (account_id, ocr_status),

    CONSTRAINT fk_import_image_batch
        FOREIGN KEY (import_batch_id) REFERENCES import_batch (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_import_image_account
        FOREIGN KEY (account_id) REFERENCES account (id)
            ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


-- -------------------------
-- 4) Import Row (OCR extracted row)
-- -------------------------
CREATE TABLE IF NOT EXISTS import_row
(
    id              BIGINT UNSIGNED                        NOT NULL AUTO_INCREMENT,
    user_id         BIGINT UNSIGNED                        NOT NULL,
    import_image_id BIGINT UNSIGNED                        NOT NULL,
    import_batch_id BIGINT UNSIGNED                        NOT NULL,
    account_id      BIGINT UNSIGNED                        NOT NULL,
    row_index       INT                                    NOT NULL COMMENT 'Row number within the image',
    raw_text        TEXT                                   NULL COMMENT 'Raw OCR text for this row',
    parsed_json     JSON                                   NULL COMMENT 'Structured parse result (date, amount, merchant, balance...)',
    confidence      DECIMAL(5, 4)                          NULL COMMENT '0~1 overall confidence',
    needs_review    TINYINT(1)                             NOT NULL DEFAULT 0,
    review_notes    VARCHAR(1024)                          NULL,
    status          ENUM ('EXTRACTED','MAPPED','REJECTED') NOT NULL DEFAULT 'EXTRACTED'
        COMMENT 'MAPPED means converted into transaction(s)',

    created_at      TIMESTAMP                              NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP                              NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    UNIQUE KEY uk_image_row (import_image_id, row_index),
    KEY idx_batch_review (import_batch_id, needs_review),
    KEY idx_account_status_time (account_id, status, created_at),
    CONSTRAINT fk_import_row_image
        FOREIGN KEY (import_image_id) REFERENCES import_image (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_import_row_batch
        FOREIGN KEY (import_batch_id) REFERENCES import_batch (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_import_row_account
        FOREIGN KEY (account_id) REFERENCES account (id)
            ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


-- -------------------------
-- 5) Transfer
-- -------------------------
CREATE TABLE IF NOT EXISTS transfer
(
    id              BIGINT UNSIGNED                          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT UNSIGNED                          NOT NULL,
    from_account_id BIGINT UNSIGNED                          NOT NULL,
    to_account_id   BIGINT UNSIGNED                          NOT NULL,
    occurred_at     DATETIME(3)                              NOT NULL COMMENT 'Transfer time (best guess / confirmed)',
    amount_minor    BIGINT                                   NOT NULL COMMENT 'Amount in minor units (JPY=yen)',
    currency        CHAR(3)                                  NOT NULL DEFAULT 'JPY',
    status          ENUM ('POSSIBLE','CONFIRMED','CANCELED') NOT NULL DEFAULT 'POSSIBLE',
    match_method    ENUM ('MANUAL','AUTO')                   NOT NULL DEFAULT 'MANUAL',
    memo            VARCHAR(512)                             NULL,

    created_at      TIMESTAMP                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    KEY idx_user_time (user_id, occurred_at),
    KEY idx_from_time (from_account_id, occurred_at),
    KEY idx_to_time (to_account_id, occurred_at),

    CONSTRAINT fk_transfer_from_account
        FOREIGN KEY (from_account_id) REFERENCES account (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_transfer_to_account
        FOREIGN KEY (to_account_id) REFERENCES account (id)
            ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


-- -------------------------
-- 6) Transaction (ledger truth table)
-- -------------------------
CREATE TABLE IF NOT EXISTS transaction
(
    id                   BIGINT UNSIGNED                      NOT NULL AUTO_INCREMENT,
    user_id              BIGINT UNSIGNED                      NOT NULL,
    account_id           BIGINT UNSIGNED                      NOT NULL,
    posted_at            DATETIME(3)                          NOT NULL COMMENT 'Transaction time',
    amount_minor         BIGINT                               NOT NULL COMMENT 'Always positive; use direction to indicate inflow/outflow',
    direction            ENUM ('INFLOW','OUTFLOW')            NOT NULL,
    currency             CHAR(3)                              NOT NULL DEFAULT 'JPY',
    description          VARCHAR(512)                         NOT NULL DEFAULT '' COMMENT 'Merchant/summary',
    category_id          BIGINT UNSIGNED                      NULL COMMENT 'Optional future category table',
    status               ENUM ('POSTED','PENDING','VOID')     NOT NULL DEFAULT 'POSTED',
    -- Source traceability
    source_type          ENUM ('IMPORT_IMAGE','MANUAL','API') NOT NULL DEFAULT 'IMPORT_IMAGE',
    source_import_row_id BIGINT UNSIGNED                      NULL COMMENT 'Pointer to import_row',
    import_batch_id      BIGINT UNSIGNED                      NULL COMMENT 'Convenience for querying',

    -- Transfer linkage (exclude from income/expense when not null)
    transfer_id          BIGINT UNSIGNED                      NULL,
    transfer_leg         ENUM ('OUT','IN')                    NULL,

    -- Dedup helper (optional): signature hash you compute
    dedup_key            BINARY(32)                           NULL COMMENT 'e.g., sha256(account_id|posted_at|amount|direction|desc)',

    created_at           TIMESTAMP                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP                            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    KEY idx_account_time (account_id, posted_at),
    KEY idx_user_time (user_id, posted_at),
    KEY idx_transfer (transfer_id),
    KEY idx_source_row (source_import_row_id),
    KEY idx_import_batch (import_batch_id),

    -- Optional: if you compute dedup_key reliably, you can enforce uniqueness per account
    -- UNIQUE KEY uk_account_dedup (account_id, dedup_key),

    CONSTRAINT fk_transaction_account
        FOREIGN KEY (account_id) REFERENCES account (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_transaction_import_row
        FOREIGN KEY (source_import_row_id) REFERENCES import_row (id)
            ON DELETE SET NULL,

    CONSTRAINT fk_transaction_import_batch
        FOREIGN KEY (import_batch_id) REFERENCES import_batch (id)
            ON DELETE SET NULL,

    CONSTRAINT fk_transaction_transfer
        FOREIGN KEY (transfer_id) REFERENCES transfer (id)
            ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
