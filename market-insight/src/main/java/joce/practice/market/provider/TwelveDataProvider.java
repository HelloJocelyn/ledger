package joce.practice.market.provider;

import joce.practice.market.config.TwelveDataProperties;
import joce.practice.market.provider.dto.DailyPrice;
import joce.practice.market.provider.dto.TwelveDataTimeSeriesResponse;
import joce.practice.market.provider.dto.TwelveDataValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TwelveDataProvider implements MarketDataProvider {

    private static final String SOURCE = "TWELVE_DATA";

    private final TwelveDataProperties properties;
    private final RestClient.Builder restClientBuilder;


    @Override
    public List<DailyPrice> fetchDailyPrices(
            String symbol,
            LocalDate from,
            LocalDate to
    ) {
        RestClient restClient = restClientBuilder
                .baseUrl(properties.getBaseUrl())
                .build();
//        RestClient.RequestHeadersSpec<?> uri = restClient.get()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/time_series")
//                        .queryParam("symbol", symbol)
//                        .queryParam("interval", "1day")
//                        .queryParam("start_date", from)
//                        .queryParam("end_date", to)
//                        .queryParam("outputsize", properties.getOutputSize())
//                        .queryParam("apikey", properties.getApiKey())
//                        .build());

        TwelveDataTimeSeriesResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/time_series")
                        .queryParam("symbol", symbol)
                        .queryParam("interval", "1day")
                        .queryParam("start_date", from)
                        .queryParam("end_date", to)
                        .queryParam("outputsize", properties.getOutputSize())
                        .queryParam("apikey", properties.getApiKey())
                        .build())
                .retrieve()
                .body(TwelveDataTimeSeriesResponse.class);

        if (response == null) {
            throw new IllegalStateException("Twelve Data response is null");
        }

        if (response.getStatus() != null && !"ok".equalsIgnoreCase(response.getStatus())) {
            throw new IllegalStateException(
                    "Twelve Data API error: " + response.getMessage()
            );
        }

        if (response.getValues() == null || response.getValues().isEmpty()) {
            return List.of();
        }

        return response.getValues().stream()
                .map(value -> toDailyPriceDto(symbol, value))
                .sorted(Comparator.comparing(DailyPrice::tradeDate))
                .toList();
    }

    private DailyPrice toDailyPriceDto(String symbol, TwelveDataValue value) {
        return new DailyPrice(
                symbol,
                LocalDate.parse(value.getDatetime()),
                toBigDecimal(value.getOpen()),
                toBigDecimal(value.getHigh()),
                toBigDecimal(value.getLow()),
                toBigDecimal(value.getClose()),
                toLong(value.getVolume())
        );
    }

    private BigDecimal toBigDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return new BigDecimal(value);
    }

    private Long toLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return Long.valueOf(value);
    }


    @Override
    public String getSource() {
        return "TwelveData";
    }
}