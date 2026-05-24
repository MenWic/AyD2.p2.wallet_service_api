package ayd2.p2b.wallet_service_api.feature.payment.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSearchCriteria {

    private UUID congressId;
    private UUID institutionId;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private int page;
    private int size;
}
