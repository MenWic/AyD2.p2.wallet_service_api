package ayd2.p2b.wallet_service_api.feature.payment.application.register;

/**
 * Signals that a payment save failed because the idempotency_key unique constraint
 * was violated by a concurrent request. The outer orchestrator (RegisterPaymentUseCase)
 * catches this to trigger race-recovery re-read.
 */
public class DuplicatePaymentIdempotencyKeyException extends RuntimeException {

    public DuplicatePaymentIdempotencyKeyException(String idempotencyKey) {
        super("Duplicate idempotency key: " + idempotencyKey);
    }
}
