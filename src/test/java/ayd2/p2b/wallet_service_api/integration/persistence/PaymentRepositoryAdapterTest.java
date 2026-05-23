package ayd2.p2b.wallet_service_api.integration.persistence;

import ayd2.p2b.wallet_service_api.feature.payment.infrastructure.persistence.entity.PaymentEntity;
import ayd2.p2b.wallet_service_api.feature.payment.infrastructure.persistence.repository.PaymentJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.TestPropertySource;

import ayd2.p2b.wallet_service_api.feature.payment.infrastructure.persistence.specification.PaymentSpecification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ayd2.p2b.wallet_service_api.TestcontainersConfiguration.class)
@TestPropertySource(properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate"
})
class PaymentRepositoryAdapterTest {

    @Autowired
    private PaymentJpaRepository repository;

    private static final UUID USER_A = UUID.fromString("aa000000-0000-0000-0000-000000000001");
    private static final UUID USER_B = UUID.fromString("bb000000-0000-0000-0000-000000000002");
    private static final UUID CONGRESS_1 = UUID.fromString("cc000000-0000-0000-0000-000000000001");
    private static final UUID CONGRESS_2 = UUID.fromString("cc000000-0000-0000-0000-000000000002");
    private static final UUID INSTITUTION_1 = UUID.fromString("dd000000-0000-0000-0000-000000000001");
    private static final UUID INSTITUTION_2 = UUID.fromString("dd000000-0000-0000-0000-000000000002");

    @BeforeEach
    void seedPayments() {
        repository.deleteAll();

        // Payment 1: congress1, institution1, date 2026-01-10
        repository.save(buildPayment(
                UUID.randomUUID(), USER_A, CONGRESS_1, INSTITUTION_1,
                "Congress A", "Inst 1",
                new BigDecimal("100.00"), LocalDate.of(2026, 1, 10),
                "idem-test-001"));

        // Payment 2: congress2, institution1, date 2026-03-20
        repository.save(buildPayment(
                UUID.randomUUID(), USER_A, CONGRESS_2, INSTITUTION_1,
                "Congress B", "Inst 1",
                new BigDecimal("150.00"), LocalDate.of(2026, 3, 20),
                "idem-test-002"));

        // Payment 3: congress1, institution2, date 2026-06-05
        repository.save(buildPayment(
                UUID.randomUUID(), USER_B, CONGRESS_1, INSTITUTION_2,
                "Congress A", "Inst 2",
                new BigDecimal("200.00"), LocalDate.of(2026, 6, 5),
                "idem-test-003"));
    }

    @Test
    void should_filter_by_congress_id_and_return_only_matching_payments() {
        Specification<PaymentEntity> spec = PaymentSpecification.withCongressId(CONGRESS_1);
        Page<PaymentEntity> page = repository.findAll(spec, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).allMatch(p -> p.getCongressId().equals(CONGRESS_1));
    }

    @Test
    void should_return_all_payments_when_congress_id_filter_is_null() {
        Specification<PaymentEntity> spec = PaymentSpecification.withCongressId(null);
        Page<PaymentEntity> page = repository.findAll(spec, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    void should_filter_by_institution_id_and_return_only_matching_payments() {
        Specification<PaymentEntity> spec = PaymentSpecification.withInstitutionId(INSTITUTION_2);
        Page<PaymentEntity> page = repository.findAll(spec, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getInstitutionId()).isEqualTo(INSTITUTION_2);
    }

    @Test
    void should_filter_by_date_from_and_exclude_earlier_payments() {
        Specification<PaymentEntity> spec = PaymentSpecification.fromDate(LocalDate.of(2026, 3, 1));
        Page<PaymentEntity> page = repository.findAll(spec, PageRequest.of(0, 10));

        // Payments on 2026-03-20 and 2026-06-05 qualify; 2026-01-10 does not
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).allMatch(p -> !p.getPaymentDate().isBefore(LocalDate.of(2026, 3, 1)));
    }

    @Test
    void should_filter_by_date_to_and_exclude_later_payments() {
        Specification<PaymentEntity> spec = PaymentSpecification.toDate(LocalDate.of(2026, 3, 31));
        Page<PaymentEntity> page = repository.findAll(spec, PageRequest.of(0, 10));

        // Payments on 2026-01-10 and 2026-03-20 qualify; 2026-06-05 does not
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).allMatch(p -> !p.getPaymentDate().isAfter(LocalDate.of(2026, 3, 31)));
    }

    @Test
    void should_apply_combined_date_range_filter_correctly() {
        Specification<PaymentEntity> spec = Specification
                .where(PaymentSpecification.fromDate(LocalDate.of(2026, 1, 15)))
                .and(PaymentSpecification.toDate(LocalDate.of(2026, 5, 31)));

        Page<PaymentEntity> page = repository.findAll(spec, PageRequest.of(0, 10));

        // Only 2026-03-20 falls inside [2026-01-15, 2026-05-31]
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getPaymentDate()).isEqualTo(LocalDate.of(2026, 3, 20));
    }

    @Test
    void should_enforce_idempotency_key_uniqueness() {
        PaymentEntity duplicate = buildPayment(
                UUID.randomUUID(), USER_A, CONGRESS_1, INSTITUTION_1,
                "Congress A", "Inst 1",
                new BigDecimal("100.00"), LocalDate.of(2026, 7, 1),
                "idem-test-001"); // same key as payment 1

        assertThatThrownBy(() -> repository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void should_find_payment_by_idempotency_key() {
        Optional<PaymentEntity> result = repository.findByIdempotencyKey("idem-test-002");

        assertThat(result).isPresent();
        assertThat(result.get().getCongressId()).isEqualTo(CONGRESS_2);
    }

    // --- fixture ---

    private PaymentEntity buildPayment(
            UUID id, UUID userId, UUID congressId, UUID institutionId,
            String congressName, String institutionName,
            BigDecimal amount, LocalDate paymentDate, String idempotencyKey) {

        BigDecimal commission = amount.multiply(new BigDecimal("0.10")).setScale(2);
        BigDecimal net = amount.subtract(commission);

        PaymentEntity entity = new PaymentEntity();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setCongressId(congressId);
        entity.setInstitutionId(institutionId);
        entity.setCongressNameSnapshot(congressName);
        entity.setInstitutionNameSnapshot(institutionName);
        entity.setCommissionPercentSnapshot(new BigDecimal("10.00"));
        entity.setAmount(amount);
        entity.setCommissionAmount(commission);
        entity.setNetAmount(net);
        entity.setPaymentDate(paymentDate);
        entity.setIdempotencyKey(idempotencyKey);
        entity.setCreatedBy(UUID.randomUUID());
        entity.setCreatedAt(Instant.now());
        return entity;
    }
}
