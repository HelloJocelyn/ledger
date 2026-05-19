package joce.practice.market.provider;

import joce.practice.market.provider.dto.DailyPrice;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.client.RestClient;

import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

//@Component
public class StooqMarketDataProvider implements MarketDataProvider {

    private static final String STOOQ_DAILY_URL =
            "https://stooq.com/q/d/l/?s={symbol}&i=d";

    private final RestClient restClient;

    public StooqMarketDataProvider(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Override
    public List<DailyPrice> fetchDailyPrices(String symbol,LocalDate from,
                                             LocalDate to) {
        String normalizedSymbol = symbol.toLowerCase();

        String csv = restClient.get()
                .uri(STOOQ_DAILY_URL, normalizedSymbol)
                .retrieve()
                .body(String.class);

        if (csv == null || csv.isBlank()) {
            return List.of();
        }

        return parseCsv(normalizedSymbol, csv);
    }

    private List<DailyPrice> parseCsv(String symbol, String csv) {
        List<DailyPrice> prices = new ArrayList<>();

        try (
                StringReader reader = new StringReader(csv);
                CSVParser parser = CSVFormat.DEFAULT
                        .builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .build()
                        .parse(reader)
        ) {
            for (CSVRecord record : parser) {
                // Stooq invalid symbol sometimes returns:
                // No data
                if (!record.isMapped("Date") || record.get("Date").equalsIgnoreCase("No data")) {
                    continue;
                }

                DailyPrice price = new DailyPrice(
                        symbol,
                        LocalDate.parse(record.get("Date")),
                        new BigDecimal(record.get("Open")),
                        new BigDecimal(record.get("High")),
                        new BigDecimal(record.get("Low")),
                        new BigDecimal(record.get("Close")),
                        parseVolume(record.get("Volume"))
                );

                prices.add(price);
            }

            return prices;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse Stooq daily prices for symbol: " + symbol, e);
        }
    }

    private Long parseVolume(String value) {
        if (value == null || value.isBlank() || value.equals("-")) {
            return null;
        }
        return Long.parseLong(value);
    }

    @Override
    public String getSource() {
        return "Stooq";
    }
}