package ayd2.p2b.wallet_service_api.feature.systemconfig.mapper;

import ayd2.p2b.wallet_service_api.feature.systemconfig.dto.internal.SystemConfigData;
import ayd2.p2b.wallet_service_api.feature.systemconfig.dto.response.SystemConfigResponse;
import ayd2.p2b.wallet_service_api.feature.systemconfig.infrastructure.persistence.entity.SystemConfigEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SystemConfigMapper {
    SystemConfigResponse toResponse(SystemConfigData data);
    SystemConfigData toData(SystemConfigEntity entity);
}
