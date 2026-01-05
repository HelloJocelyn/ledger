package com.ledgerx.ledger.core.enums;

import java.util.Arrays;

public enum AccountType {

    BANK("BANK"),
    WALLET("WALLET"),
    CARD("CARD"),
    CASH("CASH"),
    BROKERAGE("BROKERAGE"),
    OTHER("OTHER");

    private final String code;

    AccountType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static AccountType fromCode(String code) {
        return Arrays.stream(values())
                .filter(t -> t.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Unknown AccountType: " + code)
                );
    }
}
