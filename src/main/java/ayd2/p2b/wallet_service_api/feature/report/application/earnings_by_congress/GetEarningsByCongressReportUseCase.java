package ayd2.p2b.wallet_service_api.feature.report.application.earnings_by_congress;

import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.feature.report.application.port.FinancialReportQueryPort;
import ayd2.p2b.wallet_service_api.feature.report.dto.internal.EarningsByCongressRow;
import ayd2.p2b.wallet_service_api.feature.report.dto.internal.FinancialReportCriteria;
import ayd2.p2b.wallet_service_api.feature.report.dto.response.EarningsByCongressItem;
import ayd2.p2b.wallet_service_api.feature.report.dto.response.EarningsByCongressReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetEarningsByCongressReportUseCase {

    private final FinancialReportQueryPort financialReportQueryPort;

    public EarningsByCongressReportResponse execute(FinancialReportCriteria criteria) {
        validateDateRange(criteria);

        List<EarningsByCongressRow> rows = financialReportQueryPort.findEarningsByCongress(criteria);
        List<EarningsByCongressItem> items = rows.stream()
                .map(this::mapItem)
                .toList();

        return EarningsByCongressReportResponse.builder()
                .items(items)
                .totalItems(items.size())
                .grandTotalAmount(sumAmount(rows))
                .grandTotalCommission(sumCommission(rows))
                .grandTotalNet(sumNet(rows))
                .build();
    }

    private void validateDateRange(FinancialReportCriteria criteria) {
        if (criteria.getDateFrom() != null
                && criteria.getDateTo() != null
                && criteria.getDateFrom().isAfter(criteria.getDateTo())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "validation.failed", "dateFrom must not be after dateTo");
        }
    }

    private EarningsByCongressItem mapItem(EarningsByCongressRow row) {
        return EarningsByCongressItem.builder()
                .congressId(row.getCongressId())
                .congressName(row.getCongressName())
                .institutionId(row.getInstitutionId())
                .institutionName(row.getInstitutionName())
                .totalAmount(value(row.getTotalAmount()))
                .commissionAmount(value(row.getCommissionAmount()))
                .netAmount(value(row.getNetAmount()))
                .paymentCount(row.getPaymentCount())
                .build();
    }

    private BigDecimal sumAmount(List<EarningsByCongressRow> rows) {
        return rows.stream()
                .map(EarningsByCongressRow::getTotalAmount)
                .map(this::value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumCommission(List<EarningsByCongressRow> rows) {
        return rows.stream()
                .map(EarningsByCongressRow::getCommissionAmount)
                .map(this::value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumNet(List<EarningsByCongressRow> rows) {
        return rows.stream()
                .map(EarningsByCongressRow::getNetAmount)
                .map(this::value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal value(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}

