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

    @Schema(description = "Commission percentage applied to enrollment payments", example = "10.00")
    private BigDecimal commissionPercent;

    @Schema(description = "UUID of the admin who last updated this configuration")
    private UUID updatedBy;

    @Schema(description = "Timestamp of the last update (ISO 8601 UTC)")
    private Instant updatedAt;
}
