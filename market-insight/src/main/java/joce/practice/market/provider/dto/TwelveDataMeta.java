package joce.practice.market.provider.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TwelveDataMeta {

    private String symbol;

    private String interval;

    private String currency;

    private String exchange_timezone;

    private String exchange;
}