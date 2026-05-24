package ayd2.p2b.wallet_service_api.feature.payment.infrastructure.persistence.adapter;

import ayd2.p2b.wallet_service_api.common.response.PageResponse;
import ayd2.p2b.wallet_service_api.feature.payment.application.port.PaymentRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.payment.domain.model.PaymentData;
import ayd2.p2b.wallet_service_api.feature.payment.dto.internal.PaymentSearchCriteria;
import ayd2.p2b.wallet_service_api.feature.payment.infrastructure.persistence.entity.PaymentEntity;
import ayd2.p2b.wallet_service_api.feature.payment.infrastructure.persistence.repository.PaymentJpaRepository;
import ayd2.p2b.wallet_service_api.feature.payment.infrastructure.persistence.specification.PaymentSpecification;
import ayd2.p2b.wallet_service_api.feature.payment.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private final PaymentJpaRepository jpaRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentData save(PaymentData payment) {
        PaymentEntity entity = paymentMapper.toEntity(payment);
        PaymentEntity saved = jpaRepository.save(entity);
        return paymentMapper.toDomain(saved);
    }

    @Override
    public Optional<PaymentData> findById(UUID id) {
        return jpaRepository.findById(id).map(paymentMapper::toDomain);
    }

    @Override
    public Optional<PaymentData> findByIdempotencyKey(String key) {
        return jpaRepository.findByIdempotencyKey(key).map(paymentMapper::toDomain);
    }

    @Override
    public PageResponse<PaymentData> findAll(PaymentSearchCriteria criteria) {
        Specification<PaymentEntity> spec = Specification
                .where(PaymentSpecification.withCongressId(criteria.getCongressId()))
                .and(PaymentSpecification.withInstitutionId(criteria.getInstitutionId()))
                .and(PaymentSpecification.fromDate(criteria.getDateFrom()))
                .and(PaymentSpecification.toDate(criteria.getDateTo()));

        PageRequest pageable = PageRequest.of(criteria.getPage(), criteria.getSize());
        Page<PaymentEntity> page = jpaRepository.findAll(spec, pageable);

        List<PaymentData> items = page.getContent().stream()
                .map(paymentMapper::toDomain)
                .toList();

        return PageResponse.<PaymentData>builder()
                .items(items)
                .page(page.getNumber())
                .size(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
