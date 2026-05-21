package ayd2.p2b.wallet_service_api.feature.systemconfig;

import java.math.BigDecimal;
import java.util.UUID;

public interface SystemConfigRepositoryPort {
    SystemConfigData find();
    SystemConfigData save(BigDecimal commissionPercent, UUID updatedBy);
}
