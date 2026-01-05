package com.ledgerx.app;

import com.ledgerx.auth.config.AuthModuleConfiguration;
import com.ledgerx.ledger.config.LedgerModuleConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        LedgerModuleConfiguration.class,
        AuthModuleConfiguration.class
})
public class AppModuleConfiguration {
}
