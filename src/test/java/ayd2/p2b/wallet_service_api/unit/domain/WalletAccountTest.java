package ayd2.p2b.wallet_service_api.unit.domain;

import ayd2.p2b.wallet_service_api.common.exception.DomainException;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.exception.InsufficientFundsException;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.WalletAccount;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WalletAccountTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Test
    void should_create_wallet_with_zero_balance() {
        WalletAccount wallet = WalletAccount.create(USER_ID);

        assertThat(wallet.getUserId()).isEqualTo(USER_ID);
        assertThat(wallet.getBalance()).isEqualByComparingTo("0.00");
        assertThat(wallet.getVersion()).isZero();
    }

    @Test
    void should_reconstitute_wallet_with_existing_balance() {
        WalletAccount wallet = WalletAccount.reconstitute(USER_ID, new BigDecimal("150.50"), 3L);

        assertThat(wallet.getBalance()).isEqualByComparingTo("150.50");
        assertThat(wallet.getVersion()).isEqualTo(3L);
    }

    @Test
    void should_credit_amount_to_balance() {
        WalletAccount wallet = WalletAccount.create(USER_ID);

        wallet.credit(new BigDecimal("100.00"));

        assertThat(wallet.getBalance()).isEqualByComparingTo("100.00");
    }

    @Test
    void should_accumulate_multiple_credits() {
        WalletAccount wallet = WalletAccount.create(USER_ID);

        wallet.credit(new BigDecimal("50.00"));
        wallet.credit(new BigDecimal("25.50"));

        assertThat(wallet.getBalance()).isEqualByComparingTo("75.50");
    }

    @Test
    void should_reject_credit_when_amount_is_zero() {
        WalletAccount wallet = WalletAccount.create(USER_ID);

        assertThatThrownBy(() -> wallet.credit(BigDecimal.ZERO))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("wallet.amount_must_be_positive");
    }

    @Test
    void should_reject_credit_when_amount_is_negative() {
        WalletAccount wallet = WalletAccount.create(USER_ID);

        assertThatThrownBy(() -> wallet.credit(new BigDecimal("-1.00")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("wallet.amount_must_be_positive");
    }

    @Test
    void should_debit_amount_from_balance() {
        WalletAccount wallet = WalletAccount.reconstitute(USER_ID, new BigDecimal("200.00"), 1L);

        wallet.debit(new BigDecimal("75.00"));

        assertThat(wallet.getBalance()).isEqualByComparingTo("125.00");
    }

    @Test
    void should_allow_debit_that_empties_balance() {
        WalletAccount wallet = WalletAccount.reconstitute(USER_ID, new BigDecimal("100.00"), 1L);

        wallet.debit(new BigDecimal("100.00"));

        assertThat(wallet.getBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    void should_throw_insufficient_funds_when_debit_exceeds_balance() {
        WalletAccount wallet = WalletAccount.reconstitute(USER_ID, new BigDecimal("50.00"), 1L);

        assertThatThrownBy(() -> wallet.debit(new BigDecimal("50.01")))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void should_reject_debit_when_amount_is_zero() {
        WalletAccount wallet = WalletAccount.reconstitute(USER_ID, new BigDecimal("100.00"), 1L);

        assertThatThrownBy(() -> wallet.debit(BigDecimal.ZERO))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("wallet.amount_must_be_positive");
    }

    @Test
    void should_reject_debit_when_amount_is_negative() {
        WalletAccount wallet = WalletAccount.reconstitute(USER_ID, new BigDecimal("100.00"), 1L);

        assertThatThrownBy(() -> wallet.debit(new BigDecimal("-10.00")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("wallet.amount_must_be_positive");
    }
}
