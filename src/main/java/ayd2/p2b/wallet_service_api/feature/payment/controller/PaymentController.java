package ayd2.p2b.wallet_service_api.feature.payment.controller;

import ayd2.p2b.wallet_service_api.common.response.ApiResponse;
import ayd2.p2b.wallet_service_api.common.response.PageResponse;
import ayd2.p2b.wallet_service_api.core.security.AuthenticatedUser;
import ayd2.p2b.wallet_service_api.feature.payment.application.get.GetPaymentUseCase;
import ayd2.p2b.wallet_service_api.feature.payment.application.list.ListPaymentsUseCase;
import ayd2.p2b.wallet_service_api.feature.payment.application.register.RegisterPaymentUseCase;
import ayd2.p2b.wallet_service_api.feature.payment.dto.request.PaymentFilterRequest;
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
import java.util.List;
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

    @PostMapping("/payments/register")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Register a payment (service-to-service; idempotent)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Payment registered successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Insufficient funds")
    })
    public ResponseEntity<ApiResponse<PaymentResponse>> register(
            @Valid @RequestBody RegisterPaymentRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @AuthenticationPrincipal AuthenticatedUser user) {
        PaymentResponse response = registerPaymentUseCase.execute(request, idempotencyKey, user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
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
        List<String> roleNames = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toList());
        PaymentResponse response = getPaymentUseCase.execute(id, user.getUserId(), roleNames);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @GetMapping("/payments")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "List all payments with optional filters (SYSTEM_ADMIN only)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payments listed successfully"),
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

        int normalizedSize = Math.min(size, 100);
        PaymentFilterRequest filter = PaymentFilterRequest.builder()
                .congressId(congressId)
                .institutionId(institutionId)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .page(page)
                .size(normalizedSize)
                .build();

        return ResponseEntity.ok(ApiResponse.of(listPaymentsUseCase.execute(filter)));
    }
}
