package ayd2.p2b.wallet_service_api.feature.wallet.infrastructure.persistence.adapter;

import ayd2.p2b.wallet_service_api.common.response.PageResponse;
import ayd2.p2b.wallet_service_api.feature.wallet.application.port.TransactionRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionData;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.internal.TransactionSearchCriteria;
import ayd2.p2b.wallet_service_api.feature.wallet.infrastructure.persistence.entity.TransactionEntity;
import ayd2.p2b.wallet_service_api.feature.wallet.infrastructure.persistence.repository.TransactionJpaRepository;
import ayd2.p2b.wallet_service_api.feature.wallet.infrastructure.persistence.specification.TransactionSpecification;
import ayd2.p2b.wallet_service_api.feature.wallet.mapper.WalletMapper;
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
    private final WalletMapper walletMapper;

    @Override
    public TransactionData save(TransactionData transaction) {
        TransactionEntity entity = walletMapper.toEntity(transaction);
        TransactionEntity saved = jpaRepository.save(entity);
        return walletMapper.toDomain(saved);
    }

    @Override
    public PageResponse<TransactionData> findByUserId(UUID userId, TransactionSearchCriteria criteria) {
        Specification<TransactionEntity> spec = TransactionSpecification.forUser(userId)
                .and(TransactionSpecification.withType(criteria.getTransactionType()))
                .and(TransactionSpecification.fromDate(criteria.getDateFrom()))
                .and(TransactionSpecification.toDate(criteria.getDateTo()));

        PageRequest pageable = PageRequest.of(criteria.getPage(), criteria.getSize());
        Page<TransactionEntity> page = jpaRepository.findAll(spec, pageable);

        List<TransactionData> items = page.getContent().stream()
                .map(walletMapper::toDomain)
                .toList();

        return PageResponse.<TransactionData>builder()
                .items(items)
                .page(page.getNumber())
                .size(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
