package ayd2.p2b.wallet_service_api.unit.core.security;

import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.core.properties.ConferenceIntegrationProperties;
import ayd2.p2b.wallet_service_api.core.security.ConferenceServiceTokenValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ConferenceServiceTokenValidatorTest {

    @Mock
    private ConferenceIntegrationProperties properties;

    private ConferenceServiceTokenValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ConferenceServiceTokenValidator(properties);
    }

    @Test
    void should_throw_500_when_configured_token_is_null() {
        given(properties.getServiceToken()).willReturn(null);

        assertThatThrownBy(() -> validator.validate("any-token"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException apiEx = (ApiException) ex;
                    assert apiEx.getStatus().value() == 500;
                    assert "system.internal_error".equals(apiEx.getCode());
                });
    }

    @Test
    void should_throw_500_when_configured_token_is_blank() {
        given(properties.getServiceToken()).willReturn("   ");

        assertThatThrownBy(() -> validator.validate("any-token"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException apiEx = (ApiException) ex;
                    assert apiEx.getStatus().value() == 500;
                    assert "system.internal_error".equals(apiEx.getCode());
                });
    }

    @Test
    void should_throw_500_when_configured_token_is_empty_string() {
        given(properties.getServiceToken()).willReturn("");

        assertThatThrownBy(() -> validator.validate("any-token"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException apiEx = (ApiException) ex;
                    assert apiEx.getStatus().value() == 500;
                });
    }

    @Test
    void should_throw_403_when_header_token_is_null() {
        given(properties.getServiceToken()).willReturn("configured-secret");

        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException apiEx = (ApiException) ex;
                    assert apiEx.getStatus().value() == 403;
                    assert "auth.forbidden".equals(apiEx.getCode());
                });
    }

    @Test
    void should_throw_403_when_header_token_is_blank() {
        given(properties.getServiceToken()).willReturn("configured-secret");

        assertThatThrownBy(() -> validator.validate("   "))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException apiEx = (ApiException) ex;
                    assert apiEx.getStatus().value() == 403;
                    assert "auth.forbidden".equals(apiEx.getCode());
                });
    }

    @Test
    void should_throw_403_when_header_token_does_not_match() {
        given(properties.getServiceToken()).willReturn("configured-secret");

        assertThatThrownBy(() -> validator.validate("wrong-token"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException apiEx = (ApiException) ex;
                    assert apiEx.getStatus().value() == 403;
                    assert "auth.forbidden".equals(apiEx.getCode());
                });
    }

    @Test
    void should_pass_when_header_token_matches_configured_token() {
        given(properties.getServiceToken()).willReturn("configured-secret");

        assertThatCode(() -> validator.validate("configured-secret"))
                .doesNotThrowAnyException();
    }
}
