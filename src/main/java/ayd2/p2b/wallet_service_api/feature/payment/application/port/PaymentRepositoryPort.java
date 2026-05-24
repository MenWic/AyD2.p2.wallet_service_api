package ayd2.p2b.wallet_service_api.feature.payment.application.port;

import ayd2.p2b.wallet_service_api.common.response.PageResponse;
import ayd2.p2b.wallet_service_api.feature.payment.domain.model.PaymentData;
import ayd2.p2b.wallet_service_api.feature.payment.dto.internal.PaymentSearchCriteria;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepositoryPort {

    PaymentData save(PaymentData payment);

    Optional<PaymentData> findById(UUID id);

    Optional<PaymentData> findByIdempotencyKey(String key);

    PageResponse<PaymentData> findAll(PaymentSearchCriteria criteria);
}
