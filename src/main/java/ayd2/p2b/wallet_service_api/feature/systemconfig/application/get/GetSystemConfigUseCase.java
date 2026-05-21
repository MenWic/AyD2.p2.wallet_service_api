package ayd2.p2b.wallet_service_api.feature.systemconfig.application.get;

import ayd2.p2b.wallet_service_api.feature.systemconfig.SystemConfigRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.systemconfig.dto.response.SystemConfigResponse;
import ayd2.p2b.wallet_service_api.feature.systemconfig.mapper.SystemConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetSystemConfigUseCase {

    private final SystemConfigRepositoryPort repositoryPort;
    private final SystemConfigMapper mapper;

    @Transactional(readOnly = true)
    public SystemConfigResponse execute() {
        return mapper.toResponse(repositoryPort.find());
    }
}
