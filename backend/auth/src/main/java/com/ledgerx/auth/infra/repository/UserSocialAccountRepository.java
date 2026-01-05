package com.ledgerx.auth.infra.repository;

import com.ledgerx.auth.domain.model.UserSocialAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSocialAccountRepository extends JpaRepository<UserSocialAccount, Long> {

  Optional<UserSocialAccount> findByProviderAndProviderUserId(
      String provider, String providerUserId);
}
