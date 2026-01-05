package com.ledgerx.auth.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ledgerx.webauthn")
public record WebAuthnProps(
    boolean enabled, String rpName, String rpId, List<String> origins, long challengeTtlSeconds) {}
