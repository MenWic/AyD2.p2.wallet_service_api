package ayd2.p2b.wallet_service_api.feature.payment.controller;

import ayd2.p2b.wallet_service_api.common.dto.internal.RequesterContext;
import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.common.response.ApiResponse;
import ayd2.p2b.wallet_service_api.common.response.PageResponse;
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
@Tag(name = "Payments", description = "Immutable payment registration and retrieval")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

        private final RegisterPaymentUseCase registerPaymentUseCase;
        private final GetPaymentUseCase getPaymentUseCase;
        private final ListPaymentsUseCase listPaymentsUseCase;
        private final ConferenceServiceTokenValidator conferenceServiceTokenValidator;

        @PostMapping("/payments/register")
        @PreAuthorize("hasRole('PARTICIPANT')")
        // Internal endpoint: requires PARTICIPANT JWT (forwarded from
        // conference-service) +
        // X-Service-Token matching integration.conference.service-token.
        // Direct calls from browser/clients without the service token will receive 403.
        @Operation(summary = "Register a payment (conference-service -> wallet-service; idempotent)")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Payment registered successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Idempotent replay — payment already registered with same request"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed — missing or blank Idempotency-Key"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden — missing or invalid X-Service-Token, or userId mismatch"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Idempotency-Key reused with a different request"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Insufficient funds")
        })
        public ResponseEntity<ApiResponse<PaymentResponse>> register(
                        @Valid @RequestBody RegisterPaymentRequest request,
                        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                        @RequestHeader(value = "X-Service-Token", required = false) String serviceToken,
                        @AuthenticationPrincipal AuthenticatedUser user) {

                if (idempotencyKey == null || idempotencyKey.isBlank()) {
                        throw new ApiException(
                                        HttpStatus.BAD_REQUEST,
                                        "validation.failed",
                                        "Idempotency-Key header is required and must not be blank");
                }
                if (idempotencyKey.length() > 255) {
                        throw new ApiException(
                                        HttpStatus.BAD_REQUEST,
                                        "validation.failed",
                                        "Idempotency-Key must not exceed 255 characters");
                }

                // Validates presence and value of X-Service-Token; throws 403 or 500 on
                // failure.
                conferenceServiceTokenValidator.validate(serviceToken);

                // The participant JWT userId must match the payment userId to prevent
                // a participant from registering payments on behalf of another user.
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
                                .congressNameSnapshot(request.getCongressNameSnapshot())
                                .institutionNameSnapshot(request.getInstitutionNameSnapshot())
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
        @Operation(summary = "Get a payment by ID (owner or SYSTEM_ADMIN)")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment retrieved successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment not found")
        })
        public ResponseEntity<ApiResponse<PaymentResponse>> getById(
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
        @Operation(summary = "List all payments with optional filters (SYSTEM_ADMIN only)")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payments listed successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid date range"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden — SYSTEM_ADMIN role required")
        })
        public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> list(
                        @RequestParam(required = false) UUID congressId,
                        @RequestParam(required = false) UUID institutionId,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                        @RequestParam(defaultValue = "0") int page,
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
