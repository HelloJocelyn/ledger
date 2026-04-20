package com.ledgerx.auth.config;

import com.ledgerx.auth.infra.repository.LedgerxYubicoCredentialRepository;
import com.ledgerx.auth.infra.repository.UserRepository;
import com.ledgerx.auth.infra.repository.WebauthnCredentialJpaRepository;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
public class WebAuthnConfig {

    private final WebAuthnProps props;
    private final WebauthnCredentialJpaRepository credentialJpaRepository;
    private final UserRepository userRepo;

    @Bean
    public RelyingParty relyingParty() {
        RelyingPartyIdentity rpIdentity =
                RelyingPartyIdentity.builder().id(props.rpId()).name(props.rpName()).build();

        // 这里的 CredentialRepository 是 yubico 的接口名（注意别和你 JPA repo 冲突）
        CredentialRepository yubicoRepo = new LedgerxYubicoCredentialRepository(userRepo, credentialJpaRepository);

        return RelyingParty.builder()
                .identity(rpIdentity)
                .credentialRepository(yubicoRepo)
                .origins(new HashSet<>(props.origins()))
                .build();
    }
}
