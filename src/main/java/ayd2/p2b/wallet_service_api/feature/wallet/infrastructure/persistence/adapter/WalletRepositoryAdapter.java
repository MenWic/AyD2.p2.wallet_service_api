package ayd2.p2b.wallet_service_api.feature.wallet.infrastructure.persistence.adapter;

import ayd2.p2b.wallet_service_api.feature.wallet.WalletRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.WalletAccount;
import ayd2.p2b.wallet_service_api.feature.wallet.infrastructure.persistence.entity.WalletEntity;
import ayd2.p2b.wallet_service_api.feature.wallet.infrastructure.persistence.repository.WalletJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WalletRepositoryAdapter implements WalletRepositoryPort {

    private final WalletJpaRepository jpaRepository;

    @Override
    public Optional<WalletAccount> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId)
                .map(entity -> WalletAccount.reconstitute(
                        entity.getUserId(),
                        entity.getBalance(),
                        entity.getVersion()
                ));
    }

    @Override
    public WalletAccount save(WalletAccount wallet) {
        WalletEntity entity = jpaRepository.findByUserId(wallet.getUserId())
                .orElseGet(() -> {
                    WalletEntity newEntity = new WalletEntity();
                    newEntity.setUserId(wallet.getUserId());
                    newEntity.setCreatedAt(Instant.now());
                    return newEntity;
                });

        entity.setBalance(wallet.getBalance());
        entity.setUpdatedAt(Instant.now());

        WalletEntity saved = jpaRepository.save(entity);
        return WalletAccount.reconstitute(saved.getUserId(), saved.getBalance(), saved.getVersion());
    }
}
