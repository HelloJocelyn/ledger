package com.ledgerx.auth.security;

import com.ledgerx.auth.infra.persistence.entity.SignupTokenEntity;
import com.ledgerx.auth.infra.repository.SignupTokenRepository;
import com.ledgerx.auth.infra.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DbTokenService implements TokenService {

    private final SignupTokenRepository
            signupTokenRepository; // token表：token, user_id, expires_at, revoked_at...
    private final UserRepository userRepository; // user表：id, uuid...

    public DbTokenService(
            SignupTokenRepository signupTokenRepository, UserRepository userRepository) {
        this.signupTokenRepository = signupTokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<UserPrincipal> authenticate(String token) {
        // 1) 查 token 是否存在/未撤销/未过期
        Optional<SignupTokenEntity> recOpt = signupTokenRepository.findByToken(token);
        if (recOpt.isEmpty()) return Optional.empty();

        long userId = recOpt.get().getUserId();

        // 2) 查用户
        return userRepository
                .findById(userId)
                .map(
                        u -> new UserPrincipal(u.getId(), u.getUuid(), u.getEmail(), u.getDisplayName(), null, u));
    }
}
