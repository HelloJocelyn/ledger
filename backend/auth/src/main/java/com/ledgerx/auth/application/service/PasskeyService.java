package com.ledgerx.auth.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledgerx.auth.api.dto.WebauthnAuthFinishRequest;
import com.ledgerx.auth.api.dto.WebauthnRegisterFinishRequest;
import com.ledgerx.auth.config.WebAuthnProps;
import com.ledgerx.auth.infra.persistence.entity.UserEntity;
import com.ledgerx.auth.infra.persistence.entity.WebauthnCredentialEntity;
import com.ledgerx.auth.infra.repository.UserRepository;
import com.ledgerx.auth.infra.repository.WebauthnCredentialJpaRepository;
import com.ledgerx.auth.infra.webauthn.ChallengeStore;
import com.ledgerx.auth.security.UserPrincipal;
import com.ledgerx.auth.tool.Base64Url;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.data.exception.Base64UrlException;
import com.yubico.webauthn.exception.AssertionFailedException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

import static com.yubico.webauthn.data.UserVerificationRequirement.PREFERRED;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasskeyService {

    private final RelyingParty relyingParty;
    private final WebAuthnProps props;
    private final ChallengeStore challengeStore;
    private final WebauthnCredentialJpaRepository credentialRepository;
    private final UserRepository userRepository;

    private final ObjectMapper om = new ObjectMapper();

    public PublicKeyCredentialCreationOptions startRegistration(UserPrincipal userPrincipal) {

        UserIdentity user =
                UserIdentity.builder()
                        .name(userPrincipal.getEmail())
                        .displayName(userPrincipal.getUsername())
                        .id(new ByteArray(userPrincipal.getUuid().getBytes(StandardCharsets.UTF_8)))
                        .build();

        StartRegistrationOptions options =
                StartRegistrationOptions.builder()
                        .user(user)
                        .authenticatorSelection(
                                AuthenticatorSelectionCriteria.builder()
                                        .residentKey(ResidentKeyRequirement.PREFERRED)
                                        .userVerification(PREFERRED)
                                        .build())
                        //        .attestation(AttestationConveyancePreference.NONE)
                        .build();

        PublicKeyCredentialCreationOptions pkcco = relyingParty.startRegistration(options);

        challengeStore.saveRegistrationOptions(
                userPrincipal.getUserId(), pkcco, Duration.ofSeconds(props.challengeTtlSeconds()));
        return pkcco;
    }

    private static PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs>
    toYubicoCredential(WebauthnRegisterFinishRequest req) {

        ByteArray rawId = new ByteArray(Base64Url.decode(req.rawId()));
        ByteArray clientDataJson = new ByteArray(Base64Url.decode(req.response().clientDataJSON()));
        ByteArray attestationObject = new ByteArray(Base64Url.decode(req.response().attestationObject()));

        var builder = AuthenticatorAttestationResponse.builder()
                .attestationObject(attestationObject)
                .clientDataJSON(clientDataJson);

        // transports 可选：有就设置，没有就跳过
        if (req.response().transports() != null && !req.response().transports().isEmpty()) {
            java.util.Set<AuthenticatorTransport> transports = req.response().transports().stream()
                    .map(AuthenticatorTransport::of)
                    .collect(java.util.stream.Collectors.toSet());
            builder.transports(transports);
        }

        try {
            AuthenticatorAttestationResponse attestationResponse = builder.build();

            return PublicKeyCredential.<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs>builder()
                    .id(rawId)
                    .response(attestationResponse)
                    .clientExtensionResults(ClientRegistrationExtensionOutputs.builder().build())
                    .type(PublicKeyCredentialType.PUBLIC_KEY)
                    .build();
        } catch (IOException | Base64UrlException e) {
            throw new RuntimeException(e);
        }
    }


    public boolean finishRegistration(UserEntity user, WebauthnRegisterFinishRequest req) {
        PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential =
                toYubicoCredential(req);


        PublicKeyCredentialCreationOptions request =
                challengeStore
                        .getRegistrationOptions(user.getId())
                        .orElseThrow(
                                () -> new IllegalArgumentException("No registration options for " + user.getId()));

        try {
            RegistrationResult result =
                    relyingParty.finishRegistration(
                            FinishRegistrationOptions.builder()
                                    .request(request)
                                    .response(credential)
                                    .build()
                    );
            WebauthnCredentialEntity entity = WebauthnCredentialEntity.builder()
                    .userId(user.getId())
                    .user(user)
                    .credentialId(result.getKeyId().getId().getBytes())
                    .publicKeyCose(result.getPublicKeyCose().getBytes())
                    .signCount(result.getSignatureCount())
                    .discoverable(result.isDiscoverable().orElse(false))
                    .backupEligible(result.isBackupEligible())
                    .backedUp(result.isBackedUp())
                    .aaguid(result.getAaguid().getBytes())
                    .attestationType(result.getAttestationType().name())
                    .build();

            byte[] credId = Base64Url.decode(req.rawId());
            log.info("Passkey register finish: credIdHex={}", HexFormat.of().formatHex(credId));
            WebauthnCredentialEntity saved = credentialRepository.save(entity);
            log.info("Passkey saved: id={}, userId={}, credIdHex={}",
                    saved.getId(), saved.getUserId(), HexFormat.of().formatHex(saved.getCredentialId()));
            challengeStore.deleteRegistrationOptions(user.getId());
            return true;

        } catch (Exception e) {
            log.error("Error finishing registration", e);
            challengeStore.deleteRegistrationOptions(user.getId());
            return false;
        }
    }

    public PublicKeyCredentialRequestOptions startAuthentication(String email, HttpSession session) {
        if (email != null && !email.isBlank()) {
            UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> {
                throw new IllegalArgumentException("No user for email " + email);
            });

        }
        StartAssertionOptions.StartAssertionOptionsBuilder builder = StartAssertionOptions.builder()
                .userVerification(PREFERRED)
                .timeout(60000L);
        if (email != null && !email.isBlank()) {
            builder.username(email);
        }

        AssertionRequest request =
                relyingParty.startAssertion(
                        builder
                                .build());

        challengeStore.saveAssertionRequest(
                session, request, Duration.ofSeconds(props.challengeTtlSeconds()));

        return request.getPublicKeyCredentialRequestOptions();
    }

    public boolean finishAuthentication(WebauthnAuthFinishRequest req, HttpSession session) {
        try {
            AssertionRequest assertionRequest = challengeStore.getAssertionRequest(session).orElseThrow(() -> new IllegalArgumentException("No assertion request in session"));
            PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> pkc = req.toPublicKeyCredential();
            FinishAssertionOptions assertionOptions = FinishAssertionOptions.builder().request(assertionRequest).response(pkc).build();
            AssertionResult result =
                    relyingParty.finishAssertion(assertionOptions);
            if (!result.isSuccess()) {
                challengeStore.deleteAssertionRequest(session);
                return false;
            }
            byte[] credentialId = pkc.getId().getBytes();
            // 更新 signCount
            WebauthnCredentialEntity webauthnCredential = credentialRepository.findByCredentialId(credentialId).orElseThrow(() -> new IllegalArgumentException("No credential found for " + credentialId));
            webauthnCredential.setSignCount(result.getSignatureCount());
            webauthnCredential.setLastUsedAt(Instant.now());
            credentialRepository.save(webauthnCredential);
            challengeStore.deleteAssertionRequest(session);
            return true;
        } catch (Base64UrlException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (AssertionFailedException e) {
            throw new RuntimeException(e);
        }
    }
}
