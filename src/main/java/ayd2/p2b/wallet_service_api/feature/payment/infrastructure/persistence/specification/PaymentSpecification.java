package ayd2.p2b.wallet_service_api.feature.payment.infrastructure.persistence.specification;

import ayd2.p2b.wallet_service_api.feature.payment.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public final class PaymentSpecification {

    private PaymentSpecification() {
    }

    public static Specification<PaymentEntity> withCongressId(UUID congressId) {
        if (congressId == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.equal(root.get("congressId"), congressId);
    }

    public static Specification<PaymentEntity> withInstitutionId(UUID institutionId) {
        if (institutionId == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.equal(root.get("institutionId"), institutionId);
    }

    public static Specification<PaymentEntity> fromDate(LocalDate from) {
        if (from == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("paymentDate"), from);
    }

    public static Specification<PaymentEntity> toDate(LocalDate to) {
        if (to == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("paymentDate"), to);
    }
}
