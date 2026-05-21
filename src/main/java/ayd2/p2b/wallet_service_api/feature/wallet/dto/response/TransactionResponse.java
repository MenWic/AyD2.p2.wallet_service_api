package ayd2.p2b.wallet_service_api.feature.wallet.dto.response;

import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Transaction history entry")
public class TransactionResponse {

    @Schema(description = "Transaction ID")
    private UUID id;

    @Schema(description = "Transaction type: TOP_UP or PAYMENT")
    private TransactionType type;

    @Schema(description = "Amount (positive for TOP_UP, negative for PAYMENT)")
    private BigDecimal amount;

    @Schema(description = "User-supplied transaction date")
    private LocalDate transactionDate;

    @Schema(description = "Reference payment ID (set for PAYMENT type)")
    private UUID referencePaymentId;

    @Schema(description = "Timestamp when the transaction was recorded")
    private Instant createdAt;
}
