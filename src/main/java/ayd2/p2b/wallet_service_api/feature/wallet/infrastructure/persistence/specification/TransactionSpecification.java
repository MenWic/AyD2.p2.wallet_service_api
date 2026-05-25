package ayd2.p2b.wallet_service_api.feature.wallet.infrastructure.persistence.specification;

import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionType;
import ayd2.p2b.wallet_service_api.feature.wallet.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public final class TransactionSpecification {

    private TransactionSpecification() {
    }

    public static Specification<TransactionEntity> forUser(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("walletUserId"), userId);
    }

    public static Specification<TransactionEntity> withType(TransactionType type) {
        if (type == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<TransactionEntity> fromDate(LocalDate from) {
        if (from == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("transactionDate"), from);
    }

    public static Specification<TransactionEntity> toDate(LocalDate to) {
        if (to == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("transactionDate"), to);
    }
}
