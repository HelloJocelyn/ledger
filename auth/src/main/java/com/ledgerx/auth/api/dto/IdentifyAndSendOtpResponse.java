package com.ledgerx.auth.api.dto;

public record IdentifyAndSendOtpResponse(
    String identityType,
    String normalizedIdentity,
    String maskedIdentity,
    int cooldownSeconds,
    int expiresInSeconds) {}
