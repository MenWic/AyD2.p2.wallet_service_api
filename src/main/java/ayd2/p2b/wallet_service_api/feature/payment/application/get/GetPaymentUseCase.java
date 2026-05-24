package ayd2.p2b.wallet_service_api.feature.payment.application.get;

import ayd2.p2b.wallet_service_api.common.dto.internal.RequesterContext;
import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.feature.payment.application.port.PaymentRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.payment.domain.model.PaymentData;
import ayd2.p2b.wallet_service_api.feature.payment.dto.response.PaymentResponse;
import ayd2.p2b.wallet_service_api.feature.payment.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPaymentUseCase {

    private final PaymentRepositoryPort paymentRepository;
    private final PaymentMapper paymentMapper;

    public PaymentResponse execute(UUID paymentId, RequesterContext requester) {
        PaymentData payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "resource.not_found",
                        "Payment not found: " + paymentId
                ));

        boolean isOwner = payment.getUserId().equals(requester.getUserId());
        boolean isAdmin = requester.getRoles().contains("SYSTEM_ADMIN");

        if (!isOwner && !isAdmin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "auth.forbidden", "Access denied");
        }

        return paymentMapper.toResponse(payment);
    }
}
