package ayd2.p2b.wallet_service_api.feature.payment.application.list;

import ayd2.p2b.wallet_service_api.common.response.PageResponse;
import ayd2.p2b.wallet_service_api.feature.payment.application.port.PaymentRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.payment.domain.model.PaymentData;
import ayd2.p2b.wallet_service_api.feature.payment.dto.internal.PaymentSearchCriteria;
import ayd2.p2b.wallet_service_api.feature.payment.dto.response.PaymentResponse;
import ayd2.p2b.wallet_service_api.feature.payment.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListPaymentsUseCase {

    private final PaymentRepositoryPort paymentRepository;
    private final PaymentMapper paymentMapper;

    public PageResponse<PaymentResponse> execute(PaymentSearchCriteria criteria) {
        PageResponse<PaymentData> dataPage = paymentRepository.findAll(criteria);

        List<PaymentResponse> items = dataPage.getItems().stream()
                .map(paymentMapper::toResponse)
                .toList();

        return PageResponse.<PaymentResponse>builder()
                .items(items)
                .page(dataPage.getPage())
                .size(dataPage.getSize())
                .totalItems(dataPage.getTotalItems())
                .totalPages(dataPage.getTotalPages())
                .build();
    }
}
