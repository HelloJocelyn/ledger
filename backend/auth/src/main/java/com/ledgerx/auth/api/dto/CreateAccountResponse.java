package com.ledgerx.auth.api.dto;

public record CreateAccountResponse(
    boolean ok,
    String next, // "SIGNED_IN"
    String userUuid) {}
