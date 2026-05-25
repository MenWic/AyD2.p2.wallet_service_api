package ayd2.p2b.wallet_service_api.feature.report.application.platform_earnings;

import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.feature.report.application.port.FinancialReportQueryPort;
import ayd2.p2b.wallet_service_api.feature.report.dto.internal.EarningsByCongressRow;
import ayd2.p2b.wallet_service_api.feature.report.dto.internal.FinancialReportCriteria;
import ayd2.p2b.wallet_service_api.feature.report.dto.response.CongressEarningsItem;
import ayd2.p2b.wallet_service_api.feature.report.dto.response.InstitutionEarningsItem;
import ayd2.p2b.wallet_service_api.feature.report.dto.response.PlatformEarningsReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetPlatformEarningsReportUseCase {

    private final FinancialReportQueryPort financialReportQueryPort;

    public PlatformEarningsReportResponse execute(FinancialReportCriteria criteria) {
        validateDateRange(criteria);

        List<EarningsByCongressRow> rows = financialReportQueryPort.findEarningsByCongress(criteria);
        if (rows.isEmpty()) {
            return PlatformEarningsReportResponse.builder()
                    .items(List.of())
                    .totalItems(0L)
                    .grandTotalAmount(BigDecimal.ZERO)
                    .grandTotalCommission(BigDecimal.ZERO)
                    .grandTotalNet(BigDecimal.ZERO)
                    .build();
        }

        Map<UUID, List<EarningsByCongressRow>> rowsByInstitution = rows.stream()
                .collect(Collectors.groupingBy(EarningsByCongressRow::getInstitutionId));

        List<InstitutionEarningsItem> institutions = rowsByInstitution.values().stream()
                .map(this::toInstitutionItem)
                .sorted(Comparator.comparing(
                        InstitutionEarningsItem::getInstitutionName,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();

        return PlatformEarningsReportResponse.builder()
                .items(institutions)
                .totalItems(institutions.size())
                .grandTotalAmount(sumInstitutionAmount(institutions))
                .grandTotalCommission(sumInstitutionCommission(institutions))
                .grandTotalNet(sumInstitutionNet(institutions))
                .build();
    }

    private void validateDateRange(FinancialReportCriteria criteria) {
        if (criteria.getDateFrom() != null
                && criteria.getDateTo() != null
                && criteria.getDateFrom().isAfter(criteria.getDateTo())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "validation.failed", "dateFrom must not be after dateTo");
        }
    }

    private InstitutionEarningsItem toInstitutionItem(List<EarningsByCongressRow> rows) {
        EarningsByCongressRow first = rows.getFirst();

        List<CongressEarningsItem> congresses = rows.stream()
                .sorted(Comparator
                        .comparing((EarningsByCongressRow row) -> value(row.getCommissionAmount()), Comparator.reverseOrder())
                        .thenComparing(EarningsByCongressRow::getCongressName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(this::toCongressItem)
                .toList();

        return InstitutionEarningsItem.builder()
                .institutionId(first.getInstitutionId())
                .institutionName(first.getInstitutionName())
                .congresses(congresses)
                .institutionTotalAmount(rows.stream()
                        .map(EarningsByCongressRow::getTotalAmount)
                        .map(this::value)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .institutionTotalCommission(rows.stream()
                        .map(EarningsByCongressRow::getCommissionAmount)
                        .map(this::value)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .institutionTotalNet(rows.stream()
                        .map(EarningsByCongressRow::getNetAmount)
                        .map(this::value)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .paymentCount(rows.stream().mapToLong(EarningsByCongressRow::getPaymentCount).sum())
                .build();
    }

    private CongressEarningsItem toCongressItem(EarningsByCongressRow row) {
        return CongressEarningsItem.builder()
                .congressId(row.getCongressId())
                .congressName(row.getCongressName())
                .totalAmount(value(row.getTotalAmount()))
                .commissionAmount(value(row.getCommissionAmount()))
                .netAmount(value(row.getNetAmount()))
                .paymentCount(row.getPaymentCount())
                .build();
    }

    private BigDecimal sumInstitutionAmount(List<InstitutionEarningsItem> institutions) {
        return institutions.stream()
                .map(InstitutionEarningsItem::getInstitutionTotalAmount)
                .map(this::value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumInstitutionCommission(List<InstitutionEarningsItem> institutions) {
        return institutions.stream()
                .map(InstitutionEarningsItem::getInstitutionTotalCommission)
                .map(this::value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumInstitutionNet(List<InstitutionEarningsItem> institutions) {
        return institutions.stream()
                .map(InstitutionEarningsItem::getInstitutionTotalNet)
                .map(this::value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal value(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}

