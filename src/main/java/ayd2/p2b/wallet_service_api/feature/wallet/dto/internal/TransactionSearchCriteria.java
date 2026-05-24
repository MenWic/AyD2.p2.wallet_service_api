package ayd2.p2b.wallet_service_api.feature.wallet.dto.internal;

import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSearchCriteria {

    private TransactionType transactionType;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private int page;
    private int size;
}
