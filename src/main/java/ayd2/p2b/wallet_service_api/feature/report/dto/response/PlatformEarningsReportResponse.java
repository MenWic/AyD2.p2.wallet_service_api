package ayd2.p2b.wallet_service_api.feature.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Internal Wallet platform earnings report grouped by institution with nested congress aggregates from immutable payments.")
public class PlatformEarningsReportResponse {

    @Schema(description = "Institution-level aggregate items")
    private List<InstitutionEarningsItem> items;
    @Schema(description = "Number of institution items", example = "1")
    private long totalItems;
    @Schema(description = "Grand total amount across institutions (SUM of immutable payment.amount)", example = "1000.00")
    private BigDecimal grandTotalAmount;
    @Schema(description = "Grand total commission across institutions (SUM of immutable payment.commissionAmount; not recomputed from current SystemConfig)", example = "100.00")
    private BigDecimal grandTotalCommission;
    @Schema(description = "Grand total net across institutions (SUM of immutable payment.netAmount)", example = "900.00")
    private BigDecimal grandTotalNet;
}
