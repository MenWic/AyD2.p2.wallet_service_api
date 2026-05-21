package ayd2.p2b.wallet_service_api.feature.wallet.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to top up a wallet balance")
public class TopUpRequest {

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    @Schema(description = "Amount to add to the wallet (must be positive)", required = true)
    private BigDecimal amount;

    @NotNull(message = "transactionDate is required")
    @Schema(description = "User-supplied date for the transaction", required = true)
    private LocalDate transactionDate;
}
