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
        StringBuilder jpql = new StringBuilder("""
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
                where p.id is not null
                """);

        if (criteria.getCongressId() != null) {
            jpql.append(" and p.congressId = :congressId");
        }

        if (criteria.getInstitutionId() != null) {
            jpql.append(" and p.institutionId = :institutionId");
        }

        if (criteria.getDateFrom() != null) {
            jpql.append(" and p.paymentDate >= :dateFrom");
        }

        if (criteria.getDateTo() != null) {
            jpql.append(" and p.paymentDate <= :dateTo");
        }

        jpql.append("""
                
                group by p.congressId, p.congressNameSnapshot, p.institutionId, p.institutionNameSnapshot
                order by p.institutionNameSnapshot asc, p.congressNameSnapshot asc
                """);

        TypedQuery<EarningsByCongressRow> query = entityManager.createQuery(
                jpql.toString(),
                EarningsByCongressRow.class
        );

        if (criteria.getCongressId() != null) {
            query.setParameter("congressId", criteria.getCongressId());
        }

        if (criteria.getInstitutionId() != null) {
            query.setParameter("institutionId", criteria.getInstitutionId());
        }

        if (criteria.getDateFrom() != null) {
            query.setParameter("dateFrom", criteria.getDateFrom());
        }

        if (criteria.getDateTo() != null) {
            query.setParameter("dateTo", criteria.getDateTo());
        }

        return query.getResultList();
    }
}