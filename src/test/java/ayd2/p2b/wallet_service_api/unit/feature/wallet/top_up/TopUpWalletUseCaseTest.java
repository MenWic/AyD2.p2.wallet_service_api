package ayd2.p2b.wallet_service_api.unit.feature.wallet.top_up;

import ayd2.p2b.wallet_service_api.common.dto.internal.RequesterContext;
import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.common.exception.DomainException;
import ayd2.p2b.wallet_service_api.feature.wallet.application.port.TransactionRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.application.port.WalletRepositoryPort;
import ayd2.p2b.wallet_service_api.feature.wallet.application.top_up.TopUpWalletUseCase;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionData;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.WalletAccount;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.internal.TopUpCommand;
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
import java.util.Set;
import java.util.UUID;

import org.mockito.ArgumentCaptor;

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
        RequesterContext requester = new RequesterContext(userId, Set.of("PARTICIPANT"));
        WalletAccount wallet = WalletAccount.reconstitute(userId, BigDecimal.ZERO, 0L);
        TopUpCommand command = TopUpCommand.builder()
                .amount(new BigDecimal("50.00"))
                .paymentDate(LocalDate.now())
                .build();
        WalletBalanceResponse expected = WalletBalanceResponse.builder()
                .userId(userId)
                .balance(new BigDecimal("50.00"))
                .build();

        given(walletRepositoryPort.findByUserId(userId)).willReturn(Optional.of(wallet));
        given(walletRepositoryPort.save(any())).willReturn(wallet);
        given(transactionRepositoryPort.save(any())).willReturn(TransactionData.builder().build());
        given(walletMapper.toBalanceResponse(any())).willReturn(expected);

        WalletBalanceResponse result = useCase.execute(requester, command);

        assertThat(result.getBalance()).isEqualByComparingTo("50.00");
        then(walletRepositoryPort).should().save(any());
        then(transactionRepositoryPort).should().save(any());
    }

    @Test
    void should_throw_domain_exception_when_amount_is_negative() {
        UUID userId = UUID.randomUUID();
        RequesterContext requester = new RequesterContext(userId, Set.of("PARTICIPANT"));
        WalletAccount wallet = WalletAccount.reconstitute(userId, BigDecimal.ZERO, 0L);
        TopUpCommand command = TopUpCommand.builder()
                .amount(new BigDecimal("-10.00"))
                .paymentDate(LocalDate.now())
                .build();

        given(walletRepositoryPort.findByUserId(userId)).willReturn(Optional.of(wallet));

        assertThrows(DomainException.class, () -> useCase.execute(requester, command));
    }

    @Test
    void should_throw_when_wallet_not_found() {
        UUID userId = UUID.randomUUID();
        RequesterContext requester = new RequesterContext(userId, Set.of("PARTICIPANT"));
        TopUpCommand command = TopUpCommand.builder()
                .amount(new BigDecimal("50.00"))
                .paymentDate(LocalDate.now())
                .build();

        given(walletRepositoryPort.findByUserId(userId)).willReturn(Optional.empty());

        assertThrows(ApiException.class, () -> useCase.execute(requester, command));
    }

    @Test
    void should_set_created_by_to_requester_user_id() {
        UUID userId = UUID.randomUUID();
        RequesterContext requester = new RequesterContext(userId, Set.of("PARTICIPANT"));
        WalletAccount wallet = WalletAccount.reconstitute(userId, BigDecimal.ZERO, 0L);
        TopUpCommand command = TopUpCommand.builder()
                .amount(new BigDecimal("50.00"))
                .paymentDate(LocalDate.now())
                .build();
        WalletBalanceResponse expected = WalletBalanceResponse.builder()
                .userId(userId)
                .balance(new BigDecimal("50.00"))
                .build();

        given(walletRepositoryPort.findByUserId(userId)).willReturn(Optional.of(wallet));
        given(walletRepositoryPort.save(any())).willReturn(wallet);
        given(transactionRepositoryPort.save(any())).willReturn(TransactionData.builder().build());
        given(walletMapper.toBalanceResponse(any())).willReturn(expected);

        useCase.execute(requester, command);

        then(transactionRepositoryPort).should().save(argThat(t -> requester.getUserId().equals(t.getCreatedBy())));
    }

    @Test
    void should_set_transaction_date_from_command_payment_date() {
        LocalDate expectedDate = LocalDate.of(2026, 6, 15);
        UUID userId = UUID.randomUUID();
        RequesterContext requester = new RequesterContext(userId, Set.of("PARTICIPANT"));
        WalletAccount wallet = WalletAccount.reconstitute(userId, BigDecimal.ZERO, 0L);
        TopUpCommand command = TopUpCommand.builder()
                .amount(new BigDecimal("100.00"))
                .paymentDate(expectedDate)
                .build();
        WalletBalanceResponse expected = WalletBalanceResponse.builder()
                .userId(userId)
                .balance(new BigDecimal("100.00"))
                .build();

        given(walletRepositoryPort.findByUserId(userId)).willReturn(Optional.of(wallet));
        given(walletRepositoryPort.save(any())).willReturn(wallet);
        given(transactionRepositoryPort.save(any())).willReturn(TransactionData.builder().build());
        given(walletMapper.toBalanceResponse(any())).willReturn(expected);

        useCase.execute(requester, command);

        ArgumentCaptor<TransactionData> captor = ArgumentCaptor.forClass(TransactionData.class);
        then(transactionRepositoryPort).should().save(captor.capture());

        TransactionData saved = captor.getValue();
        assertThat(saved.getTransactionDate()).isEqualTo(expectedDate);
        assertThat(saved.getAmount()).isEqualByComparingTo("100.00");
        assertThat(saved.getWalletUserId()).isEqualTo(userId);
    }
}
