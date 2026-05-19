package joce.practice.market.provider;

import joce.practice.market.config.FinnhubProperties;
import joce.practice.market.provider.dto.DailyPrice;
import joce.practice.market.provider.dto.FinnhubCandleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
//@Component
public class FinnhubMarketDataProvider implements MarketDataProvider {

    private final RestClient.Builder restClientBuilder;
    private final FinnhubProperties properties;


    @Override
    public List<DailyPrice> fetchDailyPrices(String symbol, LocalDate fromDate, LocalDate toDate) {
        long from = fromDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        long to = toDate.plusDays(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC);

        RestClient restClient = restClientBuilder
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("X-Finnhub-Token", properties.getApiKey())
                .build();

        FinnhubCandleResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stock/candle")
                        .queryParam("symbol", normalizeSymbol(symbol))
                        .queryParam("resolution", "D")
                        .queryParam("from", from)
                        .queryParam("to", to)
                        .build())
                .retrieve()
                .body(FinnhubCandleResponse.class);

        if (response == null || response.s() == null || !"ok".equalsIgnoreCase(response.s())) {
            return List.of();
        }

        validateResponse(response);

        List<DailyPrice> prices = new ArrayList<>();

        for (int i = 0; i < response.t().size(); i++) {
            LocalDate tradeDate = Instant.ofEpochSecond(response.t().get(i))
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate();

            prices.add(new DailyPrice(
                    normalizeSymbol(symbol),
                    tradeDate,
                    response.o().get(i),
                    response.h().get(i),
                    response.l().get(i),
                    response.c().get(i),
                    response.v().get(i)
            ));
        }

        return prices;
    }

    private String normalizeSymbol(String symbol) {
        return symbol.toUpperCase();
    }

    private void validateResponse(FinnhubCandleResponse response) {
        int size = response.t().size();

        if (response.o().size() != size
                || response.h().size() != size
                || response.l().size() != size
                || response.c().size() != size
                || response.v().size() != size) {
            throw new IllegalStateException("Invalid Finnhub candle response: array sizes are inconsistent");
        }
    }

    @Override
    public String getSource() {
        return "Finnhub";
    }
}