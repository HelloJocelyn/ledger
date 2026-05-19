package joce.practice.market.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "market.finnhub")
public class FinnhubProperties {

    private String apiKey;

    private String baseUrl = "https://finnhub.io/api/v1";
}