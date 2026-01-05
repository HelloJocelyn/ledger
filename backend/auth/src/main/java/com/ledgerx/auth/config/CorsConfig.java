package com.ledgerx.auth.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();

    // ✅ 明确允许的前端 origin（不能用 *，因为你要用 credentials）
    config.setAllowedOrigins(
        List.of(
            "http://localhost:3000"
            // 以后可以加：
            // "https://ledgerx.dev",
            // "https://app.ledgerx.com"
            ));

    // 允许的 HTTP 方法
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

    // 允许的请求头
    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));

    // 如果你用 cookie / session / oauth2，一定要 true
    config.setAllowCredentials(true);

    // 可选：前端能读到的响应头
    config.setExposedHeaders(List.of("Set-Cookie"));

    // 预检请求缓存时间（秒）
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

    // 对所有接口生效
    source.registerCorsConfiguration("/**", config);

    return source;
  }
}
