package ayd2.p2b.wallet_service_api.feature.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Wallet internal aggregate row grouped by congress and institution from immutable payments.")
public class EarningsByCongressItem {

    @Schema(description = "Congress ID", example = "00000000-0000-0000-0000-000000000010")
    private UUID congressId;
    @Schema(description = "Congress name snapshot from payment records", example = "Congreso Nacional de Ingenieria")
    private String congressName;
    @Schema(description = "Institution ID", example = "00000000-0000-0000-0000-000000000020")
    private UUID institutionId;
    @Schema(description = "Institution name snapshot from payment records", example = "Universidad Nacional")
    private String institutionName;
    @Schema(description = "Sum of payment.amount in the group", example = "1000.00")
    private BigDecimal totalAmount;
    @Schema(description = "Sum of payment.commissionAmount in the group (immutable historical values)", example = "100.00")
    private BigDecimal commissionAmount;
    @Schema(description = "Sum of payment.netAmount in the group", example = "900.00")
    private BigDecimal netAmount;
    @Schema(description = "Count of payments in the group", example = "4")
    private long paymentCount;
}
