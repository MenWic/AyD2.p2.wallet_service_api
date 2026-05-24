package ayd2.p2b.wallet_service_api.feature.wallet.application.top_up;

import ayd2.p2b.wallet_service_api.common.dto.internal.RequesterContext;
import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.feature.wallet.application.port.TransactionRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.application.port.WalletRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionData;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionType;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.WalletAccount;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.internal.TopUpCommand;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.response.WalletBalanceResponse;
import ayd2.p2b.wallet_service_api.feature.wallet.mapper.WalletMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TopUpWalletUseCase {

    private final WalletRepositoryPort walletRepositoryPort;
    private final TransactionRepositoryPort transactionRepositoryPort;
    private final WalletMapper walletMapper;

    @Transactional
    public WalletBalanceResponse execute(RequesterContext requester, TopUpCommand command) {
        UUID userId = requester.getUserId();

        WalletAccount wallet = walletRepositoryPort.findByUserId(userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "resource.not_found",
                        "Wallet not found for user: " + userId));

        wallet.credit(command.getAmount());
        WalletAccount savedWallet = walletRepositoryPort.save(wallet);

        TransactionData transaction = TransactionData.builder()
                .id(UUID.randomUUID())
                .walletUserId(userId)
                .type(TransactionType.TOP_UP)
                .amount(command.getAmount())
                .transactionDate(command.getPaymentDate())
                .createdBy(requester.getUserId())
                .createdAt(Instant.now())
                .build();

        transactionRepositoryPort.save(transaction);

        return walletMapper.toBalanceResponse(savedWallet);
    }
}
