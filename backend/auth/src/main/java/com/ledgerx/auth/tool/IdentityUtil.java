package com.ledgerx.auth.tool;

import java.util.regex.Pattern;

public final class IdentityUtil {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    // 允许：+81 90-1234-5678 / 09012345678 / 81xxxxxxxxxx
    private static final Pattern PHONE_LAX = Pattern.compile("^\\+?\\d[\\d\\s-]{6,}$");

    private IdentityUtil() {
    }

    public static String normalize(IdentityType type, String identity) {
        if (type == IdentityType.EMAIL) {
            return normalizeEmail(identity);
        } else if (type == IdentityType.PHONE) {
            return normalizePhoneJapanLike(identity);
        } else throw new IllegalArgumentException("Invalid IdentityType: " + type);
    }

    public enum IdentityType {
        EMAIL,
        PHONE
    }

    public static IdentityType detectType(String raw) {
        String s = raw == null ? "" : raw.trim();
        if (EMAIL.matcher(s).matches()) return IdentityType.EMAIL;
        if (PHONE_LAX.matcher(s).matches()) return IdentityType.PHONE;
        throw new IllegalArgumentException("Invalid email/phone format");
    }

    public static String normalizeEmail(String raw) {
        return raw.trim().toLowerCase();
    }

    /**
     * 简化版 phone normalize： - 去空格/破折号 - 如果以 0 开头：按日本手机号规则转成 +81（仅示例） 生产建议：用 google libphonenumber 做真正的
     * E.164
     */
    public static String normalizePhoneJapanLike(String raw) {
        String s = raw.trim().replace(" ", "").replace("-", "");
        if (s.startsWith("+")) return s;
        if (s.startsWith("0")) {
            // 090xxxx -> +8190xxxx
            return "+81" + s.substring(1);
        }
        // 81xxxx -> +81xxxx
        if (s.startsWith("81")) return "+" + s;
        // fallback：当作国际号前面补 +
        return "+" + s;
    }

    public static String maskEmail(String email) {
        int at = email.indexOf("@");
        if (at <= 1) return "***" + email.substring(at);
        String name = email.substring(0, at);
        String domain = email.substring(at);
        return name.charAt(0) + "***" + name.charAt(name.length() - 1) + domain;
    }

    public static String maskPhone(String e164) {
        // +819012345678 -> +81 •••• 5678
        String digits = e164.replaceAll("\\D", "");
        if (digits.length() <= 4) return e164;
        String last4 = digits.substring(digits.length() - 4);
        String cc = digits.length() >= 2 ? digits.substring(0, 2) : digits;
        return "+" + cc + " •••• " + last4;
    }
}
