package ayd2.p2b.wallet_service_api.feature.wallet.application.topup;

import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.feature.wallet.TransactionRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.WalletRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionData;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionType;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.WalletAccount;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.request.TopUpRequest;
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
        public WalletBalanceResponse execute(UUID userId, TopUpRequest request) {
                return execute(userId, userId, request);
        }

        @Transactional
        public WalletBalanceResponse execute(UUID userId, UUID callerId, TopUpRequest request) {
                WalletAccount wallet = walletRepositoryPort.findByUserId(userId)
                                .orElseThrow(() -> new ApiException(
                                                HttpStatus.NOT_FOUND,
                                                "resource.not_found",
                                                "Wallet not found for user: " + userId));

                wallet.credit(request.getAmount());
                WalletAccount savedWallet = walletRepositoryPort.save(wallet);

                TransactionData transaction = TransactionData.builder()
                                .id(UUID.randomUUID())
                                .walletUserId(userId)
                                .type(TransactionType.TOP_UP)
                                .amount(request.getAmount())
                                .transactionDate(request.getTransactionDate())
                                .createdBy(callerId)
                                .createdAt(Instant.now())
                                .build();

                transactionRepositoryPort.save(transaction);

                return walletMapper.toBalanceResponse(savedWallet);
        }
}
