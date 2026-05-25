package ayd2.p2b.wallet_service_api.unit.feature.report.platform_earnings;

import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.feature.report.application.platform_earnings.GetPlatformEarningsReportUseCase;
import ayd2.p2b.wallet_service_api.feature.report.application.port.FinancialReportQueryPort;
import ayd2.p2b.wallet_service_api.feature.report.dto.internal.EarningsByCongressRow;
import ayd2.p2b.wallet_service_api.feature.report.dto.internal.FinancialReportCriteria;
import ayd2.p2b.wallet_service_api.feature.report.dto.response.PlatformEarningsReportResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GetPlatformEarningsReportUseCaseTest {

    @Mock
    private FinancialReportQueryPort financialReportQueryPort;

    private GetPlatformEarningsReportUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetPlatformEarningsReportUseCase(financialReportQueryPort);
    }

    @Test
    void should_return_empty_items_and_zero_totals_when_query_returns_no_rows() {
        FinancialReportCriteria criteria = FinancialReportCriteria.builder().build();
        given(financialReportQueryPort.findEarningsByCongress(criteria)).willReturn(List.of());

        PlatformEarningsReportResponse result = useCase.execute(criteria);

        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotalItems()).isZero();
        assertThat(result.getGrandTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getGrandTotalCommission()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getGrandTotalNet()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void should_group_by_institution_calculate_totals_sort_and_compute_grand_totals() {
        UUID institutionAlpha = UUID.fromString("00000000-0000-0000-0000-000000000111");
        UUID institutionBeta = UUID.fromString("00000000-0000-0000-0000-000000000222");

        FinancialReportCriteria criteria = FinancialReportCriteria.builder().build();
        List<EarningsByCongressRow> rows = List.of(
                row("Zeta Congress", institutionAlpha, "Alpha Institute", "150.00", "20.00", "130.00", 2),
                row("Alpha Congress", institutionAlpha, "Alpha Institute", "200.00", "20.00", "180.00", 3),
                row("Beta Congress", institutionBeta, "Beta University", "300.00", "50.00", "250.00", 4),
                row("Gamma Congress", institutionBeta, "Beta University", "120.00", "10.00", "110.00", 1));
        given(financialReportQueryPort.findEarningsByCongress(criteria)).willReturn(rows);

        PlatformEarningsReportResponse result = useCase.execute(criteria);

        assertThat(result.getTotalItems()).isEqualTo(2);
        assertThat(result.getGrandTotalAmount()).isEqualByComparingTo("770.00");
        assertThat(result.getGrandTotalCommission()).isEqualByComparingTo("100.00");
        assertThat(result.getGrandTotalNet()).isEqualByComparingTo("670.00");

        assertThat(result.getItems().get(0).getInstitutionName()).isEqualTo("Alpha Institute");
        assertThat(result.getItems().get(1).getInstitutionName()).isEqualTo("Beta University");

        assertThat(result.getItems().get(0).getInstitutionTotalAmount()).isEqualByComparingTo("350.00");
        assertThat(result.getItems().get(0).getInstitutionTotalCommission()).isEqualByComparingTo("40.00");
        assertThat(result.getItems().get(0).getInstitutionTotalNet()).isEqualByComparingTo("310.00");
        assertThat(result.getItems().get(0).getPaymentCount()).isEqualTo(5L);

        assertThat(result.getItems().get(0).getCongresses().get(0).getCongressName()).isEqualTo("Alpha Congress");
        assertThat(result.getItems().get(0).getCongresses().get(1).getCongressName()).isEqualTo("Zeta Congress");

        assertThat(result.getItems().get(1).getInstitutionTotalAmount()).isEqualByComparingTo("420.00");
        assertThat(result.getItems().get(1).getInstitutionTotalCommission()).isEqualByComparingTo("60.00");
        assertThat(result.getItems().get(1).getInstitutionTotalNet()).isEqualByComparingTo("360.00");
        assertThat(result.getItems().get(1).getPaymentCount()).isEqualTo(5L);

        assertThat(result.getItems().get(1).getCongresses().get(0).getCongressName()).isEqualTo("Beta Congress");
        assertThat(result.getItems().get(1).getCongresses().get(1).getCongressName()).isEqualTo("Gamma Congress");
    }

    @Test
    void should_pass_institution_and_date_filters_through_to_query_port() {
        FinancialReportCriteria criteria = FinancialReportCriteria.builder()
                .institutionId(UUID.fromString("00000000-0000-0000-0000-000000000501"))
                .dateFrom(LocalDate.of(2026, 2, 1))
                .dateTo(LocalDate.of(2026, 2, 28))
                .build();
        given(financialReportQueryPort.findEarningsByCongress(criteria)).willReturn(List.of());

        useCase.execute(criteria);

        then(financialReportQueryPort).should().findEarningsByCongress(criteria);
    }

    @Test
    void should_throw_validation_failed_when_date_from_is_after_date_to() {
        FinancialReportCriteria criteria = FinancialReportCriteria.builder()
                .dateFrom(LocalDate.of(2026, 6, 1))
                .dateTo(LocalDate.of(2026, 5, 31))
                .build();

        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(criteria));

        assertThat(ex.getStatus().value()).isEqualTo(400);
        assertThat(ex.getCode()).isEqualTo("validation.failed");
    }

    private EarningsByCongressRow row(
            String congressName,
            UUID institutionId,
            String institutionName,
            String totalAmount,
            String commissionAmount,
            String netAmount,
            long paymentCount) {
        return EarningsByCongressRow.builder()
                .congressId(UUID.randomUUID())
                .congressName(congressName)
                .institutionId(institutionId)
                .institutionName(institutionName)
                .totalAmount(new BigDecimal(totalAmount))
                .commissionAmount(new BigDecimal(commissionAmount))
                .netAmount(new BigDecimal(netAmount))
                .paymentCount(paymentCount)
                .build();
    }
}

