package ayd2.p2b.wallet_service_api.integration.controller;

import ayd2.p2b.wallet_service_api.WithMockJwt;
import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.common.response.PageResponse;
import ayd2.p2b.wallet_service_api.core.security.ConferenceServiceTokenValidator;
import ayd2.p2b.wallet_service_api.core.security.JwtTokenParser;
import ayd2.p2b.wallet_service_api.core.security.RestAuthenticationEntryPoint;
import ayd2.p2b.wallet_service_api.core.security.SecurityConfig;
import ayd2.p2b.wallet_service_api.feature.payment.application.get.GetPaymentUseCase;
import ayd2.p2b.wallet_service_api.feature.payment.application.list.ListPaymentsUseCase;
import ayd2.p2b.wallet_service_api.feature.payment.application.register.RegisterPaymentResult;
import ayd2.p2b.wallet_service_api.feature.payment.application.register.RegisterPaymentUseCase;
import ayd2.p2b.wallet_service_api.feature.payment.controller.PaymentController;
import ayd2.p2b.wallet_service_api.feature.payment.dto.internal.PaymentSearchCriteria;
import ayd2.p2b.wallet_service_api.feature.payment.dto.response.PaymentResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import(SecurityConfig.class)
class PaymentControllerTest {

        @Autowired
        private MockMvc mvc;

        @MockitoBean
        private RegisterPaymentUseCase registerPaymentUseCase;

        @MockitoBean
        private GetPaymentUseCase getPaymentUseCase;

        @MockitoBean
        private ListPaymentsUseCase listPaymentsUseCase;

        @MockitoBean
        private ConferenceServiceTokenValidator conferenceServiceTokenValidator;

        @MockitoBean
        private JwtTokenParser jwtTokenParser;

        @MockitoBean
        private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

        private static final UUID PAYMENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
        private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
        private static final UUID CONGRESS_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
        private static final UUID INSTITUTION_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
        private static final String VALID_SERVICE_TOKEN = "valid-conference-service-token";

        @BeforeEach
        void setUp() throws Exception {
                doAnswer(invocation -> {
                        HttpServletResponse response = invocation.getArgument(1);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return null;
                }).when(restAuthenticationEntryPoint).commence(any(), any(), any());
        }

        private String validRegisterBody() {
                return "{\"userId\":\"" + USER_ID + "\","
                                + "\"congressId\":\"" + CONGRESS_ID + "\","
                                + "\"institutionId\":\"" + INSTITUTION_ID + "\","
                                + "\"congressNameSnapshot\":\"Test Congress\","
                                + "\"institutionNameSnapshot\":\"Test Institution\","
                                + "\"amount\":100.00,"
                                + "\"paymentDate\":\"2026-05-20\"}";
        }

        private String registerBodyWithUserId(UUID userId) {
                return "{\"userId\":\"" + userId + "\","
                                + "\"congressId\":\"" + CONGRESS_ID + "\","
                                + "\"institutionId\":\"" + INSTITUTION_ID + "\","
                                + "\"congressNameSnapshot\":\"Test Congress\","
                                + "\"institutionNameSnapshot\":\"Test Institution\","
                                + "\"amount\":100.00,"
                                + "\"paymentDate\":\"2026-05-20\"}";
        }

        // --- Register payment ---

        @Test
        @WithMockJwt(userId = "00000000-0000-0000-0000-000000000001", roles = "PARTICIPANT")
        void participant_with_valid_service_token_and_self_user_id_returns_201() throws Exception {
                PaymentResponse response = PaymentResponse.builder()
                                .id(PAYMENT_ID)
                                .userId(USER_ID)
                                .amount(new BigDecimal("100.00"))
                                .commissionAmount(new BigDecimal("10.00"))
                                .netAmount(new BigDecimal("90.00"))
                                .build();

                given(registerPaymentUseCase.execute(any(), any(), any()))
                                .willReturn(RegisterPaymentResult.newPayment(response));

                mvc.perform(post("/payments/register")
                                .header("Idempotency-Key", "test-key-001")
                                .header("X-Service-Token", VALID_SERVICE_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validRegisterBody()))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.data.amount").value(100.00));
        }

        @Test
        @WithMockJwt(userId = "00000000-0000-0000-0000-000000000001", roles = "PARTICIPANT")
        void participant_with_valid_service_token_replay_returns_200_idempotency_replay() throws Exception {
                PaymentResponse response = PaymentResponse.builder()
                                .id(PAYMENT_ID)
                                .userId(USER_ID)
                                .amount(new BigDecimal("100.00"))
                                .build();

                given(registerPaymentUseCase.execute(any(), any(), any()))
                                .willReturn(RegisterPaymentResult.replay(response));

                mvc.perform(post("/payments/register")
                                .header("Idempotency-Key", "existing-key-001")
                                .header("X-Service-Token", VALID_SERVICE_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validRegisterBody()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("idempotency.replay"))
                                .andExpect(jsonPath("$.data.amount").value(100.00));
        }

        @Test
        @WithMockJwt(userId = "00000000-0000-0000-0000-000000000001", roles = "PARTICIPANT")
        void participant_missing_service_token_returns_403() throws Exception {
                doThrow(new ApiException(HttpStatus.FORBIDDEN, "auth.forbidden", "Missing or invalid X-Service-Token"))
                                .when(conferenceServiceTokenValidator).validate(null);

                mvc.perform(post("/payments/register")
                                .header("Idempotency-Key", "test-key-001")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validRegisterBody()))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockJwt(userId = "00000000-0000-0000-0000-000000000001", roles = "PARTICIPANT")
        void participant_invalid_service_token_returns_403() throws Exception {
                doThrow(new ApiException(HttpStatus.FORBIDDEN, "auth.forbidden", "Missing or invalid X-Service-Token"))
                                .when(conferenceServiceTokenValidator).validate("wrong-token");

                mvc.perform(post("/payments/register")
                                .header("Idempotency-Key", "test-key-001")
                                .header("X-Service-Token", "wrong-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validRegisterBody()))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockJwt(userId = "00000000-0000-0000-0000-000000000001", roles = "PARTICIPANT")
        void participant_with_different_request_user_id_returns_403() throws Exception {
                UUID differentUser = UUID.fromString("00000000-0000-0000-0000-000000000099");

                mvc.perform(post("/payments/register")
                                .header("Idempotency-Key", "test-key-001")
                                .header("X-Service-Token", VALID_SERVICE_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registerBodyWithUserId(differentUser)))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.code").value("auth.forbidden"));
        }

        @Test
        @WithMockJwt(userId = "00000000-0000-0000-0000-000000000001", roles = "CONGRESS_ADMIN")
        void congress_admin_with_valid_service_token_returns_403() throws Exception {
                mvc.perform(post("/payments/register")
                                .header("Idempotency-Key", "test-key-001")
                                .header("X-Service-Token", VALID_SERVICE_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validRegisterBody()))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockJwt(userId = "00000000-0000-0000-0000-000000000001", roles = "SYSTEM_ADMIN")
        void system_admin_with_valid_service_token_returns_403() throws Exception {
                mvc.perform(post("/payments/register")
                                .header("Idempotency-Key", "test-key-001")
                                .header("X-Service-Token", VALID_SERVICE_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validRegisterBody()))
                                .andExpect(status().isForbidden());
        }

        @Test
        void no_jwt_returns_401() throws Exception {
                mvc.perform(post("/payments/register")
                                .header("Idempotency-Key", "test-key-001")
                                .header("X-Service-Token", VALID_SERVICE_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validRegisterBody()))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockJwt(userId = "00000000-0000-0000-0000-000000000001", roles = "PARTICIPANT")
        void missing_idempotency_key_returns_400() throws Exception {
                mvc.perform(post("/payments/register")
                                .header("X-Service-Token", VALID_SERVICE_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validRegisterBody()))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("validation.failed"));
        }

        @Test
        @WithMockJwt(userId = "00000000-0000-0000-0000-000000000001", roles = "PARTICIPANT")
        void blank_idempotency_key_returns_400() throws Exception {
                mvc.perform(post("/payments/register")
                                .header("Idempotency-Key", "   ")
                                .header("X-Service-Token", VALID_SERVICE_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validRegisterBody()))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("validation.failed"));
        }

        @Test
        @WithMockJwt(userId = "00000000-0000-0000-0000-000000000001", roles = "PARTICIPANT")
        void idempotency_key_too_long_returns_400() throws Exception {
                String tooLong = "a".repeat(121);

                mvc.perform(post("/payments/register")
                                .header("Idempotency-Key", tooLong)
                                .header("X-Service-Token", VALID_SERVICE_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validRegisterBody()))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("validation.failed"));
        }

        @Test
        @WithMockJwt(userId = "00000000-0000-0000-0000-000000000001", roles = "PARTICIPANT")
        void amount_with_more_than_two_decimal_places_returns_400() throws Exception {
                String body = "{\"userId\":\"" + USER_ID + "\","
                                + "\"congressId\":\"" + CONGRESS_ID + "\","
                                + "\"institutionId\":\"" + INSTITUTION_ID + "\","
                                + "\"congressNameSnapshot\":\"Test Congress\","
                                + "\"institutionNameSnapshot\":\"Test Institution\","
                                + "\"amount\":100.001,"
                                + "\"paymentDate\":\"2026-05-20\"}";

                mvc.perform(post("/payments/register")
                                .header("Idempotency-Key", "test-key-001")
                                .header("X-Service-Token", VALID_SERVICE_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("validation.failed"));
        }

        @Test
        @WithMockJwt(userId = "00000000-0000-0000-0000-000000000001", roles = "PARTICIPANT")
        void snapshot_too_long_returns_400() throws Exception {
                String tooLong = "a".repeat(256);
                String body = "{\"userId\":\"" + USER_ID + "\","
                                + "\"congressId\":\"" + CONGRESS_ID + "\","
                                + "\"institutionId\":\"" + INSTITUTION_ID + "\","
                                + "\"congressNameSnapshot\":\"" + tooLong + "\","
                                + "\"institutionNameSnapshot\":\"Test Institution\","
                                + "\"amount\":100.00,"
                                + "\"paymentDate\":\"2026-05-20\"}";

                mvc.perform(post("/payments/register")
                                .header("Idempotency-Key", "test-key-001")
                                .header("X-Service-Token", VALID_SERVICE_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("validation.failed"));
        }

        // --- Get payment by ID ---

        @Test
        @WithMockJwt(userId = "00000000-0000-0000-0000-000000000001", roles = "SYSTEM_ADMIN")
        void should_return_200_when_get_payment_by_id() throws Exception {
                PaymentResponse response = PaymentResponse.builder()
                                .id(PAYMENT_ID)
                                .userId(USER_ID)
                                .amount(new BigDecimal("100.00"))
                                .build();

                // GetPaymentUseCase.execute(UUID, RequesterContext) — 2 args
                given(getPaymentUseCase.execute(any(), any())).willReturn(response);

                mvc.perform(get("/payments/{id}", PAYMENT_ID))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.id").value(PAYMENT_ID.toString()));
        }

        @Test
        void should_return_401_when_no_auth_on_get_payment() throws Exception {
                mvc.perform(get("/payments/{id}", PAYMENT_ID))
                                .andExpect(status().isUnauthorized());
        }

        // --- List payments ---

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

        @Test
        void should_return_401_when_no_auth_on_list_payments() throws Exception {
                mvc.perform(get("/payments"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockJwt(roles = "PARTICIPANT")
        void should_return_403_when_participant_tries_to_list_payments() throws Exception {
                mvc.perform(get("/payments"))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockJwt(roles = "SYSTEM_ADMIN")
        void should_return_400_when_list_payments_date_from_is_after_date_to() throws Exception {
                mvc.perform(get("/payments?dateFrom=2025-12-31&dateTo=2025-01-01"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("validation.failed"));
        }

        @Test
        @WithMockJwt(roles = "SYSTEM_ADMIN")
        void should_normalize_negative_page_to_zero_when_list_payments() throws Exception {
                PageResponse<PaymentResponse> page = PageResponse.<PaymentResponse>builder()
                                .items(List.of())
                                .page(0)
                                .size(20)
                                .totalItems(0)
                                .totalPages(0)
                                .build();
                given(listPaymentsUseCase.execute(any())).willReturn(page);

                mvc.perform(get("/payments?page=-5&size=20"))
                                .andExpect(status().isOk());

                ArgumentCaptor<PaymentSearchCriteria> captor = ArgumentCaptor.forClass(PaymentSearchCriteria.class);
                then(listPaymentsUseCase).should().execute(captor.capture());
                assertThat(captor.getValue().getPage()).isEqualTo(0);
        }

        @Test
        @WithMockJwt(roles = "SYSTEM_ADMIN")
        void should_normalize_non_positive_size_to_twenty_when_list_payments() throws Exception {
                PageResponse<PaymentResponse> page = PageResponse.<PaymentResponse>builder()
                                .items(List.of())
                                .page(0)
                                .size(20)
                                .totalItems(0)
                                .totalPages(0)
                                .build();
                given(listPaymentsUseCase.execute(any())).willReturn(page);

                mvc.perform(get("/payments?page=0&size=0"))
                                .andExpect(status().isOk());

                ArgumentCaptor<PaymentSearchCriteria> captor = ArgumentCaptor.forClass(PaymentSearchCriteria.class);
                then(listPaymentsUseCase).should().execute(captor.capture());
                assertThat(captor.getValue().getSize()).isEqualTo(20);
        }

        @Test
        @WithMockJwt(roles = "SYSTEM_ADMIN")
        void should_cap_size_to_one_hundred_when_list_payments() throws Exception {
                PageResponse<PaymentResponse> page = PageResponse.<PaymentResponse>builder()
                                .items(List.of())
                                .page(0)
                                .size(100)
                                .totalItems(0)
                                .totalPages(0)
                                .build();
                given(listPaymentsUseCase.execute(any())).willReturn(page);

                mvc.perform(get("/payments?page=0&size=250"))
                                .andExpect(status().isOk());

                ArgumentCaptor<PaymentSearchCriteria> captor = ArgumentCaptor.forClass(PaymentSearchCriteria.class);
                then(listPaymentsUseCase).should().execute(captor.capture());
                assertThat(captor.getValue().getSize()).isEqualTo(100);
        }
}
