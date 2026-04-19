package com.ledgerx.auth.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ledgerx.auth")
public record AuthProps(Duration signupTokenTtl, BasicAuthClient basicAuthClient) {

  public AuthProps {
    if (basicAuthClient == null) {
      basicAuthClient = BasicAuthClient.disabled();
    }
  }

  public record BasicAuthClient(boolean enabled, String clientId, String clientSecret) {

    static BasicAuthClient disabled() {
      return new BasicAuthClient(false, "", "");
    }

    public boolean hasCredentials() {
      return clientId != null
          && !clientId.isBlank()
          && clientSecret != null
          && !clientSecret.isBlank();
    }
  }
}
