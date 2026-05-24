package ayd2.p2b.wallet_service_api.feature.systemconfig.controller;

import ayd2.p2b.wallet_service_api.common.response.ApiResponse;
import ayd2.p2b.wallet_service_api.core.security.AuthenticatedUser;
import ayd2.p2b.wallet_service_api.feature.systemconfig.application.get.GetSystemConfigUseCase;
import ayd2.p2b.wallet_service_api.feature.systemconfig.application.update.UpdateSystemConfigUseCase;
import ayd2.p2b.wallet_service_api.feature.systemconfig.dto.request.UpdateSystemConfigRequest;
import ayd2.p2b.wallet_service_api.feature.systemconfig.dto.response.SystemConfigResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "System Configuration", description = "Platform commission configuration management")
@SecurityRequirement(name = "bearerAuth")
public class SystemConfigController {

    private final GetSystemConfigUseCase getUseCase;
    private final UpdateSystemConfigUseCase updateUseCase;

    @GetMapping("/system/config")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Get current commission configuration")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Configuration retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized — JWT absent or invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden — SYSTEM_ADMIN role required")
    })
    public ResponseEntity<ApiResponse<SystemConfigResponse>> get() {
        return ResponseEntity.ok(ApiResponse.of(getUseCase.execute()));
    }

    @PutMapping("/system/config")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Update commission percentage")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Configuration updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed — commissionPercent out of [0,100] range"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized — JWT absent or invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden — SYSTEM_ADMIN role required")
    })
    public ResponseEntity<ApiResponse<SystemConfigResponse>> update(
            @Valid @RequestBody UpdateSystemConfigRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.of(
                updateUseCase.execute(request.getCommissionPercent(), user.getUserId())
        ));
    }
}
