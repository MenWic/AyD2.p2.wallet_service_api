package ayd2.p2b.wallet_service_api.unit.feature.payment.get;

import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.feature.payment.PaymentRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.payment.application.get.GetPaymentUseCase;
import ayd2.p2b.wallet_service_api.feature.payment.domain.model.PaymentData;
import ayd2.p2b.wallet_service_api.feature.payment.dto.response.PaymentResponse;
import ayd2.p2b.wallet_service_api.feature.payment.mapper.PaymentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetPaymentUseCaseTest {

    @Mock
    private PaymentRepositoryPort paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    private GetPaymentUseCase useCase;

    private static final UUID PAYMENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID OWNER_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OTHER_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @BeforeEach
    void setUp() {
        useCase = new GetPaymentUseCase(paymentRepository, paymentMapper);
    }

    @Test
    void should_return_payment_when_found_and_owner() {
        PaymentData payment = PaymentData.builder()
                .id(PAYMENT_ID)
                .userId(OWNER_USER_ID)
                .amount(new BigDecimal("100.00"))
                .build();

        PaymentResponse expectedResponse = PaymentResponse.builder()
                .id(PAYMENT_ID)
                .userId(OWNER_USER_ID)
                .amount(new BigDecimal("100.00"))
                .build();

        given(paymentRepository.findById(PAYMENT_ID)).willReturn(Optional.of(payment));
        given(paymentMapper.toResponse(payment)).willReturn(expectedResponse);

        PaymentResponse result = useCase.execute(PAYMENT_ID, OWNER_USER_ID, List.of("PARTICIPANT"));

        assertThat(result.getId()).isEqualTo(PAYMENT_ID);
        assertThat(result.getUserId()).isEqualTo(OWNER_USER_ID);
    }

    @Test
    void should_throw_forbidden_when_not_owner_and_not_admin() {
        PaymentData payment = PaymentData.builder()
                .id(PAYMENT_ID)
                .userId(OWNER_USER_ID)
                .amount(new BigDecimal("100.00"))
                .build();

        given(paymentRepository.findById(PAYMENT_ID)).willReturn(Optional.of(payment));

        ApiException ex = assertThrows(ApiException.class,
                () -> useCase.execute(PAYMENT_ID, OTHER_USER_ID, List.of("PARTICIPANT")));

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getCode()).isEqualTo("auth.forbidden");
    }

    @Test
    void should_allow_system_admin_to_get_any_payment() {
        PaymentData payment = PaymentData.builder()
                .id(PAYMENT_ID)
                .userId(OWNER_USER_ID)
                .amount(new BigDecimal("100.00"))
                .build();

        PaymentResponse expectedResponse = PaymentResponse.builder()
                .id(PAYMENT_ID)
                .userId(OWNER_USER_ID)
                .amount(new BigDecimal("100.00"))
                .build();

        given(paymentRepository.findById(PAYMENT_ID)).willReturn(Optional.of(payment));
        given(paymentMapper.toResponse(payment)).willReturn(expectedResponse);

        PaymentResponse result = useCase.execute(PAYMENT_ID, OTHER_USER_ID, List.of("SYSTEM_ADMIN"));

        assertThat(result.getId()).isEqualTo(PAYMENT_ID);
    }

    @Test
    void should_throw_not_found_when_payment_does_not_exist() {
        given(paymentRepository.findById(PAYMENT_ID)).willReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> useCase.execute(PAYMENT_ID, OWNER_USER_ID, List.of("PARTICIPANT")));

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getCode()).isEqualTo("resource.not_found");
    }
}
