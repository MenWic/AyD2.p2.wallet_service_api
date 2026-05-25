package ayd2.p2b.wallet_service_api.unit.feature.report.earnings_by_congress;

import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.feature.report.application.earnings_by_congress.GetEarningsByCongressReportUseCase;
import ayd2.p2b.wallet_service_api.feature.report.application.port.FinancialReportQueryPort;
import ayd2.p2b.wallet_service_api.feature.report.dto.internal.EarningsByCongressRow;
import ayd2.p2b.wallet_service_api.feature.report.dto.internal.FinancialReportCriteria;
import ayd2.p2b.wallet_service_api.feature.report.dto.response.EarningsByCongressReportResponse;
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
class GetEarningsByCongressReportUseCaseTest {

    @Mock
    private FinancialReportQueryPort financialReportQueryPort;

    private GetEarningsByCongressReportUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetEarningsByCongressReportUseCase(financialReportQueryPort);
    }

    @Test
    void should_return_empty_items_and_zero_totals_when_query_returns_no_rows() {
        FinancialReportCriteria criteria = FinancialReportCriteria.builder().build();
        given(financialReportQueryPort.findEarningsByCongress(criteria)).willReturn(List.of());

        EarningsByCongressReportResponse result = useCase.execute(criteria);

        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotalItems()).isZero();
        assertThat(result.getGrandTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getGrandTotalCommission()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getGrandTotalNet()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void should_calculate_grand_totals_exactly_for_multiple_rows() {
        FinancialReportCriteria criteria = FinancialReportCriteria.builder().build();
        List<EarningsByCongressRow> rows = List.of(
                row("Congress A", "Inst 1", "100.00", "10.00", "90.00", 1),
                row("Congress B", "Inst 2", "250.50", "25.05", "225.45", 2));
        given(financialReportQueryPort.findEarningsByCongress(criteria)).willReturn(rows);

        EarningsByCongressReportResponse result = useCase.execute(criteria);

        assertThat(result.getTotalItems()).isEqualTo(2);
        assertThat(result.getGrandTotalAmount()).isEqualByComparingTo("350.50");
        assertThat(result.getGrandTotalCommission()).isEqualByComparingTo("35.05");
        assertThat(result.getGrandTotalNet()).isEqualByComparingTo("315.45");
    }

    @Test
    void should_preserve_row_data_in_response_items() {
        FinancialReportCriteria criteria = FinancialReportCriteria.builder().build();
        UUID congressId = UUID.fromString("00000000-0000-0000-0000-000000000101");
        UUID institutionId = UUID.fromString("00000000-0000-0000-0000-000000000201");
        EarningsByCongressRow row = EarningsByCongressRow.builder()
                .congressId(congressId)
                .congressName("Congress Snapshot")
                .institutionId(institutionId)
                .institutionName("Institution Snapshot")
                .totalAmount(new BigDecimal("80.00"))
                .commissionAmount(new BigDecimal("8.00"))
                .netAmount(new BigDecimal("72.00"))
                .paymentCount(4L)
                .build();
        given(financialReportQueryPort.findEarningsByCongress(criteria)).willReturn(List.of(row));

        EarningsByCongressReportResponse result = useCase.execute(criteria);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().getFirst().getCongressId()).isEqualTo(congressId);
        assertThat(result.getItems().getFirst().getCongressName()).isEqualTo("Congress Snapshot");
        assertThat(result.getItems().getFirst().getInstitutionId()).isEqualTo(institutionId);
        assertThat(result.getItems().getFirst().getInstitutionName()).isEqualTo("Institution Snapshot");
        assertThat(result.getItems().getFirst().getPaymentCount()).isEqualTo(4L);
    }

    @Test
    void should_pass_filters_through_to_query_port() {
        FinancialReportCriteria criteria = FinancialReportCriteria.builder()
                .congressId(UUID.fromString("00000000-0000-0000-0000-000000000301"))
                .institutionId(UUID.fromString("00000000-0000-0000-0000-000000000401"))
                .dateFrom(LocalDate.of(2026, 1, 1))
                .dateTo(LocalDate.of(2026, 12, 31))
                .build();
        given(financialReportQueryPort.findEarningsByCongress(criteria)).willReturn(List.of());

        useCase.execute(criteria);

        then(financialReportQueryPort).should().findEarningsByCongress(criteria);
    }

    @Test
    void should_throw_validation_failed_when_date_from_is_after_date_to() {
        FinancialReportCriteria criteria = FinancialReportCriteria.builder()
                .dateFrom(LocalDate.of(2026, 5, 20))
                .dateTo(LocalDate.of(2026, 5, 19))
                .build();

        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(criteria));

        assertThat(ex.getStatus().value()).isEqualTo(400);
        assertThat(ex.getCode()).isEqualTo("validation.failed");
    }

    private EarningsByCongressRow row(
            String congressName,
            String institutionName,
            String totalAmount,
            String commissionAmount,
            String netAmount,
            long paymentCount) {
        return EarningsByCongressRow.builder()
                .congressId(UUID.randomUUID())
                .congressName(congressName)
                .institutionId(UUID.randomUUID())
                .institutionName(institutionName)
                .totalAmount(new BigDecimal(totalAmount))
                .commissionAmount(new BigDecimal(commissionAmount))
                .netAmount(new BigDecimal(netAmount))
                .paymentCount(paymentCount)
                .build();
    }
}

