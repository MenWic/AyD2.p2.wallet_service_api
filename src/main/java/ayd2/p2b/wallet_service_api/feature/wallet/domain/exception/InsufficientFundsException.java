package ayd2.p2b.wallet_service_api.feature.wallet.domain.exception;

import ayd2.p2b.wallet_service_api.common.exception.DomainException;

public class InsufficientFundsException extends DomainException {

    public InsufficientFundsException() {
        super("wallet.insufficient_funds");
    }
}
