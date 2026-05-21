package ayd2.p2b.wallet_service_api.feature.wallet.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new wallet for a user")
public class CreateWalletRequest {

    @NotNull(message = "userId is required")
    @Schema(description = "User ID for which the wallet is created", required = true)
    private UUID userId;
}
