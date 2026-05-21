package ayd2.p2b.wallet_service_api.feature.wallet.domain.model;

import ayd2.p2b.wallet_service_api.common.exception.DomainException;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.exception.InsufficientFundsException;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Getter
public class WalletAccount {

    private final UUID userId;
    private BigDecimal balance;
    private final long version;

    private WalletAccount(UUID userId, BigDecimal balance, long version) {
        this.userId = userId;
        this.balance = balance;
        this.version = version;
    }

    public static WalletAccount create(UUID userId) {
        return new WalletAccount(userId, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), 0L);
    }

    public static WalletAccount reconstitute(UUID userId, BigDecimal balance, long version) {
        return new WalletAccount(userId, balance, version);
    }

    public void credit(BigDecimal amount) {
        requirePositive(amount);
        this.balance = this.balance.add(amount).setScale(2, RoundingMode.HALF_UP);
    }

    public void debit(BigDecimal amount) {
        requirePositive(amount);
        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }
        this.balance = this.balance.subtract(amount).setScale(2, RoundingMode.HALF_UP);
    }

    private void requirePositive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("wallet.amount_must_be_positive");
        }
    }
}
