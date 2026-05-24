package ayd2.p2b.wallet_service_api.feature.wallet.infrastructure.persistence.repository;

import ayd2.p2b.wallet_service_api.feature.wallet.infrastructure.persistence.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WalletJpaRepository extends JpaRepository<WalletEntity, UUID> {

    Optional<WalletEntity> findByUserId(UUID userId);
}
