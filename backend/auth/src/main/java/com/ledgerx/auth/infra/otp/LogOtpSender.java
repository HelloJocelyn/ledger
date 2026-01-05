package com.ledgerx.auth.infra.otp;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogOtpSender implements OtpSender {
  @Override
  public void sendEmail(String email, String otp) {
    log.info("[OTP][EMAIL] to={} otp={}", email, otp);
  }

  @Override
  public void sendSms(String phoneE164, String otp) {
    log.info("[OTP][SMS] to={} otp={}", phoneE164, otp);
  }
}
