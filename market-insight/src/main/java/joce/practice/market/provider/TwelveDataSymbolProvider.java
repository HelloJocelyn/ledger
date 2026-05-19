package joce.practice.market.provider;

import joce.practice.market.config.TwelveDataProperties;
import joce.practice.market.provider.dto.TwelveDataStocksResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class TwelveDataSymbolProvider {

    private final RestClient.Builder restClientBuilder;

    private final TwelveDataProperties properties;

    public TwelveDataStocksResponse fetchStocksByExchange(String exchange) {
        RestClient restClient = restClientBuilder
                .baseUrl(properties.getBaseUrl())
                .build();
        return restClient.get()
            .uri(uriBuilder -> uriBuilder
                .scheme("https")
                .host("api.twelvedata.com")
                .path("/stocks")
                .queryParam("exchange", exchange)
                .queryParam("apikey", properties.getApiKey())
                .build())
            .retrieve()
            .body(TwelveDataStocksResponse.class);
    }
}