package com.ledgerx.auth.infra.repository;

import com.ledgerx.auth.infra.persistence.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

  Optional<UserEntity> findByUuid(String uuid);

  Optional<UserEntity> findByEmail(String email);

  Optional<UserEntity> findByPhoneE164(String phoneE164);
}
