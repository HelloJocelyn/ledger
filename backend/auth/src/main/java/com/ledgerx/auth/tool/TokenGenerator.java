package com.ledgerx.auth.tool;

import java.security.SecureRandom;
import java.util.Base64;

public final class TokenGenerator {
    private static final SecureRandom RND = new SecureRandom();

    private TokenGenerator() {
    }

    public static String newSignupToken() {
        byte[] bytes = new byte[32]; // 256-bit
        RND.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
