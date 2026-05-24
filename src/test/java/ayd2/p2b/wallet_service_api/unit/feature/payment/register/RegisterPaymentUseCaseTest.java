package ayd2.p2b.wallet_service_api.unit.feature.payment.register;

import ayd2.p2b.wallet_service_api.common.dto.internal.RequesterContext;
import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.feature.payment.application.port.PaymentRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.payment.application.register.RegisterPaymentResult;
import ayd2.p2b.wallet_service_api.feature.payment.application.register.RegisterPaymentUseCase;
import ayd2.p2b.wallet_service_api.feature.payment.domain.model.PaymentData;
import ayd2.p2b.wallet_service_api.feature.payment.dto.internal.RegisterPaymentCommand;
import ayd2.p2b.wallet_service_api.feature.payment.dto.response.PaymentResponse;
import ayd2.p2b.wallet_service_api.feature.payment.mapper.PaymentMapper;
import ayd2.p2b.wallet_service_api.feature.systemconfig.dto.internal.SystemConfigData;
import ayd2.p2b.wallet_service_api.feature.systemconfig.application.port.SystemConfigRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.application.port.TransactionRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.application.port.WalletRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.exception.InsufficientFundsException;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionData;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionType;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.WalletAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
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
        private static final UUID REQUESTER_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
        private static final String IDEMPOTENCY_KEY = "test-idempotency-key-001";

        @BeforeEach
        void setUp() {
                useCase = new RegisterPaymentUseCase(
                                walletRepository, transactionRepository, paymentRepository, configRepository,
                                paymentMapper);
        }

        // [RED] Test added to assert createdBy is set on both Payment and Transaction
        // records
        @Test
        void should_set_createdBy_on_transaction_when_registering_payment() {
                // Arrange
                WalletAccount wallet = WalletAccount.reconstitute(USER_ID, new BigDecimal("200.00"), 0L);
                SystemConfigData config = SystemConfigData.builder()
                                .commissionPercent(new BigDecimal("10.00"))
                                .build();
                RegisterPaymentCommand command = buildCommand();
                PaymentData savedPayment = buildSavedPayment();
                RequesterContext requester = RequesterContext.of(REQUESTER_ID, Set.of("CONGRESS_ADMIN"));

                given(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.empty());
                given(walletRepository.findByUserId(USER_ID)).willReturn(Optional.of(wallet));
                given(configRepository.find()).willReturn(config);
                given(walletRepository.save(any())).willReturn(wallet);
                given(paymentRepository.save(any())).willReturn(savedPayment);
                given(transactionRepository.save(any())).willReturn(TransactionData.builder().build());
                given(paymentMapper.toResponse(any())).willReturn(PaymentResponse.builder().build());

                // Act
                useCase.execute(command, IDEMPOTENCY_KEY, requester);

                // Assert — capture the TransactionData argument and verify all fields
                ArgumentCaptor<TransactionData> txCaptor = ArgumentCaptor.forClass(TransactionData.class);
                then(transactionRepository).should().save(txCaptor.capture());

                TransactionData savedTx = txCaptor.getValue();
                assertThat(savedTx.getCreatedBy()).isEqualTo(REQUESTER_ID);
                assertThat(savedTx.getType()).isEqualTo(TransactionType.PAYMENT);
                assertThat(savedTx.getAmount()).isNegative();
                assertThat(savedTx.getReferencePaymentId()).isNotNull();
                assertThat(savedTx.getTransactionDate()).isEqualTo(command.getPaymentDate());
        }

        @Test
        void should_set_createdBy_on_payment_record_when_registering() {
                // Arrange
                WalletAccount wallet = WalletAccount.reconstitute(USER_ID, new BigDecimal("200.00"), 0L);
                SystemConfigData config = SystemConfigData.builder()
                                .commissionPercent(new BigDecimal("10.00"))
                                .build();
                RegisterPaymentCommand command = buildCommand();
                PaymentData savedPayment = buildSavedPayment();
                RequesterContext requester = RequesterContext.of(REQUESTER_ID, Set.of("CONGRESS_ADMIN"));

                given(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.empty());
                given(walletRepository.findByUserId(USER_ID)).willReturn(Optional.of(wallet));
                given(configRepository.find()).willReturn(config);
                given(walletRepository.save(any())).willReturn(wallet);
                given(paymentRepository.save(any())).willReturn(savedPayment);
                given(transactionRepository.save(any())).willReturn(TransactionData.builder().build());
                given(paymentMapper.toResponse(any())).willReturn(PaymentResponse.builder().build());

                // Act
                useCase.execute(command, IDEMPOTENCY_KEY, requester);

                // Assert — capture the PaymentData argument and verify createdBy
                ArgumentCaptor<PaymentData> paymentCaptor = ArgumentCaptor.forClass(PaymentData.class);
                then(paymentRepository).should().save(paymentCaptor.capture());

                PaymentData capturedPayment = paymentCaptor.getValue();
                assertThat(capturedPayment.getCreatedBy()).isEqualTo(REQUESTER_ID);
        }

        @Test
        void should_register_payment_and_debit_wallet_when_valid_request() {
                WalletAccount wallet = WalletAccount.reconstitute(USER_ID, new BigDecimal("200.00"), 0L);
                SystemConfigData config = SystemConfigData.builder()
                                .commissionPercent(new BigDecimal("10.00"))
                                .build();
                RegisterPaymentCommand command = buildCommand();
                PaymentData savedPayment = buildSavedPayment();
                RequesterContext requester = RequesterContext.of(REQUESTER_ID, Set.of("CONGRESS_ADMIN"));

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

                RegisterPaymentResult result = useCase.execute(command, IDEMPOTENCY_KEY, requester);

                assertThat(result.isReplay()).isFalse();
                assertThat(result.getPayload().getAmount()).isEqualByComparingTo("100.00");
                assertThat(result.getPayload().getCommissionAmount()).isEqualByComparingTo("10.00");
                assertThat(result.getPayload().getNetAmount()).isEqualByComparingTo("90.00");
                then(walletRepository).should().save(any());
                then(transactionRepository).should().save(any());
                then(paymentRepository).should().save(any());
        }

        @Test
        void should_return_existing_payment_when_idempotency_key_already_used() {
                PaymentData existingPayment = buildSavedPayment();
                PaymentResponse expectedResponse = PaymentResponse.builder()
                                .amount(new BigDecimal("100.00"))
                                .build();
                RequesterContext requester = RequesterContext.of(REQUESTER_ID, Set.of("CONGRESS_ADMIN"));

                given(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.of(existingPayment));
                given(paymentMapper.toResponse(existingPayment)).willReturn(expectedResponse);

                RegisterPaymentResult result = useCase.execute(buildCommand(), IDEMPOTENCY_KEY, requester);

                assertThat(result.isReplay()).isTrue();
                assertThat(result.getPayload().getAmount()).isEqualByComparingTo("100.00");
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
                RequesterContext requester = RequesterContext.of(REQUESTER_ID, Set.of("CONGRESS_ADMIN"));

                given(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.empty());
                given(walletRepository.findByUserId(USER_ID)).willReturn(Optional.of(wallet));
                given(configRepository.find()).willReturn(config);

                assertThrows(InsufficientFundsException.class,
                                () -> useCase.execute(buildCommand(), IDEMPOTENCY_KEY, requester));
        }

        @Test
        void should_throw_when_wallet_not_found() {
                RequesterContext requester = RequesterContext.of(REQUESTER_ID, Set.of("CONGRESS_ADMIN"));

                given(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.empty());
                given(walletRepository.findByUserId(USER_ID)).willReturn(Optional.empty());

                assertThrows(ApiException.class,
                                () -> useCase.execute(buildCommand(), IDEMPOTENCY_KEY, requester));
        }

        // [RED] Idempotency hardening: same key + same request returns replay
        @Test
        void same_key_same_request_returns_replay() {
                PaymentData existing = buildSavedPayment();
                PaymentResponse mappedResponse = PaymentResponse.builder().id(existing.getId()).build();
                RequesterContext requester = RequesterContext.of(REQUESTER_ID, Set.of("PARTICIPANT"));

                given(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.of(existing));
                given(paymentMapper.toResponse(existing)).willReturn(mappedResponse);

                RegisterPaymentResult result = useCase.execute(buildCommand(), IDEMPOTENCY_KEY, requester);

                assertThat(result.isReplay()).isTrue();
                assertThat(result.getPayload().getId()).isEqualTo(existing.getId());
                then(walletRepository).should(never()).findByUserId(any());
        }

        // [RED] Idempotency hardening: same key + different amount returns 409
        @Test
        void same_key_different_amount_returns_409() {
                PaymentData existing = buildSavedPayment();
                RequesterContext requester = RequesterContext.of(REQUESTER_ID, Set.of("PARTICIPANT"));

                given(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.of(existing));

                RegisterPaymentCommand differentAmount = RegisterPaymentCommand.builder()
                                .userId(USER_ID).congressId(CONGRESS_ID).institutionId(INSTITUTION_ID)
                                .congressNameSnapshot("Test Congress").institutionNameSnapshot("Test Institution")
                                .amount(new BigDecimal("999.00"))
                                .paymentDate(LocalDate.of(2026, 5, 20))
                                .build();

                ApiException ex = assertThrows(ApiException.class,
                                () -> useCase.execute(differentAmount, IDEMPOTENCY_KEY, requester));
                assertThat(ex.getStatus().value()).isEqualTo(409);
                assertThat(ex.getCode()).isEqualTo("resource.conflict");
        }

        // [RED] Idempotency hardening: same key + different userId returns 409
        @Test
        void same_key_different_user_returns_409() {
                PaymentData existing = buildSavedPayment();
                RequesterContext requester = RequesterContext.of(REQUESTER_ID, Set.of("PARTICIPANT"));

                given(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.of(existing));

                UUID differentUser = UUID.fromString("00000000-0000-0000-0000-000000000099");
                RegisterPaymentCommand differentUserCommand = RegisterPaymentCommand.builder()
                                .userId(differentUser).congressId(CONGRESS_ID).institutionId(INSTITUTION_ID)
                                .congressNameSnapshot("Test Congress").institutionNameSnapshot("Test Institution")
                                .amount(new BigDecimal("100.00"))
                                .paymentDate(LocalDate.of(2026, 5, 20))
                                .build();

                ApiException ex = assertThrows(ApiException.class,
                                () -> useCase.execute(differentUserCommand, IDEMPOTENCY_KEY, requester));
                assertThat(ex.getStatus().value()).isEqualTo(409);
                assertThat(ex.getCode()).isEqualTo("resource.conflict");
        }

        // [RED] Idempotency hardening: same key + different congressId returns 409
        @Test
        void same_key_different_congress_returns_409() {
                PaymentData existing = buildSavedPayment();
                RequesterContext requester = RequesterContext.of(REQUESTER_ID, Set.of("PARTICIPANT"));

                given(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.of(existing));

                RegisterPaymentCommand differentCongress = RegisterPaymentCommand.builder()
                                .userId(USER_ID).congressId(UUID.fromString("00000000-0000-0000-0000-000000000099"))
                                .institutionId(INSTITUTION_ID)
                                .congressNameSnapshot("Test Congress").institutionNameSnapshot("Test Institution")
                                .amount(new BigDecimal("100.00"))
                                .paymentDate(LocalDate.of(2026, 5, 20))
                                .build();

                ApiException ex = assertThrows(ApiException.class,
                                () -> useCase.execute(differentCongress, IDEMPOTENCY_KEY, requester));
                assertThat(ex.getStatus().value()).isEqualTo(409);
                assertThat(ex.getCode()).isEqualTo("resource.conflict");
        }

        // [RED] Idempotency hardening: same key + different paymentDate returns 409
        @Test
        void same_key_different_payment_date_returns_409() {
                PaymentData existing = buildSavedPayment();
                RequesterContext requester = RequesterContext.of(REQUESTER_ID, Set.of("PARTICIPANT"));

                given(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.of(existing));

                RegisterPaymentCommand differentDate = RegisterPaymentCommand.builder()
                                .userId(USER_ID).congressId(CONGRESS_ID).institutionId(INSTITUTION_ID)
                                .congressNameSnapshot("Test Congress").institutionNameSnapshot("Test Institution")
                                .amount(new BigDecimal("100.00"))
                                .paymentDate(LocalDate.of(2026, 12, 31))
                                .build();

                ApiException ex = assertThrows(ApiException.class,
                                () -> useCase.execute(differentDate, IDEMPOTENCY_KEY, requester));
                assertThat(ex.getStatus().value()).isEqualTo(409);
                assertThat(ex.getCode()).isEqualTo("resource.conflict");
        }

        // --- helpers ---

        private RegisterPaymentCommand buildCommand() {
                return RegisterPaymentCommand.builder()
                                .userId(USER_ID)
                                .congressId(CONGRESS_ID)
                                .institutionId(INSTITUTION_ID)
                                .congressNameSnapshot("Test Congress")
                                .institutionNameSnapshot("Test Institution")
                                .amount(new BigDecimal("100.00"))
                                .paymentDate(LocalDate.of(2026, 5, 20))
                                .build();
        }

        private PaymentData buildSavedPayment() {
                return PaymentData.builder()
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
                                .createdBy(REQUESTER_ID)
                                .build();
        }
}
