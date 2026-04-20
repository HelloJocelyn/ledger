package com.ledgerx.auth.api.dto;

public record VerifyOtpResponse(
    boolean verified,
    String next,
    Boolean isNewUser,
    String signupToken,
    String errorCode,
    boolean hasPasskey) {}
