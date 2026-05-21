package ayd2.p2b.wallet_service_api.integration.controller;

import ayd2.p2b.wallet_service_api.WithMockJwt;
import ayd2.p2b.wallet_service_api.common.response.PageResponse;
import ayd2.p2b.wallet_service_api.core.security.JwtTokenParser;
import ayd2.p2b.wallet_service_api.core.security.RestAuthenticationEntryPoint;
import ayd2.p2b.wallet_service_api.core.security.SecurityConfig;
import ayd2.p2b.wallet_service_api.feature.payment.application.get.GetPaymentUseCase;
import ayd2.p2b.wallet_service_api.feature.payment.application.list.ListPaymentsUseCase;
import ayd2.p2b.wallet_service_api.feature.payment.application.register.RegisterPaymentUseCase;
import ayd2.p2b.wallet_service_api.feature.payment.controller.PaymentController;
import ayd2.p2b.wallet_service_api.feature.payment.dto.response.PaymentResponse;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import(SecurityConfig.class)
class PaymentControllerTest {

        @Autowired
        private MockMvc mvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private RegisterPaymentUseCase registerPaymentUseCase;

        @MockitoBean
        private GetPaymentUseCase getPaymentUseCase;

        @MockitoBean
        private ListPaymentsUseCase listPaymentsUseCase;

        @MockitoBean
        private JwtTokenParser jwtTokenParser;

        @MockitoBean
        private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

        private static final UUID PAYMENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
        private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
        private static final UUID CONGRESS_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
        private static final UUID INSTITUTION_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

        @BeforeEach
        void setUp() throws Exception {
                doAnswer(invocation -> {
                        HttpServletResponse response = invocation.getArgument(1);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return null;
                }).when(restAuthenticationEntryPoint).commence(any(), any(), any());
        }

        @Test
        @WithMockJwt(userId = "00000000-0000-0000-0000-000000000001", roles = "CONGRESS_ADMIN")
        void should_return_201_when_payment_registered() throws Exception {
                PaymentResponse response = PaymentResponse.builder()
                                .id(PAYMENT_ID)
                                .userId(USER_ID)
                                .amount(new BigDecimal("100.00"))
                                .commissionAmount(new BigDecimal("10.00"))
                                .netAmount(new BigDecimal("90.00"))
                                .build();

                given(registerPaymentUseCase.execute(any(), any(), any())).willReturn(response);

                String body = objectMapper.writeValueAsString(
                                new java.util.HashMap<String, Object>() {
                                        {
                                                put("userId", USER_ID.toString());
                                                put("congressId", CONGRESS_ID.toString());
                                                put("institutionId", INSTITUTION_ID.toString());
                                                put("congressNameSnapshot", "Test Congress");
                                                put("institutionNameSnapshot", "Test Institution");
                                                put("amount", "100.00");
                                                put("paymentDate", "2026-05-20");
                                        }
                                });

                mvc.perform(post("/payments/register")
                                .header("Idempotency-Key", "test-key-001")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.data.amount").value(100.00));
        }

        @Test
        void should_return_401_when_no_auth_on_register() throws Exception {
                String body = "{\"userId\":\"" + USER_ID + "\",\"congressId\":\"" + CONGRESS_ID
                                + "\",\"institutionId\":\"" + INSTITUTION_ID
                                + "\",\"congressNameSnapshot\":\"Test\",\"institutionNameSnapshot\":\"Inst\""
                                + ",\"amount\":100.00,\"paymentDate\":\"2026-05-20\"}";

                mvc.perform(post("/payments/register")
                                .header("Idempotency-Key", "test-key-001")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockJwt(userId = "00000000-0000-0000-0000-000000000001", roles = "PARTICIPANT")
        void should_return_403_when_participant_tries_to_register_payment() throws Exception {
                String body = objectMapper.writeValueAsString(
                                new java.util.HashMap<String, Object>() {
                                        {
                                                put("userId", USER_ID.toString());
                                                put("congressId", CONGRESS_ID.toString());
                                                put("institutionId", INSTITUTION_ID.toString());
                                                put("congressNameSnapshot", "Test Congress");
                                                put("institutionNameSnapshot", "Test Institution");
                                                put("amount", "100.00");
                                                put("paymentDate", "2026-05-20");
                                        }
                                });

                mvc.perform(post("/payments/register")
                                .header("Idempotency-Key", "test-key-002")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockJwt(userId = "00000000-0000-0000-0000-000000000001", roles = "SYSTEM_ADMIN")
        void should_return_200_when_get_payment_by_id() throws Exception {
                PaymentResponse response = PaymentResponse.builder()
                                .id(PAYMENT_ID)
                                .userId(USER_ID)
                                .amount(new BigDecimal("100.00"))
                                .build();

                given(getPaymentUseCase.execute(any(), any(), any())).willReturn(response);

                mvc.perform(get("/payments/{id}", PAYMENT_ID))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.id").value(PAYMENT_ID.toString()));
        }

        @Test
        @WithMockJwt(roles = "SYSTEM_ADMIN")
        void should_return_200_when_list_payments() throws Exception {
                PageResponse<PaymentResponse> page = PageResponse.<PaymentResponse>builder()
                                .items(List.of())
                                .page(0)
                                .size(20)
                                .totalItems(0)
                                .totalPages(0)
                                .build();

                given(listPaymentsUseCase.execute(any())).willReturn(page);

                mvc.perform(get("/payments"))
                                .andExpect(status().isOk());
        }
}
