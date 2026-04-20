package com.ledgerx.auth.api.dto;

import com.ledgerx.auth.tool.Base64Url;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.data.exception.Base64UrlException;

import java.io.IOException;


public record WebauthnAuthFinishRequest(
        String id,
        String rawId,
        String type,
        AssertionResponse response
) {

    public PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> toPublicKeyCredential() throws Base64UrlException, IOException {
        return PublicKeyCredential.<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs>builder()
                .id(new ByteArray(Base64Url.decode(rawId)))
                .response(AuthenticatorAssertionResponse.builder()
                        .authenticatorData(new ByteArray(Base64Url.decode(response.authenticatorData())))
                        .clientDataJSON(new ByteArray(Base64Url.decode(response.clientDataJSON())))
                        .signature(new ByteArray(Base64Url.decode(response.signature())))
                        .userHandle(response.userHandle() == null ? null : new ByteArray(Base64Url.decode(response.userHandle())))
                        .build())
                .clientExtensionResults(ClientAssertionExtensionOutputs.builder().build())
                .type(PublicKeyCredentialType.PUBLIC_KEY)
                .build();
    }

    public record AssertionResponse(
            String clientDataJSON,
            String authenticatorData,
            String signature,
            String userHandle
    ) {
    }
}