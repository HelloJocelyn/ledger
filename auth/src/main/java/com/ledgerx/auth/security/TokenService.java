package com.ledgerx.auth.security;

import java.util.Optional;

public interface TokenService {
  Optional<UserPrincipal> authenticate(String token);
}
