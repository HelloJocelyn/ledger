package com.ledgerx.auth.application.service;

import com.ledgerx.auth.api.dto.CreateAccountRequest;
import com.ledgerx.auth.api.dto.CreateAccountResponse;
import com.ledgerx.auth.infra.persistence.entity.SignupTokenEntity;
import com.ledgerx.auth.infra.persistence.entity.UserEntity;
import com.ledgerx.auth.infra.repository.SignupTokenRepository;
import com.ledgerx.auth.infra.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignupService {

  private final SignupTokenRepository signupTokenRepo;
  private final UserRepository userRepo;
  private final AuthSessionService authSessionService; // 下面给骨架

  @Transactional
  public CreateAccountResponse createAccount(CreateAccountRequest req) {

    if (req.signupToken() == null || req.signupToken().isBlank()) {
      throw new IllegalArgumentException("signupToken is required");
    }
    if (req.displayName() == null || req.displayName().isBlank()) {
      throw new IllegalArgumentException("displayName is required");
    }

    SignupTokenEntity st =
        signupTokenRepo
            .findByToken(req.signupToken())
            .orElseThrow(() -> new IllegalArgumentException("INVALID_SIGNUP_TOKEN"));

    Instant now = Instant.now();
    if (st.getUsedAt() != null) throw new IllegalArgumentException("SIGNUP_TOKEN_USED");
    if (st.getExpiresAt().isBefore(now)) throw new IllegalArgumentException("SIGNUP_TOKEN_EXPIRED");

    String identityType = st.getIdentityType(); // EMAIL / PHONE
    String identityValue = st.getIdentityValue(); // normalized email / phone_e164

    // 如果并发/重复提交，先查一下是否已存在
    if ("EMAIL".equals(identityType) && userRepo.findByEmail(identityValue).isPresent()) {
      throw new IllegalArgumentException("EMAIL_ALREADY_EXISTS");
    }
    if ("PHONE".equals(identityType) && userRepo.findByPhoneE164(identityValue).isPresent()) {
      throw new IllegalArgumentException("PHONE_ALREADY_EXISTS");
    }

    UserEntity user = new UserEntity();
    user.setUuid(UUID.randomUUID().toString());
    user.setDisplayName(req.displayName().trim());
    user.setStatus("ACTIVE");

    if ("EMAIL".equals(identityType)) {
      user.setEmail(identityValue);
    } else if ("PHONE".equals(identityType)) {
      user.setPhoneE164(identityValue);
    } else {
      throw new IllegalArgumentException("UNKNOWN_IDENTITY_TYPE");
    }

    userRepo.save(user);

    // 消费 token
    st.setUsedAt(now);
    signupTokenRepo.save(st);

    // ✅ 建立登录态（session）。如果你还没做，也可以先不调用
    authSessionService.signIn(user);

    return new CreateAccountResponse(true, "SIGNED_IN", user.getUuid());
  }
}
