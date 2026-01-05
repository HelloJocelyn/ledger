package com.ledgerx.auth.api.controller;

import com.ledgerx.auth.api.dto.IdentifyAndSendOtpRequest;
import com.ledgerx.auth.api.dto.IdentifyAndSendOtpResponse;
import com.ledgerx.auth.api.dto.VerifyOtpRequest;
import com.ledgerx.auth.api.dto.VerifyOtpResponse;
import com.ledgerx.auth.application.service.AuthIdentifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthIdentifyService authService;

  @PostMapping("/identify-and-send-otp")
  public IdentifyAndSendOtpResponse identifyAndSendOtp(@RequestBody IdentifyAndSendOtpRequest req) {
    return authService.identifyAndSendOtp(req.identity());
  }

  @PostMapping("/otp/verify")
  public VerifyOtpResponse verify(@RequestBody VerifyOtpRequest req) {
    return authService.verify(req);
  }
}
