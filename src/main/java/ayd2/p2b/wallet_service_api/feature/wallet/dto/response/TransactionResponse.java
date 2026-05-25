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

    @Schema(description = "Transaction ID", example = "11111111-1111-1111-1111-111111111111")
    private UUID id;

    @Schema(description = "ID of the wallet owner", example = "00000000-0000-0000-0000-000000000001")
    private UUID walletUserId;

    @Schema(description = "Transaction type", example = "TOP_UP")
    private TransactionType type;

    @Schema(description = "Amount (positive for TOP_UP, negative for PAYMENT)", example = "150.00")
    private BigDecimal amount;

    @Schema(description = "User-supplied transaction date", example = "2026-05-20")
    private LocalDate transactionDate;

    @Schema(description = "Reference payment ID (present for PAYMENT transactions)", example = "22222222-2222-2222-2222-222222222222")
    private UUID referencePaymentId;

    @Schema(description = "Timestamp when the transaction was recorded", example = "2026-05-20T13:10:00Z")
    private Instant createdAt;
}
