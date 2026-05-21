package ayd2.p2b.wallet_service_api.feature.payment.infrastructure.persistence.adapter;

import ayd2.p2b.wallet_service_api.common.response.PageResponse;
import ayd2.p2b.wallet_service_api.feature.payment.PaymentRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.payment.domain.model.PaymentData;
import ayd2.p2b.wallet_service_api.feature.payment.dto.request.PaymentFilterRequest;
import ayd2.p2b.wallet_service_api.feature.payment.infrastructure.persistence.entity.PaymentEntity;
import ayd2.p2b.wallet_service_api.feature.payment.infrastructure.persistence.repository.PaymentJpaRepository;
import ayd2.p2b.wallet_service_api.feature.payment.infrastructure.persistence.specification.PaymentSpecification;
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

    @Override
    public PaymentData save(PaymentData payment) {
        PaymentEntity entity = toEntity(payment);
        PaymentEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<PaymentData> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<PaymentData> findByIdempotencyKey(String key) {
        return jpaRepository.findByIdempotencyKey(key).map(this::toDomain);
    }

    @Override
    public PageResponse<PaymentData> findAll(PaymentFilterRequest filter) {
        Specification<PaymentEntity> spec = Specification
                .where(PaymentSpecification.withCongressId(filter.getCongressId()))
                .and(PaymentSpecification.withInstitutionId(filter.getInstitutionId()))
                .and(PaymentSpecification.fromDate(filter.getDateFrom()))
                .and(PaymentSpecification.toDate(filter.getDateTo()));

        PageRequest pageable = PageRequest.of(filter.getPage(), filter.getSize());
        Page<PaymentEntity> page = jpaRepository.findAll(spec, pageable);

        List<PaymentData> items = page.getContent().stream()
                .map(this::toDomain)
                .toList();

        return PageResponse.<PaymentData>builder()
                .items(items)
                .page(page.getNumber())
                .size(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    private PaymentEntity toEntity(PaymentData data) {
        PaymentEntity entity = new PaymentEntity();
        entity.setId(data.getId());
        entity.setUserId(data.getUserId());
        entity.setCongressId(data.getCongressId());
        entity.setInstitutionId(data.getInstitutionId());
        entity.setCongressNameSnapshot(data.getCongressNameSnapshot());
        entity.setInstitutionNameSnapshot(data.getInstitutionNameSnapshot());
        entity.setCommissionPercentSnapshot(data.getCommissionPercentSnapshot());
        entity.setAmount(data.getAmount());
        entity.setCommissionAmount(data.getCommissionAmount());
        entity.setNetAmount(data.getNetAmount());
        entity.setPaymentDate(data.getPaymentDate());
        entity.setIdempotencyKey(data.getIdempotencyKey());
        entity.setCreatedBy(data.getCreatedBy());
        entity.setCreatedAt(data.getCreatedAt());
        return entity;
    }

    private PaymentData toDomain(PaymentEntity entity) {
        return PaymentData.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .congressId(entity.getCongressId())
                .institutionId(entity.getInstitutionId())
                .congressNameSnapshot(entity.getCongressNameSnapshot())
                .institutionNameSnapshot(entity.getInstitutionNameSnapshot())
                .commissionPercentSnapshot(entity.getCommissionPercentSnapshot())
                .amount(entity.getAmount())
                .commissionAmount(entity.getCommissionAmount())
                .netAmount(entity.getNetAmount())
                .paymentDate(entity.getPaymentDate())
                .idempotencyKey(entity.getIdempotencyKey())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
