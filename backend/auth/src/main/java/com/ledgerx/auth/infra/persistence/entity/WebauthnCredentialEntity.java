package com.ledgerx.auth.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Entity
@Table(
        name = "webauthn_credential",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_webauthn_credential_id", columnNames = "credential_id")
        },
        indexes = {
                @Index(name = "idx_webauthn_user_id", columnList = "user_id"),
                @Index(name = "idx_webauthn_last_used", columnList = "last_used_at")
        })
@Builder
public class WebauthnCredentialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * FK: user_id -> userEntity.id
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = true,
            foreignKey = @ForeignKey(name = "fk_webauthn_user"))
    private UserEntity user;

    /**
     * 只读 userId（避免与 user 关联字段重复写）
     */
    @Column(name = "user_id", nullable = false, insertable = false, updatable = false)
    private Long userId;

    /**
     * WebAuthn Credential ID (raw bytes)
     */
    @Column(name = "credential_id", nullable = false, columnDefinition = "VARBINARY(1024)")
    private byte[] credentialId;

    /**
     * COSE public key (CBOR bytes)
     */
    @Lob
    @Column(name = "public_key_cose", nullable = false)
    private byte[] publicKeyCose;

    /**
     * Signature counter
     */
    @Column(name = "sign_count", nullable = false)
    @Builder.Default
    private long signCount = 0L;

    @Column(name = "nickname", length = 255)
    private String nickname;

    /**
     * Authenticator AAGUID (16 bytes)
     */
    @Column(name = "aaguid", columnDefinition = "BINARY(16)")
    private byte[] aaguid;

    @Column(name = "authenticator_attachment", length = 20)
    private String authenticatorAttachment; // platform / cross-platform

    @Column(name = "transports", length = 255)
    private String transports; // internal,hybrid,usb,nfc,ble

    @Column(name = "is_discoverable", nullable = false)
    @Builder.Default
    private boolean discoverable = false;

    @Column(name = "is_backup_eligible")
    private Boolean backupEligible;

    @Column(name = "is_backed_up")
    private Boolean backedUp;

    @Column(name = "attestation_type", length = 50)
    private String attestationType;

    @Column(name = "fmt", length = 50)
    private String fmt;

    /**
     * created_at 由 DB DEFAULT CURRENT_TIMESTAMP 写入
     */
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private Instant createdAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    /**
     * 业务辅助方法（不入库）
     */
    @Transient
    public boolean isRevoked() {
        return revokedAt != null;
    }
}
