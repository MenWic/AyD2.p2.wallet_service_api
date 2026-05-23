package ayd2.p2b.wallet_service_api.unit.feature.payment.list;

import ayd2.p2b.wallet_service_api.common.response.PageResponse;
import ayd2.p2b.wallet_service_api.feature.payment.application.list.ListPaymentsUseCase;
import ayd2.p2b.wallet_service_api.feature.payment.application.port.PaymentRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.payment.domain.model.PaymentData;
import ayd2.p2b.wallet_service_api.feature.payment.dto.internal.PaymentSearchCriteria;
import ayd2.p2b.wallet_service_api.feature.payment.dto.response.PaymentResponse;
import ayd2.p2b.wallet_service_api.feature.payment.mapper.PaymentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ListPaymentsUseCaseTest {

    @Mock
    private PaymentRepositoryPort paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    private ListPaymentsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListPaymentsUseCase(paymentRepository, paymentMapper);
    }

    @Test
    void should_return_paged_payments_when_called_with_criteria() {
        PaymentSearchCriteria criteria = PaymentSearchCriteria.builder()
                .page(0)
                .size(20)
                .build();

        PaymentData paymentData = PaymentData.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .commissionAmount(new BigDecimal("10.00"))
                .netAmount(new BigDecimal("90.00"))
                .build();

        PageResponse<PaymentData> dataPage = PageResponse.<PaymentData>builder()
                .items(List.of(paymentData))
                .page(0)
                .size(20)
                .totalItems(1)
                .totalPages(1)
                .build();

        PaymentResponse paymentResponse = PaymentResponse.builder()
                .id(paymentData.getId())
                .amount(new BigDecimal("100.00"))
                .build();

        given(paymentRepository.findAll(criteria)).willReturn(dataPage);
        given(paymentMapper.toResponse(paymentData)).willReturn(paymentResponse);

        PageResponse<PaymentResponse> result = useCase.execute(criteria);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getTotalItems()).isEqualTo(1);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(20);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }
}
