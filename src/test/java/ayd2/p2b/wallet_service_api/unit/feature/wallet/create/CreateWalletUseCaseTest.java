package ayd2.p2b.wallet_service_api.unit.feature.wallet.create;

import ayd2.p2b.wallet_service_api.feature.wallet.WalletRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.application.create.CreateWalletUseCase;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CreateWalletUseCaseTest {

    @Mock
    private WalletRepositoryPort walletRepositoryPort;

    @Mock
    private WalletMapper walletMapper;

    private CreateWalletUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateWalletUseCase(walletRepositoryPort, walletMapper);
    }

    @Test
    void should_create_and_return_wallet_when_not_exists() {
        UUID userId = UUID.randomUUID();
        WalletAccount created = WalletAccount.create(userId);
        WalletBalanceResponse expected = WalletBalanceResponse.builder()
                .userId(userId).balance(BigDecimal.ZERO).build();

        given(walletRepositoryPort.findByUserId(userId)).willReturn(Optional.empty());
        given(walletRepositoryPort.save(any())).willReturn(created);
        given(walletMapper.toBalanceResponse(created)).willReturn(expected);

        WalletBalanceResponse result = useCase.execute(userId);

        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void should_return_existing_wallet_without_saving_when_already_exists() {
        UUID userId = UUID.randomUUID();
        WalletAccount existing = WalletAccount.reconstitute(userId, new BigDecimal("50.00"), 2L);
        WalletBalanceResponse expected = WalletBalanceResponse.builder()
                .userId(userId).balance(new BigDecimal("50.00")).build();

        given(walletRepositoryPort.findByUserId(userId)).willReturn(Optional.of(existing));
        given(walletMapper.toBalanceResponse(existing)).willReturn(expected);

        WalletBalanceResponse result = useCase.execute(userId);

        assertThat(result.getBalance()).isEqualByComparingTo("50.00");
        then(walletRepositoryPort).should().findByUserId(userId);
        then(walletRepositoryPort).shouldHaveNoMoreInteractions();
    }
}
