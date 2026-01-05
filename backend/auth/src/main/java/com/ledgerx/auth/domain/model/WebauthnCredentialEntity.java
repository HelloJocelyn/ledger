// package com.ledgerx.auth.domain.model;
//
// import jakarta.persistence.*;
// import lombok.Getter;
// import lombok.Setter;
//
// import java.time.Instant;
//
// @Entity
// @Table(
//        name = "user_passkey",
//        uniqueConstraints = @UniqueConstraint(name = "uk_credential_id", columnNames =
// "credential_id"),
//        indexes = @Index(name = "idx_user", columnList = "user_id"))
// @Getter
// @Setter
// public class WebauthnCredentialEntity {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "user_id", nullable = false, length = 64)
//    private String userId;
//
//    @Column(name = "username", nullable = false, length = 128)
//    private String username;
//
/// /  @Lob
/// /  @Column(name = "credential_id", nullable = false)
/// /  private byte[] credentialId;
//
//    @Lob
//    @Column(name = "public_key_cose", nullable = false)
//    private byte[] publicKeyCose;
//
//    @Column(name = "sign_count", nullable = false)
//    private long signCount;
//
//    @Column(name = "transports")
//    private String transports; // 简单存成 JSON 字符串或逗号分隔
//
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private Instant createdAt = Instant.now();
//
//    @Column(name = "updated_at", nullable = false)
//    private Instant updatedAt = Instant.now();
//
//    @PreUpdate
//    public void preUpdate() {
//        this.updatedAt = Instant.now();
//    }
// }
