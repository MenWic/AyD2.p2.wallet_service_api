package ayd2.p2b.wallet_service_api.integration.controller;

import ayd2.p2b.wallet_service_api.WithMockJwt;
import ayd2.p2b.wallet_service_api.common.response.PageResponse;
import ayd2.p2b.wallet_service_api.core.security.JwtTokenParser;
import ayd2.p2b.wallet_service_api.core.security.RestAuthenticationEntryPoint;
import ayd2.p2b.wallet_service_api.core.security.SecurityConfig;
import ayd2.p2b.wallet_service_api.feature.wallet.application.balance.GetWalletBalanceUseCase;
import ayd2.p2b.wallet_service_api.feature.wallet.application.create.CreateWalletUseCase;
import ayd2.p2b.wallet_service_api.feature.wallet.application.topup.TopUpWalletUseCase;
import ayd2.p2b.wallet_service_api.feature.wallet.application.transactions.GetTransactionHistoryUseCase;
import ayd2.p2b.wallet_service_api.feature.wallet.controller.WalletController;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.response.TransactionResponse;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.response.WalletBalanceResponse;
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

@WebMvcTest(WalletController.class)
@Import(SecurityConfig.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateWalletUseCase createWalletUseCase;

    @MockitoBean
    private GetWalletBalanceUseCase getWalletBalanceUseCase;

    @MockitoBean
    private TopUpWalletUseCase topUpWalletUseCase;

    @MockitoBean
    private GetTransactionHistoryUseCase getTransactionHistoryUseCase;

    @MockitoBean
    private JwtTokenParser jwtTokenParser;

    @MockitoBean
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(1);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }).when(restAuthenticationEntryPoint).commence(any(), any(), any());
    }

    @Test
    @WithMockJwt(userId = "00000000-0000-0000-0000-000000000001", roles = "PARTICIPANT")
    void should_return_201_when_wallet_created() throws Exception {
        WalletBalanceResponse response = WalletBalanceResponse.builder()
                .userId(TEST_USER_ID)
                .balance(BigDecimal.ZERO)
                .build();

        given(createWalletUseCase.execute(any())).willReturn(response);

        mvc.perform(post("/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"00000000-0000-0000-0000-000000000001\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.userId").value(TEST_USER_ID.toString()));
    }

    @Test
    @WithMockJwt(userId = "00000000-0000-0000-0000-000000000001", roles = "PARTICIPANT")
    void should_return_200_when_get_balance() throws Exception {
        WalletBalanceResponse response = WalletBalanceResponse.builder()
                .userId(TEST_USER_ID)
                .balance(new BigDecimal("100.00"))
                .build();

        given(getWalletBalanceUseCase.execute(any())).willReturn(response);

        mvc.perform(get("/wallet/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(100.00));
    }

    @Test
    void should_return_401_when_get_balance_unauthenticated() throws Exception {
        mvc.perform(get("/wallet/balance"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockJwt(userId = "00000000-0000-0000-0000-000000000001", roles = "PARTICIPANT")
    void should_return_200_when_top_up() throws Exception {
        WalletBalanceResponse response = WalletBalanceResponse.builder()
                .userId(TEST_USER_ID)
                .balance(new BigDecimal("50.00"))
                .build();

        given(topUpWalletUseCase.execute(any(), any())).willReturn(response);

        mvc.perform(post("/wallet/top-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":50.00,\"transactionDate\":\"2026-01-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(50.00));
    }

    @Test
    @WithMockJwt(userId = "00000000-0000-0000-0000-000000000001", roles = "PARTICIPANT")
    void should_return_400_when_top_up_invalid_amount() throws Exception {
        mvc.perform(post("/wallet/top-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":-10.00,\"transactionDate\":\"2026-01-01\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockJwt(userId = "00000000-0000-0000-0000-000000000001", roles = "PARTICIPANT")
    void should_return_200_when_get_transactions() throws Exception {
        PageResponse<TransactionResponse> page = PageResponse.<TransactionResponse>builder()
                .items(List.of())
                .page(0)
                .size(20)
                .totalItems(0)
                .totalPages(0)
                .build();

        given(getTransactionHistoryUseCase.execute(any(), any())).willReturn(page);

        mvc.perform(get("/wallet/transactions"))
                .andExpect(status().isOk());
    }
}
