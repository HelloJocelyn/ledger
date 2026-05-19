package joce.practice.market.provider;

import joce.practice.market.config.AlphaVantageProperties;
import joce.practice.market.provider.dto.DailyPrice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

//@Component("alphaVantageMarketDataProvider")
@RequiredArgsConstructor
public class AlphaVantageMarketDataProvider implements MarketDataProvider {

    private final RestClient.Builder restClientBuilder;
    private final AlphaVantageProperties properties;

    @Override
    public List<DailyPrice> fetchDailyPrices(String symbol,LocalDate from,
                                             LocalDate to) {
        RestClient restClient = restClientBuilder
                .baseUrl(properties.getBaseUrl())
                .build();

        AlphaVantageDailyResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/query")
                        .queryParam("function", "TIME_SERIES_DAILY")
                        .queryParam("symbol", normalizeSymbol(symbol))
                        .queryParam("outputsize", "compact")
                        .queryParam("apikey", properties.getApiKey())
                        .build())
                .retrieve()
                .body(AlphaVantageDailyResponse.class);

        if (response == null || response.timeSeriesDaily() == null) {
            return List.of();
        }

        List<DailyPrice> prices = new ArrayList<>();

        response.timeSeriesDaily().forEach((dateText, value) -> {
            prices.add(new DailyPrice(
                    normalizeSymbol(symbol),
                    LocalDate.parse(dateText),
                    value.open(),
                    value.high(),
                    value.low(),
                    value.close(),
                    parseVolume(value.volume())
            ));
        });

        prices.sort(Comparator.comparing(DailyPrice::tradeDate));
        return prices;
    }

    @Override
    public String getSource() {
        return "AlphaVantage";
    }

    private String normalizeSymbol(String symbol) {
        return symbol.toUpperCase();
    }

    private Long parseVolume(String volume) {
        if (volume == null || volume.isBlank()) {
            return null;
        }
        return Long.parseLong(volume);
    }

    private record AlphaVantageDailyResponse(
            @com.fasterxml.jackson.annotation.JsonProperty("Time Series (Daily)")
            Map<String, AlphaVantageDailyValue> timeSeriesDaily
    ) {
    }

    private record AlphaVantageDailyValue(
            @com.fasterxml.jackson.annotation.JsonProperty("1. open")
            BigDecimal open,

            @com.fasterxml.jackson.annotation.JsonProperty("2. high")
            BigDecimal high,

            @com.fasterxml.jackson.annotation.JsonProperty("3. low")
            BigDecimal low,

            @com.fasterxml.jackson.annotation.JsonProperty("4. close")
            BigDecimal close,

            @com.fasterxml.jackson.annotation.JsonProperty("6. volume")
            String volume
    ) {
    }
}