package ayd2.p2b.wallet_service_api.feature.report.application.port;

import ayd2.p2b.wallet_service_api.feature.report.dto.internal.EarningsByCongressRow;
import ayd2.p2b.wallet_service_api.feature.report.dto.internal.FinancialReportCriteria;

import java.util.List;

public interface FinancialReportQueryPort {

    List<EarningsByCongressRow> findEarningsByCongress(FinancialReportCriteria criteria);
}

