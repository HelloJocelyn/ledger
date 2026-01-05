package com.ledgerx.ledger.core.enums;

import java.util.Arrays;

public enum Provider {

    PAYPAY("PAYPAY"),
    SEVEN_BANK("SEVEN_BANK"),
    RAKUTEN_BANK("RAKUTEN_BANK"),
    MIZUHO("MIZUHO"),
    MUFG("MUFG"),
    OTHER("OTHER");

    private final String code;

    Provider(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static Provider fromCode(String code) {
        return Arrays.stream(values())
                .filter(p -> p.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Unknown Provider: " + code)
                );
    }
}
