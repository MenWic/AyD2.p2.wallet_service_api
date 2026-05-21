package ayd2.p2b.wallet_service_api.feature.wallet.dto.request;

import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filter parameters for transaction history")
public class TransactionFilterRequest {

    @Schema(description = "Filter by transaction type (TOP_UP or PAYMENT)")
    private TransactionType transactionType;

    @Schema(description = "Filter transactions from this date (inclusive)")
    private LocalDate dateFrom;

    @Schema(description = "Filter transactions up to this date (inclusive)")
    private LocalDate dateTo;

    @Builder.Default
    @Schema(description = "Page number (0-based)", defaultValue = "0")
    private int page = 0;

    @Builder.Default
    @Schema(description = "Page size", defaultValue = "20")
    private int size = 20;
}
