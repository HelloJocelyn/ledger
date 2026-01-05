package com.ledgerx.auth.security;

import com.ledgerx.auth.security.filter.BearerTokenAuthFilter;
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
      HttpSecurity http, BearerTokenAuthFilter authFilter) throws Exception {

    http.cors(Customizer.withDefaults())
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/", "/login", "/css/**", "/js/**")
                    .permitAll()
                    .requestMatchers(("/api/auth/**"))
                    .permitAll()
                    .requestMatchers("/api/passkey/**")
                    .permitAll()
                    .requestMatchers("/oauth2/**")
                    .permitAll()
                    .requestMatchers("/error")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
        .httpBasic(Customizer.withDefaults());
    //                .oauth2Login(
    //                        oauth2 ->
    //                                oauth2
    //                                        .loginPage("/login") // 你可以做一个简单的登录页，按钮跳
    // /oauth2/authorization/github
    //                                        .userInfoEndpoint(userInfo ->
    // userInfo.userService(customOAuth2UserService))
    //                                        .defaultSuccessUrl("/me", true) // 登录后跳转页面，可以改成你前端的地址
    //                );

    return http.build();
  }

  @Bean
  public BearerTokenAuthFilter bearerTokenAuthFilter(TokenService tokenService) {
    return new BearerTokenAuthFilter(tokenService);
  }
}
