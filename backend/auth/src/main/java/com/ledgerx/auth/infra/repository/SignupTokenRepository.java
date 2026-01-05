package com.ledgerx.auth.infra.repository;

import com.ledgerx.auth.infra.persistence.entity.SignupTokenEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignupTokenRepository extends JpaRepository<SignupTokenEntity, Long> {
  Optional<SignupTokenEntity> findByToken(String token);
}
