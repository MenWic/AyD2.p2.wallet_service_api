package ayd2.p2b.wallet_service_api.feature.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Request to register a payment for a congress enrollment")
public class RegisterPaymentRequest {

    @NotNull(message = "userId is required")
    @Schema(description = "ID of the user making the payment", required = true)
    private UUID userId;

    @NotNull(message = "congressId is required")
    @Schema(description = "ID of the congress being paid for", required = true)
    private UUID congressId;

    @NotNull(message = "institutionId is required")
    @Schema(description = "ID of the institution that owns the congress", required = true)
    private UUID institutionId;

    @NotBlank(message = "congressNameSnapshot is required")
    @Size(max = 255, message = "congressNameSnapshot must not exceed 255 characters")
    @Schema(description = "Congress name at payment time (immutable snapshot)", required = true)
    private String congressNameSnapshot;

    @NotBlank(message = "institutionNameSnapshot is required")
    @Size(max = 255, message = "institutionNameSnapshot must not exceed 255 characters")
    @Schema(description = "Institution name at payment time (immutable snapshot)", required = true)
    private String institutionNameSnapshot;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    @Schema(description = "Total amount charged for the enrollment", required = true)
    private BigDecimal amount;

    @NotNull(message = "paymentDate is required")
    @Schema(description = "User-supplied date for the payment", required = true)
    private LocalDate paymentDate;
}
