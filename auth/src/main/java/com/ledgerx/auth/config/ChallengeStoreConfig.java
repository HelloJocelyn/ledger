package com.ledgerx.auth.config;

import com.ledgerx.auth.infra.webauthn.ChallengeStore;
import com.ledgerx.auth.infra.webauthn.InMemoryChallengeStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChallengeStoreConfig {

    @Bean
    public ChallengeStore challengeStore(WebAuthnProps props) {
        // 先用内存版；你要换 Redis，只需要替换这个 Bean
        return new InMemoryChallengeStore();
    }
}
