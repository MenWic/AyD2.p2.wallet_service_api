package ayd2.p2b.wallet_service_api.feature.systemconfig.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "System configuration data")
public class SystemConfigResponse {

    @Schema(description = "Current commission percentage used for new payment registrations", example = "10.00")
    private BigDecimal commissionPercent;

    @Schema(description = "UUID of the admin who last updated this configuration", example = "00000000-0000-0000-0000-000000000999")
    private UUID updatedBy;

    @Schema(description = "Timestamp of the last update (ISO 8601 UTC)", example = "2026-05-20T13:00:00Z")
    private Instant updatedAt;
}
