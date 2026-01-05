package com.ledgerx.auth.application.service;

import com.ledgerx.auth.api.dto.IdentifyAndSendOtpResponse;
import com.ledgerx.auth.api.dto.VerifyOtpRequest;
import com.ledgerx.auth.api.dto.VerifyOtpResponse;
import com.ledgerx.auth.config.AuthProps;
import com.ledgerx.auth.infra.otp.OtpSender;
import com.ledgerx.auth.infra.otp.OtpStore;
import com.ledgerx.auth.infra.persistence.entity.SignupTokenEntity;
import com.ledgerx.auth.infra.persistence.entity.UserEntity;
import com.ledgerx.auth.infra.persistence.entity.WebauthnCredentialEntity;
import com.ledgerx.auth.infra.repository.SignupTokenRepository;
import com.ledgerx.auth.infra.repository.UserRepository;
import com.ledgerx.auth.infra.repository.WebauthnCredentialJpaRepository;
import com.ledgerx.auth.tool.IdentityUtil;
import com.ledgerx.auth.tool.TokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthIdentifyService {

    private final UserRepository userRepo;
    private final OtpStore otpStore;
    private final OtpSender otpSender;
    private final SignupTokenRepository signupTokenRepo;
    private final AuthProps authProps;
    private final WebauthnCredentialJpaRepository credentialJpaRepository;

    private final SecureRandom random = new SecureRandom();

    // 你可以放到配置里
    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final int COOLDOWN_SECONDS = 30;

    // 如果你坚持要给前端返回 registered（不推荐），开这个开关
    private static final boolean EXPOSE_REGISTERED = false;

    public IdentifyAndSendOtpResponse identifyAndSendOtp(String rawIdentity) {
        var type = IdentityUtil.detectType(rawIdentity);

        final String normalized;
        final String masked;
        final boolean registered;

        if (type == IdentityUtil.IdentityType.EMAIL) {
            normalized = IdentityUtil.normalizeEmail(rawIdentity);
            masked = IdentityUtil.maskEmail(normalized);
            registered = userRepo.findByEmail(normalized).isPresent();
            String otp = genOtp6();
            otpStore.save(key(type, normalized), otp, OTP_TTL);
            otpSender.sendEmail(normalized, otp);
        } else {
            normalized = IdentityUtil.normalizePhoneJapanLike(rawIdentity);
            masked = IdentityUtil.maskPhone(normalized);
            registered = userRepo.findByPhoneE164(normalized).isPresent();
            String otp = genOtp6();
            otpStore.save(key(type, normalized), otp, OTP_TTL);
            otpSender.sendSms(normalized, otp);
        }

        return new IdentifyAndSendOtpResponse(
                type.name(), normalized, masked, COOLDOWN_SECONDS, (int) OTP_TTL.toSeconds());
    }

    private String key(IdentityUtil.IdentityType type, String normalized) {
        return type.name() + ":" + normalized;
    }

    private String genOtp6() {
        int n = random.nextInt(1_000_000); // 0..999999
        return String.format("%06d", n);
    }

    public VerifyOtpResponse verify(VerifyOtpRequest req) {
        IdentityUtil.IdentityType type = IdentityUtil.IdentityType.valueOf(req.identityType());
        String normalized = IdentityUtil.normalize(type, req.identity());
        String key = type.name() + ":" + normalized;
        String expected = otpStore.get(key).orElse(null);
        if (expected == null || !expected.equals(req.code())) {

            return new VerifyOtpResponse(false, null, null, null, "INVALID_CODE", false);
        }
        otpStore.delete(key);
        UserEntity user =
                (type == IdentityUtil.IdentityType.EMAIL)
                        ? userRepo.findByEmail(normalized).orElse(null)
                        : userRepo.findByPhoneE164(normalized).orElse(null);

        Optional<WebauthnCredentialEntity> passkey = credentialJpaRepository.findByUserId(user.getId());
        SignupTokenEntity token = genToken(req.identityType(), req.identity());
        VerifyOtpResponse response;
        if (user != null) {
            token.setUserId(user.getId());
            response = new VerifyOtpResponse(
                    true, "SIGNED_IN", false, token.getToken(), null, passkey.isPresent());
        } else {
            response = new VerifyOtpResponse(
                    true, "CREATE_ACCOUNT", true, token.getToken(), null, passkey.isPresent());
        }
        signupTokenRepo.save(token);
        return response;
    }

    private SignupTokenEntity genToken(String identityType, String identity) {
        String token = TokenGenerator.newSignupToken();
        SignupTokenEntity tokenEntity = new SignupTokenEntity();
        tokenEntity.setToken(token);
        tokenEntity.setIdentityType(identityType);
        tokenEntity.setIdentityValue(identity);
        tokenEntity.setExpiresAt(Instant.now().plus(authProps.signupTokenTtl()));
        return tokenEntity;
    }
}
