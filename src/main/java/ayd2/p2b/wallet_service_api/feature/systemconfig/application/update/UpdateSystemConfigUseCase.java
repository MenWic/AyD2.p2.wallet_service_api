package ayd2.p2b.wallet_service_api.feature.systemconfig.application.update;

import ayd2.p2b.wallet_service_api.feature.systemconfig.SystemConfigRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.systemconfig.dto.response.SystemConfigResponse;
import ayd2.p2b.wallet_service_api.feature.systemconfig.mapper.SystemConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateSystemConfigUseCase {

    private final SystemConfigRepositoryPort repositoryPort;
    private final SystemConfigMapper mapper;

    @Transactional
    public SystemConfigResponse execute(BigDecimal commissionPercent, UUID updatedBy) {
        return mapper.toResponse(repositoryPort.save(commissionPercent, updatedBy));
    }
}
