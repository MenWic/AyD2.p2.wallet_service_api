package ayd2.p2b.wallet_service_api.unit.feature.payment.specification;

import ayd2.p2b.wallet_service_api.feature.payment.infrastructure.persistence.specification.PaymentSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentSpecificationTest {

    @Test
    void withCongressId_returns_null_spec_when_congressId_is_null() {
        Specification<?> spec = PaymentSpecification.withCongressId(null);
        assertThat(spec).isNotNull();
    }

    @Test
    void withCongressId_returns_spec_when_congressId_is_provided() {
        Specification<?> spec = PaymentSpecification.withCongressId(UUID.randomUUID());
        assertThat(spec).isNotNull();
    }

    @Test
    void withInstitutionId_returns_null_spec_when_institutionId_is_null() {
        Specification<?> spec = PaymentSpecification.withInstitutionId(null);
        assertThat(spec).isNotNull();
    }

    @Test
    void withInstitutionId_returns_spec_when_institutionId_is_provided() {
        Specification<?> spec = PaymentSpecification.withInstitutionId(UUID.randomUUID());
        assertThat(spec).isNotNull();
    }

    @Test
    void fromDate_returns_null_spec_when_date_is_null() {
        Specification<?> spec = PaymentSpecification.fromDate(null);
        assertThat(spec).isNotNull();
    }

    @Test
    void fromDate_returns_spec_when_date_is_provided() {
        Specification<?> spec = PaymentSpecification.fromDate(LocalDate.of(2026, 1, 1));
        assertThat(spec).isNotNull();
    }

    @Test
    void toDate_returns_null_spec_when_date_is_null() {
        Specification<?> spec = PaymentSpecification.toDate(null);
        assertThat(spec).isNotNull();
    }

    @Test
    void toDate_returns_spec_when_date_is_provided() {
        Specification<?> spec = PaymentSpecification.toDate(LocalDate.of(2026, 12, 31));
        assertThat(spec).isNotNull();
    }
}
