package ayd2.p2b.wallet_service_api.unit.feature.systemconfig.get;

import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.feature.systemconfig.dto.internal.SystemConfigData;
import ayd2.p2b.wallet_service_api.feature.systemconfig.application.port.SystemConfigRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.systemconfig.application.get.GetSystemConfigUseCase;
import ayd2.p2b.wallet_service_api.feature.systemconfig.dto.response.SystemConfigResponse;
import ayd2.p2b.wallet_service_api.feature.systemconfig.mapper.SystemConfigMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GetSystemConfigUseCaseTest {

    @Mock
    private SystemConfigRepositoryPort repositoryPort;

    @Mock
    private SystemConfigMapper mapper;

    private GetSystemConfigUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetSystemConfigUseCase(repositoryPort, mapper);
    }

    @Test
    void should_return_current_commission_percent() {
        SystemConfigData data = SystemConfigData.builder()
                .commissionPercent(new BigDecimal("10.00"))
                .updatedBy(UUID.randomUUID())
                .updatedAt(Instant.now())
                .build();
        SystemConfigResponse expected = SystemConfigResponse.builder()
                .commissionPercent(new BigDecimal("10.00"))
                .build();
        given(repositoryPort.find()).willReturn(data);
        given(mapper.toResponse(data)).willReturn(expected);

        SystemConfigResponse result = useCase.execute();

        assertThat(result.getCommissionPercent()).isEqualByComparingTo("10.00");
        then(repositoryPort).should().find();
        then(mapper).should().toResponse(data);
    }

    @Test
    void should_propagate_exception_when_config_not_found() {
        given(repositoryPort.find()).willThrow(
                new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "system.internal_error", "Config not found")
        );

        assertThrows(ApiException.class, () -> useCase.execute());
    }
}
