package ayd2.p2b.wallet_service_api.integration.persistence;

import ayd2.p2b.wallet_service_api.feature.payment.infrastructure.persistence.entity.PaymentEntity;
import ayd2.p2b.wallet_service_api.feature.payment.infrastructure.persistence.repository.PaymentJpaRepository;
import ayd2.p2b.wallet_service_api.feature.report.dto.internal.EarningsByCongressRow;
import ayd2.p2b.wallet_service_api.feature.report.dto.internal.FinancialReportCriteria;
import ayd2.p2b.wallet_service_api.feature.report.infrastructure.persistence.query.JpaFinancialReportQueryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        ayd2.p2b.wallet_service_api.TestcontainersConfiguration.class,
        JpaFinancialReportQueryAdapter.class
})
@TestPropertySource(properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate"
})
class FinancialReportQueryAdapterTest {

    @Autowired
    private PaymentJpaRepository paymentJpaRepository;

    @Autowired
    private JpaFinancialReportQueryAdapter financialReportQueryAdapter;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CONGRESS_A = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID CONGRESS_B = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID CONGRESS_C = UUID.fromString("30000000-0000-0000-0000-000000000003");
    private static final UUID INSTITUTION_1 = UUID.fromString("40000000-0000-0000-0000-000000000001");
    private static final UUID INSTITUTION_2 = UUID.fromString("50000000-0000-0000-0000-000000000002");

    @BeforeEach
    void seed() {
        paymentJpaRepository.deleteAll();

        paymentJpaRepository.save(payment(
                CONGRESS_A,
                INSTITUTION_1,
                "Congress A Snapshot",
                "Institution One Snapshot",
                "100.00",
                "10.00",
                "90.00",
                LocalDate.of(2026, 1, 10),
                "idem-report-001"));

        paymentJpaRepository.save(payment(
                CONGRESS_A,
                INSTITUTION_1,
                "Congress A Snapshot",
                "Institution One Snapshot",
                "150.00",
                "15.00",
                "135.00",
                LocalDate.of(2026, 1, 11),
                "idem-report-002"));

        paymentJpaRepository.save(payment(
                CONGRESS_B,
                INSTITUTION_1,
                "Congress B Snapshot",
                "Institution One Snapshot",
                "80.00",
                "8.00",
                "72.00",
                LocalDate.of(2026, 2, 5),
                "idem-report-003"));

        paymentJpaRepository.save(payment(
                CONGRESS_C,
                INSTITUTION_2,
                "Congress C Snapshot",
                "Institution Two Snapshot",
                "200.00",
                "20.00",
                "180.00",
                LocalDate.of(2026, 3, 15),
                "idem-report-004"));
    }

    @Test
    void should_aggregate_by_institution_and_congress_with_exact_sums_counts_and_snapshot_names() {
        List<EarningsByCongressRow> rows = financialReportQueryAdapter.findEarningsByCongress(
                FinancialReportCriteria.builder().build());

        assertThat(rows).hasSize(3);

        EarningsByCongressRow congressA = findRow(rows, CONGRESS_A, INSTITUTION_1);
        assertThat(congressA.getCongressName()).isEqualTo("Congress A Snapshot");
        assertThat(congressA.getInstitutionName()).isEqualTo("Institution One Snapshot");
        assertThat(congressA.getTotalAmount()).isEqualByComparingTo("250.00");
        assertThat(congressA.getCommissionAmount()).isEqualByComparingTo("25.00");
        assertThat(congressA.getNetAmount()).isEqualByComparingTo("225.00");
        assertThat(congressA.getPaymentCount()).isEqualTo(2L);

        EarningsByCongressRow congressB = findRow(rows, CONGRESS_B, INSTITUTION_1);
        assertThat(congressB.getTotalAmount()).isEqualByComparingTo("80.00");
        assertThat(congressB.getCommissionAmount()).isEqualByComparingTo("8.00");
        assertThat(congressB.getNetAmount()).isEqualByComparingTo("72.00");
        assertThat(congressB.getPaymentCount()).isEqualTo(1L);

        EarningsByCongressRow congressC = findRow(rows, CONGRESS_C, INSTITUTION_2);
        assertThat(congressC.getTotalAmount()).isEqualByComparingTo("200.00");
        assertThat(congressC.getCommissionAmount()).isEqualByComparingTo("20.00");
        assertThat(congressC.getNetAmount()).isEqualByComparingTo("180.00");
        assertThat(congressC.getPaymentCount()).isEqualTo(1L);
    }

    @Test
    void should_filter_by_congress_id() {
        List<EarningsByCongressRow> rows = financialReportQueryAdapter.findEarningsByCongress(
                FinancialReportCriteria.builder()
                        .congressId(CONGRESS_A)
                        .build());

        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().getCongressId()).isEqualTo(CONGRESS_A);
        assertThat(rows.getFirst().getTotalAmount()).isEqualByComparingTo("250.00");
    }

    @Test
    void should_filter_by_institution_id_and_exclude_other_institutions() {
        List<EarningsByCongressRow> rows = financialReportQueryAdapter.findEarningsByCongress(
                FinancialReportCriteria.builder()
                        .institutionId(INSTITUTION_2)
                        .build());

        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().getInstitutionId()).isEqualTo(INSTITUTION_2);
        assertThat(rows.getFirst().getCongressId()).isEqualTo(CONGRESS_C);
    }

    @Test
    void should_apply_date_from_inclusively_and_exclude_older_payments() {
        List<EarningsByCongressRow> rows = financialReportQueryAdapter.findEarningsByCongress(
                FinancialReportCriteria.builder()
                        .dateFrom(LocalDate.of(2026, 2, 5))
                        .build());

        assertThat(rows).hasSize(2);
        assertThat(rows).extracting(EarningsByCongressRow::getCongressId)
                .containsExactlyInAnyOrder(CONGRESS_B, CONGRESS_C);
    }

    @Test
    void should_apply_date_to_inclusively_and_exclude_newer_payments() {
        List<EarningsByCongressRow> rows = financialReportQueryAdapter.findEarningsByCongress(
                FinancialReportCriteria.builder()
                        .dateTo(LocalDate.of(2026, 2, 5))
                        .build());

        assertThat(rows).hasSize(2);
        assertThat(rows).extracting(EarningsByCongressRow::getCongressId)
                .containsExactlyInAnyOrder(CONGRESS_A, CONGRESS_B);
    }

    private EarningsByCongressRow findRow(List<EarningsByCongressRow> rows, UUID congressId, UUID institutionId) {
        return rows.stream()
                .filter(row -> congressId.equals(row.getCongressId()) && institutionId.equals(row.getInstitutionId()))
                .findFirst()
                .orElseThrow();
    }

    private PaymentEntity payment(
            UUID congressId,
            UUID institutionId,
            String congressSnapshot,
            String institutionSnapshot,
            String amount,
            String commission,
            String net,
            LocalDate paymentDate,
            String idempotencyKey) {

        PaymentEntity entity = new PaymentEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(USER_ID);
        entity.setCongressId(congressId);
        entity.setInstitutionId(institutionId);
        entity.setCongressNameSnapshot(congressSnapshot);
        entity.setInstitutionNameSnapshot(institutionSnapshot);
        entity.setCommissionPercentSnapshot(new BigDecimal("10.00"));
        entity.setAmount(new BigDecimal(amount));
        entity.setCommissionAmount(new BigDecimal(commission));
        entity.setNetAmount(new BigDecimal(net));
        entity.setPaymentDate(paymentDate);
        entity.setIdempotencyKey(idempotencyKey);
        entity.setCreatedBy(USER_ID);
        entity.setCreatedAt(Instant.now());
        return entity;
    }
}

