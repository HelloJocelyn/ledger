package com.ledgerx.auth.api.controller;

import com.ledgerx.auth.api.dto.CreateAccountRequest;
import com.ledgerx.auth.api.dto.CreateAccountResponse;
import com.ledgerx.auth.application.service.SignupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class SignupController {

  private final SignupService signupService;

  @PostMapping("/create-account")
  public CreateAccountResponse createAccount(@RequestBody CreateAccountRequest req) {
    return signupService.createAccount(req);
  }
}
