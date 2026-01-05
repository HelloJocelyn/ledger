package com.ledgerx.auth.infra.webauthn;

import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryChallengeStore implements ChallengeStore {

    private static final class Entry<T> {
        final T value;
        final Instant expireAt;

        Entry(T value, Instant expireAt) {
            this.value = value;
            this.expireAt = expireAt;
        }

        boolean expired() {
            return Instant.now().isAfter(expireAt);
        }
    }

    private final Map<Long, Entry<PublicKeyCredentialCreationOptions>> reg =
            new ConcurrentHashMap<>();
    private final Map<HttpSession, Entry<AssertionRequest>> auth = new ConcurrentHashMap<>();

    @Override
    public void saveRegistrationOptions(
            Long userId, PublicKeyCredentialCreationOptions options, Duration ttl) {
        reg.put(userId, new Entry<>(options, Instant.now().plus(ttl)));
    }

    @Override
    public Optional<PublicKeyCredentialCreationOptions> getRegistrationOptions(Long userId) {
        Entry<PublicKeyCredentialCreationOptions> e = reg.get(userId);
        if (e == null) return Optional.empty();
        if (e.expired()) {
            reg.remove(userId);
            return Optional.empty();
        }
        return Optional.of(e.value);
    }

    @Override
    public void deleteRegistrationOptions(Long userId) {
        reg.remove(userId);
    }

    @Override
    public void saveAssertionRequest(HttpSession session, AssertionRequest request, Duration ttl) {
        auth.put(session, new Entry<>(request, Instant.now().plus(ttl)));
    }

    @Override
    public Optional<AssertionRequest> getAssertionRequest(HttpSession session) {
        Entry<AssertionRequest> e = auth.get(session);
        if (e == null) return Optional.empty();
        if (e.expired()) {
            auth.remove(session);
            return Optional.empty();
        }
        return Optional.of(e.value);
    }

    @Override
    public void deleteAssertionRequest(HttpSession session) {
        auth.remove(session);
    }
}
