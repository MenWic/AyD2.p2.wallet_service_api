package ayd2.p2b.wallet_service_api.unit.feature.wallet.topup;

import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.common.exception.DomainException;
import ayd2.p2b.wallet_service_api.feature.wallet.TransactionRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.WalletRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.application.topup.TopUpWalletUseCase;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionData;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.WalletAccount;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.request.TopUpRequest;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.response.WalletBalanceResponse;
import ayd2.p2b.wallet_service_api.feature.wallet.mapper.WalletMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TopUpWalletUseCaseTest {

    @Mock
    private WalletRepositoryPort walletRepositoryPort;

    @Mock
    private TransactionRepositoryPort transactionRepositoryPort;

    @Mock
    private WalletMapper walletMapper;

    private TopUpWalletUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new TopUpWalletUseCase(walletRepositoryPort, transactionRepositoryPort, walletMapper);
    }

    @Test
    void should_credit_wallet_and_create_transaction_when_top_up() {
        UUID userId = UUID.randomUUID();
        WalletAccount wallet = WalletAccount.reconstitute(userId, BigDecimal.ZERO, 0L);
        TopUpRequest request = TopUpRequest.builder()
                .amount(new BigDecimal("50.00"))
                .transactionDate(LocalDate.now())
                .build();
        WalletBalanceResponse expected = WalletBalanceResponse.builder()
                .userId(userId)
                .balance(new BigDecimal("50.00"))
                .build();

        given(walletRepositoryPort.findByUserId(userId)).willReturn(Optional.of(wallet));
        given(walletRepositoryPort.save(any())).willReturn(wallet);
        given(transactionRepositoryPort.save(any())).willReturn(TransactionData.builder().build());
        given(walletMapper.toBalanceResponse(any())).willReturn(expected);

        WalletBalanceResponse result = useCase.execute(userId, request);

        assertThat(result.getBalance()).isEqualByComparingTo("50.00");
        then(walletRepositoryPort).should().save(any());
        then(transactionRepositoryPort).should().save(any());
    }

    @Test
    void should_throw_domain_exception_when_amount_is_negative() {
        UUID userId = UUID.randomUUID();
        WalletAccount wallet = WalletAccount.reconstitute(userId, BigDecimal.ZERO, 0L);
        TopUpRequest request = TopUpRequest.builder()
                .amount(new BigDecimal("-10.00"))
                .transactionDate(LocalDate.now())
                .build();

        given(walletRepositoryPort.findByUserId(userId)).willReturn(Optional.of(wallet));

        assertThrows(DomainException.class, () -> useCase.execute(userId, request));
    }

    @Test
    void should_throw_when_wallet_not_found() {
        UUID userId = UUID.randomUUID();
        TopUpRequest request = TopUpRequest.builder()
                .amount(new BigDecimal("50.00"))
                .transactionDate(LocalDate.now())
                .build();

        given(walletRepositoryPort.findByUserId(userId)).willReturn(Optional.empty());

        assertThrows(ApiException.class, () -> useCase.execute(userId, request));
    }

    @Test
    void should_set_created_by_to_caller_id_not_wallet_owner_id() {
        UUID walletOwnerId = UUID.randomUUID();
        UUID callerId = UUID.randomUUID();
        WalletAccount wallet = WalletAccount.reconstitute(walletOwnerId, BigDecimal.ZERO, 0L);
        TopUpRequest request = TopUpRequest.builder()
                .amount(new BigDecimal("50.00"))
                .transactionDate(LocalDate.now())
                .build();
        WalletBalanceResponse expected = WalletBalanceResponse.builder()
                .userId(walletOwnerId)
                .balance(new BigDecimal("50.00"))
                .build();

        given(walletRepositoryPort.findByUserId(walletOwnerId)).willReturn(Optional.of(wallet));
        given(walletRepositoryPort.save(any())).willReturn(wallet);
        given(transactionRepositoryPort.save(any())).willReturn(TransactionData.builder().build());
        given(walletMapper.toBalanceResponse(any())).willReturn(expected);

        useCase.execute(walletOwnerId, callerId, request);

        then(transactionRepositoryPort).should().save(argThat(t -> callerId.equals(t.getCreatedBy())));
    }
}
