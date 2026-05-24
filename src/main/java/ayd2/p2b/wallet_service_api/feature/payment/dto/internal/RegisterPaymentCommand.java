package ayd2.p2b.wallet_service_api.feature.payment.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterPaymentCommand {

    private UUID userId;
    private UUID congressId;
    private UUID institutionId;
    private String congressNameSnapshot;
    private String institutionNameSnapshot;
    private BigDecimal amount;
    private LocalDate paymentDate;
}
