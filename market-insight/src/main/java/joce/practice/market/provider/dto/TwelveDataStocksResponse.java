package joce.practice.market.provider.dto;

import lombok.Data;

import java.util.List;

@Data
public class TwelveDataStocksResponse {
    private List<TwelveDataStockDto> data;
}

