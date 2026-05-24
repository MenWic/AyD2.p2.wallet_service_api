package ayd2.p2b.wallet_service_api.feature.wallet.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopUpCommand {

    private BigDecimal amount;
    private LocalDate paymentDate;
}
