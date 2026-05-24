package ayd2.p2b.wallet_service_api.feature.systemconfig.infrastructure.persistence.adapter;

import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.feature.systemconfig.dto.internal.SystemConfigData;
import ayd2.p2b.wallet_service_api.feature.systemconfig.application.port.SystemConfigRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.systemconfig.infrastructure.persistence.entity.SystemConfigEntity;
import ayd2.p2b.wallet_service_api.feature.systemconfig.infrastructure.persistence.repository.SystemConfigJpaRepository;
import ayd2.p2b.wallet_service_api.feature.systemconfig.mapper.SystemConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SystemConfigRepositoryAdapter implements SystemConfigRepositoryPort {

    private static final int SINGLETON_ID = 1;

    private final SystemConfigJpaRepository jpaRepository;
    private final SystemConfigMapper mapper;

    @Override
    public SystemConfigData find() {
        return jpaRepository.findById(SINGLETON_ID)
                .map(mapper::toData)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "system.internal_error",
                        "System configuration not found"
                ));
    }

    @Override
    public SystemConfigData save(BigDecimal commissionPercent, UUID updatedBy) {
        SystemConfigEntity entity = jpaRepository.findById(SINGLETON_ID)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "system.internal_error",
                        "System configuration not found"
                ));
        entity.setCommissionPercent(commissionPercent);
        entity.setUpdatedBy(updatedBy);
        entity.setUpdatedAt(Instant.now());
        return mapper.toData(jpaRepository.save(entity));
    }
}
