package joce.practice.market.provider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TwelveDataStockDto {

    private String symbol;

    private String name;

    private String currency;

    private String exchange;

    @JsonProperty("mic_code")
    private String micCode;

    private String country;

    private String type;
}
