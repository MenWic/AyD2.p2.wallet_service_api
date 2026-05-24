package ayd2.p2b.wallet_service_api.feature.wallet.application.port;

import ayd2.p2b.wallet_service_api.common.response.PageResponse;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionData;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.internal.TransactionSearchCriteria;

import java.util.UUID;

public interface TransactionRepositoryPort {

    TransactionData save(TransactionData transaction);

    PageResponse<TransactionData> findByUserId(UUID userId, TransactionSearchCriteria criteria);
}
