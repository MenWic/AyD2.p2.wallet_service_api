package ayd2.p2b.wallet_service_api.feature.wallet.application.port;

import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.WalletAccount;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepositoryPort {

    Optional<WalletAccount> findByUserId(UUID userId);

    WalletAccount save(WalletAccount wallet);
}
