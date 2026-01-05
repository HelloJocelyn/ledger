package com.ledgerx.ledger.api.controller;

import com.ledgerx.ledger.api.dto.LedgerMeta;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/meta/ledger")
public class LedgerMetaController {

    @GetMapping
    public Map<String, Object> getLedgerMeta() {
        return Map.of(
                "accountTypes", LedgerMeta.ACCOUNT_TYPES,
                "providerAllowedAccountTypes", LedgerMeta.PROVIDER_ALLOWED_ACCOUNT_TYPES
        );
    }
}
