package com.ledgerx.app;

import com.ledgerx.auth.config.AuthProps;
import com.ledgerx.auth.config.WebAuthnProps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(
        scanBasePackages = {
                "com.ledgerx.ledger",
                "com.ledgerx.auth"
        }
)
@EnableConfigurationProperties({AuthProps.class, WebAuthnProps.class})
public class LedgerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LedgerApplication.class, args);
    }
}
