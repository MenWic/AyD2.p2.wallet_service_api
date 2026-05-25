package ayd2.p2b.wallet_service_api.feature.report.infrastructure.persistence.query;

import ayd2.p2b.wallet_service_api.feature.report.application.port.FinancialReportQueryPort;
import ayd2.p2b.wallet_service_api.feature.report.dto.internal.EarningsByCongressRow;
import ayd2.p2b.wallet_service_api.feature.report.dto.internal.FinancialReportCriteria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JpaFinancialReportQueryAdapter implements FinancialReportQueryPort {

    private final EntityManager entityManager;

    @Override
    public List<EarningsByCongressRow> findEarningsByCongress(FinancialReportCriteria criteria) {
        String jpql = """
                select new ayd2.p2b.wallet_service_api.feature.report.dto.internal.EarningsByCongressRow(
                    p.congressId,
                    p.congressNameSnapshot,
                    p.institutionId,
                    p.institutionNameSnapshot,
                    coalesce(sum(p.amount), 0),
                    coalesce(sum(p.commissionAmount), 0),
                    coalesce(sum(p.netAmount), 0),
                    count(p.id)
                )
                from PaymentEntity p
                where (:congressId is null or p.congressId = :congressId)
                  and (:institutionId is null or p.institutionId = :institutionId)
                  and (:dateFrom is null or p.paymentDate >= :dateFrom)
                  and (:dateTo is null or p.paymentDate <= :dateTo)
                group by p.congressId, p.congressNameSnapshot, p.institutionId, p.institutionNameSnapshot
                order by p.institutionNameSnapshot asc, p.congressNameSnapshot asc
                """;

        TypedQuery<EarningsByCongressRow> query = entityManager.createQuery(jpql, EarningsByCongressRow.class);
        query.setParameter("congressId", criteria.getCongressId());
        query.setParameter("institutionId", criteria.getInstitutionId());
        query.setParameter("dateFrom", criteria.getDateFrom());
        query.setParameter("dateTo", criteria.getDateTo());
        return query.getResultList();
    }
}

