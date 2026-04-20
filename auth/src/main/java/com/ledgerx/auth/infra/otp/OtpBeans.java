package com.ledgerx.auth.infra.otp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OtpBeans {
  @Bean
  public OtpStore otpStore() {
    return new InMemoryOtpStore();
  }

  @Bean
  public OtpSender otpSender() {
    return new LogOtpSender();
  }
}
