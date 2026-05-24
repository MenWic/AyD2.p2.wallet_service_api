package ayd2.p2b.wallet_service_api.integration.controller;

import ayd2.p2b.wallet_service_api.WithMockJwt;
import ayd2.p2b.wallet_service_api.core.security.JwtTokenParser;
import ayd2.p2b.wallet_service_api.core.security.RestAuthenticationEntryPoint;
import ayd2.p2b.wallet_service_api.core.security.SecurityConfig;
import ayd2.p2b.wallet_service_api.feature.systemconfig.application.get.GetSystemConfigUseCase;
import ayd2.p2b.wallet_service_api.feature.systemconfig.application.update.UpdateSystemConfigUseCase;
import ayd2.p2b.wallet_service_api.feature.systemconfig.controller.SystemConfigController;
import ayd2.p2b.wallet_service_api.feature.systemconfig.dto.response.SystemConfigResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SystemConfigController.class)
@Import(SecurityConfig.class)
class SystemConfigControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GetSystemConfigUseCase getUseCase;

    @MockitoBean
    private UpdateSystemConfigUseCase updateUseCase;

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
    @WithMockJwt(roles = "SYSTEM_ADMIN")
    void should_return_200_with_config_when_system_admin() throws Exception {
        given(getUseCase.execute()).willReturn(SystemConfigResponse.builder()
                .commissionPercent(new BigDecimal("10.00"))
                .build());

        mvc.perform(get("/system/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commissionPercent").value(10.00));
    }

    @Test
    @WithMockJwt(roles = "PARTICIPANT")
    void should_return_403_when_participant_accesses_config() throws Exception {
        mvc.perform(get("/system/config"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockJwt(roles = "CONGRESS_ADMIN")
    void should_return_403_when_congress_admin_accesses_config() throws Exception {
        mvc.perform(get("/system/config"))
                .andExpect(status().isForbidden());
    }

    @Test
    void should_return_401_when_not_authenticated_on_get() throws Exception {
        mvc.perform(get("/system/config"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockJwt(userId = "00000000-0000-0000-0000-000000000001", roles = "SYSTEM_ADMIN")
    void should_return_200_on_valid_update() throws Exception {
        UUID adminId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        given(updateUseCase.execute(any(), eq(adminId))).willReturn(
                SystemConfigResponse.builder()
                        .commissionPercent(new BigDecimal("15.00"))
                        .updatedBy(adminId)
                        .build()
        );

        mvc.perform(put("/system/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"commissionPercent\": 15.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commissionPercent").value(15.00));
    }

    @Test
    @WithMockJwt(roles = "SYSTEM_ADMIN")
    void should_return_400_when_commission_above_100() throws Exception {
        mvc.perform(put("/system/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"commissionPercent\": 101.00}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation.failed"));
    }

    @Test
    @WithMockJwt(roles = "SYSTEM_ADMIN")
    void should_return_400_when_commission_below_0() throws Exception {
        mvc.perform(put("/system/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"commissionPercent\": -0.01}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation.failed"));
    }

    @Test
    @WithMockJwt(roles = "SYSTEM_ADMIN")
    void should_return_400_when_commission_is_null() throws Exception {
        mvc.perform(put("/system/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"commissionPercent\": null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation.failed"));
    }

    @Test
    @WithMockJwt(roles = "SYSTEM_ADMIN")
    void should_accept_commission_of_zero() throws Exception {
        UUID adminId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        given(updateUseCase.execute(any(), any())).willReturn(
                SystemConfigResponse.builder()
                        .commissionPercent(BigDecimal.ZERO)
                        .build()
        );

        mvc.perform(put("/system/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"commissionPercent\": 0.00}"))
                .andExpect(status().isOk());
    }

    @Test
    void should_return_401_when_not_authenticated_on_put() throws Exception {
        mvc.perform(put("/system/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"commissionPercent\": 10.00}"))
                .andExpect(status().isUnauthorized());
    }
}
