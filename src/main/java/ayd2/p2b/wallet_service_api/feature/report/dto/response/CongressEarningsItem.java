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
@Schema(description = "Congress-level earnings aggregate from immutable payment records.")
public class CongressEarningsItem {

    @Schema(description = "Congress ID", example = "00000000-0000-0000-0000-000000000010")
    private UUID congressId;
    @Schema(description = "Congress name snapshot", example = "Congreso Nacional de Ingenieria")
    private String congressName;
    @Schema(description = "Sum of payment.amount for this congress", example = "1000.00")
    private BigDecimal totalAmount;
    @Schema(description = "Sum of payment.commissionAmount for this congress", example = "100.00")
    private BigDecimal commissionAmount;
    @Schema(description = "Sum of payment.netAmount for this congress", example = "900.00")
    private BigDecimal netAmount;
    @Schema(description = "Count of payments for this congress", example = "4")
    private long paymentCount;
}
