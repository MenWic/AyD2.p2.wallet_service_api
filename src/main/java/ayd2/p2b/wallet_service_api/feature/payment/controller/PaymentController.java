package ayd2.p2b.wallet_service_api.feature.payment.controller;

import ayd2.p2b.wallet_service_api.common.dto.internal.RequesterContext;
import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.common.response.ApiResponse;
import ayd2.p2b.wallet_service_api.common.response.PageResponse;
import ayd2.p2b.wallet_service_api.core.openapi.OpenApiExamples;
import ayd2.p2b.wallet_service_api.core.security.AuthenticatedUser;
import ayd2.p2b.wallet_service_api.core.security.ConferenceServiceTokenValidator;
import ayd2.p2b.wallet_service_api.feature.payment.application.get.GetPaymentUseCase;
import ayd2.p2b.wallet_service_api.feature.payment.application.list.ListPaymentsUseCase;
import ayd2.p2b.wallet_service_api.feature.payment.application.register.RegisterPaymentResult;
import ayd2.p2b.wallet_service_api.feature.payment.application.register.RegisterPaymentUseCase;
import ayd2.p2b.wallet_service_api.feature.payment.dto.internal.PaymentSearchCriteria;
import ayd2.p2b.wallet_service_api.feature.payment.dto.internal.RegisterPaymentCommand;
import ayd2.p2b.wallet_service_api.feature.payment.dto.request.RegisterPaymentRequest;
import ayd2.p2b.wallet_service_api.feature.payment.dto.response.PaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Immutable payment registration and retrieval endpoints.")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

        private final RegisterPaymentUseCase registerPaymentUseCase;
        private final GetPaymentUseCase getPaymentUseCase;
        private final ListPaymentsUseCase listPaymentsUseCase;
        private final ConferenceServiceTokenValidator conferenceServiceTokenValidator;

        @PostMapping("/payments/register")
        @PreAuthorize("hasRole('PARTICIPANT')")
        @SecurityRequirement(name = "bearerAuth")
        @SecurityRequirement(name = "serviceTokenAuth")
        @Operation(
                        summary = "Register payment (conference-service -> wallet-service)",
                        description = "Internal service-to-service endpoint. Requires PARTICIPANT JWT (forwarded by conference-service), "
                                        + "X-Service-Token, and Idempotency-Key. Returns 201 for new payment or 200 with message idempotency.replay.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                        required = true,
                        description = "Payment registration payload",
                        content = @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = RegisterPaymentRequest.class),
                                        examples = @ExampleObject(name = "registerPaymentRequest", value = OpenApiExamples.REQUEST_REGISTER_PAYMENT)))
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                                        responseCode = "201",
                                        description = "Payment registered successfully",
                                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.RESPONSE_PAYMENT_REGISTERED))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                                        responseCode = "200",
                                        description = "Idempotency replay (same Idempotency-Key and same payload)",
                                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.RESPONSE_PAYMENT_REPLAY))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                                        responseCode = "400",
                                        description = "Validation failed (missing/blank Idempotency-Key or invalid payload)",
                                        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_VALIDATION_FAILED))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                                        responseCode = "401",
                                        description = "Missing or invalid JWT",
                                        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_UNAUTHORIZED))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                                        responseCode = "403",
                                        description = "Forbidden (invalid X-Service-Token or authenticated user mismatch)",
                                        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_FORBIDDEN))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                                        responseCode = "409",
                                        description = "Idempotency conflict (same key with different request)",
                                        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_CONFLICT))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                                        responseCode = "422",
                                        description = "Insufficient wallet funds",
                                        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_INSUFFICIENT_FUNDS))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                                        responseCode = "500",
                                        description = "Server misconfiguration (missing configured service token)",
                                        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_INTERNAL_ERROR)))
        })
        public ResponseEntity<ApiResponse<PaymentResponse>> register(
                        @Valid @RequestBody RegisterPaymentRequest request,
                        @Parameter(
                                        in = ParameterIn.HEADER,
                                        required = true,
                                        description = "Idempotency key provided by caller; same value must be reused for retries",
                                        example = "idem-12345")
                        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                        @Parameter(
                                        in = ParameterIn.HEADER,
                                        required = true,
                                        description = "Conference service shared token for wallet internal endpoints",
                                        example = "conference-wallet-shared-token")
                        @RequestHeader(value = "X-Service-Token", required = false) String serviceToken,
                        @AuthenticationPrincipal AuthenticatedUser user) {

                if (idempotencyKey == null || idempotencyKey.isBlank()) {
                        throw new ApiException(
                                        HttpStatus.BAD_REQUEST,
                                        "validation.failed",
                                        "Idempotency-Key header is required and must not be blank");
                }
                if (idempotencyKey.length() > 120) {
                        throw new ApiException(
                                        HttpStatus.BAD_REQUEST,
                                        "validation.failed",
                                        "Idempotency-Key must not exceed 120 characters");
                }

                if (request.getAmount() != null && request.getAmount().scale() > 2) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "validation.failed",
                                        "amount must have at most 2 decimal places");
                }

                conferenceServiceTokenValidator.validate(serviceToken);

                if (!user.getUserId().equals(request.getUserId())) {
                        throw new ApiException(
                                        HttpStatus.FORBIDDEN,
                                        "auth.forbidden",
                                        "Authenticated user does not match the payment userId");
                }

                Set<String> roleNames = user.getRoles().stream()
                                .map(Enum::name)
                                .collect(Collectors.toSet());
                RequesterContext requester = RequesterContext.of(user.getUserId(), roleNames);

                RegisterPaymentCommand command = RegisterPaymentCommand.builder()
                                .userId(request.getUserId())
                                .congressId(request.getCongressId())
                                .institutionId(request.getInstitutionId())
                                .congressNameSnapshot(request.getCongressNameSnapshot().strip())
                                .institutionNameSnapshot(request.getInstitutionNameSnapshot().strip())
                                .amount(request.getAmount())
                                .paymentDate(request.getPaymentDate())
                                .build();

                RegisterPaymentResult result = registerPaymentUseCase.execute(command, idempotencyKey, requester);
                if (result.isReplay()) {
                        return ResponseEntity.ok(ApiResponse.of(result.getPayload(), "idempotency.replay"));
                }
                return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(result.getPayload()));
        }

        @GetMapping("/payments/{id}")
        @PreAuthorize("isAuthenticated()")
        @Operation(
                        summary = "Get payment by ID",
                        description = "Authenticated endpoint. Accessible by payment owner or SYSTEM_ADMIN.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                                        responseCode = "200",
                                        description = "Payment retrieved",
                                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.RESPONSE_PAYMENT_DETAIL))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                                        responseCode = "401",
                                        description = "Missing or invalid JWT",
                                        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_UNAUTHORIZED))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                                        responseCode = "403",
                                        description = "Forbidden (not owner and not SYSTEM_ADMIN)",
                                        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_FORBIDDEN))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                                        responseCode = "404",
                                        description = "Payment not found",
                                        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_NOT_FOUND)))
        })
        public ResponseEntity<ApiResponse<PaymentResponse>> getById(
                        @Parameter(in = ParameterIn.PATH, required = true, description = "Payment identifier", example = "22222222-2222-2222-2222-222222222222")
                        @PathVariable UUID id,
                        @AuthenticationPrincipal AuthenticatedUser user) {
                Set<String> roleNames = user.getRoles().stream()
                                .map(Enum::name)
                                .collect(Collectors.toSet());
                RequesterContext requester = RequesterContext.of(user.getUserId(), roleNames);
                PaymentResponse response = getPaymentUseCase.execute(id, requester);
                return ResponseEntity.ok(ApiResponse.of(response));
        }

        @GetMapping("/payments")
        @PreAuthorize("hasRole('SYSTEM_ADMIN')")
        @Operation(
                        summary = "List payments (SYSTEM_ADMIN only)",
                        description = "Authenticated endpoint for SYSTEM_ADMIN. Optional filters by congress, institution and date range. "
                                        + "dateFrom/dateTo are inclusive. Pagination normalization: page default 0, size default 20, max 100.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                                        responseCode = "200",
                                        description = "Payments page retrieved",
                                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.RESPONSE_PAYMENTS_PAGE))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                                        responseCode = "400",
                                        description = "Invalid date range",
                                        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_VALIDATION_FAILED))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                                        responseCode = "401",
                                        description = "Missing or invalid JWT",
                                        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_UNAUTHORIZED))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                                        responseCode = "403",
                                        description = "Forbidden (SYSTEM_ADMIN role required)",
                                        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_FORBIDDEN)))
        })
        public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> list(
                        @Parameter(in = ParameterIn.QUERY, description = "Filter by congress ID", example = "00000000-0000-0000-0000-000000000010")
                        @RequestParam(required = false) UUID congressId,
                        @Parameter(in = ParameterIn.QUERY, description = "Filter by institution ID", example = "00000000-0000-0000-0000-000000000020")
                        @RequestParam(required = false) UUID institutionId,
                        @Parameter(in = ParameterIn.QUERY, description = "Start paymentDate (inclusive)", example = "2026-05-01")
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                        @Parameter(in = ParameterIn.QUERY, description = "End paymentDate (inclusive)", example = "2026-05-31")
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                        @Parameter(in = ParameterIn.QUERY, description = "Zero-based page index; negative values normalize to 0", example = "0")
                        @RequestParam(defaultValue = "0") int page,
                        @Parameter(in = ParameterIn.QUERY, description = "Page size; values <= 0 normalize to 20 and values > 100 cap at 100", example = "20")
                        @RequestParam(defaultValue = "20") int size) {

                if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "validation.failed",
                                        "dateFrom must not be after dateTo");
                }
                int resolvedPage = Math.max(page, 0);
                int resolvedSize = (size <= 0) ? 20 : Math.min(size, 100);

                PaymentSearchCriteria criteria = PaymentSearchCriteria.builder()
                                .congressId(congressId)
                                .institutionId(institutionId)
                                .dateFrom(dateFrom)
                                .dateTo(dateTo)
                                .page(resolvedPage)
                                .size(resolvedSize)
                                .build();

                return ResponseEntity.ok(ApiResponse.of(listPaymentsUseCase.execute(criteria)));
        }
}

