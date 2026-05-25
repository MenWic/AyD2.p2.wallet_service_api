package ayd2.p2b.wallet_service_api.feature.wallet.controller;

import ayd2.p2b.wallet_service_api.common.dto.internal.RequesterContext;
import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.common.response.ApiResponse;
import ayd2.p2b.wallet_service_api.common.response.PageResponse;
import ayd2.p2b.wallet_service_api.core.openapi.OpenApiExamples;
import ayd2.p2b.wallet_service_api.core.security.AuthenticatedUser;
import ayd2.p2b.wallet_service_api.feature.wallet.application.balance.GetWalletBalanceUseCase;
import ayd2.p2b.wallet_service_api.feature.wallet.application.create.CreateWalletUseCase;
import ayd2.p2b.wallet_service_api.feature.wallet.application.top_up.TopUpWalletUseCase;
import ayd2.p2b.wallet_service_api.feature.wallet.application.transactions.GetTransactionHistoryUseCase;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionType;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.internal.TopUpCommand;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.internal.TransactionSearchCriteria;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.request.CreateWalletRequest;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.request.TopUpRequest;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.response.TransactionResponse;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.response.WalletBalanceResponse;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Tag(name = "Wallet", description = "Wallet management endpoints: creation, balance, top-up and transaction history.")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {

    private final CreateWalletUseCase createWalletUseCase;
    private final GetWalletBalanceUseCase getWalletBalanceUseCase;
    private final TopUpWalletUseCase topUpWalletUseCase;
    private final GetTransactionHistoryUseCase getTransactionHistoryUseCase;

    @PostMapping("/wallets")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Create wallet for authenticated user",
            description = "Authenticated endpoint. Creates or returns the wallet for the current JWT principal. "
                    + "The request userId must match the authenticated user.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Wallet creation request",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateWalletRequest.class),
                    examples = @ExampleObject(name = "createWalletRequest", value = OpenApiExamples.REQUEST_CREATE_WALLET)))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Wallet created (or existing wallet returned for same user)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.RESPONSE_WALLET_BALANCE))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_VALIDATION_FAILED))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_UNAUTHORIZED))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Request userId does not match authenticated principal",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_FORBIDDEN)))
    })
    public ResponseEntity<ApiResponse<WalletBalanceResponse>> createWallet(
            @Valid @RequestBody CreateWalletRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        if (!request.getUserId().equals(user.getUserId())) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "auth.forbidden",
                    "Cannot create wallet for a different user");
        }
        WalletBalanceResponse response = createWalletUseCase.execute(request.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    @GetMapping("/wallet/balance")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get wallet balance",
            description = "Authenticated endpoint. Returns current wallet balance for the JWT principal.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Balance retrieved",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.RESPONSE_WALLET_BALANCE))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_UNAUTHORIZED))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Wallet not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_NOT_FOUND)))
    })
    public ResponseEntity<ApiResponse<WalletBalanceResponse>> getBalance(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.of(getWalletBalanceUseCase.execute(user.getUserId())));
    }

    @PostMapping("/wallet/top-up")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Top up wallet",
            description = "Authenticated endpoint. Adds positive amount to the authenticated user's wallet using a user-supplied payment date.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Wallet top-up request",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TopUpRequest.class),
                    examples = @ExampleObject(name = "topUpRequest", value = OpenApiExamples.REQUEST_TOP_UP)))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Top-up applied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.RESPONSE_WALLET_BALANCE))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_VALIDATION_FAILED))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_UNAUTHORIZED))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Wallet not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_NOT_FOUND)))
    })
    public ResponseEntity<ApiResponse<WalletBalanceResponse>> topUp(
            @Valid @RequestBody TopUpRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
        RequesterContext requester = RequesterContext.of(user.getUserId(), roleNames);
        TopUpCommand command = TopUpCommand.builder()
                .amount(request.getAmount())
                .paymentDate(request.getPaymentDate())
                .build();
        return ResponseEntity.ok(ApiResponse.of(topUpWalletUseCase.execute(requester, command)));
    }

    @GetMapping("/wallet/transactions")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get wallet transactions",
            description = "Authenticated endpoint. Optional filters: type, dateFrom, dateTo. "
                    + "dateFrom/dateTo are inclusive. Pagination normalization: page default 0, size default 20, max 100.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Transaction page retrieved",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.RESPONSE_TRANSACTION_PAGE))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid date range (dateFrom > dateTo)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_VALIDATION_FAILED))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_UNAUTHORIZED)))
    })
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getTransactions(
            @Parameter(in = ParameterIn.QUERY, description = "Transaction type filter", example = "TOP_UP")
            @RequestParam(required = false) TransactionType type,
            @Parameter(in = ParameterIn.QUERY, description = "Start date filter (inclusive)", example = "2026-05-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(in = ParameterIn.QUERY, description = "End date filter (inclusive)", example = "2026-05-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @Parameter(in = ParameterIn.QUERY, description = "Zero-based page index; negative values normalize to 0", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(in = ParameterIn.QUERY, description = "Page size; values <= 0 normalize to 20 and values > 100 cap at 100", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedUser user) {

        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "validation.failed",
                    "dateFrom must not be after dateTo");
        }
        int resolvedPage = Math.max(page, 0);
        int resolvedSize = (size <= 0) ? 20 : Math.min(size, 100);

        TransactionSearchCriteria criteria = TransactionSearchCriteria.builder()
                .transactionType(type)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .page(resolvedPage)
                .size(resolvedSize)
                .build();

        return ResponseEntity.ok(ApiResponse.of(
                getTransactionHistoryUseCase.execute(user.getUserId(), criteria)));
    }
}

