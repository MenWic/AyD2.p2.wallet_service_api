package ayd2.p2b.wallet_service_api.feature.report.controller;

import ayd2.p2b.wallet_service_api.common.response.ApiResponse;
import ayd2.p2b.wallet_service_api.core.openapi.OpenApiExamples;
import ayd2.p2b.wallet_service_api.core.security.ConferenceServiceTokenValidator;
import ayd2.p2b.wallet_service_api.feature.report.application.earnings_by_congress.GetEarningsByCongressReportUseCase;
import ayd2.p2b.wallet_service_api.feature.report.application.platform_earnings.GetPlatformEarningsReportUseCase;
import ayd2.p2b.wallet_service_api.feature.report.dto.internal.FinancialReportCriteria;
import ayd2.p2b.wallet_service_api.feature.report.dto.response.EarningsByCongressReportResponse;
import ayd2.p2b.wallet_service_api.feature.report.dto.response.PlatformEarningsReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Tag(name = "Internal Financial Reports", description = "Internal Wallet financial aggregates consumed by conference-service.")
@SecurityRequirement(name = "serviceTokenAuth")
public class InternalFinancialReportController {

    private final ConferenceServiceTokenValidator conferenceServiceTokenValidator;
    private final GetEarningsByCongressReportUseCase getEarningsByCongressReportUseCase;
    private final GetPlatformEarningsReportUseCase getPlatformEarningsReportUseCase;

    @GetMapping("/internal/reports/earnings-by-congress")
    @Operation(
            summary = "Internal earnings by congress",
            description = "Internal service-to-service endpoint for conference-service. Requires X-Service-Token. "
                    + "Wallet does not require end-user JWT here; conference-service must enforce end-user authorization before calling. "
                    + "dateFrom/dateTo filters are inclusive. Wallet totals come from immutable PaymentEntity values.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Report generated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.RESPONSE_EARNINGS_BY_CONGRESS))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid date range (dateFrom > dateTo)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_VALIDATION_FAILED))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Missing or invalid X-Service-Token",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_FORBIDDEN))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Service token misconfiguration on Wallet server",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_INTERNAL_ERROR)))
    })
    public ResponseEntity<ApiResponse<EarningsByCongressReportResponse>> getEarningsByCongress(
            @Parameter(in = ParameterIn.HEADER, required = true, description = "Conference service shared secret token", example = "conference-wallet-shared-token")
            @RequestHeader(value = "X-Service-Token", required = false) String serviceToken,
            @Parameter(in = ParameterIn.QUERY, description = "Filter by congress ID", example = "00000000-0000-0000-0000-000000000010")
            @RequestParam(required = false) UUID congressId,
            @Parameter(in = ParameterIn.QUERY, description = "Filter by institution ID", example = "00000000-0000-0000-0000-000000000020")
            @RequestParam(required = false) UUID institutionId,
            @Parameter(in = ParameterIn.QUERY, description = "Start paymentDate filter (inclusive)", example = "2026-05-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(in = ParameterIn.QUERY, description = "End paymentDate filter (inclusive)", example = "2026-05-31")
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
            summary = "Internal platform earnings",
            description = "Internal service-to-service endpoint for conference-service. Requires X-Service-Token. "
                    + "Wallet does not require end-user JWT here; conference-service must enforce end-user authorization before calling. "
                    + "dateFrom/dateTo filters are inclusive. Response groups institutions with congress children using Wallet-owned immutable payment aggregates.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Report generated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.RESPONSE_PLATFORM_EARNINGS))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid date range (dateFrom > dateTo)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_VALIDATION_FAILED))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Missing or invalid X-Service-Token",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_FORBIDDEN))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Service token misconfiguration on Wallet server",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.PROBLEM_INTERNAL_ERROR)))
    })
    public ResponseEntity<ApiResponse<PlatformEarningsReportResponse>> getPlatformEarnings(
            @Parameter(in = ParameterIn.HEADER, required = true, description = "Conference service shared secret token", example = "conference-wallet-shared-token")
            @RequestHeader(value = "X-Service-Token", required = false) String serviceToken,
            @Parameter(in = ParameterIn.QUERY, description = "Filter by institution ID", example = "00000000-0000-0000-0000-000000000020")
            @RequestParam(required = false) UUID institutionId,
            @Parameter(in = ParameterIn.QUERY, description = "Start paymentDate filter (inclusive)", example = "2026-05-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(in = ParameterIn.QUERY, description = "End paymentDate filter (inclusive)", example = "2026-05-31")
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

