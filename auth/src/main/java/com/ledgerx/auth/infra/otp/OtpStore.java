package com.ledgerx.auth.infra.otp;

import java.time.Duration;
import java.util.Optional;

public interface OtpStore {
  void save(String key, String otp, Duration ttl);

  Optional<String> get(String key);

  void delete(String key);
}
