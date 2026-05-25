package ayd2.p2b.wallet_service_api.feature.report.controller;

import ayd2.p2b.wallet_service_api.common.response.ApiResponse;
import ayd2.p2b.wallet_service_api.core.security.ConferenceServiceTokenValidator;
import ayd2.p2b.wallet_service_api.feature.report.application.earnings_by_congress.GetEarningsByCongressReportUseCase;
import ayd2.p2b.wallet_service_api.feature.report.application.platform_earnings.GetPlatformEarningsReportUseCase;
import ayd2.p2b.wallet_service_api.feature.report.dto.internal.FinancialReportCriteria;
import ayd2.p2b.wallet_service_api.feature.report.dto.response.EarningsByCongressReportResponse;
import ayd2.p2b.wallet_service_api.feature.report.dto.response.PlatformEarningsReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Internal Financial Reports", description = "Internal wallet-owned financial aggregates for conference-service")
@SecurityRequirement(name = "serviceTokenAuth")
public class InternalFinancialReportController {

    private final ConferenceServiceTokenValidator conferenceServiceTokenValidator;
    private final GetEarningsByCongressReportUseCase getEarningsByCongressReportUseCase;
    private final GetPlatformEarningsReportUseCase getPlatformEarningsReportUseCase;

    @GetMapping("/internal/reports/earnings-by-congress")
    @Operation(
            summary = "Internal earnings-by-congress report for conference-service",
            description = "Conference must validate end-user authorization before calling this endpoint. "
                    + "Wallet validates X-Service-Token only. dateFrom/dateTo filters are inclusive.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Report generated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid date range"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Missing or invalid X-Service-Token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Service token misconfiguration")
    })
    public ResponseEntity<ApiResponse<EarningsByCongressReportResponse>> getEarningsByCongress(
            @Parameter(required = true, description = "Service-to-service token for conference-service")
            @RequestHeader(value = "X-Service-Token", required = false) String serviceToken,
            @RequestParam(required = false) UUID congressId,
            @RequestParam(required = false) UUID institutionId,
            @Parameter(description = "Inclusive date filter (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "Inclusive date filter (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        conferenceServiceTokenValidator.validate(serviceToken);

        FinancialReportCriteria criteria = FinancialReportCriteria.builder()
                .congressId(congressId)
                .institutionId(institutionId)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .build();

        return ResponseEntity.ok(ApiResponse.of(getEarningsByCongressReportUseCase.execute(criteria)));
    }

    @GetMapping("/internal/reports/earnings")
    @Operation(
            summary = "Internal platform earnings report for conference-service",
            description = "Conference must validate end-user authorization before calling this endpoint. "
                    + "Wallet validates X-Service-Token only. dateFrom/dateTo filters are inclusive.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Report generated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid date range"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Missing or invalid X-Service-Token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Service token misconfiguration")
    })
    public ResponseEntity<ApiResponse<PlatformEarningsReportResponse>> getPlatformEarnings(
            @Parameter(required = true, description = "Service-to-service token for conference-service")
            @RequestHeader(value = "X-Service-Token", required = false) String serviceToken,
            @RequestParam(required = false) UUID institutionId,
            @Parameter(description = "Inclusive date filter (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "Inclusive date filter (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        conferenceServiceTokenValidator.validate(serviceToken);

        FinancialReportCriteria criteria = FinancialReportCriteria.builder()
                .institutionId(institutionId)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .build();

        return ResponseEntity.ok(ApiResponse.of(getPlatformEarningsReportUseCase.execute(criteria)));
    }
}
