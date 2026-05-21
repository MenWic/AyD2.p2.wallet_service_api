package ayd2.p2b.wallet_service_api.feature.wallet.controller;

import ayd2.p2b.wallet_service_api.common.response.ApiResponse;
import ayd2.p2b.wallet_service_api.common.response.PageResponse;
import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.core.security.AuthenticatedUser;
import ayd2.p2b.wallet_service_api.feature.wallet.application.balance.GetWalletBalanceUseCase;
import ayd2.p2b.wallet_service_api.feature.wallet.application.create.CreateWalletUseCase;
import ayd2.p2b.wallet_service_api.feature.wallet.application.topup.TopUpWalletUseCase;
import ayd2.p2b.wallet_service_api.feature.wallet.application.transactions.GetTransactionHistoryUseCase;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionType;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.request.CreateWalletRequest;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.request.TopUpRequest;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.request.TransactionFilterRequest;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.response.TransactionResponse;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.response.WalletBalanceResponse;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@Tag(name = "Wallet", description = "Wallet management — balance, top-up, and transaction history")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {

        private final CreateWalletUseCase createWalletUseCase;
        private final GetWalletBalanceUseCase getWalletBalanceUseCase;
        private final TopUpWalletUseCase topUpWalletUseCase;
        private final GetTransactionHistoryUseCase getTransactionHistoryUseCase;

        @PostMapping("/wallets")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Create a wallet for a user (idempotent)")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Wallet created or already exists"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed — userId is required"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
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
        @Operation(summary = "Get the current wallet balance for the authenticated user")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Balance retrieved successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Wallet not found")
        })
        public ResponseEntity<ApiResponse<WalletBalanceResponse>> getBalance(
                        @AuthenticationPrincipal AuthenticatedUser user) {
                return ResponseEntity.ok(ApiResponse.of(getWalletBalanceUseCase.execute(user.getUserId())));
        }

        @PostMapping("/wallet/top-up")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Top up the wallet balance")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Balance updated successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Wallet not found")
        })
        public ResponseEntity<ApiResponse<WalletBalanceResponse>> topUp(
                        @Valid @RequestBody TopUpRequest request,
                        @AuthenticationPrincipal AuthenticatedUser user) {
                return ResponseEntity.ok(ApiResponse
                                .of(topUpWalletUseCase.execute(user.getUserId(), user.getUserId(), request)));
        }

        @GetMapping("/wallet/transactions")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Get transaction history for the authenticated user")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transaction history retrieved successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getTransactions(
                        @RequestParam(required = false) TransactionType type,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @AuthenticationPrincipal AuthenticatedUser user) {

                int normalizedSize = Math.min(size, 100);
                TransactionFilterRequest filter = TransactionFilterRequest.builder()
                                .transactionType(type)
                                .dateFrom(dateFrom)
                                .dateTo(dateTo)
                                .page(page)
                                .size(normalizedSize)
                                .build();

                return ResponseEntity.ok(ApiResponse.of(
                                getTransactionHistoryUseCase.execute(user.getUserId(), filter)));
        }
}
