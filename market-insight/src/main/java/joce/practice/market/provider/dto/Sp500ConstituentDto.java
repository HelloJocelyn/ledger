package joce.practice.market.provider.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Sp500ConstituentDto {

    private String symbol;

    private String securityName;

    private String gicsSector;

    private String gicsSubIndustry;
}