package com.ledgerx.app;

import com.ledgerx.ledger.config.LedgerModuleConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        LedgerModuleConfiguration.class,
})
public class AppModuleConfiguration {
}
