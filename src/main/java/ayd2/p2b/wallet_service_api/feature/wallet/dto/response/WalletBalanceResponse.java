package ayd2.p2b.wallet_service_api.feature.wallet.dto.response;

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
@Schema(description = "Wallet balance response")
public class WalletBalanceResponse {

    @Schema(description = "User ID that owns the wallet")
    private UUID userId;

    @Schema(description = "Current wallet balance")
    private BigDecimal balance;
}
