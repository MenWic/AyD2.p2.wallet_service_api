package ayd2.p2b.wallet_service_api.feature.payment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentData {

    private UUID id;
    private UUID userId;
    private UUID congressId;
    private UUID institutionId;
    private String congressNameSnapshot;
    private String institutionNameSnapshot;
    private BigDecimal commissionPercentSnapshot;
    private BigDecimal amount;
    private BigDecimal commissionAmount;
    private BigDecimal netAmount;
    private LocalDate paymentDate;
    private String idempotencyKey;
    private UUID createdBy;
    private Instant createdAt;
}
