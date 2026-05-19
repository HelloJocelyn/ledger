package joce.practice.market.provider.dto;

import java.math.BigDecimal;
import java.util.List;

public record FinnhubCandleResponse(
        List<BigDecimal> c, // close
        List<BigDecimal> h, // high
        List<BigDecimal> l, // low
        List<BigDecimal> o, // open
        List<Long> t,       // timestamp
        List<Long> v,       // volume
        String s            // status: ok / no_data
) {
}