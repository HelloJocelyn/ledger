package com.ledgerx.auth.api.controller;

import com.ledgerx.auth.api.dto.WebauthnAuthFinishRequest;
import com.ledgerx.auth.api.dto.WebauthnAuthOptionsRequest;
import com.ledgerx.auth.api.dto.WebauthnRegisterFinishRequest;
import com.ledgerx.auth.application.service.PasskeyService;
import com.ledgerx.auth.infra.persistence.entity.UserEntity;
import com.ledgerx.auth.infra.webauthn.ChallengeStore;
import com.ledgerx.auth.security.UserPrincipal;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.PublicKeyCredentialRequestOptions;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/passkey")
@RequiredArgsConstructor
public class PasskeyController {

    private final PasskeyService passkeyService;
    private final ChallengeStore challengeStore;

    @PostMapping("/registration/options")
    public PublicKeyCredentialCreationOptions registrationOptions(
            @AuthenticationPrincipal UserPrincipal principal) {
        return passkeyService.startRegistration(principal);
    }

    @PostMapping("/registration/finish")
    public VerifyResp registrationVerify(@AuthenticationPrincipal UserPrincipal principal,
                                         @RequestBody WebauthnRegisterFinishRequest req) {
        UserEntity user = principal.getUser();

        boolean registered = passkeyService.finishRegistration(user, req);
        return new VerifyResp(registered);
    }

    @PostMapping("/authentication/options")
    public PublicKeyCredentialRequestOptions authenticationOptions(WebauthnAuthOptionsRequest request, HttpSession session) {
        return passkeyService.startAuthentication(request.email(), session);
    }

    @PostMapping("/authentication/finish")
    public VerifyResp authenticationVerify(@RequestBody WebauthnAuthFinishRequest req, HttpSession session) {
        boolean verified = passkeyService.finishAuthentication(req, session);
        return new VerifyResp(verified);
    }


    public record VerifyResp(boolean verified) {
    }

}
