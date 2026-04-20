package com.ledgerx.auth.infra.otp;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryOtpStore implements OtpStore {

  private record Entry(String otp, Instant expireAt) {}

  private final Map<String, Entry> map = new ConcurrentHashMap<>();

  @Override
  public void save(String key, String otp, Duration ttl) {
    map.put(key, new Entry(otp, Instant.now().plus(ttl)));
  }

  @Override
  public Optional<String> get(String key) {
    Entry e = map.get(key);
    if (e == null) return Optional.empty();
    if (Instant.now().isAfter(e.expireAt())) {
      map.remove(key);
      return Optional.empty();
    }
    return Optional.of(e.otp());
  }

  @Override
  public void delete(String key) {
    map.remove(key);
  }
}
