package ayd2.p2b.wallet_service_api.feature.systemconfig.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body to update the platform commission configuration")
public class UpdateSystemConfigRequest {

    @NotNull(message = "Commission percent is required")
    @DecimalMin(value = "0.00", message = "Commission percent must be >= 0")
    @DecimalMax(value = "100.00", message = "Commission percent must be <= 100")
    @Schema(description = "New commission percentage to apply to enrollment payments", example = "10.00")
    private BigDecimal commissionPercent;
}
