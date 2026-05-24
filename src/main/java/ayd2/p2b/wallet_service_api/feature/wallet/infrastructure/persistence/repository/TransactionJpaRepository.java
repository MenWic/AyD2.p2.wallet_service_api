package ayd2.p2b.wallet_service_api.feature.wallet.infrastructure.persistence.repository;

import ayd2.p2b.wallet_service_api.feature.wallet.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID>,
        JpaSpecificationExecutor<TransactionEntity> {
}
