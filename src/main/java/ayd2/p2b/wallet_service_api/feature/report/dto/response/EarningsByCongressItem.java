package ayd2.p2b.wallet_service_api.feature.report.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarningsByCongressItem {

    private UUID congressId;
    private String congressName;
    private UUID institutionId;
    private String institutionName;
    private BigDecimal totalAmount;
    private BigDecimal commissionAmount;
    private BigDecimal netAmount;
    private long paymentCount;
}

