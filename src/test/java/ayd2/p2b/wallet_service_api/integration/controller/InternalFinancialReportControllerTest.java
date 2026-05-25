package ayd2.p2b.wallet_service_api.integration.controller;

import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.core.security.ConferenceServiceTokenValidator;
import ayd2.p2b.wallet_service_api.core.security.JwtTokenParser;
import ayd2.p2b.wallet_service_api.core.security.RestAuthenticationEntryPoint;
import ayd2.p2b.wallet_service_api.core.security.SecurityConfig;
import ayd2.p2b.wallet_service_api.feature.report.application.earnings_by_congress.GetEarningsByCongressReportUseCase;
import ayd2.p2b.wallet_service_api.feature.report.application.platform_earnings.GetPlatformEarningsReportUseCase;
import ayd2.p2b.wallet_service_api.feature.report.controller.InternalFinancialReportController;
import ayd2.p2b.wallet_service_api.feature.report.dto.internal.FinancialReportCriteria;
import ayd2.p2b.wallet_service_api.feature.report.dto.response.CongressEarningsItem;
import ayd2.p2b.wallet_service_api.feature.report.dto.response.EarningsByCongressItem;
import ayd2.p2b.wallet_service_api.feature.report.dto.response.EarningsByCongressReportResponse;
import ayd2.p2b.wallet_service_api.feature.report.dto.response.InstitutionEarningsItem;
import ayd2.p2b.wallet_service_api.feature.report.dto.response.PlatformEarningsReportResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalFinancialReportController.class)
@Import(SecurityConfig.class)
class InternalFinancialReportControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ConferenceServiceTokenValidator conferenceServiceTokenValidator;

    @MockitoBean
    private GetEarningsByCongressReportUseCase getEarningsByCongressReportUseCase;

    @MockitoBean
    private GetPlatformEarningsReportUseCase getPlatformEarningsReportUseCase;

    @MockitoBean
    private JwtTokenParser jwtTokenParser;

    @MockitoBean
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(1);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }).when(restAuthenticationEntryPoint).commence(any(), any(), any());
    }

    @Test
    void should_return_403_when_service_token_is_missing() throws Exception {
        doThrow(new ApiException(HttpStatus.FORBIDDEN, "auth.forbidden", "Missing or invalid X-Service-Token"))
                .when(conferenceServiceTokenValidator).validate(null);

        mvc.perform(get("/internal/reports/earnings-by-congress"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("auth.forbidden"));
    }

    @Test
    void should_return_403_when_service_token_is_invalid() throws Exception {
        doThrow(new ApiException(HttpStatus.FORBIDDEN, "auth.forbidden", "Missing or invalid X-Service-Token"))
                .when(conferenceServiceTokenValidator).validate("wrong-token");

        mvc.perform(get("/internal/reports/earnings").header("X-Service-Token", "wrong-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("auth.forbidden"));
    }

    @Test
    void should_return_500_when_service_token_server_configuration_is_missing() throws Exception {
        doThrow(new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "system.internal_error", "Service token missing on server"))
                .when(conferenceServiceTokenValidator).validate("valid-token");

        mvc.perform(get("/internal/reports/earnings")
                        .header("X-Service-Token", "valid-token"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("system.internal_error"));
    }

    @Test
    void should_return_200_for_earnings_by_congress_with_valid_service_token_without_jwt() throws Exception {
        EarningsByCongressReportResponse response = EarningsByCongressReportResponse.builder()
                .items(List.of(
                        EarningsByCongressItem.builder()
                                .congressId(UUID.fromString("00000000-0000-0000-0000-000000000011"))
                                .congressName("Congress A")
                                .institutionId(UUID.fromString("00000000-0000-0000-0000-000000000021"))
                                .institutionName("Institution A")
                                .totalAmount(new BigDecimal("100.00"))
                                .commissionAmount(new BigDecimal("10.00"))
                                .netAmount(new BigDecimal("90.00"))
                                .paymentCount(1L)
                                .build()))
                .totalItems(1L)
                .grandTotalAmount(new BigDecimal("100.00"))
                .grandTotalCommission(new BigDecimal("10.00"))
                .grandTotalNet(new BigDecimal("90.00"))
                .build();
        given(getEarningsByCongressReportUseCase.execute(any())).willReturn(response);

        mvc.perform(get("/internal/reports/earnings-by-congress")
                        .header("X-Service-Token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.items[0].institutionName").value("Institution A"))
                .andExpect(jsonPath("$.data.grandTotalCommission").value(10.00));
    }

    @Test
    void should_return_200_for_platform_earnings_without_jwt_when_service_token_is_valid() throws Exception {
        PlatformEarningsReportResponse response = PlatformEarningsReportResponse.builder()
                .items(List.of(
                        InstitutionEarningsItem.builder()
                                .institutionId(UUID.fromString("00000000-0000-0000-0000-000000000101"))
                                .institutionName("Institution A")
                                .institutionTotalAmount(new BigDecimal("200.00"))
                                .institutionTotalCommission(new BigDecimal("20.00"))
                                .institutionTotalNet(new BigDecimal("180.00"))
                                .paymentCount(2L)
                                .congresses(List.of(
                                        CongressEarningsItem.builder()
                                                .congressId(UUID.fromString("00000000-0000-0000-0000-000000000201"))
                                                .congressName("Congress A")
                                                .totalAmount(new BigDecimal("200.00"))
                                                .commissionAmount(new BigDecimal("20.00"))
                                                .netAmount(new BigDecimal("180.00"))
                                                .paymentCount(2L)
                                                .build()))
                                .build()))
                .totalItems(1L)
                .grandTotalAmount(new BigDecimal("200.00"))
                .grandTotalCommission(new BigDecimal("20.00"))
                .grandTotalNet(new BigDecimal("180.00"))
                .build();
        given(getPlatformEarningsReportUseCase.execute(any())).willReturn(response);

        mvc.perform(get("/internal/reports/earnings")
                        .header("X-Service-Token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].institutionTotalAmount").value(200.00))
                .andExpect(jsonPath("$.data.items[0].institutionTotalCommission").value(20.00))
                .andExpect(jsonPath("$.data.items[0].institutionTotalNet").value(180.00));
    }

    @Test
    void should_return_400_when_date_range_is_invalid() throws Exception {
        given(getEarningsByCongressReportUseCase.execute(any()))
                .willThrow(new ApiException(HttpStatus.BAD_REQUEST, "validation.failed", "dateFrom must not be after dateTo"));

        mvc.perform(get("/internal/reports/earnings-by-congress")
                        .header("X-Service-Token", "valid-token")
                        .queryParam("dateFrom", "2026-12-31")
                        .queryParam("dateTo", "2026-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation.failed"));
    }

    @Test
    void should_pass_query_params_to_earnings_by_congress_use_case() throws Exception {
        given(getEarningsByCongressReportUseCase.execute(any())).willReturn(EarningsByCongressReportResponse.builder()
                .items(List.of())
                .totalItems(0L)
                .grandTotalAmount(BigDecimal.ZERO)
                .grandTotalCommission(BigDecimal.ZERO)
                .grandTotalNet(BigDecimal.ZERO)
                .build());

        UUID congressId = UUID.fromString("00000000-0000-0000-0000-000000000311");
        UUID institutionId = UUID.fromString("00000000-0000-0000-0000-000000000322");

        mvc.perform(get("/internal/reports/earnings-by-congress")
                        .header("X-Service-Token", "valid-token")
                        .queryParam("congressId", congressId.toString())
                        .queryParam("institutionId", institutionId.toString())
                        .queryParam("dateFrom", "2026-01-01")
                        .queryParam("dateTo", "2026-03-31"))
                .andExpect(status().isOk());

        ArgumentCaptor<FinancialReportCriteria> captor = ArgumentCaptor.forClass(FinancialReportCriteria.class);
        then(getEarningsByCongressReportUseCase).should().execute(captor.capture());
        assertThat(captor.getValue().getCongressId()).isEqualTo(congressId);
        assertThat(captor.getValue().getInstitutionId()).isEqualTo(institutionId);
        assertThat(captor.getValue().getDateFrom()).hasToString("2026-01-01");
        assertThat(captor.getValue().getDateTo()).hasToString("2026-03-31");
    }

    @Test
    void should_pass_query_params_to_platform_earnings_use_case() throws Exception {
        given(getPlatformEarningsReportUseCase.execute(any())).willReturn(PlatformEarningsReportResponse.builder()
                .items(List.of())
                .totalItems(0L)
                .grandTotalAmount(BigDecimal.ZERO)
                .grandTotalCommission(BigDecimal.ZERO)
                .grandTotalNet(BigDecimal.ZERO)
                .build());

        UUID institutionId = UUID.fromString("00000000-0000-0000-0000-000000000411");

        mvc.perform(get("/internal/reports/earnings")
                        .header("X-Service-Token", "valid-token")
                        .queryParam("institutionId", institutionId.toString())
                        .queryParam("dateFrom", "2026-02-01")
                        .queryParam("dateTo", "2026-02-28"))
                .andExpect(status().isOk());

        ArgumentCaptor<FinancialReportCriteria> captor = ArgumentCaptor.forClass(FinancialReportCriteria.class);
        then(getPlatformEarningsReportUseCase).should().execute(captor.capture());
        assertThat(captor.getValue().getInstitutionId()).isEqualTo(institutionId);
        assertThat(captor.getValue().getDateFrom()).hasToString("2026-02-01");
        assertThat(captor.getValue().getDateTo()).hasToString("2026-02-28");
    }
}
