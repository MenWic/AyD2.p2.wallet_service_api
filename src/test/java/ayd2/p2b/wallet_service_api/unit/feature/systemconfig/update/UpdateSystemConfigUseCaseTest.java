package ayd2.p2b.wallet_service_api.unit.feature.systemconfig.update;

import ayd2.p2b.wallet_service_api.feature.systemconfig.SystemConfigData;
import ayd2.p2b.wallet_service_api.feature.systemconfig.SystemConfigRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.systemconfig.application.update.UpdateSystemConfigUseCase;
import ayd2.p2b.wallet_service_api.feature.systemconfig.dto.response.SystemConfigResponse;
import ayd2.p2b.wallet_service_api.feature.systemconfig.mapper.SystemConfigMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class UpdateSystemConfigUseCaseTest {

    @Mock
    private SystemConfigRepositoryPort repositoryPort;

    @Mock
    private SystemConfigMapper mapper;

    private UpdateSystemConfigUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateSystemConfigUseCase(repositoryPort, mapper);
    }

    @Test
    void should_save_and_return_updated_commission_percent() {
        UUID adminId = UUID.randomUUID();
        BigDecimal newPercent = new BigDecimal("15.00");
        SystemConfigData saved = SystemConfigData.builder()
                .commissionPercent(newPercent)
                .updatedBy(adminId)
                .updatedAt(LocalDateTime.now())
                .build();
        SystemConfigResponse expected = SystemConfigResponse.builder()
                .commissionPercent(newPercent)
                .updatedBy(adminId)
                .build();
        given(repositoryPort.save(newPercent, adminId)).willReturn(saved);
        given(mapper.toResponse(saved)).willReturn(expected);

        SystemConfigResponse result = useCase.execute(newPercent, adminId);

        assertThat(result.getCommissionPercent()).isEqualByComparingTo("15.00");
        assertThat(result.getUpdatedBy()).isEqualTo(adminId);
        then(repositoryPort).should().save(newPercent, adminId);
    }

    @Test
    void should_delegate_to_repository_with_correct_arguments() {
        UUID adminId = UUID.randomUUID();
        BigDecimal percent = new BigDecimal("0.00");
        SystemConfigData data = SystemConfigData.builder()
                .commissionPercent(percent)
                .updatedBy(adminId)
                .updatedAt(LocalDateTime.now())
                .build();
        SystemConfigResponse response = SystemConfigResponse.builder()
                .commissionPercent(percent)
                .build();
        given(repositoryPort.save(percent, adminId)).willReturn(data);
        given(mapper.toResponse(data)).willReturn(response);

        useCase.execute(percent, adminId);

        then(repositoryPort).should().save(percent, adminId);
    }

    @Test
    void should_accept_maximum_commission_of_100() {
        UUID adminId = UUID.randomUUID();
        BigDecimal maxPercent = new BigDecimal("100.00");
        SystemConfigData data = SystemConfigData.builder()
                .commissionPercent(maxPercent)
                .updatedBy(adminId)
                .updatedAt(LocalDateTime.now())
                .build();
        SystemConfigResponse response = SystemConfigResponse.builder()
                .commissionPercent(maxPercent)
                .build();
        given(repositoryPort.save(maxPercent, adminId)).willReturn(data);
        given(mapper.toResponse(data)).willReturn(response);

        SystemConfigResponse result = useCase.execute(maxPercent, adminId);

        assertThat(result.getCommissionPercent()).isEqualByComparingTo("100.00");
    }
}
