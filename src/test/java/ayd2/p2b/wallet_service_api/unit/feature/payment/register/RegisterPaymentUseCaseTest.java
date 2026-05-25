package ayd2.p2b.wallet_service_api.unit.feature.payment.register;

import ayd2.p2b.wallet_service_api.common.dto.internal.RequesterContext;
import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.feature.payment.application.port.PaymentRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.payment.application.register.DuplicatePaymentIdempotencyKeyException;
import ayd2.p2b.wallet_service_api.feature.payment.application.register.RegisterPaymentResult;
import ayd2.p2b.wallet_service_api.feature.payment.application.register.RegisterPaymentUseCase;
import ayd2.p2b.wallet_service_api.feature.payment.application.register.RegisterPaymentWriteOperation;
import ayd2.p2b.wallet_service_api.feature.payment.domain.model.PaymentData;
import ayd2.p2b.wallet_service_api.feature.payment.dto.internal.RegisterPaymentCommand;
import ayd2.p2b.wallet_service_api.feature.payment.dto.response.PaymentResponse;
import ayd2.p2b.wallet_service_api.feature.payment.mapper.PaymentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private PaymentRepositoryPort paymentRepository;

    @Mock
    private RegisterPaymentWriteOperation writeOperation;

    @Mock
    private PaymentMapper paymentMapper;

    private RegisterPaymentUseCase useCase;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CONGRESS_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID INSTITUTION_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final String IDEMPOTENCY_KEY = "test-idempotency-key-001";

    @BeforeEach
    void setUp() {
        useCase = new RegisterPaymentUseCase(paymentRepository, writeOperation, paymentMapper);
    }

    // --- Authorization guard ---

    @Test
    void should_throw_403_when_requester_is_null() {
        ApiException ex = assertThrows(ApiException.class,
                () -> useCase.execute(buildCommand(), IDEMPOTENCY_KEY, null));
        assertThat(ex.getStatus().value()).isEqualTo(403);
        assertThat(ex.getCode()).isEqualTo("auth.forbidden");
    }

    @Test
    void should_throw_403_when_requester_lacks_participant_role() {
        RequesterContext requester = RequesterContext.of(USER_ID, Set.of("CONGRESS_ADMIN"));

        ApiException ex = assertThrows(ApiException.class,
                () -> useCase.execute(buildCommand(), IDEMPOTENCY_KEY, requester));
        assertThat(ex.getStatus().value()).isEqualTo(403);
        assertThat(ex.getCode()).isEqualTo("auth.forbidden");
    }

    @Test
    void should_throw_403_when_system_admin_calls_without_participant_role() {
        RequesterContext requester = RequesterContext.of(USER_ID, Set.of("SYSTEM_ADMIN"));

        ApiException ex = assertThrows(ApiException.class,
                () -> useCase.execute(buildCommand(), IDEMPOTENCY_KEY, requester));
        assertThat(ex.getStatus().value()).isEqualTo(403);
        assertThat(ex.getCode()).isEqualTo("auth.forbidden");
    }

    @Test
    void should_throw_403_when_requester_user_id_differs_from_command_user_id() {
        UUID differentUser = UUID.fromString("00000000-0000-0000-0000-000000000099");
        RequesterContext requester = RequesterContext.of(differentUser, Set.of("PARTICIPANT"));

        ApiException ex = assertThrows(ApiException.class,
                () -> useCase.execute(buildCommand(), IDEMPOTENCY_KEY, requester));
        assertThat(ex.getStatus().value()).isEqualTo(403);
        assertThat(ex.getCode()).isEqualTo("auth.forbidden");
        then(paymentRepository).should(never()).findByIdempotencyKey(any());
    }

    @Test
    void should_throw_400_when_idempotency_key_is_blank() {
        RequesterContext requester = RequesterContext.of(USER_ID, Set.of("PARTICIPANT"));

        ApiException ex = assertThrows(ApiException.class,
                () -> useCase.execute(buildCommand(), "   ", requester));
        assertThat(ex.getStatus().value()).isEqualTo(400);
        assertThat(ex.getCode()).isEqualTo("validation.failed");
    }

    // --- Sequential replay (same key already persisted before write) ---

    @Test
    void same_key_same_payload_returns_replay_without_write() {
        PaymentData existing = buildSavedPayment();
        PaymentResponse mappedResponse = PaymentResponse.builder().id(existing.getId()).build();
        RequesterContext requester = RequesterContext.of(USER_ID, Set.of("PARTICIPANT"));

        given(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.of(existing));
        given(paymentMapper.toResponse(existing)).willReturn(mappedResponse);

        RegisterPaymentResult result = useCase.execute(buildCommand(), IDEMPOTENCY_KEY, requester);

        assertThat(result.isReplay()).isTrue();
        assertThat(result.getPayload().getId()).isEqualTo(existing.getId());
        then(writeOperation).should(never()).write(any(), any(), any());
    }

    @Test
    void same_key_different_amount_returns_409_without_write() {
        PaymentData existing = buildSavedPayment();
        RequesterContext requester = RequesterContext.of(USER_ID, Set.of("PARTICIPANT"));

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
        then(writeOperation).should(never()).write(any(), any(), any());
    }

    @Test
    void same_key_different_congress_id_returns_409() {
        PaymentData existing = buildSavedPayment();
        RequesterContext requester = RequesterContext.of(USER_ID, Set.of("PARTICIPANT"));

        given(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.of(existing));

        RegisterPaymentCommand differentCongress = RegisterPaymentCommand.builder()
                .userId(USER_ID)
                .congressId(UUID.fromString("00000000-0000-0000-0000-000000000099"))
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

    @Test
    void same_key_with_whitespace_padded_snapshots_returns_replay() {
        UUID paymentId = UUID.fromString("00000000-0000-0000-0000-000000000010");
        PaymentData existing = PaymentData.builder()
                .id(paymentId).userId(USER_ID).congressId(CONGRESS_ID).institutionId(INSTITUTION_ID)
                .congressNameSnapshot("Test Congress").institutionNameSnapshot("Test Institution")
                .amount(new BigDecimal("100.00")).paymentDate(LocalDate.of(2026, 5, 20))
                .idempotencyKey(IDEMPOTENCY_KEY).build();

        given(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.of(existing));
        given(paymentMapper.toResponse(existing)).willReturn(PaymentResponse.builder().id(paymentId).build());

        RegisterPaymentCommand paddedCmd = RegisterPaymentCommand.builder()
                .userId(USER_ID).congressId(CONGRESS_ID).institutionId(INSTITUTION_ID)
                .congressNameSnapshot("  Test Congress  ").institutionNameSnapshot("  Test Institution  ")
                .amount(new BigDecimal("100.00")).paymentDate(LocalDate.of(2026, 5, 20)).build();

        RegisterPaymentResult result = useCase.execute(paddedCmd, IDEMPOTENCY_KEY,
                RequesterContext.of(USER_ID, Set.of("PARTICIPANT")));

        assertThat(result.isReplay()).isTrue();
        then(writeOperation).should(never()).write(any(), any(), any());
    }

    // --- Successful new payment ---

    @Test
    void should_register_new_payment_and_return_non_replay_result() {
        RequesterContext requester = RequesterContext.of(USER_ID, Set.of("PARTICIPANT"));
        PaymentResponse expectedResponse = PaymentResponse.builder()
                .amount(new BigDecimal("100.00"))
                .commissionAmount(new BigDecimal("10.00"))
                .netAmount(new BigDecimal("90.00"))
                .build();

        given(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.empty());
        given(writeOperation.write(any(), any(), any())).willReturn(expectedResponse);

        RegisterPaymentResult result = useCase.execute(buildCommand(), IDEMPOTENCY_KEY, requester);

        assertThat(result.isReplay()).isFalse();
        assertThat(result.getPayload().getAmount()).isEqualByComparingTo("100.00");
        then(writeOperation).should().write(any(), any(), any());
    }

    // --- Race condition recovery ---

    @Test
    void duplicate_key_race_same_payload_rolls_back_and_returns_replay() {
        PaymentData committed = buildSavedPayment();
        PaymentResponse mappedResponse = PaymentResponse.builder().id(committed.getId()).build();
        RequesterContext requester = RequesterContext.of(USER_ID, Set.of("PARTICIPANT"));

        // Pre-check: key not found (race window)
        given(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .willReturn(Optional.empty())  // first call: pre-check
                .willReturn(Optional.of(committed));  // second call: recovery re-read

        // Write attempt throws because the concurrent winner committed first
        given(writeOperation.write(any(), any(), any()))
                .willThrow(new DuplicatePaymentIdempotencyKeyException(IDEMPOTENCY_KEY));

        given(paymentMapper.toResponse(committed)).willReturn(mappedResponse);

        RegisterPaymentResult result = useCase.execute(buildCommand(), IDEMPOTENCY_KEY, requester);

        assertThat(result.isReplay()).isTrue();
        assertThat(result.getPayload().getId()).isEqualTo(committed.getId());
    }

    @Test
    void duplicate_key_race_different_payload_returns_409_after_recovery() {
        // The winning request stored a different congress
        PaymentData committed = PaymentData.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .congressId(UUID.fromString("00000000-0000-0000-0000-000000000099"))
                .institutionId(INSTITUTION_ID)
                .congressNameSnapshot("Test Congress").institutionNameSnapshot("Test Institution")
                .amount(new BigDecimal("100.00")).paymentDate(LocalDate.of(2026, 5, 20))
                .idempotencyKey(IDEMPOTENCY_KEY).build();

        RequesterContext requester = RequesterContext.of(USER_ID, Set.of("PARTICIPANT"));

        given(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .willReturn(Optional.empty())
                .willReturn(Optional.of(committed));

        given(writeOperation.write(any(), any(), any()))
                .willThrow(new DuplicatePaymentIdempotencyKeyException(IDEMPOTENCY_KEY));

        ApiException ex = assertThrows(ApiException.class,
                () -> useCase.execute(buildCommand(), IDEMPOTENCY_KEY, requester));
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
                .createdBy(USER_ID)
                .build();
    }
}
