package com.ledgerx.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthOpenApiConfiguration {

  private static final String AUTH_GROUP = "auth";

  @Bean
  public OpenAPI authOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("LedgerX Auth API")
                .description("Authentication, signup, and WebAuthn passkey endpoints.")
                .version("1.0.0"));
  }

  @Bean
  public GroupedOpenApi authGroupedOpenApi() {
    return GroupedOpenApi.builder()
        .group(AUTH_GROUP)
        .pathsToMatch("/auth/**", "/api/auth/**", "/auth/passkey/**", "/api/passkey/**")
        .build();
  }
}
