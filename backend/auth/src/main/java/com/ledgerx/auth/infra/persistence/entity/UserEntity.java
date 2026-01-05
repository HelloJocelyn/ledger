package com.ledgerx.auth.infra.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 36, unique = true)
  private String uuid;

  @Column(unique = true)
  private String email;

  @Column(unique = true, name = "phone_e164", length = 20)
  private String phoneE164;

  @Column(name = "display_name")
  private String displayName;

  @Column(name = "avatar_url", length = 1024)
  private String avatarUrl;

  @Column(nullable = false, length = 20)
  @Builder.Default
  private String status = "ACTIVE";

  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @PrePersist
  public void prePersist() {
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = Instant.now();
  }
}
