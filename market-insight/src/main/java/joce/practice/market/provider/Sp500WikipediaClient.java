package joce.practice.market.provider;

import joce.practice.market.provider.dto.Sp500ConstituentDto;
import lombok.extern.slf4j.Slf4j;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class Sp500WikipediaClient {

    private static final String URL =
        "https://en.wikipedia.org/wiki/List_of_S%26P_500_companies";

    public List<Sp500ConstituentDto> fetchCurrentConstituents() {
        try {
            Document doc = Jsoup.connect(URL)
                .userAgent("Mozilla/5.0")
                .timeout(15_000)
                .get();

            Element table = doc.selectFirst("table#constituents");

            if (table == null) {
                throw new IllegalStateException("S&P500 constituents table not found");
            }

            Elements rows = table.select("tbody tr");

            List<Sp500ConstituentDto> result = new ArrayList<>();

            for (int i = 1; i < rows.size(); i++) {
                Elements cols = rows.get(i).select("td");

                if (cols.size() < 4) {
                    continue;
                }

                String symbol = clean(cols.get(0).text());
                String securityName = clean(cols.get(1).text());
                String gicsSector = clean(cols.get(2).text());
                String gicsSubIndustry = clean(cols.get(3).text());

                result.add(Sp500ConstituentDto.builder()
                    .symbol(symbol)
                    .securityName(securityName)
                    .gicsSector(gicsSector)
                    .gicsSubIndustry(gicsSubIndustry)
                    .build());
            }

            return result;

        } catch (Exception e) {
            log.error("Failed to fetch S&P500 constituents from Wikipedia", e);
            throw new IllegalStateException("Failed to fetch S&P500 constituents", e);
        }
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }
}