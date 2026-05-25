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
    @Schema(
            description = "User ID being charged. Must match authenticated PARTICIPANT JWT subject.",
            example = "00000000-0000-0000-0000-000000000001",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID userId;

    @NotNull(message = "congressId is required")
    @Schema(
            description = "Congress ID associated with the enrollment payment.",
            example = "00000000-0000-0000-0000-000000000010",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID congressId;

    @NotNull(message = "institutionId is required")
    @Schema(
            description = "Institution ID that owns the congress.",
            example = "00000000-0000-0000-0000-000000000020",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID institutionId;

    @NotBlank(message = "congressNameSnapshot is required")
    @Size(max = 255, message = "congressNameSnapshot must not exceed 255 characters")
    @Schema(
            description = "Congress name snapshot captured at payment time (immutable for reports).",
            example = "Congreso Nacional de Ingenieria",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String congressNameSnapshot;

    @NotBlank(message = "institutionNameSnapshot is required")
    @Size(max = 255, message = "institutionNameSnapshot must not exceed 255 characters")
    @Schema(
            description = "Institution name snapshot captured at payment time (immutable for reports).",
            example = "Universidad Nacional",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String institutionNameSnapshot;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    @Schema(
            description = "Total enrollment charge amount.",
            example = "350.00",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @NotNull(message = "paymentDate is required")
    @Schema(
            description = "User-supplied payment date (ISO-8601 date).",
            example = "2026-05-20",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate paymentDate;
}
