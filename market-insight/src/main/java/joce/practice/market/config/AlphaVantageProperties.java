package joce.practice.market.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "market.alpha-vantage")
public class AlphaVantageProperties {

    private String apiKey;
    private String baseUrl = "https://www.alphavantage.co";
}