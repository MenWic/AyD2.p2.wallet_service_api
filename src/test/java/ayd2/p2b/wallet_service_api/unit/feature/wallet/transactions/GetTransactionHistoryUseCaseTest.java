package ayd2.p2b.wallet_service_api.unit.feature.wallet.transactions;

import ayd2.p2b.wallet_service_api.common.response.PageResponse;
import ayd2.p2b.wallet_service_api.feature.wallet.application.port.TransactionRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.application.transactions.GetTransactionHistoryUseCase;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionData;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionType;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.internal.TransactionSearchCriteria;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.response.TransactionResponse;
import ayd2.p2b.wallet_service_api.feature.wallet.mapper.WalletMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetTransactionHistoryUseCaseTest {

    @Mock
    private TransactionRepositoryPort transactionRepositoryPort;

    @Mock
    private WalletMapper walletMapper;

    private GetTransactionHistoryUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetTransactionHistoryUseCase(transactionRepositoryPort, walletMapper);
    }

    @Test
    void should_return_paged_transactions_when_no_filters() {
        UUID userId = UUID.randomUUID();
        TransactionSearchCriteria criteria = TransactionSearchCriteria.builder()
                .page(0)
                .size(20)
                .build();

        UUID txId1 = UUID.randomUUID();
        UUID txId2 = UUID.randomUUID();

        TransactionData data1 = TransactionData.builder()
                .id(txId1)
                .walletUserId(userId)
                .type(TransactionType.TOP_UP)
                .amount(new BigDecimal("100.00"))
                .transactionDate(LocalDate.now())
                .createdAt(Instant.now())
                .build();

        TransactionData data2 = TransactionData.builder()
                .id(txId2)
                .walletUserId(userId)
                .type(TransactionType.PAYMENT)
                .amount(new BigDecimal("-50.00"))
                .transactionDate(LocalDate.now())
                .createdAt(Instant.now())
                .build();

        PageResponse<TransactionData> dataPage = PageResponse.<TransactionData>builder()
                .items(List.of(data1, data2))
                .page(0)
                .size(20)
                .totalItems(2)
                .totalPages(1)
                .build();

        TransactionResponse response1 = TransactionResponse.builder()
                .id(txId1)
                .type(TransactionType.TOP_UP)
                .amount(new BigDecimal("100.00"))
                .transactionDate(LocalDate.now())
                .createdAt(Instant.now())
                .build();

        TransactionResponse response2 = TransactionResponse.builder()
                .id(txId2)
                .type(TransactionType.PAYMENT)
                .amount(new BigDecimal("-50.00"))
                .transactionDate(LocalDate.now())
                .createdAt(Instant.now())
                .build();

        given(transactionRepositoryPort.findByUserId(userId, criteria)).willReturn(dataPage);
        given(walletMapper.toTransactionResponse(data1)).willReturn(response1);
        given(walletMapper.toTransactionResponse(data2)).willReturn(response2);

        PageResponse<TransactionResponse> result = useCase.execute(userId, criteria);

        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getTotalItems()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }
}
