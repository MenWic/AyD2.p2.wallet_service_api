package ayd2.p2b.wallet_service_api.unit.feature.payment.register;

import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.feature.payment.PaymentRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.payment.application.register.RegisterPaymentUseCase;
import ayd2.p2b.wallet_service_api.feature.payment.domain.model.PaymentData;
import ayd2.p2b.wallet_service_api.feature.payment.dto.request.RegisterPaymentRequest;
import ayd2.p2b.wallet_service_api.feature.payment.dto.response.PaymentResponse;
import ayd2.p2b.wallet_service_api.feature.payment.mapper.PaymentMapper;
import ayd2.p2b.wallet_service_api.feature.systemconfig.SystemConfigData;
import ayd2.p2b.wallet_service_api.feature.systemconfig.SystemConfigRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.TransactionRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.WalletRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.exception.InsufficientFundsException;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionData;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.WalletAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class RegisterPaymentUseCaseTest {

    @Mock
    private WalletRepositoryPort walletRepository;

    @Mock
    private TransactionRepositoryPort transactionRepository;

    @Mock
    private PaymentRepositoryPort paymentRepository;

    @Mock
    private SystemConfigRepositoryPort configRepository;

    @Mock
    private PaymentMapper paymentMapper;

    private RegisterPaymentUseCase useCase;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CONGRESS_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID INSTITUTION_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID CREATED_BY = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final String IDEMPOTENCY_KEY = "test-idempotency-key-001";

    @BeforeEach
    void setUp() {
        useCase = new RegisterPaymentUseCase(
                walletRepository, transactionRepository, paymentRepository, configRepository, paymentMapper
        );
    }

    @Test
    void should_register_payment_and_debit_wallet_when_valid_request() {
        WalletAccount wallet = WalletAccount.reconstitute(USER_ID, new BigDecimal("200.00"), 0L);
        SystemConfigData config = SystemConfigData.builder()
                .commissionPercent(new BigDecimal("10.00"))
                .build();

        RegisterPaymentRequest request = RegisterPaymentRequest.builder()
                .userId(USER_ID)
                .congressId(CONGRESS_ID)
                .institutionId(INSTITUTION_ID)
                .congressNameSnapshot("Test Congress")
                .institutionNameSnapshot("Test Institution")
                .amount(new BigDecimal("100.00"))
                .paymentDate(LocalDate.of(2026, 5, 20))
                .build();

        PaymentData savedPayment = PaymentData.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .congressId(CONGRESS_ID)
                .institutionId(INSTITUTION_ID)
                .congressNameSnapshot("Test Congress")
                .institutionNameSnapshot("Test Institution")
                .commissionPercentSnapshot(new BigDecimal("10.00"))
                .amount(new BigDecimal("100.00"))
                .commissionAmount(new BigDecimal("10.00"))
                .netAmount(new BigDecimal("90.00"))
                .paymentDate(LocalDate.of(2026, 5, 20))
                .idempotencyKey(IDEMPOTENCY_KEY)
                .createdBy(CREATED_BY)
                .build();

        PaymentResponse expectedResponse = PaymentResponse.builder()
                .amount(new BigDecimal("100.00"))
                .commissionAmount(new BigDecimal("10.00"))
                .netAmount(new BigDecimal("90.00"))
                .build();

        given(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.empty());
        given(walletRepository.findByUserId(USER_ID)).willReturn(Optional.of(wallet));
        given(configRepository.find()).willReturn(config);
        given(walletRepository.save(any())).willReturn(wallet);
        given(transactionRepository.save(any())).willReturn(TransactionData.builder().build());
        given(paymentRepository.save(any())).willReturn(savedPayment);
        given(paymentMapper.toResponse(any())).willReturn(expectedResponse);

        PaymentResponse result = useCase.execute(request, IDEMPOTENCY_KEY, CREATED_BY);

        assertThat(result.getAmount()).isEqualByComparingTo("100.00");
        assertThat(result.getCommissionAmount()).isEqualByComparingTo("10.00");
        assertThat(result.getNetAmount()).isEqualByComparingTo("90.00");
        then(walletRepository).should().save(any());
        then(transactionRepository).should().save(any());
        then(paymentRepository).should().save(any());
    }

    @Test
    void should_return_existing_payment_when_idempotency_key_already_used() {
        PaymentData existingPayment = PaymentData.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .amount(new BigDecimal("100.00"))
                .commissionAmount(new BigDecimal("10.00"))
                .netAmount(new BigDecimal("90.00"))
                .idempotencyKey(IDEMPOTENCY_KEY)
                .build();

        PaymentResponse expectedResponse = PaymentResponse.builder()
                .amount(new BigDecimal("100.00"))
                .build();

        RegisterPaymentRequest request = RegisterPaymentRequest.builder()
                .userId(USER_ID)
                .congressId(CONGRESS_ID)
                .institutionId(INSTITUTION_ID)
                .congressNameSnapshot("Test Congress")
                .institutionNameSnapshot("Test Institution")
                .amount(new BigDecimal("100.00"))
                .paymentDate(LocalDate.of(2026, 5, 20))
                .build();

        given(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.of(existingPayment));
        given(paymentMapper.toResponse(existingPayment)).willReturn(expectedResponse);

        PaymentResponse result = useCase.execute(request, IDEMPOTENCY_KEY, CREATED_BY);

        assertThat(result.getAmount()).isEqualByComparingTo("100.00");
        then(walletRepository).should(never()).findByUserId(any());
        then(walletRepository).should(never()).save(any());
        then(transactionRepository).should(never()).save(any());
        then(paymentRepository).should(never()).save(any());
    }

    @Test
    void should_throw_when_wallet_insufficient_funds() {
        WalletAccount wallet = WalletAccount.reconstitute(USER_ID, new BigDecimal("30.00"), 0L);
        SystemConfigData config = SystemConfigData.builder()
                .commissionPercent(new BigDecimal("10.00"))
                .build();

        RegisterPaymentRequest request = RegisterPaymentRequest.builder()
                .userId(USER_ID)
                .congressId(CONGRESS_ID)
                .institutionId(INSTITUTION_ID)
                .congressNameSnapshot("Test Congress")
                .institutionNameSnapshot("Test Institution")
                .amount(new BigDecimal("100.00"))
                .paymentDate(LocalDate.of(2026, 5, 20))
                .build();

        given(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.empty());
        given(walletRepository.findByUserId(USER_ID)).willReturn(Optional.of(wallet));
        given(configRepository.find()).willReturn(config);

        assertThrows(InsufficientFundsException.class,
                () -> useCase.execute(request, IDEMPOTENCY_KEY, CREATED_BY));
    }

    @Test
    void should_throw_when_wallet_not_found() {
        RegisterPaymentRequest request = RegisterPaymentRequest.builder()
                .userId(USER_ID)
                .congressId(CONGRESS_ID)
                .institutionId(INSTITUTION_ID)
                .congressNameSnapshot("Test Congress")
                .institutionNameSnapshot("Test Institution")
                .amount(new BigDecimal("100.00"))
                .paymentDate(LocalDate.of(2026, 5, 20))
                .build();

        given(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.empty());
        given(walletRepository.findByUserId(USER_ID)).willReturn(Optional.empty());

        assertThrows(ApiException.class,
                () -> useCase.execute(request, IDEMPOTENCY_KEY, CREATED_BY));
    }
}
