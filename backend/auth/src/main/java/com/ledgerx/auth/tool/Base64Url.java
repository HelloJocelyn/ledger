package com.ledgerx.auth.tool;

import java.util.Base64;

public final class Base64Url {

    public static byte[] decode(String base64url) {
        return Base64.getUrlDecoder().decode(base64url);
    }

    public static String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
