package ayd2.p2b.wallet_service_api.feature.report.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionEarningsItem {

    private UUID institutionId;
    private String institutionName;
    private List<CongressEarningsItem> congresses;
    private BigDecimal institutionTotalAmount;
    private BigDecimal institutionTotalCommission;
    private BigDecimal institutionTotalNet;
    private long paymentCount;
}

