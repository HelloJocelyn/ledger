package joce.practice.market.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "market.twelvedata")
@Getter
@Setter
public class TwelveDataProperties {

    private String baseUrl;

    private String apiKey;

    private int outputSize = 1000;
}