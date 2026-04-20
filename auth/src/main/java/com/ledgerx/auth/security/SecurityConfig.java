package com.ledgerx.auth.security;

import com.ledgerx.auth.security.filter.BearerTokenAuthFilter;
import com.ledgerx.auth.security.filter.ClientBasicAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

  //    private final CustomOAuth2UserService customOAuth2UserService;
  //
  //    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService) {
  //        this.customOAuth2UserService = customOAuth2UserService;
  //    }

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      ClientBasicAuthFilter clientBasicAuthFilter,
      BearerTokenAuthFilter authFilter)
      throws Exception {

    http.cors(Customizer.withDefaults())
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/", "/login", "/css/**", "/js/**")
                    .permitAll()
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                    .permitAll()
                    .requestMatchers("/auth/**")
                    .permitAll()
                    .requestMatchers("/api/auth/**")
                    .permitAll()
                    .requestMatchers("/api/passkey/**")
                    .permitAll()
                    .requestMatchers("/oauth2/**")
                    .permitAll()
                    .requestMatchers("/error")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(clientBasicAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(authFilter, ClientBasicAuthFilter.class);

    return http.build();
  }

  @Bean
  public BearerTokenAuthFilter bearerTokenAuthFilter(TokenService tokenService) {
    return new BearerTokenAuthFilter(tokenService);
  }
}
