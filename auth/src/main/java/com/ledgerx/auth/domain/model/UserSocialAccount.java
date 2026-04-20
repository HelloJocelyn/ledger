package com.ledgerx.auth.domain.model;

import com.ledgerx.auth.infra.persistence.entity.UserEntity;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Table(
    name = "user_social_account",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_provider_user",
          columnNames = {"provider", "provider_user_id"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSocialAccount {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity userEntity;

  @Column(nullable = false, length = 50)
  private String provider; // "GITHUB"

  @Column(name = "provider_user_id", nullable = false, length = 255)
  private String providerUserId;

  private String email;

  @Column(name = "access_token", columnDefinition = "TEXT")
  private String accessToken;

  @Column(name = "refresh_token", columnDefinition = "TEXT")
  private String refreshToken;

  @Column(name = "raw_profile", columnDefinition = "JSON")
  private String rawProfile;

  @Column(name = "linked_at", updatable = false)
  private Instant linkedAt;

  @PrePersist
  public void prePersist() {
    this.linkedAt = Instant.now();
  }

  public UserEntity getUserEntity() {
    return userEntity;
  }

  // getters/setters ...
}
