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

    @Schema(description = "Unique payment identifier")
    private UUID id;

    @Schema(description = "User who made the payment")
    private UUID userId;

    @Schema(description = "Congress the payment is for")
    private UUID congressId;

    @Schema(description = "Institution that owns the congress")
    private UUID institutionId;

    @Schema(description = "Congress name captured at payment time")
    private String congressNameSnapshot;

    @Schema(description = "Institution name captured at payment time")
    private String institutionNameSnapshot;

    @Schema(description = "Commission percentage applied at payment time")
    private BigDecimal commissionPercentSnapshot;

    @Schema(description = "Total amount charged")
    private BigDecimal amount;

    @Schema(description = "Commission portion of the amount")
    private BigDecimal commissionAmount;

    @Schema(description = "Net amount after commission")
    private BigDecimal netAmount;

    @Schema(description = "User-supplied payment date")
    private LocalDate paymentDate;

    @Schema(description = "Idempotency key used for this payment")
    private String idempotencyKey;

    @Schema(description = "Timestamp when the payment was recorded")
    private Instant createdAt;
}
