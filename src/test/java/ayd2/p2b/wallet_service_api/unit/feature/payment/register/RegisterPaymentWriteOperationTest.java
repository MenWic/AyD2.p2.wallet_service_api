package ayd2.p2b.wallet_service_api.unit.feature.payment.register;

import ayd2.p2b.wallet_service_api.common.dto.internal.RequesterContext;
import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.feature.payment.application.port.PaymentRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.payment.application.register.DuplicatePaymentIdempotencyKeyException;
import ayd2.p2b.wallet_service_api.feature.payment.application.register.RegisterPaymentWriteOperation;
import ayd2.p2b.wallet_service_api.feature.payment.domain.model.PaymentData;
import ayd2.p2b.wallet_service_api.feature.payment.dto.internal.RegisterPaymentCommand;
import ayd2.p2b.wallet_service_api.feature.payment.dto.response.PaymentResponse;
import ayd2.p2b.wallet_service_api.feature.payment.mapper.PaymentMapper;
import ayd2.p2b.wallet_service_api.feature.systemconfig.application.port.SystemConfigRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.systemconfig.dto.internal.SystemConfigData;
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

@ExtendWith(MockitoExtension.class)
class RegisterPaymentWriteOperationTest {

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

    private RegisterPaymentWriteOperation operation;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CONGRESS_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID INSTITUTION_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID REQUESTER_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final String IDEMPOTENCY_KEY = "test-idempotency-key-001";

    @BeforeEach
    void setUp() {
        operation = new RegisterPaymentWriteOperation(
                walletRepository, transactionRepository, paymentRepository, configRepository, paymentMapper);
    }

    @Test
    void should_debit_wallet_save_payment_and_transaction_on_success() {
        WalletAccount wallet = WalletAccount.reconstitute(USER_ID, new BigDecimal("200.00"), 0L);
        SystemConfigData config = SystemConfigData.builder()
                .commissionPercent(new BigDecimal("10.00")).build();
        PaymentData savedPayment = buildSavedPayment();
        RequesterContext requester = RequesterContext.of(REQUESTER_ID, Set.of("PARTICIPANT"));

        given(walletRepository.findByUserId(USER_ID)).willReturn(Optional.of(wallet));
        given(configRepository.find()).willReturn(config);
        given(walletRepository.save(any())).willReturn(wallet);
        given(paymentRepository.save(any())).willReturn(savedPayment);
        given(transactionRepository.save(any())).willReturn(TransactionData.builder().build());
        given(paymentMapper.toResponse(any())).willReturn(
                PaymentResponse.builder().amount(new BigDecimal("100.00")).build());

        PaymentResponse response = operation.write(buildCommand(), IDEMPOTENCY_KEY, requester);

        assertThat(response.getAmount()).isEqualByComparingTo("100.00");
        then(walletRepository).should().save(any());
        then(paymentRepository).should().save(any());
        then(transactionRepository).should().save(any());
    }

    @Test
    void should_set_created_by_from_requester_on_payment_and_transaction() {
        WalletAccount wallet = WalletAccount.reconstitute(USER_ID, new BigDecimal("200.00"), 0L);
        SystemConfigData config = SystemConfigData.builder()
                .commissionPercent(new BigDecimal("10.00")).build();
        PaymentData savedPayment = buildSavedPayment();
        RequesterContext requester = RequesterContext.of(REQUESTER_ID, Set.of("PARTICIPANT"));

        given(walletRepository.findByUserId(USER_ID)).willReturn(Optional.of(wallet));
        given(configRepository.find()).willReturn(config);
        given(walletRepository.save(any())).willReturn(wallet);
        given(paymentRepository.save(any())).willReturn(savedPayment);
        given(transactionRepository.save(any())).willReturn(TransactionData.builder().build());
        given(paymentMapper.toResponse(any())).willReturn(PaymentResponse.builder().build());

        operation.write(buildCommand(), IDEMPOTENCY_KEY, requester);

        ArgumentCaptor<PaymentData> paymentCaptor = ArgumentCaptor.forClass(PaymentData.class);
        then(paymentRepository).should().save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getCreatedBy()).isEqualTo(REQUESTER_ID);

        ArgumentCaptor<TransactionData> txCaptor = ArgumentCaptor.forClass(TransactionData.class);
        then(transactionRepository).should().save(txCaptor.capture());
        TransactionData savedTx = txCaptor.getValue();
        assertThat(savedTx.getCreatedBy()).isEqualTo(REQUESTER_ID);
        assertThat(savedTx.getType()).isEqualTo(TransactionType.PAYMENT);
        assertThat(savedTx.getAmount()).isNegative();
        assertThat(savedTx.getReferencePaymentId()).isNotNull();
        assertThat(savedTx.getTransactionDate()).isEqualTo(buildCommand().getPaymentDate());
    }

    @Test
    void should_split_commission_correctly() {
        WalletAccount wallet = WalletAccount.reconstitute(USER_ID, new BigDecimal("200.00"), 0L);
        SystemConfigData config = SystemConfigData.builder()
                .commissionPercent(new BigDecimal("10.00")).build();
        RequesterContext requester = RequesterContext.of(REQUESTER_ID, Set.of("PARTICIPANT"));

        given(walletRepository.findByUserId(USER_ID)).willReturn(Optional.of(wallet));
        given(configRepository.find()).willReturn(config);
        given(walletRepository.save(any())).willReturn(wallet);
        given(transactionRepository.save(any())).willReturn(TransactionData.builder().build());

        ArgumentCaptor<PaymentData> paymentCaptor = ArgumentCaptor.forClass(PaymentData.class);
        given(paymentRepository.save(paymentCaptor.capture())).willAnswer(inv -> inv.getArgument(0));
        given(paymentMapper.toResponse(any())).willReturn(PaymentResponse.builder().build());

        operation.write(buildCommand(), IDEMPOTENCY_KEY, requester);

        PaymentData captured = paymentCaptor.getValue();
        assertThat(captured.getAmount()).isEqualByComparingTo("100.00");
        assertThat(captured.getCommissionAmount()).isEqualByComparingTo("10.00");
        assertThat(captured.getNetAmount()).isEqualByComparingTo("90.00");
        assertThat(captured.getCommissionPercentSnapshot()).isEqualByComparingTo("10.00");
    }

    @Test
    void should_throw_insufficient_funds_when_balance_too_low() {
        WalletAccount wallet = WalletAccount.reconstitute(USER_ID, new BigDecimal("30.00"), 0L);
        SystemConfigData config = SystemConfigData.builder()
                .commissionPercent(new BigDecimal("10.00")).build();
        RequesterContext requester = RequesterContext.of(REQUESTER_ID, Set.of("PARTICIPANT"));

        given(walletRepository.findByUserId(USER_ID)).willReturn(Optional.of(wallet));
        given(configRepository.find()).willReturn(config);

        assertThrows(InsufficientFundsException.class,
                () -> operation.write(buildCommand(), IDEMPOTENCY_KEY, requester));
    }

    @Test
    void should_throw_not_found_when_wallet_absent() {
        RequesterContext requester = RequesterContext.of(REQUESTER_ID, Set.of("PARTICIPANT"));
        given(walletRepository.findByUserId(USER_ID)).willReturn(Optional.empty());

        assertThrows(ApiException.class,
                () -> operation.write(buildCommand(), IDEMPOTENCY_KEY, requester));
    }

    @Test
    void should_propagate_duplicate_key_exception_so_transaction_rolls_back() {
        WalletAccount wallet = WalletAccount.reconstitute(USER_ID, new BigDecimal("200.00"), 0L);
        SystemConfigData config = SystemConfigData.builder()
                .commissionPercent(new BigDecimal("10.00")).build();
        RequesterContext requester = RequesterContext.of(REQUESTER_ID, Set.of("PARTICIPANT"));

        given(walletRepository.findByUserId(USER_ID)).willReturn(Optional.of(wallet));
        given(configRepository.find()).willReturn(config);
        given(walletRepository.save(any())).willReturn(wallet);
        given(paymentRepository.save(any()))
                .willThrow(new DuplicatePaymentIdempotencyKeyException(IDEMPOTENCY_KEY));

        assertThrows(DuplicatePaymentIdempotencyKeyException.class,
                () -> operation.write(buildCommand(), IDEMPOTENCY_KEY, requester));

        // transaction must NOT have been saved when payment save fails
        then(transactionRepository).should(org.mockito.Mockito.never()).save(any());
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
