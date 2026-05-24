package ayd2.p2b.wallet_service_api.feature.wallet.application.transactions;

import ayd2.p2b.wallet_service_api.common.response.PageResponse;
import ayd2.p2b.wallet_service_api.feature.wallet.application.port.TransactionRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionData;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.internal.TransactionSearchCriteria;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.response.TransactionResponse;
import ayd2.p2b.wallet_service_api.feature.wallet.mapper.WalletMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetTransactionHistoryUseCase {

    private final TransactionRepositoryPort transactionRepositoryPort;
    private final WalletMapper walletMapper;

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> execute(UUID userId, TransactionSearchCriteria criteria) {
        PageResponse<TransactionData> dataPage = transactionRepositoryPort.findByUserId(userId, criteria);
        List<TransactionResponse> mapped = dataPage.getItems().stream()
                .map(walletMapper::toTransactionResponse)
                .toList();
        return PageResponse.<TransactionResponse>builder()
                .items(mapped)
                .page(dataPage.getPage())
                .size(dataPage.getSize())
                .totalItems(dataPage.getTotalItems())
                .totalPages(dataPage.getTotalPages())
                .build();
    }
}
