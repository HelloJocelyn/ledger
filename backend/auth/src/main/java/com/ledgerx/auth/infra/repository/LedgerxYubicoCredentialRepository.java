package com.ledgerx.auth.infra.repository;

import com.ledgerx.auth.infra.persistence.entity.UserEntity;
import com.ledgerx.auth.infra.persistence.entity.WebauthnCredentialEntity;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * LedgerX implementation of Yubico CredentialRepository.
 *
 * <p>Notes: - username: treated as login identifier (email / phone_e164 / uuid fallback) -
 * userHandle: user.uuid (UTF-8 bytes)
 */
@RequiredArgsConstructor
public class LedgerxYubicoCredentialRepository implements CredentialRepository {

    private final UserRepository userRepo;
    //    private final WebauthnCredentialDao credentialDao;
    private final WebauthnCredentialJpaRepository credentialJpaRepository;

    /**
     * Get the credential IDs of all credentials registered to the user with the given username.
     */
    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        if (username == null || username.isBlank()) {
            return Collections.emptySet();
        }

        Optional<UserEntity> userOpt = findUserByUsername(username);
        if (userOpt.isEmpty()) {
            return Collections.emptySet();
        }

        long userId = userOpt.get().getId();
        return credentialJpaRepository.findByUserId(userId).stream()
                .map(
                        c ->
                                PublicKeyCredentialDescriptor.builder()
                                        .id(new ByteArray(c.getCredentialId()))
                                        .build())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Get the user handle corresponding to the given username.
     */
    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return findUserByUsername(username).map(u -> toUserHandle(u.getUuid()));
    }

    /**
     * Get the username corresponding to the given user handle. For LedgerX, we return email if
     * present; otherwise phone_e164; otherwise uuid string.
     */
    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        if (userHandle == null) {
            return Optional.empty();
        }

        String uuid = fromUserHandle(userHandle);
        Optional<UserEntity> userOpt = userRepo.findByUuid(uuid);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        UserEntity u = userOpt.get();
        if (u.getEmail() != null && !u.getEmail().isBlank()) {
            return Optional.of(u.getEmail());
        }
        if (u.getPhoneE164() != null && !u.getPhoneE164().isBlank()) {
            return Optional.of(u.getPhoneE164());
        }
        return Optional.of(u.getUuid());
    }

    /**
     * Look up the public key and stored signature count for the given credential registered to the
     * given user.
     */
    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        if (credentialId == null || userHandle == null) {
            return Optional.empty();
        }

        String uuid = fromUserHandle(userHandle);
        Optional<UserEntity> userOpt = userRepo.findByUuid(uuid);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        long userId = userOpt.get().getId();

        // IMPORTANT: we only accept active (not revoked) credentials
        Optional<WebauthnCredentialEntity> credOpt =
                credentialJpaRepository.findByCredentialId(credentialId.getBytes());

        return credOpt.map(
                c ->
                        RegisteredCredential.builder()
                                .credentialId(new ByteArray(c.getCredentialId()))
                                .userHandle(toUserHandle(uuid))
                                .publicKeyCose(new ByteArray(c.getPublicKeyCose()))
                                .signatureCount(c.getSignCount())
                                .build());
    }

    /**
     * Look up all credentials with the given credential ID, regardless of what user they're
     * registered to. Used to refuse registration of duplicate credential IDs.
     */
    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        if (credentialId == null) {
            return Collections.emptySet();
        }

        // For duplicate check, you can choose whether to include revoked credentials.
        // I recommend: include BOTH active + revoked to prevent re-registering a previously revoked
        // credential id.
        List<WebauthnCredentialEntity> records =
                credentialJpaRepository.findAnyByCredentialId(credentialId.getBytes());

        return records.stream()
                .map(
                        c -> {
                            // Map DB user_id -> user.uuid -> userHandle
                            Optional<UserEntity> userOpt = userRepo.findById(c.getUserId());
                            if (userOpt.isEmpty()) {
                                return null;
                            }
                            String uuid = userOpt.get().getUuid();

                            return RegisteredCredential.builder()
                                    .credentialId(new ByteArray(c.getCredentialId()))
                                    .userHandle(toUserHandle(uuid))
                                    .publicKeyCose(new ByteArray(c.getPublicKeyCose()))
                                    .signatureCount(c.getSignCount())
                                    .build();
                        })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    // =========================
    // helpers
    // =========================

    private Optional<UserEntity> findUserByUsername(String username) {
        String u = username.trim();

        // Heuristic routing:
        // - email contains "@"
        // - phone_e164 starts with "+"
        // - otherwise try uuid
        if (u.contains("@")) {
            return userRepo.findByEmail(u);
        }
        if (u.startsWith("+")) {
            return userRepo.findByPhoneE164(u);
        }

        // fallback: treat as uuid
        return userRepo.findByUuid(u);
    }

    private static ByteArray toUserHandle(String uuid) {
        // user.uuid is CHAR(36); use UTF-8 bytes as userHandle
        return new ByteArray(uuid.getBytes(StandardCharsets.UTF_8));
    }

    private static String fromUserHandle(ByteArray userHandle) {
        return new String(userHandle.getBytes(), StandardCharsets.UTF_8);
    }
}
