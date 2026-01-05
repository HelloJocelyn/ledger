package com.ledgerx.auth.infra.otp;

public interface OtpSender {
  void sendEmail(String email, String otp);

  void sendSms(String phoneE164, String otp);
}
