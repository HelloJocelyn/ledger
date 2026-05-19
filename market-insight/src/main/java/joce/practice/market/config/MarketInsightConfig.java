package joce.practice.market.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TwelveDataProperties.class)
public class MarketInsightConfig {
}