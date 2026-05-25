package ayd2.p2b.wallet_service_api.feature.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Institution-level earnings aggregate with congress-level detail.")
public class InstitutionEarningsItem {

    @Schema(description = "Institution ID", example = "00000000-0000-0000-0000-000000000020")
    private UUID institutionId;
    @Schema(description = "Institution name snapshot", example = "Universidad Nacional")
    private String institutionName;
    @Schema(description = "Congress-level aggregates under this institution")
    private List<CongressEarningsItem> congresses;
    @Schema(description = "Institution total amount (sum of congress totalAmount)", example = "1000.00")
    private BigDecimal institutionTotalAmount;
    @Schema(description = "Institution total commission (sum of immutable commissionAmount values)", example = "100.00")
    private BigDecimal institutionTotalCommission;
    @Schema(description = "Institution total net (sum of congress netAmount)", example = "900.00")
    private BigDecimal institutionTotalNet;
    @Schema(description = "Total payments counted in this institution", example = "4")
    private long paymentCount;
}
