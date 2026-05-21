package ayd2.p2b.wallet_service_api.feature.systemconfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigData {
    private BigDecimal commissionPercent;
    private UUID updatedBy;
    private LocalDateTime updatedAt;
}
