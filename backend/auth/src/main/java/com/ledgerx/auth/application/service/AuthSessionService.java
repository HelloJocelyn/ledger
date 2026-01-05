package com.ledgerx.auth.application.service;

import com.ledgerx.auth.infra.persistence.entity.UserEntity;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthSessionService {

  private final HttpSession session;

  public void signIn(UserEntity user) {
    session.setAttribute("USER_UUID", user.getUuid());
    session.setAttribute("USER_ID", user.getId());
  }
}
