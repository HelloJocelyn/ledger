package com.ledgerx.ledger.api.dto;


import com.ledgerx.ledger.core.enums.AccountType;
import com.ledgerx.ledger.core.enums.Provider;

import java.util.List;
import java.util.Map;

public final class LedgerMeta {

    private LedgerMeta() {
    }

    public static final List<AccountTypeMeta> ACCOUNT_TYPES = List.of(
            new AccountTypeMeta(AccountType.BANK.getCode(), "银行账户", "building-2", 10, "JPY"),
            new AccountTypeMeta(AccountType.WALLET.getCode(), "电子钱包", "wallet", 20, "JPY"),
            new AccountTypeMeta(AccountType.CARD.getCode(), "银行卡 / 信用卡", "credit-card", 30, "JPY"),
            new AccountTypeMeta(AccountType.CASH.getCode(), "现金", "banknote", 40, "JPY"),
            new AccountTypeMeta(AccountType.BROKERAGE.getCode(), "投资账户", "line-chart", 50, "JPY"),
            new AccountTypeMeta(AccountType.OTHER.getCode(), "其他", "square", 999, "JPY")
    );

    public static final Map<String, List<String>> PROVIDER_ALLOWED_ACCOUNT_TYPES = Map.of(
            Provider.PAYPAY.getCode(),
            List.of(AccountType.WALLET.getCode()),

            Provider.SEVEN_BANK.getCode(),
            List.of(AccountType.BANK.getCode()),

            Provider.RAKUTEN_BANK.getCode(),
            List.of(AccountType.BANK.getCode()),

            Provider.MIZUHO.getCode(),
            List.of(AccountType.BANK.getCode()),

            Provider.MUFG.getCode(),
            List.of(
                    AccountType.BANK.getCode(),
                    AccountType.CARD.getCode()
            ),

            Provider.OTHER.getCode(),
            List.of(
                    AccountType.BANK.getCode(),
                    AccountType.WALLET.getCode(),
                    AccountType.CARD.getCode(),
                    AccountType.CASH.getCode(),
                    AccountType.BROKERAGE.getCode(),
                    AccountType.OTHER.getCode()
            )
    );
}
