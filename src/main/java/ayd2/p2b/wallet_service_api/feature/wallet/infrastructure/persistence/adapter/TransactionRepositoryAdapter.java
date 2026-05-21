package ayd2.p2b.wallet_service_api.feature.wallet.infrastructure.persistence.adapter;

import ayd2.p2b.wallet_service_api.common.response.PageResponse;
import ayd2.p2b.wallet_service_api.feature.wallet.TransactionRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionData;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionType;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.request.TransactionFilterRequest;
import ayd2.p2b.wallet_service_api.feature.wallet.infrastructure.persistence.entity.TransactionEntity;
import ayd2.p2b.wallet_service_api.feature.wallet.infrastructure.persistence.repository.TransactionJpaRepository;
import ayd2.p2b.wallet_service_api.feature.wallet.infrastructure.persistence.specification.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepositoryPort {

    private final TransactionJpaRepository jpaRepository;

    @Override
    public TransactionData save(TransactionData transaction) {
        TransactionEntity entity = toEntity(transaction);
        TransactionEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public PageResponse<TransactionData> findByUserId(UUID userId, TransactionFilterRequest filter) {
        Specification<TransactionEntity> spec = TransactionSpecification.forUser(userId)
                .and(TransactionSpecification.withType(filter.getTransactionType()))
                .and(TransactionSpecification.fromDate(filter.getDateFrom()))
                .and(TransactionSpecification.toDate(filter.getDateTo()));

        PageRequest pageable = PageRequest.of(filter.getPage(), filter.getSize());
        Page<TransactionEntity> page = jpaRepository.findAll(spec, pageable);

        List<TransactionData> items = page.getContent().stream()
                .map(this::toDomain)
                .toList();

        return PageResponse.<TransactionData>builder()
                .items(items)
                .page(page.getNumber())
                .size(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    private TransactionEntity toEntity(TransactionData data) {
        TransactionEntity entity = new TransactionEntity();
        entity.setId(data.getId());
        entity.setWalletUserId(data.getWalletUserId());
        entity.setType(data.getType());
        entity.setAmount(data.getAmount());
        entity.setTransactionDate(data.getTransactionDate());
        entity.setReferencePaymentId(data.getReferencePaymentId());
        entity.setCreatedBy(data.getWalletUserId());
        entity.setCreatedAt(data.getCreatedAt());
        return entity;
    }

    private TransactionData toDomain(TransactionEntity entity) {
        return TransactionData.builder()
                .id(entity.getId())
                .walletUserId(entity.getWalletUserId())
                .type(entity.getType())
                .amount(entity.getAmount())
                .transactionDate(entity.getTransactionDate())
                .referencePaymentId(entity.getReferencePaymentId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
