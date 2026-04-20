package com.ledgerx.auth.domain.model;

public record Identity(IdentityType type, String normalized, String masked) {}
