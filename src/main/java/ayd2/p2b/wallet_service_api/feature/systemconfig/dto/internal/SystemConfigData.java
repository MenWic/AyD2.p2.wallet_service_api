package ayd2.p2b.wallet_service_api.feature.systemconfig.dto.internal;

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
public class SystemConfigData {
    private BigDecimal commissionPercent;
    private UUID updatedBy;
    private Instant updatedAt;
}
