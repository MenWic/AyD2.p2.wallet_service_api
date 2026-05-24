package ayd2.p2b.wallet_service_api.feature.payment.application.register;

import ayd2.p2b.wallet_service_api.common.dto.internal.RequesterContext;
import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.feature.payment.application.port.PaymentRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.payment.domain.model.PaymentData;
import ayd2.p2b.wallet_service_api.feature.payment.dto.internal.RegisterPaymentCommand;
import ayd2.p2b.wallet_service_api.feature.payment.dto.response.PaymentResponse;
import ayd2.p2b.wallet_service_api.feature.payment.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegisterPaymentUseCase {

    private final PaymentRepositoryPort paymentRepository;
    private final RegisterPaymentWriteOperation writeOperation;
    private final PaymentMapper paymentMapper;

    /**
     * Orchestrates payment registration with idempotency and race-recovery support.
     *
     * No @Transactional here — the narrow transactional boundary lives in
     * RegisterPaymentWriteOperation so that a duplicate-key rollback does NOT
     * inadvertently commit a wallet debit on the losing concurrent request.
     */
    public RegisterPaymentResult execute(RegisterPaymentCommand command, String idempotencyKey,
            RequesterContext requester) {

        guardAuthorization(requester, command, idempotencyKey);

        // Sequential replay pre-check: fast path before acquiring any row locks.
        Optional<PaymentData> existing = paymentRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return resolveExisting(existing.get(), command);
        }

        try {
            PaymentResponse response = writeOperation.write(command, idempotencyKey, requester);
            return RegisterPaymentResult.newPayment(response);
        } catch (DuplicatePaymentIdempotencyKeyException ex) {
            // Race: another concurrent request committed first. Re-read outside the
            // rolled-back transaction and apply the same replay / conflict logic.
            return paymentRepository.findByIdempotencyKey(idempotencyKey)
                    .map(recovered -> resolveExisting(recovered, command))
                    .orElseThrow(() -> new ApiException(
                            HttpStatus.CONFLICT,
                            "resource.conflict",
                            "Idempotency conflict could not be recovered"));
        }
    }

    private RegisterPaymentResult resolveExisting(PaymentData existing, RegisterPaymentCommand command) {
        if (!matchesSameRequest(existing, command)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "resource.conflict",
                    "Idempotency-Key was already used with a different request");
        }
        return RegisterPaymentResult.replay(paymentMapper.toResponse(existing));
    }

    /**
     * Returns true if the existing payment matches all key fields of the command.
     * Commission fields excluded — replay returns original values without recalculation.
     */
    private boolean matchesSameRequest(PaymentData existing, RegisterPaymentCommand command) {
        return existing.getUserId().equals(command.getUserId())
                && existing.getCongressId().equals(command.getCongressId())
                && existing.getInstitutionId().equals(command.getInstitutionId())
                && existing.getAmount().compareTo(command.getAmount()) == 0
                && existing.getPaymentDate().equals(command.getPaymentDate())
                && existing.getCongressNameSnapshot().strip().equals(command.getCongressNameSnapshot().strip())
                && existing.getInstitutionNameSnapshot().strip().equals(command.getInstitutionNameSnapshot().strip());
    }

    private void guardAuthorization(RequesterContext requester, RegisterPaymentCommand command,
            String idempotencyKey) {
        if (requester == null || command == null) {
            throw new ApiException(HttpStatus.FORBIDDEN, "auth.forbidden",
                    "Requester and command are required");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "validation.failed",
                    "Idempotency-Key is required");
        }
        if (!requester.getRoles().contains("PARTICIPANT")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "auth.forbidden",
                    "PARTICIPANT role required to register a payment");
        }
        if (!requester.getUserId().equals(command.getUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "auth.forbidden",
                    "Requester userId must match command userId");
        }
    }
}
