package ayd2.p2b.wallet_service_api.feature.wallet.application.balance;

import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.feature.wallet.application.port.WalletRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.WalletAccount;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.response.WalletBalanceResponse;
import ayd2.p2b.wallet_service_api.feature.wallet.mapper.WalletMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetWalletBalanceUseCase {

    private final WalletRepositoryPort walletRepositoryPort;
    private final WalletMapper walletMapper;

    @Transactional(readOnly = true)
    public WalletBalanceResponse execute(UUID userId) {
        WalletAccount wallet = walletRepositoryPort.findByUserId(userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "resource.not_found",
                        "Wallet not found for user: " + userId
                ));
        return walletMapper.toBalanceResponse(wallet);
    }
}
