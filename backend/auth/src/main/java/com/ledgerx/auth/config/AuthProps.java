package com.ledgerx.auth.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ledgerx.auth")
public record AuthProps(Duration signupTokenTtl) {
  //  private Duration signupTokenTtl = Duration.ofMinutes(15);
  // getter / setter
}
