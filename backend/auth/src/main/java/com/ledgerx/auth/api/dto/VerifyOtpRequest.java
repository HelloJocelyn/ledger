package com.ledgerx.auth.api.dto;

public record VerifyOtpRequest(String identityType, String identity, String code) {}
