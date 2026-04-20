package com.ledgerx.auth.infra.repository;

import com.ledgerx.auth.infra.persistence.entity.WebauthnCredentialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WebauthnCredentialJpaRepository
        extends JpaRepository<WebauthnCredentialEntity, Long> {

    List<WebauthnCredentialEntity> findByNickname(String nickname);

    Optional<WebauthnCredentialEntity> findByUserId(long userId);

    List<WebauthnCredentialEntity> findAllByUserId(long userId);

    Optional<WebauthnCredentialEntity> findByCredentialId(byte[] credentialId);

    List<WebauthnCredentialEntity> findAnyByCredentialId(
            @Param("credentialId") byte[] credentialId
    );
}
