package com.ledgerx.auth.infra.webauthn;

import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import jakarta.servlet.http.HttpSession;

import java.time.Duration;
import java.util.Optional;

public interface ChallengeStore {

    void saveRegistrationOptions(
            Long userId, PublicKeyCredentialCreationOptions options, Duration ttl);

    Optional<PublicKeyCredentialCreationOptions> getRegistrationOptions(Long userId);

    void deleteRegistrationOptions(Long userId);

    void saveAssertionRequest(HttpSession session, AssertionRequest request, Duration ttl);

    Optional<AssertionRequest> getAssertionRequest(HttpSession session);

    void deleteAssertionRequest(HttpSession session);
}
