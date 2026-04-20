package com.ledgerx.auth.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "signup_token")
@Getter
@Setter
public class SignupTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true, unique = true)
    private Long userId;

    @Column(nullable = false, unique = true, length = 128)
    private String token;

    @Column(name = "identity_type", nullable = false, length = 10)
    private String identityType; // "EMAIL" / "PHONE"

    @Column(name = "identity_value", nullable = false, length = 255)
    private String identityValue; // normalized

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        //        this.usedAt = now;
    }

    //    @PostLoad
    //    public void postLoad() {
    //        this.usedAt = Instant.now();
    //    }
}
