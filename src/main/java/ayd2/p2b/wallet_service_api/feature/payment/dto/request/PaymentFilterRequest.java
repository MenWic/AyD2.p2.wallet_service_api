package ayd2.p2b.wallet_service_api.feature.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filter parameters for listing payments")
public class PaymentFilterRequest {

    @Schema(description = "Filter by congress ID")
    private UUID congressId;

    @Schema(description = "Filter by institution ID")
    private UUID institutionId;

    @Schema(description = "Filter payments from this date (inclusive)")
    private LocalDate dateFrom;

    @Schema(description = "Filter payments up to this date (inclusive)")
    private LocalDate dateTo;

    @Builder.Default
    @Schema(description = "Zero-based page index", defaultValue = "0")
    private int page = 0;

    @Builder.Default
    @Schema(description = "Page size (max 100)", defaultValue = "20")
    private int size = 20;
}
