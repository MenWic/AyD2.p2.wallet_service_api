package ayd2.p2b.wallet_service_api.feature.report.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformEarningsReportResponse {

    private List<InstitutionEarningsItem> items;
    private long totalItems;
    private BigDecimal grandTotalAmount;
    private BigDecimal grandTotalCommission;
    private BigDecimal grandTotalNet;
}

