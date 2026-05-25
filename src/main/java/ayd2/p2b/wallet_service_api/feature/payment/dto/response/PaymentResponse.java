package ayd2.p2b.wallet_service_api.feature.payment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment record response")
public class PaymentResponse {

    @Schema(description = "Unique payment identifier", example = "22222222-2222-2222-2222-222222222222")
    private UUID id;

    @Schema(description = "User who made the payment", example = "00000000-0000-0000-0000-000000000001")
    private UUID userId;

    @Schema(description = "Congress the payment is for", example = "00000000-0000-0000-0000-000000000010")
    private UUID congressId;

    @Schema(description = "Institution that owns the congress", example = "00000000-0000-0000-0000-000000000020")
    private UUID institutionId;

    @Schema(description = "Congress name captured at payment time", example = "Congreso Nacional de Ingenieria")
    private String congressNameSnapshot;

    @Schema(description = "Institution name captured at payment time", example = "Universidad Nacional")
    private String institutionNameSnapshot;

    @Schema(description = "Commission percentage applied at payment time", example = "10.00")
    private BigDecimal commissionPercentSnapshot;

    @Schema(description = "Total amount charged", example = "350.00")
    private BigDecimal amount;

    @Schema(description = "Commission portion of the amount", example = "35.00")
    private BigDecimal commissionAmount;

    @Schema(description = "Net amount after commission", example = "315.00")
    private BigDecimal netAmount;

    @Schema(description = "User-supplied payment date", example = "2026-05-20")
    private LocalDate paymentDate;

    @Schema(description = "Idempotency key used for this payment", example = "idem-12345")
    private String idempotencyKey;

    @Schema(description = "Timestamp when the payment was recorded", example = "2026-05-20T13:12:00Z")
    private Instant createdAt;
}
