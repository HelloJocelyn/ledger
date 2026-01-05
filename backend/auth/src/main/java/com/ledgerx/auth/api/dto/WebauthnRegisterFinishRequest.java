package com.ledgerx.auth.api.dto;

import java.util.List;

public record WebauthnRegisterFinishRequest(
        String id,
        String rawId,   // base64url
        String type,
        Response response
) {
    public record Response(
            String clientDataJSON,       // base64url
            String attestationObject,    // base64url
            List<String> transports      // optional
    ) {
    }
}
