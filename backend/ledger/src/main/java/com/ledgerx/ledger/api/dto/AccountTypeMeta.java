package com.ledgerx.ledger.api.dto;

public class AccountTypeMeta {

    private final String code;
    private final String label;
    private final String icon;
    private final int sort;
    private final String defaultCurrency;

    public AccountTypeMeta(
            String code,
            String label,
            String icon,
            int sort,
            String defaultCurrency
    ) {
        this.code = code;
        this.label = label;
        this.icon = icon;
        this.sort = sort;
        this.defaultCurrency = defaultCurrency;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getIcon() {
        return icon;
    }

    public int getSort() {
        return sort;
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }
}
