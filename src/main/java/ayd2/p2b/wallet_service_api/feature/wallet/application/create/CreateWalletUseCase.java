package ayd2.p2b.wallet_service_api.feature.wallet.application.create;

import ayd2.p2b.wallet_service_api.feature.wallet.WalletRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.WalletAccount;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.response.WalletBalanceResponse;
import ayd2.p2b.wallet_service_api.feature.wallet.mapper.WalletMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateWalletUseCase {

    private final WalletRepositoryPort walletRepositoryPort;
    private final WalletMapper walletMapper;

    @Transactional
    public WalletBalanceResponse execute(UUID userId) {
        Optional<WalletAccount> existing = walletRepositoryPort.findByUserId(userId);
        if (existing.isPresent()) {
            return walletMapper.toBalanceResponse(existing.get());
        }
        WalletAccount newWallet = WalletAccount.create(userId);
        WalletAccount saved = walletRepositoryPort.save(newWallet);
        return walletMapper.toBalanceResponse(saved);
    }
}
