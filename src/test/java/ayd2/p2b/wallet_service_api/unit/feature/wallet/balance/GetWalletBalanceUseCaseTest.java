package ayd2.p2b.wallet_service_api.unit.feature.wallet.balance;

import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.feature.wallet.application.port.WalletRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.application.balance.GetWalletBalanceUseCase;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.WalletAccount;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.response.WalletBalanceResponse;
import ayd2.p2b.wallet_service_api.feature.wallet.mapper.WalletMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetWalletBalanceUseCaseTest {

    @Mock
    private WalletRepositoryPort walletRepositoryPort;

    @Mock
    private WalletMapper walletMapper;

    private GetWalletBalanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetWalletBalanceUseCase(walletRepositoryPort, walletMapper);
    }

    @Test
    void should_return_balance_when_wallet_exists() {
        UUID userId = UUID.randomUUID();
        WalletAccount wallet = WalletAccount.reconstitute(userId, new BigDecimal("75.50"), 1L);
        WalletBalanceResponse expected = WalletBalanceResponse.builder()
                .userId(userId)
                .balance(new BigDecimal("75.50"))
                .build();

        given(walletRepositoryPort.findByUserId(userId)).willReturn(Optional.of(wallet));
        given(walletMapper.toBalanceResponse(wallet)).willReturn(expected);

        WalletBalanceResponse result = useCase.execute(userId);

        assertThat(result.getBalance()).isEqualByComparingTo("75.50");
    }

    @Test
    void should_throw_when_wallet_not_found() {
        UUID userId = UUID.randomUUID();

        given(walletRepositoryPort.findByUserId(userId)).willReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(userId));
        assertThat(ex.getCode()).isEqualTo("resource.not_found");
    }
}
