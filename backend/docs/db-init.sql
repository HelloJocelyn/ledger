-- =====================================
--   Create Database
-- =====================================
CREATE DATABASE IF NOT EXISTS ledgerx
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ledgerx;

-- =====================================
--   USER TABLE
-- =====================================
CREATE TABLE IF NOT EXISTS user
(
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '内部主键',
    uuid         CHAR(36)        NOT NULL COMMENT '对外暴露的稳定 UUID',
    email        VARCHAR(255)    NULL UNIQUE COMMENT '邮箱，可为空（社交登录/Passkey 用户）',
    phone_e164   VARCHAR(20)     NULL UNIQUE COMMENT '手机号（E.164 格式，如 +819012345678）',
    display_name VARCHAR(255)    NULL COMMENT '展示名',
    avatar_url   VARCHAR(1024)   NULL COMMENT '头像 URL',
    status       VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / LOCKED / DELETED',
    created_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_uuid (uuid),
    UNIQUE KEY uk_user_phone_e164 (phone_e164)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- =====================================
--   SOCIAL LOGIN TABLE
-- =====================================
CREATE TABLE IF NOT EXISTS user_social_account
(
    id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id          BIGINT UNSIGNED NOT NULL COMMENT '关联 userEntity.id',
    provider         VARCHAR(50)     NOT NULL COMMENT 'GOOGLE / GITHUB / APPLE / LINE',
    provider_user_id VARCHAR(255)    NOT NULL COMMENT '第三方返回的 SUB / ID',
    email            VARCHAR(255)    NULL COMMENT '社交登录返回的邮箱',
    access_token     TEXT            NULL COMMENT '可选：access token',
    refresh_token    TEXT            NULL COMMENT '可选：refresh token',
    raw_profile      JSON            NULL COMMENT '完整 profile JSON',
    linked_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_provider_user (provider, provider_user_id),
    KEY idx_user_id (user_id),
    CONSTRAINT fk_social_user
        FOREIGN KEY (user_id)
            REFERENCES user (id)
            ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- =====================================
--   PASSKEY (WEBAUTHN) TABLE
-- =====================================
CREATE TABLE IF NOT EXISTS user_passkey
(
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id       BIGINT UNSIGNED NOT NULL COMMENT '关联 userEntity.id',
    credential_id VARBINARY(255)  NOT NULL COMMENT 'WebAuthn Credential ID（二进制）',
    public_key    TEXT            NOT NULL COMMENT '公钥数据（COSE Key / PEM）',
    sign_count    BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '防重放计数',
    nickname      VARCHAR(255)    NULL COMMENT '用户给 passkey 的命名',
    device_type   VARCHAR(50)     NULL COMMENT 'PLATFORM / CROSS_PLATFORM',
    created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at  TIMESTAMP       NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_credential_id (credential_id),
    KEY idx_user_id (user_id),
    CONSTRAINT fk_passkey_user
        FOREIGN KEY (user_id)
            REFERENCES user (id)
            ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS webauthn_credential

(
    id                       BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id                  BIGINT UNSIGNED NOT NULL COMMENT '关联 user.id',

-- WebAuthn credential id (raw bytes)
    credential_id            VARBINARY(1024) NOT NULL COMMENT 'WebAuthn Credential ID (raw bytes)',

-- COSE public key bytes (CBOR)
    public_key_cose          BLOB            NOT NULL COMMENT 'Public key in COSE/CBOR bytes',

-- signature counter (note: can be 0 and some authenticators don’t increment reliably)
    sign_count               BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Signature counter',

-- Optional metadata for device management
    nickname                 VARCHAR(255)    NULL COMMENT '用户自定义名称',
    aaguid                   BINARY(16)      NULL COMMENT 'Authenticator AAGUID (16 bytes)',
    authenticator_attachment VARCHAR(20)     NULL COMMENT 'platform / cross-platform',
    transports               VARCHAR(255)    NULL COMMENT 'internal,hybrid,usb,nfc,ble',
    is_discoverable          TINYINT(1)      NOT NULL DEFAULT 0 COMMENT 'Resident/discoverable credential',
    is_backup_eligible       TINYINT(1)      NULL COMMENT 'BE flag (backup eligible)',
    is_backed_up             TINYINT(1)      NULL COMMENT 'BS flag (backed up)',
    attestation_type         VARCHAR(50)     NULL COMMENT 'none/basic/self/attca/...',
    fmt                      VARCHAR(50)     NULL COMMENT 'attestation format (e.g. packed, fido-u2f)',

    created_at               TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at             TIMESTAMP       NULL,
    revoked_at               TIMESTAMP       NULL COMMENT '撤销时间（软删除）',

    PRIMARY KEY (id),

    UNIQUE KEY uk_webauthn_credential_id (credential_id),
    KEY idx_webauthn_user_id (user_id),
    KEY idx_webauthn_last_used (last_used_at),

    CONSTRAINT fk_webauthn_user
        FOREIGN KEY (user_id)
            REFERENCES user (id)
            ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;



-- =====================================
--   REFRESH TOKEN TABLE (可选但推荐)
-- =====================================
CREATE TABLE IF NOT EXISTS auth_refresh_token
(
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id    BIGINT UNSIGNED NOT NULL,
    token_hash CHAR(64)        NOT NULL COMMENT 'refresh token 的 sha256 hash',
    user_agent VARCHAR(512)    NULL COMMENT '设备 UA',
    ip_address VARCHAR(45)     NULL COMMENT 'IP 地址',
    expires_at TIMESTAMP       NOT NULL COMMENT '过期时间',
    revoked    TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否已撤销',
    created_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_token_hash (token_hash),
    KEY idx_user_id (user_id),
    CONSTRAINT fk_refresh_user
        FOREIGN KEY (user_id)
            REFERENCES user (id)
            ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- =====================================
--   LOGIN AUDIT TABLE (可选，但非常实用）
-- =====================================
CREATE TABLE IF NOT EXISTS auth_login_audit
(
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id    BIGINT UNSIGNED NULL COMMENT '可能为空（失败时无 user_id）',
    login_type VARCHAR(50)     NOT NULL COMMENT 'SOCIAL / PASSKEY / EMAIL',
    provider   VARCHAR(50)     NULL COMMENT 'GOOGLE/GITHUB/APPLE 等',
    success    TINYINT(1)      NOT NULL COMMENT '1=成功, 0=失败',
    ip_address VARCHAR(45)     NULL,
    user_agent VARCHAR(512)    NULL,
    reason     VARCHAR(255)    NULL COMMENT '失败原因',
    created_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_created_at (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;



CREATE TABLE IF NOT EXISTS signup_token
(
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id        BIGINT          NULL,
    token          VARCHAR(128)    NOT NULL,
    identity_type  VARCHAR(10)     NOT NULL COMMENT 'EMAIL / PHONE',
    identity_value VARCHAR(255)    NOT NULL COMMENT 'normalized email or phone_e164',
    expires_at     TIMESTAMP       NOT NULL,
    used_at        TIMESTAMP       NULL,
    created_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_signup_token (token),
    KEY idx_signup_token_expires (expires_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
