package joce.practice.market.provider.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TwelveDataTimeSeriesResponse {

    private TwelveDataMeta meta;

    private List<TwelveDataValue> values;

    private String status;

    private String message;
}