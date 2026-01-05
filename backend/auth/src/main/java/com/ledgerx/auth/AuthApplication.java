package com.ledgerx.auth;

import com.ledgerx.auth.config.AuthProps;
import com.ledgerx.auth.config.WebAuthnProps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({WebAuthnProps.class, AuthProps.class})
public class AuthApplication {

  public static void main(String[] args) {
    SpringApplication.run(AuthApplication.class, args);
  }
}
